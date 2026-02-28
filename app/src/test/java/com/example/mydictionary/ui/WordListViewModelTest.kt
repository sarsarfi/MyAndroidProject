package com.example.mydictionary.ui

import WordsRepository
import com.example.mydictionary.data.Word
import com.example.mydictionary.ui.wordlist.WordListViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class WordListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val allWordsFlow = MutableStateFlow<List<Word>>(emptyList())
    private val skippedWordsFlow = MutableStateFlow<List<Word>>(emptyList())

    private val mockkRepository = mockk<WordsRepository> (relaxed = true){
        every { getAllWordsDictionary() } returns allWordsFlow
        every { getAllSkippedWords() } returns skippedWordsFlow
    }

    private lateinit var viewModel : WordListViewModel

    @Before
    fun setUp(){
        viewModel = WordListViewModel(mockkRepository)
    }
    @Test
    fun deleteWord_Test() = runTest{

        val word = Word(3,"Red","قرمز")

        viewModel.deleteWord(word)

        coVerify { mockkRepository.deleteWord(any()) }

    }

    @Test
    fun uiState_Test_Combine_AllWords_SkippedWords() = runTest {
        val word1 = Word(2,"cat","گربه")
        val word2 = Word(4,"apple","سیب")

        val job = launch{viewModel.uiState.collect {  }} // collect flow

        allWordsFlow.value = listOf(word1)
        skippedWordsFlow.value = listOf(word2)

        advanceUntilIdle()//waite to run coroutine

        val state = viewModel.uiState.value

        //Assert
        assertEquals(listOf(word1), state.wordsList)
        assertEquals(listOf(word2),state.skippedWords)

        job.cancel()
    }
}