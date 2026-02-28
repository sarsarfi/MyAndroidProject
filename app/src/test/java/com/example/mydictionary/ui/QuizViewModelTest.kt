package com.example.mydictionary.ui

import WordsRepository
import com.example.mydictionary.data.GameState
import com.example.mydictionary.data.GameStateRepository
import com.example.mydictionary.data.Word
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockWordRepo = mockk<WordsRepository>(relaxed = true)
    private val mockStateRepo = mockk<GameStateRepository>(relaxed = true)
    private lateinit var viewModel: QuizViewModel

    @Before
    fun setUp() {

        val wordList = listOf(Word(1, "fox", "روباه"), Word(2, "cat", "گربه"))
        every { mockWordRepo.getAllWordsDictionary() } returns MutableStateFlow(wordList)

        viewModel = QuizViewModel(mockWordRepo, mockStateRepo)
    }
    @Test
    fun wordRandom_Select_Hard_words() = runTest {

        coEvery { mockStateRepo.getGameStateByWordId(1) } returns null //weight+1=0+1=1
        val hardState = mockk<GameState>{every { wrongAnswer } returns 9}
        coEvery { mockStateRepo.getGameStateByWordId(2) } returns hardState//weight+9=9+1=10

        val collectJob = launch { viewModel.allWords.collect {  } } // Activate data flow by collecting flow
        viewModel.allWords.first { it.wordList.isNotEmpty() } //Wait for the first non-empty list to appear

        viewModel.randomGenerator = {_ -> 11} // total weight = 10+1 (deterministic test)

        viewModel.wordRandom()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        val isCat = uiState.currentWord.any{it == 'c'}

        assertTrue(isCat,"the hard word is ${uiState.currentWord}")

        collectJob.cancel()
    }
}