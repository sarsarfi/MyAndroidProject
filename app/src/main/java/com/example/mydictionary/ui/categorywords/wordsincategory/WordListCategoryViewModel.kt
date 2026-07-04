package com.example.mydictionary.ui.categorywords.wordsincategory

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordCategoryCrossRef
import com.example.mydictionary.data.repository.WordCategoryCrossRefRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class WordListCategoryUiState(
    val wordsListCategory: List<Word> = listOf(),
    val totalWords: Int = 0
)

class WordListCategoryViewModel(
    private val wordCategoryCrossRefRepository: WordCategoryCrossRefRepository,
    savedStateHandle: SavedStateHandle,
): ViewModel(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    fun initializeTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = false
            } else {
                isTtsInitialized = true
            }
        }
    }

    fun speakWord(word: String) {
        if (isTtsInitialized && tts != null && word.isNotBlank()) {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "WORD_TTS_ID")
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    //  گرفتن آیدی دسته‌بندیِ کلیک شده از صفحه قبلی
    //  ابتدا دیتا را به صورت String می‌گیریم، سپس آن را به عدد (Int) تبدیل می‌کنیم
    val categoryId: Int = checkNotNull(savedStateHandle.get<Int>(WordsListCategoryScreenDestination.categoryIdArg))
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    //  پاس دادن categoryId به متد ریپازیتوری برای فیلتر کردن لغات همان دسته
    val uiState: StateFlow<WordListCategoryUiState> = wordCategoryCrossRefRepository.getWordsForCategory(categoryId)
        .map { filteredWords ->
            WordListCategoryUiState(
                wordsListCategory = filteredWords,
                totalWords = filteredWords.size
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = WordListCategoryUiState()
        )

    fun deleteWord(wordCategoryCrossRef: WordCategoryCrossRef) {
        viewModelScope.launch {
            wordCategoryCrossRefRepository.deleteRelation(wordCategoryCrossRef)
        }
    }

}