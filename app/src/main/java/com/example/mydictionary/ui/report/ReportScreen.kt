package com.example.mydictionary.ui.report

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.data.WordReport
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme


object ReportScreenDestination : NavigationDestination {
    override val route = "report"
    override val titleRes = R.string.report
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navigateBack: () -> Unit,
    viewModel: ReportViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiState by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            DictionaryTopAppBar(
                title = stringResource(ReportScreenDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        // استفاده از LazyColumn به جای Column معمولی برای اسکرول کل صفحه
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (!uiState.isLoading) {
                // ۱. نمودار میله‌ای
                item {
                    SimpleBarChart(
                        chartDataList = uiState.weeklyChartData,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // ۲. کارت آمار کلی (درست و غلط)
                item {
                    GameSummaryHeader(
                        correct = uiState.totalCorrect,
                        wrong = uiState.totalWrong
                    )
                }

                // ۳. لیست گزارش کلمات (جایگزین جدول قبلی برای پرفورمنس بهتر)
                item {
                    Text(
                        text = "Word Performance Details",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                item{
                    TopHardWordsSection(hardWords = uiState.topHardWords)
                }

                items(uiState.wordReports) { report ->

                    WordReportItem(report = report)

                }

            } else {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading statistics...")
                    }
                }
            }
        }
    }
}

// یک کارت جدید برای نمایش آمار بازی (درست/غلط)
@Composable
fun GameSummaryHeader(correct: Int, wrong: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Correct", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("$correct", style = MaterialTheme.typography.headlineMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Wrong", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Text("$wrong", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
@Composable
fun TopHardWordsSection(hardWords: List<WordReport>) {
    if (hardWords.isEmpty()) return

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Top 5 Hard Words",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hardWords) { word ->
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = word.englishWord,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Wrong: ${word.wrongCount}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WordReportItem(report: WordReport) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                    text = report.englishWord,
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            Text(
                text = "✓ ${report.correctCount}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "✗ ${report.wrongCount}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.error
            )
        }
        androidx.compose.material3.HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}
@Composable
fun SimpleBarChart(
    chartDataList: List<ChartData>,
    modifier: Modifier = Modifier
) {
    val maxVal = chartDataList.maxOfOrNull { it.count }?.takeIf { it > 0 } ?: 1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Last 7 Days Activity",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom ,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(chartDataList) { data ->
                        val animatedHeight by animateDpAsState(
                            targetValue = ((data.count.toFloat() / maxVal) * 140).dp,
                            animationSpec = tween(durationMillis = 1000)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(48.dp)
                        ) {
                            Text(
                                text = if (data.count > 0) data.count.toString() else "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(animatedHeight)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primaryContainer
                                            )
                                        ),
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                            )

                            Text(
                                text = data.dayName,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun SimpleBarChartPreview() {
    MyDictionaryTheme {
        ReportScreen(
            navigateBack = {} ,
            viewModel = viewModel(factory = AppViewModelProvider.Factory)
        )
    }
}