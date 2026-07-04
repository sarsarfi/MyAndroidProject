package com.example.mydictionary.ui.wordlist

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.repository.WordStatsRepository
import com.example.mydictionary.data.repository.WordsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class WordListViewModel(
    private val wordsRepository: WordsRepository ,
    private val wordStatsRepository: WordStatsRepository
    ) : ViewModel() , OnInitListener{

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    fun initializeTts(context : Context){
        if (tts == null){
            tts = TextToSpeech(context.applicationContext , this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = false
            }else{
                isTtsInitialized = true
            }
        }
    }

    fun speakWord(word: String){
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

    fun deleteWord(word: Word){
        viewModelScope.launch {
            wordsRepository.deleteWord(word)
        }

    }
    companion object{
        private const val TIMEOUT_MILLIS = 5_000L
    }
    val uiState : StateFlow<WordListUiState> = wordsRepository.getAllWordsDictionary()
        .combine(wordStatsRepository.getAllSkippedWords()) { allWords, skippedWords ->
            // combine two flow in one ui state
            WordListUiState(
                wordsList = allWords ,
                skippedWords = skippedWords ,
                totalWords = allWords.size
                )
        }
        .stateIn(
            scope = viewModelScope ,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS) ,
            initialValue = WordListUiState()
        )

    }

data class WordListUiState(
    val wordsList : List<Word> = listOf() ,
    val skippedWords : List<Word> = listOf() ,
    val totalWords : Int = 0
)