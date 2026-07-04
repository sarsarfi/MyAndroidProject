package com.example.mydictionary.ui.addword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.repository.WordCategoryCrossRefRepository
import com.example.mydictionary.data.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddWordUiState(
    val addWordDetails: AddWordDetails = AddWordDetails(),
    val isValid: Boolean = false,
)

data class AddWordDetails(
    val englishWord: String = "",
    val meaningWord: String = ""
)

fun AddWordDetails.toWord(): Word {
    val trimmedEnglish = englishWord.trim().lowercase() // lowercase برای یکدستی داده‌ها
    return Word(
        english = trimmedEnglish,
        persian = meaningWord.trim(),
        //  ساخت و ذخیره خودکار آدرس وب برای کلمه‌ای که کاربر دستی تایپ کرده است
        searchUrl = "https://www.google.com/search?tbm=isch&q=$trimmedEnglish"
    )
}

class AddWordViewModel(
    private val wordsRepository: WordsRepository,
    private val wordCategoryCrossRefRepository: WordCategoryCrossRefRepository, //  اضافه شدن ریپازیتوری واسط جهت اتصال لغت به دسته
    savedStateHandle: SavedStateHandle //  ۲. اضافه شدن برای دریافت آیدی دسته از نویگیشن
) : ViewModel() {

    //  ۳. استخراج آیدی دسته‌بندی فرستاده شده (اگر کاربر از داخل دسته‌ای نیامده باشد، پیش‌فرض ۱- می‌گیرد)
    private val categoryId: Int = savedStateHandle.get<Int>("categoryId") ?: -1

    private val _uiState = MutableStateFlow(AddWordUiState())
    val uiState: StateFlow<AddWordUiState> = _uiState.asStateFlow()

    /**
     * update
     */
    fun update(addWordDetails: AddWordDetails) {
        _uiState.update { currentState ->
            currentState.copy(
                addWordDetails = addWordDetails,
                isValid = isValidInput(addWordDetails)
            )
        }
    }

    /**
     * valid input
     */
    private fun isValidInput(details: AddWordDetails): Boolean = with(details) {
        englishWord.trim().isNotBlank() && meaningWord.trim().isNotBlank()
    }

    /**
     * save vocab in database
     */
    fun saveWord(onSuccess: () -> Unit = {}) {
        val currentState = _uiState.value
        if (!currentState.isValid) return

        viewModelScope.launch {
            try {
                val englishText = currentState.addWordDetails.englishWord.trim()

                //  اول از همه بگردیم ببینیم این کلمه از قبل در دیتابیس وجود دارد یا خیر؟
                val existingWordId = wordsRepository.getWordIdByEnglish(englishText)

                if (categoryId != -1) {
                    //  کاربر داخل یک دسته‌بندی خاص است و می‌خواهد کلمه اضافه کند
                    if (existingWordId != null) {
                        // کلمه از قبل در دیتابیس بوده (مثل small)؛ پس مستقیم فقط رابطه‌اش را می‌سازیم
                        wordCategoryCrossRefRepository.insertWordToCategory(
                            word = currentState.addWordDetails.toWord().copy(id = existingWordId),
                            categoryId = categoryId
                        )
                    } else {
                        // کلمه کاملاً جدید است؛ آن را می‌فرستیم تا هم کلمه ساخته شود هم رابطه‌اش با دسته
                        wordCategoryCrossRefRepository.insertWordToCategory(
                            word = currentState.addWordDetails.toWord(),
                            categoryId = categoryId
                        )
                    }
                } else {
                    //  کاربر در منوی اصلی است (categoryId == -1) و فقط کلمه عمومی اضافه می‌کند
                    if (existingWordId == null) {
                        // فقط اگر جدید است در جدول کلمات ذخیره می‌کنیم
                        wordsRepository.insertWord(currentState.addWordDetails.toWord())
                    }
                    // اگر از قبل وجود داشت، در منوی اصلی هیچ کاری لازم نیست بکنیم (چون کلمه از قبل موجود است)
                }

                resetForm()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun resetForm() {
        _uiState.update {
            it.copy(
                addWordDetails = AddWordDetails(),
                isValid = false
            )
        }
    }
}