package com.example.mydictionary.ui.addword

import WordsRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
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


fun AddWordDetails.toWord(): Word = Word(
    english = englishWord.trim(),
    persian = meaningWord.trim()
)


class AddWordViewModel(
    private val wordsRepository: WordsRepository
) : ViewModel() {

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
            wordsRepository.insertWord(currentState.addWordDetails.toWord())
            resetForm()
            onSuccess()
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