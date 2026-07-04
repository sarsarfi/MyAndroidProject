package com.example.mydictionary.ui

import com.example.mydictionary.data.repository.WordsRepository
import com.example.mydictionary.data.entities.WordsState
import com.example.mydictionary.data.repository.WordStatsRepository
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.ui.quiz.QuizViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockWordRepo = mockk<WordsRepository>(relaxed = true)
    private val mockStateRepo = mockk<WordStatsRepository>(relaxed = true)

    private val wordsFlow = MutableStateFlow<List<Word>>(emptyList())

    @Before
    fun setUp() {
        every { mockWordRepo.getAllWordsDictionary() } returns wordsFlow
    }

    @Test
    fun wordRandom_WhenAllCategoriesExist_ShouldBuildCorrectTotalWeight() = runTest {

        coEvery { mockStateRepo.getGameStateByWordId(1) } returns null // دسته اول: وزن ۶۰
        coEvery { mockStateRepo.getGameStateByWordId(2) } returns mockk<WordsState> {
            every { wrongAnswer } returns 3
            every { correctAnswer } returns 1
        }
        coEvery { mockStateRepo.getGameStateByWordId(3) } returns mockk<WordsState> {
            every { wrongAnswer } returns 0
            every { correctAnswer } returns 4
        }

        val viewModel = QuizViewModel(mockWordRepo, mockStateRepo)

        viewModel.randomGenerator = { _ -> 95 }

        val collectJob = launch { viewModel.allWords.collect { } }

        val wordList = listOf(
            Word(1, "fox", "روباه"),
            Word(2, "cat", "گربه"),
            Word(3, "dog", "سگ")
        )
        wordsFlow.value = wordList

        viewModel.allWords.first { it.wordList.isNotEmpty() }
        advanceUntilIdle()

        val finalUiState = viewModel.uiState.first { it.currentWord.isNotEmpty() }

        val expectedWord = "dog"
        val actualShuffledWord = finalUiState.currentWord


        assertEquals(
            expectedWord.toList().sorted(),
            actualShuffledWord.toList().sorted(),
            "The selected word should be '$expectedWord', but found '$actualShuffledWord'"
        )

        collectJob.cancel()
    }
}
