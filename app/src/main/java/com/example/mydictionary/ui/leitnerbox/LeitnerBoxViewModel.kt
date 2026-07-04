package com.example.mydictionary.ui.leitnerbox

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordsState
import com.example.mydictionary.data.repository.WordStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class LeitnerBoxViewModel(
    private val wordStatsRepository: WordStatsRepository,
    private val nowProvider : () -> Long = { System.currentTimeMillis() }
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
            } else {
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

    private val boxIntervals = mapOf(
        1 to 0L,
        2 to 1L * 24 * 60 * 60 * 1000L,
        3 to 3L * 24 * 60 * 60 * 1000L,
        4 to 7L * 24 * 60 * 60 * 1000L,
        5 to 14L * 24 * 60 * 60 * 1000L
    )

    fun markWordAsLearned(wordState: WordsState) {
        viewModelScope.launch {
            if (wordState.wordId <= 0) return@launch

            // ابتدا وضعیت فعلی کلمه رو از دیتابیس می‌گیریم
            val currentStats = wordStatsRepository.getGameStateByWordId(wordState.wordId)
            val currentBox = currentStats?.leitnerBox ?: 1

            // اگر کلمه رو بلد بود، یک جعبه میره جلو (حداکثر تا جعبه ۵)
            val newBox = (currentBox + 1).coerceAtMost(5)
            wordStatsRepository.updateLeitnerBox(wordState.wordId, newBox)
            wordStatsRepository.updateNextReviewDate(wordState.wordId, calculateNextReview(newBox))

            //  چون کلمه رو یاد گرفته، حالا دیگه از حالت رد شده (High Priority) خارج میشه و علامتش پاک میشه
            wordStatsRepository.updateSkipStatus(wordState.wordId, false)
        }
    }

    fun markWordAsForgotten(wordState: WordsState) {
        viewModelScope.launch {
            if (wordState.wordId <= 0) return@launch

            //  هر وقت دکمه نمیدانم زده شد، کلمه بدون چون و چرا میره/می‌مونه تو جعبه ۱
            wordStatsRepository.updateLeitnerBox(wordState.wordId, 1)
            wordStatsRepository.updateNextReviewDate(wordState.wordId, calculateNextReview(1))

            //  اینجا اون باگ قبلی رو حل کردیم:
            // چون کاربر کلمه رو بلد نبوده و «نمی‌دانم» رو زده، پس کلمه *همچنان* باید رد شده و با اولویت بالا باقی بمونه (true)
            // اینطوری علامت هشدار (SMS) روی کارت باقی می‌مونه تا کاربر دوباره و دوباره مروریش کنه.
            wordStatsRepository.updateSkipStatus(wordState.wordId, true)
        }
    }

    fun calculateNextReview(boxId: Int, now: Long = nowProvider()): Long {
        val interval = boxIntervals[boxId] ?: 0L
        return now + interval
    }

    val uiState: StateFlow<LeitnerUiState> =
        wordStatsRepository.getAllWordForReview(nowProvider())
            .combine(wordStatsRepository.getAllSkippedWords()) { reviewWords, skippedWords ->

                val skippedIds = skippedWords.map { it.id }.toSet()

                LeitnerUiState(
                    highPriorityWords = skippedWords,
                    normalPriorityWords = reviewWords.filter { it.id !in skippedIds },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = LeitnerUiState()
            )

    private var lastShownWordId: Int? = null

    fun getNextWordToReview(uiState: LeitnerUiState): Word? {
        val skipped = uiState.highPriorityWords
        val normal = uiState.normalPriorityWords

        val orderedList = (skipped + normal).distinctBy { it.id }

        if (orderedList.isEmpty()) return null

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
    val highPriorityWords: List<Word> = emptyList(),
    val normalPriorityWords: List<Word> = emptyList(),
)