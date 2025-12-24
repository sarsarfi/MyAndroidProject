package com.example.mydictionary.ui.wordlist

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

class LeitnerBoxViewModel(private val wordsRepository: WordsRepository) : ViewModel() , OnInitListener {

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

    // فواصل زمانی مرور برای هر جعبه (بر حسب میلی‌ثانیه)
    private val BOX_INTERVALS = mapOf(
        2 to 1L * 24 * 60 * 60 * 1000L,  // 1 روز
        3 to 3L * 24 * 60 * 60 * 1000L,  // 3 روز
        4 to 7L * 24 * 60 * 60 * 1000L, // 7 روز
        5 to 14L * 24 * 60 * 60 * 1000L  // 14 روز
    )

    // کاربر کلمه را بلد شد
    fun markWordAsLearned(word: Word) {
        viewModelScope.launch {
            if (word.id <= 0) return@launch

            val newBox = (word.leitnerBox + 1).coerceAtMost(5)
            wordsRepository.updateLeitnerBox(word.id, newBox)
            wordsRepository.updateNextReviewDate(word.id, calculateNextReview(newBox))

            // اگر کلمه اسکیپ شده بود، از high priority خارج شود
            if (word.isSkipped) {
                wordsRepository.updateSkipStatus(word.id, false)
            }
        }
    }

    // کاربر کلمه را فراموش کرد
    fun markWordAsForgotten(word: Word) {
        viewModelScope.launch {
            if (word.id <= 0) return@launch

            // همیشه Box = 1 و مرور فوری
            wordsRepository.updateLeitnerBox(word.id, 1)
            wordsRepository.updateNextReviewDate(word.id, calculateNextReview(1))

            // به عنوان high priority / اسکیپ شده
            wordsRepository.updateSkipStatus(word.id, true)
        }
    }

    private fun calculateNextReview(boxId: Int): Long {
        return if (boxId == 1) {
            System.currentTimeMillis()
        } else {
            val interval = BOX_INTERVALS[boxId]
                ?: error("Invalid boxId: $boxId")
            System.currentTimeMillis() + interval
        }
    }


    // StateFlow برای UI
    val uiState: StateFlow<LeitnerUiState> =
        wordsRepository.getAllWordForReview(System.currentTimeMillis())
            .combine(wordsRepository.getAllSkippedWords()) { reviewWords, skippedWords ->
                // اولویت‌بندی:
                val highPriorityWords = reviewWords.filter { it.isSkipped }
                val normalPriorityWords = reviewWords.filter { !it.isSkipped }

                LeitnerUiState(
                    allWords = reviewWords,
                    highPriorityWords = highPriorityWords,
                    normalPriorityWords = normalPriorityWords,
                    allSkippedWords = skippedWords
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = LeitnerUiState()
            )

    // انتخاب کلمه بعدی برای نمایش
    fun getNextWordToReview(uiState: LeitnerUiState): Word? {
        // اول high priority، بعد normal
        return uiState.highPriorityWords.firstOrNull()
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

// State با اولویت‌بندی
data class LeitnerUiState(
    val allWords: List<Word> = emptyList(),
    val highPriorityWords: List<Word> = emptyList(), // کلمات اسکیپ شده - اولویت بالا
    val normalPriorityWords: List<Word> = emptyList(), // کلمات عادی - اولویت پایین
    val allSkippedWords: List<Word> = emptyList()
)