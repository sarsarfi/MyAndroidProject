package com.example.mydictionary.ui

import WordsRepository
import com.example.mydictionary.data.Word
import com.example.mydictionary.ui.leitnerbox.LeitnerBoxViewModel
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


    private val mockkRepository = mockk<WordsRepository>(relaxed = true){
        every { getAllWordForReview(
            currentTime = 1000_000L
        ) } returns normalWordsFlow
        every { getAllSkippedWords() } returns highPriorityWordsFlow
    }

    private lateinit var viewModel: LeitnerBoxViewModel

    @Before
    fun setUp(){
        viewModel = LeitnerBoxViewModel(mockkRepository){1000_000L}
    }

    @Test
    fun markWordAsLearned_Know()= runTest {

        var word = Word(1,"apple","سیب", leitnerBox = 1 , isSkipped = true)


        viewModel.markWordAsLearned(word)

        advanceUntilIdle()

        coVerify { mockkRepository.updateLeitnerBox(word.id,2) }
        coVerify { mockkRepository.updateNextReviewDate(word.id,any()) }
        coVerify { mockkRepository.updateSkipStatus(word.id,false) }

    }
    @Test
    fun markWordAsForgotten() = runTest {
        var word = Word(1,"apple","سیب", leitnerBox = 4 , isSkipped = false)

        viewModel.markWordAsForgotten(word)

        advanceUntilIdle()

        coVerify { mockkRepository.updateLeitnerBox(word.id,1) }
        coVerify { mockkRepository.updateNextReviewDate(word.id , any()) }
        coVerify { mockkRepository.updateSkipStatus(word.id , true) }
    }
    @Test
    fun calculate_Next_Review() = run{
        val word1 = Word(3,"green","سبز", leitnerBox = 1)
        val word2 = Word(4,"yellow","زرد", leitnerBox = 3)

        val timeNow = 1000000L

        assertEquals(timeNow,viewModel.calculateNextReview(word1.leitnerBox , timeNow))
        assertEquals(timeNow + 3L * 24 * 60 * 60 * 1000L,viewModel.calculateNextReview(word2.leitnerBox , timeNow))
    }

    @Test
    fun leitnerBox_Flash_Cards()=runTest {
        val word1 = Word(1, "cat", "گربه", leitnerBox = 2, isSkipped = false)
        val word2 = Word(2, "dog", "سگ", leitnerBox = 1, isSkipped = true)


        val job = launch { viewModel.uiState.collect { } }

        normalWordsFlow.value = listOf(word1)
        highPriorityWordsFlow.value = listOf(word2)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(listOf(word1), state.normalPriorityWords)
        assertEquals(listOf(word2), state.highPriorityWords)

        job.cancel()

    }

}