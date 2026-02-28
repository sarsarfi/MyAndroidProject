package com.example.mydictionary.ui.leitnerbox

import WordsRepository
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import java.util.Locale

class LeitnerBoxViewModel(
    private val wordsRepository: WordsRepository ,
    private val nowProvider : () -> Long = {System.currentTimeMillis()}
) : ViewModel() , OnInitListener {

    private var tts : TextToSpeech? = null
    private var isTtsInitialized = false

    fun initializeTts(context : Context){
        if (tts == null){
            tts = TextToSpeech(context.applicationContext , this)
        }
    }
    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){

            val result = tts?.setLanguage(Locale.ENGLISH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                isTtsInitialized = false
            }else{
                isTtsInitialized = true
            }
        }
    }

    fun speakWord(text: String){
        if (isTtsInitialized && tts != null && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "WORD_TTS_ID")
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val BOX_INTERVALS = mapOf(
        1 to 0L,//immediately
        2 to 1L * 24 * 60 * 60 * 1000L,  // 1
        3 to 3L * 24 * 60 * 60 * 1000L,  // 3
        4 to 7L * 24 * 60 * 60 * 1000L, // 7
        5 to 14L * 24 * 60 * 60 * 1000L  // 14
    )

    fun markWordAsLearned(word: Word) {
        viewModelScope.launch {
            if (word.id <= 0) return@launch

            val newBox = (word.leitnerBox + 1).coerceAtMost(5)
            wordsRepository.updateLeitnerBox(word.id, newBox)
            wordsRepository.updateNextReviewDate(word.id, calculateNextReview(newBox))

            if (word.isSkipped) {
                wordsRepository.updateSkipStatus(word.id, false)
            }
        }
    }


    fun markWordAsForgotten(word: Word) {
        viewModelScope.launch {
            if (word.id <= 0) return@launch

            wordsRepository.updateLeitnerBox(word.id, 1)
            wordsRepository.updateNextReviewDate(word.id, calculateNextReview(1))

            wordsRepository.updateSkipStatus(word.id, true)
        }
    }

     fun calculateNextReview(boxId: Int , now : Long = nowProvider()): Long {
        return if (boxId == 1) now else now + BOX_INTERVALS[boxId]!!
    }

    //receive data flow from repository
    val uiState: StateFlow<LeitnerUiState> =
        wordsRepository.getAllWordForReview(nowProvider())
            .combine(wordsRepository.getAllSkippedWords()) { reviewWords, skippedWords ->
                LeitnerUiState(
                    highPriorityWords = skippedWords,
                    normalPriorityWords = reviewWords.filter { !it.isSkipped },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = LeitnerUiState()
            )

    // select next word to show (with priority)
    private var lastShownWordId: Int? = null

    fun getNextWordToReview(uiState: LeitnerUiState): Word? {

        // skipped words
        val skipped = uiState.highPriorityWords

        // normal words
        val normal = uiState.normalPriorityWords

        // combine skipped and normal words
        val orderedList = (skipped + normal)
            .distinctBy { it.id }   // جلوگیری از duplicate

        if (orderedList.isEmpty()) return null

        // delete last shown word from list
        val filtered = orderedList.filter { it.id != lastShownWordId }

        val nextWord = when {
            filtered.isNotEmpty() -> filtered.first()
            else -> orderedList.first()
        }

        lastShownWordId = nextWord.id

        return nextWord
    }

    private val _meaningWord = MutableStateFlow(false)
    val isMeaningVisible: StateFlow<Boolean> = _meaningWord

    fun onClickToShowMeaning() {
        _meaningWord.value = true
    }

    fun resetMeaning() {
        _meaningWord.value = false
    }


}

data class LeitnerUiState(
    val highPriorityWords: List<Word> = emptyList(), // skipped words
    val normalPriorityWords: List<Word> = emptyList(), // normal words for review
)