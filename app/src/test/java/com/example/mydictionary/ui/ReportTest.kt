package com.example.mydictionary.ui

import WordsRepository
import com.example.mydictionary.data.GameStateRepository
import com.example.mydictionary.ui.report.ReportViewModel
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

class ReportTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // flow تاریخ‌ها
    private var allDatesFlow =

        MutableStateFlow<List<Long>>(emptyList())

    private val wordsRepository =

        mockk<WordsRepository>(relaxed = true) {

            every {
                getAllDateAdded()
            } returns allDatesFlow
        }

    private val gameStateRepository =
        mockk<GameStateRepository>(relaxed = true)

    private lateinit var viewModel: ReportViewModel

    @Before
    fun setup() {

        viewModel = ReportViewModel(
            wordsRepository,
            gameStateRepository
        )
    }

    @Test
    fun weeklyChartData_shouldGenerateCorrectCounts() = runTest {

        val now = System.currentTimeMillis()

        val oneDay =
            24L * 60 * 60 * 1000

        // داده تست:
        // امروز = 2 کلمه
        // دیروز = 1 کلمه
        // سه روز قبل = 1 کلمه

        allDatesFlow.value = listOf(
            now,
            now,
            now - oneDay,
            now - (3 * oneDay)
        )

        val job = launch {
            viewModel.state.collect { }
        }

        advanceUntilIdle()

        val chartData =
            viewModel.state.value.weeklyChartData

        // باید ۷ روز داشته باشیم
        assertEquals(7, chartData.size)

        // امروز
        assertEquals(
            2,
            chartData[6].count
        )

        // دیروز
        assertEquals(
            1,
            chartData[5].count
        )

        // سه روز قبل
        assertEquals(
            1,
            chartData[3].count
        )

        job.cancel()
    }
}