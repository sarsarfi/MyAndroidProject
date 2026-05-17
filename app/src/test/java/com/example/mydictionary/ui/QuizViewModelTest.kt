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

    // یک Flow کنترل‌پذیر می‌سازیم که اولش خالی است تا بلاک init پیش‌دستی نکند!
    private val wordsFlow = MutableStateFlow<List<Word>>(emptyList())

    @Before
    fun setUp() {
        every { mockWordRepo.getAllWordsDictionary() } returns wordsFlow
    }

    @Test
    fun wordRandom_Select_Hard_words() = runTest {
        // ۱. تعیین وضعیت کلمات در دیتابیس
        coEvery { mockStateRepo.getGameStateByWordId(1) } returns null

        val hardState = mockk<GameState> {
            every { wrongAnswer } returns 5
            every { correctAnswer } returns 1
        }
        coEvery { mockStateRepo.getGameStateByWordId(2) } returns hardState

        // ۲. ساخت ViewModel (چون wordsFlow فعلاً خالی است، بلاک init کاری انجام نمی‌دهد و منتظر می‌ماند)
        val viewModel = QuizViewModel(mockWordRepo, mockStateRepo)

        // ۳. قفل کردن عدد رندوم روی ۷۵ (محدوده کلمات ضعیف) قبل از فرستادن دیتا
        viewModel.randomGenerator = { _ -> 75 }

        // ۴. فعال‌سازی شنونده‌ی Flow در تست
        val collectJob = launch { viewModel.allWords.collect { } }

        // ۵. حالا که همه چیز آماده است، کلمات را به دیتابیس تزریق می‌کنیم!
        val wordList = listOf(
            Word(1, "fox", "روباه"),
            Word(2, "cat", "گربه")
        )
        wordsFlow.value = wordList // با این خط، بلاک init تازه الان فعال می‌شود و عدد رندوم ۷۵ را می‌بیند!

        // منتظر می‌مانیم تا اولین لیست غیرخالی پردازش شود
        viewModel.allWords.first { it.wordList.isNotEmpty() }
        advanceUntilIdle()

        // ۶. بررسی وضعیت نهایی
        val uiState = viewModel.uiState.value

        // آیا کلمه‌ی به هم ریخته شده متعلق به cat است؟
        val isCat = uiState.currentWord.any { it == 'c' || it == 'a' || it == 't' }

        assertTrue(isCat, "Expected the hard word (cat) to be selected, but got: ${uiState.currentWord}")

        collectJob.cancel()
    }
}