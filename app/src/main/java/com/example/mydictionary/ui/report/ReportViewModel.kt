package com.example.mydictionary.ui.report

import WordsRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.GameState
import com.example.mydictionary.data.GameStateRepository
import com.example.mydictionary.data.WordReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class ReportUiState(
    val isLoading: Boolean = false,
    val weeklyChartData: List<ChartData> = emptyList(),
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val wordReports: List<WordReport> = emptyList(),
    val topHardWords: List<WordReport> = emptyList()
)

data class ChartData(val count: Int, val dayName: String)

class ReportViewModel(
    private val wordsRepository: WordsRepository,
    private val gameStatsRepository: GameStateRepository 
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            //  Get timestamp from WordsRepository
            launch {
                wordsRepository.getAllDateAdded().collect { allDate ->
                    val chartData = processDataToChartDataPersian(allDate)
                    _uiState.update { it.copy(weeklyChartData = chartData) }
                }
            }

            //  Get game stats fromGameStatsRepository
            launch {
                gameStatsRepository.getFullReport().collect { reportList ->

                    val correct = reportList.sumOf { it.correctCount }//sum of all correct
                    val wrong = reportList.sumOf { it.wrongCount }// sum of wrong

                    val hardWords = reportList
                        .filter { it.wrongCount > 0 }//deleted correctWord (just wrongWord at list)
                        .sortedByDescending { it.wrongCount }// sorted by wrongCount(high wrong)
                        .take(5)// select just 5 wrong answer where have high wrongCount

                    _uiState.update {
                        it.copy(
                            totalCorrect = correct,
                            totalWrong = wrong,
                            wordReports = reportList,
                            topHardWords = hardWords,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
    // convert timestamp date to chart data

    private fun processDataToChartDataPersian(allDate: List<Long>): List<ChartData> {

        val counts = MutableList(7) { 0 }

        val today = PersianDate()

        allDate.forEach { timeMillis ->

            val itemDate = PersianDate(timeMillis)

            val diffMillis = today.time - itemDate.time
            val daysAgo = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

            if (daysAgo in 0..6) {
                counts[6 - daysAgo]++
            }
        }

        val formatter = PersianDateFormat("l") // نام روز هفته فارسی

        val dayNames = (6 downTo 0).map { i ->
            val date = PersianDate().apply {
                addDate(0, 0, (-i).toLong())
            }
            formatter.format(date)
        }

        return counts.mapIndexed { index, count ->
            ChartData(count, dayNames[index])
        }
    }
}