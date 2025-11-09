package com.example.mydictionary.ui.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // ⬅️ برای اجرای Coroutine در ViewModel
import com.example.mydictionary.data.Word
import com.example.mydictionary.data.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect // ⬅️ برای جمع‌آوری Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch // ⬅️ برای راه‌اندازی Coroutine

class WordViewModelList(wordsRepository: WordsRepository) : ViewModel(){

    companion object{
        private const val TIMOUT_MILLIS = 5_000L
    }

    val uiState : StateFlow<WordListUiState> = wordsRepository.getAllWordsDictionary()
        .map { WordListUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMOUT_MILLIS) ,
            initialValue = WordListUiState()
        )
}

data class WordListUiState(val wordList : List<Word> = listOf())