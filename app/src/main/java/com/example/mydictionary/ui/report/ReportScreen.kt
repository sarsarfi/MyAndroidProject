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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.mydictionary.data.entities.WordReport
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.adaptive.DeviceType
import com.example.mydictionary.ui.adaptive.rememberDeviceType
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
    val deviceType = rememberDeviceType()

    // تعیین پدینگ کناری بر اساس نوع دستگاه
    val horizontalPadding = when (deviceType) {
        DeviceType.Phone -> 0.dp
        DeviceType.Foldable -> 16.dp
        DeviceType.Tablet -> 32.dp
    }

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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = horizontalPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (!uiState.isLoading) {
                // ۱. نمودار میله‌ای
                item {
                    AdaptiveSimpleBarChart(
                        chartDataList = uiState.weeklyChartData,
                        deviceType = deviceType,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // ۲. کارت آمار کلی (درست و غلط)
                item {
                    AdaptiveGameSummaryHeader(
                        correct = uiState.totalCorrect,
                        wrong = uiState.totalWrong,
                        totalGame = uiState.totalGame,
                        deviceType = deviceType
                    )
                }

                // ۳. عنوان بخش کلمات سخت
                item {
                    Text(
                        text = "Word Performance Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = when (deviceType) {
                            DeviceType.Phone -> 14.sp
                            DeviceType.Foldable -> 16.sp
                            DeviceType.Tablet -> 18.sp
                        },
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                // ۴. کلمات سخت (Horizontal Scroller)
                item {
                    AdaptiveTopHardWordsSection(
                        hardWords = uiState.topHardWords,
                        deviceType = deviceType
                    )
                }

                // ۵. لیست گزارش کلمات
                items(uiState.wordReports) { report ->
                    AdaptiveWordReportItem(
                        report = report,
                        deviceType = deviceType
                    )
                }

            } else {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading statistics...",
                            fontSize = when (deviceType) {
                                DeviceType.Phone -> 14.sp
                                DeviceType.Foldable -> 16.sp
                                DeviceType.Tablet -> 18.sp
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdaptiveGameSummaryHeader(
    correct: Int,
    wrong: Int,
    totalGame : Int ,
    deviceType: DeviceType
) {
    // تعیین سایز فونت‌ها
    val labelFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    val valueFontSize = when (deviceType) {
        DeviceType.Phone -> 30.sp
        DeviceType.Foldable -> 36.sp
        DeviceType.Tablet -> 42.sp
    }

    val cardPadding = when (deviceType) {
        DeviceType.Phone -> 24.dp
        DeviceType.Foldable -> 28.dp
        DeviceType.Tablet -> 32.dp
    }

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
                .padding(cardPadding)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Correct",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = labelFontSize
                )
                Text(
                    "$correct",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = valueFontSize,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Wrong",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = labelFontSize
                )
                Text(
                    "$wrong",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = valueFontSize,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Games",
                    color = MaterialTheme.colorScheme.primary ,
                    fontWeight = FontWeight.Bold,
                    fontSize = labelFontSize
                )
                Text(
                    text = "$totalGame" ,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = valueFontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdaptiveTopHardWordsSection(
    hardWords: List<WordReport>,
    deviceType: DeviceType
) {
    if (hardWords.isEmpty()) return

    // تعیین سایز کارت‌ها
    val cardWidth = when (deviceType) {
        DeviceType.Phone -> 150.dp
        DeviceType.Foldable -> 180.dp
        DeviceType.Tablet -> 200.dp
    }

    val cardHeight = when (deviceType) {
        DeviceType.Phone -> 100.dp
        DeviceType.Foldable -> 110.dp
        DeviceType.Tablet -> 120.dp
    }

    val titleFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    val wordFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 15.sp
        DeviceType.Tablet -> 16.sp
    }

    val wrongFontSize = when (deviceType) {
        DeviceType.Phone -> 12.sp
        DeviceType.Foldable -> 13.sp
        DeviceType.Tablet -> 14.sp
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Top 5 Hard Words",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hardWords) { word ->
                Card(
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = word.englishWord,
                            fontWeight = FontWeight.Bold,
                            fontSize = wordFontSize,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Wrong: ${word.wrongCount}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = wrongFontSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdaptiveWordReportItem(
    report: WordReport,
    deviceType: DeviceType
) {
    // تعیین سایز فونت‌ها
    val wordFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 15.sp
        DeviceType.Tablet -> 16.sp
    }

    val countFontSize = when (deviceType) {
        DeviceType.Phone -> 13.sp
        DeviceType.Foldable -> 14.sp
        DeviceType.Tablet -> 15.sp
    }

    val horizontalPadding = when (deviceType) {
        DeviceType.Phone -> 16.dp
        DeviceType.Foldable -> 20.dp
        DeviceType.Tablet -> 24.dp
    }

    val verticalPadding = when (deviceType) {
        DeviceType.Phone -> 12.dp
        DeviceType.Foldable -> 14.dp
        DeviceType.Tablet -> 16.dp
    }

    Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = report.englishWord,
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                fontSize = wordFontSize
            )
            Text(
                text = "✓ ${report.correctCount}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = countFontSize
            )
            Text(
                text = "✗ ${report.wrongCount}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.error,
                fontSize = countFontSize
            )
        }
        androidx.compose.material3.HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}

@Composable
fun AdaptiveSimpleBarChart(
    chartDataList: List<ChartData>,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    val maxVal = chartDataList.maxOfOrNull { it.count }?.takeIf { it > 0 } ?: 1

    // تعیین ارتفاع نمودار
    val chartHeight = when (deviceType) {
        DeviceType.Phone -> 200.dp
        DeviceType.Foldable -> 220.dp
        DeviceType.Tablet -> 250.dp
    }

    // تعیین عرض هر ستون
    val barWidth = when (deviceType) {
        DeviceType.Phone -> 32.dp
        DeviceType.Foldable -> 36.dp
        DeviceType.Tablet -> 40.dp
    }

    val columnWidth = when (deviceType) {
        DeviceType.Phone -> 48.dp
        DeviceType.Foldable -> 52.dp
        DeviceType.Tablet -> 56.dp
    }

    // تعیین سایز فونت‌ها
    val titleFontSize = when (deviceType) {
        DeviceType.Phone -> 12.sp
        DeviceType.Foldable -> 13.sp
        DeviceType.Tablet -> 14.sp
    }

    val valueFontSize = when (deviceType) {
        DeviceType.Phone -> 11.sp
        DeviceType.Foldable -> 12.sp
        DeviceType.Tablet -> 13.sp
    }

    // ✅ محاسبه حداکثر ارتفاع به صورت عددی (Float)
    val maxBarHeight = (chartHeight - 60.dp).value

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
                fontSize = titleFontSize,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(chartDataList) { data ->
                        // ✅ محاسبه صحیح ارتفاع (ابتدا ضرب Float در Float، سپس تبدیل به Dp)
                        val targetHeight = ((data.count.toFloat() / maxVal) * maxBarHeight).dp

                        val animatedHeight by animateDpAsState(
                            targetValue = targetHeight,
                            animationSpec = tween(durationMillis = 1000)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(columnWidth)
                        ) {
                            Text(
                                text = if (data.count > 0) data.count.toString() else "",
                                fontSize = valueFontSize,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Box(
                                modifier = Modifier
                                    .width(barWidth)
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
                                fontSize = valueFontSize,
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

// نگه داشتن کدهای قدیمی برای Preview
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
                    verticalAlignment = Alignment.Bottom,
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

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun ReportScreenPreview() {
    MyDictionaryTheme {
        ReportScreen(
            navigateBack = {},
            viewModel = viewModel(factory = AppViewModelProvider.Factory)
        )
    }
}