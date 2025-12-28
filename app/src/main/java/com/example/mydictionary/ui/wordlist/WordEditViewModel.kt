package com.example.mydictionary.ui.wordlist

import WordsRepository
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
import kotlinx.coroutines.flow.*
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


fun WordDetails.toWord(): Word = Word(
    id = id,
    english = englishWord.trim(),
    persian = meaningWord.trim()
)

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

    private val wordId: Int = checkNotNull(savedStateHandle[WordEditDestination.wordIdArg])

    init {
        viewModelScope.launch {
            // ۱. خواندن کلمه از دیتابیس (به صورت Flow)
            wordsRepository.getWordDictionary(wordId.toLong())
                .filterNotNull()
                .first() // گرفتن اولین مقدار موجود
                .let { word ->
                    // ۲. تبدیل کلمه دیتابیس به وضعیت UI
                    _uiState.value = WordUiState(
                        wordDetails = word.toWordDetails(),
                        isEntryValid = true // چون کلمه از قبل وجود دارد، معتبر است
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