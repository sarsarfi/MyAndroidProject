package com.example.mydictionary.ui.report

import WordsRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.GameStateRepository
import com.example.mydictionary.data.WordReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.util.concurrent.TimeUnit

data class ReportUiState(
    val isLoading: Boolean = false,
    val weeklyChartData: List<ChartData> = emptyList(),
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val wordReports: List<WordReport> = emptyList(),
    val topHardWords: List<WordReport> = emptyList()
)

data class ChartData(
    val count: Int,
    val dayName: String
)

class ReportViewModel(
    private val wordsRepository: WordsRepository,
    private val gameStatsRepository: GameStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())

    val state: StateFlow<ReportUiState> =
        _uiState.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(isLoading = true)
            }

            // دریافت داده‌های نمودار هفتگی
            launch {

                wordsRepository.getAllDateAdded().collect { allDate ->

                    val chartData =
                        processDataToChartDataPersian(allDate)

                    _uiState.update {
                        it.copy(
                            weeklyChartData = chartData
                        )
                    }
                }
            }

            // دریافت آمار بازی‌ها
            launch {

                gameStatsRepository.getFullReport().collect { reportList ->

                    val correct =
                        reportList.sumOf { it.correctCount }

                    val wrong =
                        reportList.sumOf { it.wrongCount }

                    val hardWords =
                        reportList
                            .filter { it.wrongCount > 0 }
                            .sortedByDescending { it.wrongCount }
                            .take(5)

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

    /**
     * بردن زمان به ابتدای همان روز
     * ساعت 00:00:00
     */
    private fun getStartOfDayMillis(
        date: PersianDate
    ): Long {

        val startDay = PersianDate(date.time).apply {

            hour = 0
            minute = 0
            second = 0
        }

        return startDay.time
    }

    /**
     * تبدیل لیست timestamp
     * به داده‌ی نمودار ۷ روز اخیر
     */
    private fun processDataToChartDataPersian(
        allDate: List<Long>
    ): List<ChartData> {

        val counts = MutableList(7) { 0 }

        // شروع امروز
        val todayStartMillis =
            getStartOfDayMillis(PersianDate())

        allDate.forEach { timeMillis ->

            // شروع روز مربوط به هر آیتم
            val itemDateStartMillis =
                getStartOfDayMillis(
                    PersianDate(timeMillis)
                )

            // اختلاف روزها
            val diffMillis =
                todayStartMillis - itemDateStartMillis

            val daysAgo =
                TimeUnit.MILLISECONDS
                    .toDays(diffMillis)
                    .toInt()

            // فقط ۷ روز اخیر
            if (daysAgo in 0..6) {

                counts[6 - daysAgo]++
            }
        }

        // فرمت نام روزها
        val formatter =
            PersianDateFormat("l")

        // ساخت نام ۷ روز اخیر
        val dayNames =
            (6 downTo 0).map { i ->

                val date = PersianDate().apply {

                    addDate(
                        0,
                        0,
                        (-i).toLong()
                    )
                }

                formatter.format(date)
            }

        // ساخت خروجی نهایی نمودار
        return counts.mapIndexed { index, count ->

            ChartData(
                count = count,
                dayName = dayNames[index]
            )
        }
    }
}