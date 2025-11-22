package com.example.mydictionary.ui.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
import com.example.mydictionary.data.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LeitnerBoxViewModel(private val wordsRepository: WordsRepository) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    // فواصل زمانی مرور برای هر جعبه (بر حسب میلی‌ثانیه)
    private val BOX_INTERVALS = mapOf(
        1 to System.currentTimeMillis(),  // 1 روز
        2 to 1L * 24 * 60 * 60 * 1000L,  // 3 روز
        3 to 3L * 24 * 60 * 60 * 1000L,  // 7 روز
        4 to 7L * 24 * 60 * 60 * 1000L, // 14 روز
        5 to 14L * 24 * 60 * 60 * 1000L  // 30 روز
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
        val interval = BOX_INTERVALS[boxId] ?: BOX_INTERVALS[1]!!
        if (boxId == 1) {
            return System.currentTimeMillis()
        } else {
            return System.currentTimeMillis() + interval
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
        return uiState.highPriorityWords.firstOrNull() ?: uiState.normalPriorityWords.firstOrNull()
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
