package com.example.mydictionary.ui.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
import com.example.mydictionary.data.WordsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordListViewModel(private val wordsRepository: WordsRepository) : ViewModel(){

    fun deleteWord(word: Word){
        viewModelScope.launch {
            wordsRepository.deleteWord(word)
        }

    }
    companion object{
        private const val TIMOUT_MILLIS = 5_000L
    }
    val uiState : StateFlow<WordListUiState> = wordsRepository.getAllWordsDictionary()
        .combine(wordsRepository.getAllSkippedWords()) { allWords, skippedWords ->
            // ترکیب نتایج دو Flow در یک UiState
            WordListUiState(wordsList = allWords , skippedWords = skippedWords)
        }
        .stateIn(
            scope = viewModelScope ,
            started = SharingStarted.WhileSubscribed(TIMOUT_MILLIS) ,
            initialValue = WordListUiState() // مقدار اولیه
        )


}

data class WordListUiState(
    val wordsList : List<Word> = listOf() ,
    val skippedWords : List<Word> = listOf()
)