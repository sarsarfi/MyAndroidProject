package com.example.mydictionary.ui

import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordsState
import com.example.mydictionary.data.repository.WordStatsRepository // 💡 اصلاح شد
import com.example.mydictionary.ui.leitnerbox.LeitnerBoxViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test

class LeitnerBoxViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private var normalWordsFlow = MutableStateFlow<List<Word>>(emptyList())
    private var highPriorityWordsFlow = MutableStateFlow<List<Word>>(emptyList())

    private val mockkRepository = mockk<WordStatsRepository>(relaxed = true) {
        every { getAllWordForReview(currentTime = 1000_000L) } returns normalWordsFlow
        every { getAllSkippedWords() } returns highPriorityWordsFlow
    }

    private lateinit var viewModel: LeitnerBoxViewModel

    @Before
    fun setUp() {
        viewModel = LeitnerBoxViewModel(mockkRepository) { 1000_000L }
    }

    @Test
    fun markWordAsLearned_Know() = runTest {
        val wordsState = WordsState(wordId = 1, leitnerBox = 1, isSkipped = true)

        //  شبیه‌سازی متد داخلی ویومدل که وضعیت فعلی لغت را از دیتابیس می‌خواند
        coEvery { mockkRepository.getGameStateByWordId(wordsState.wordId) } returns wordsState

        viewModel.markWordAsLearned(wordsState)

        advanceUntilIdle()

        coVerify { mockkRepository.updateLeitnerBox(wordsState.wordId, 2) }
        coVerify { mockkRepository.updateNextReviewDate(wordsState.wordId, any()) }
        coVerify { mockkRepository.updateSkipStatus(wordsState.wordId, false) }
    }

    @Test
    fun markWordAsForgotten() = runTest {
        //  اصلاح ورودی به WordStats
        val wordsState = WordsState(wordId = 1, leitnerBox = 4, isSkipped = false)

        coEvery { mockkRepository.getGameStateByWordId(wordsState.wordId) } returns wordsState

        viewModel.markWordAsForgotten(wordsState)

        advanceUntilIdle()

        //  بررسی طبق منطق جدید: بازگشت به جعبه ۱ و true شدن وضعیت هشدار
        coVerify { mockkRepository.updateLeitnerBox(wordsState.wordId, 1) }
        coVerify { mockkRepository.updateNextReviewDate(wordsState.wordId, any()) }
        coVerify { mockkRepository.updateSkipStatus(wordsState.wordId, true) }
    }

    @Test
    fun calculate_Next_Review() = run {
        val timeNow = 1000_000L

        // بررسی فاصله زمانی جعبه ۱ (فاصله 0)
        assertEquals(timeNow, viewModel.calculateNextReview(1, timeNow))
        // بررسی فاصله زمانی جعبه ۳ (فاصله ۳ روز)
        assertEquals(timeNow + 3L * 24 * 60 * 60 * 1000L, viewModel.calculateNextReview(3, timeNow))
    }

    @Test
    fun leitnerBox_Flash_Cards() = runTest {
        val word1 = Word(1, "cat", "گربه")
        val word2 = Word(2, "dog", "سگ")

        val job = launch { viewModel.uiState.collect { } }

        // مقداردهی جریان‌های داده (Flow) برای شبیه‌سازی خروجی دیتابیس
        normalWordsFlow.value = listOf(word1)
        highPriorityWordsFlow.value = listOf(word2)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        //  در منطق لایه UiState شما، کلمات بخش normalPriorityWords فیلتر می‌شوند
        // تا کلماتی که در highPriorityWords هستند مجدداً در لیست عادی نمایش داده نشوند.
        assertEquals(listOf(word1), state.normalPriorityWords)
        assertEquals(listOf(word2), state.highPriorityWords)

        job.cancel()
    }
}