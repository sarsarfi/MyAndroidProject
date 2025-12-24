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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

// وضعیت ظاهری صفحه گزارش
data class ReportUiState(
    val isLoading: Boolean = false,
    val weeklyChartData: List<ChartData> = emptyList(),
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val topHardWords: List<WordReport> = emptyList()
)

data class ChartData(val count: Int, val dayName: String)

class ReportViewModel(
    private val wordsRepository: WordsRepository,
    private val gameStatsRepository: GameStateRepository // اضافه کردن ریپازیتوری آمار
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. دریافت داده‌های نمودار هفتگی از WordsRepository
            launch {
                wordsRepository.getAllDateAdded().collect { allDate ->
                    val chartData = processDataToChartData(allDate)
                    _uiState.update { it.copy(weeklyChartData = chartData) }
                }
            }

            // 2. دریافت آمار بازی از GameStatsRepository
            // 2. دریافت آمار بازی از GameStatsRepository
            launch {
                // به جای getAllGameState از getFullReport استفاده کنید
                gameStatsRepository.getFullReport().collect { reportList ->
                    val correct = reportList.sumOf { it.correctCount }
                    val wrong = reportList.sumOf { it.wrongCount }

                    // پیدا کردن لغاتی که کاربر بیشترین غلط را در آن‌ها داشته
                    // حالا در اینجا به englishWord دسترسی دارید
                    val hardWords = reportList
                        .filter { it.wrongCount > 0 }
                        .sortedByDescending { it.wrongCount }
                        .take(5)

                    _uiState.update {
                        it.copy(
                            totalCorrect = correct,
                            totalWrong = wrong,
                            // توجه: باید نوع داده را در ReportUiState به List<WordReport> تغییر دهید
                            topHardWords = hardWords,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    // منطق تبدیل تاریخ‌های خام به فرمت نمودار
    private fun processDataToChartData(allDate: List<Long>): List<ChartData> {
        val counts = MutableList(7) { 0 }
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        allDate.forEach { timeMillis ->
            val date = Instant.ofEpochMilli(timeMillis).atZone(zoneId).toLocalDate()
            val daysAgo = ChronoUnit.DAYS.between(date, today).toInt()
            if (daysAgo in 0..6) {
                counts[6 - daysAgo]++
            }
        }

        val dayNames = (6 downTo 0).map { i ->
            today.minusDays(i.toLong()).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
        }

        return counts.mapIndexed { index, count ->
            ChartData(count, dayNames[index])
        }
    }
}