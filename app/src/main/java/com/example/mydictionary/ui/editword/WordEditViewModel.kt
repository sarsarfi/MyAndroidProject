package com.example.mydictionary.ui.editword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class WordUiState(
    val wordDetails: WordDetails = WordDetails(),
    val isEntryValid: Boolean = false
)
data class WordDetails(
    val id: Int = 0,
    val englishWord: String = "",
    val meaningWord: String = ""
)


fun WordDetails.toWord(): Word {
    val trimmedEnglish = englishWord.trim().lowercase() //lowercase برای یکدستی داده‌ها
    return Word(
        id = id,
        english = trimmedEnglish,
        persian = meaningWord.trim(),
        //  آدرس جدید بر اساس کلمه انگلیسیِ ویرایش‌شده ساخته می‌شود تا با تغییر کلمه، لینک هم اصلاح شود
        searchUrl = "https://www.google.com/search?tbm=isch&q=$trimmedEnglish"
    )
}

fun Word.toWordDetails(): WordDetails = WordDetails(
    id = id,
    englishWord = english,
    meaningWord = persian
)


class WordEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val wordsRepository: WordsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WordUiState())

    val uiState: StateFlow<WordUiState> = _uiState.asStateFlow()

    private

    val wordId: Int = checkNotNull(savedStateHandle[WordEditDestination.wordIdArg]) //get word id from navigation to access one word in data base

    init {
        viewModelScope.launch {
            // read word with id from data base
            wordsRepository.getWordDictionary(wordId.toLong())
                .filterNotNull()
                .first() // get first
                .let { word ->
                    _uiState.value = WordUiState(
                        wordDetails = word.toWordDetails(),
                        isEntryValid = true
                    )
                }
        }
    }


    fun updateUiState(wordDetails: WordDetails) {
        _uiState.update { currentState ->
            currentState.copy(
                wordDetails = wordDetails,
                isEntryValid = validateInput(wordDetails)
            )
        }
    }

    private fun validateInput(uiState: WordDetails): Boolean {
        return uiState.englishWord.isNotBlank() && uiState.meaningWord.isNotBlank()
    }

    suspend fun updateWord() {
        if (validateInput(_uiState.value.wordDetails)) {
            wordsRepository.updateWord(_uiState.value.wordDetails.toWord())
        }
    }
}

