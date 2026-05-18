package com.example.mydictionary.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.ui.adaptive.DeviceType
import com.example.mydictionary.ui.adaptive.rememberDeviceType
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme


object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLeitnerBox: () -> Unit,
    onAllWord: () -> Unit,
    onExcelWord: () -> Unit,
    modifier: Modifier = Modifier,
    onAbout: () -> Unit,
    onAddWord: () -> Unit,
    onQuiz: () -> Unit
) {
    val deviceType = rememberDeviceType()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val gridCards = listOf(
        CategoryItem(
            titleRes = R.string.leitner_box,
            descriptionRes = R.string.description_leitner_box,
            color = Color(0xFFE8F5E9),
            icon = R.drawable.box,
            onClick = onLeitnerBox
        ),
        CategoryItem(
            titleRes = R.string.list_words_excel,
            descriptionRes = R.string.description_excel_words,
            color = Color(0xFFD1C4E9),
            icon = R.drawable.excel,
            onClick = onExcelWord
        )
    )

    MyDictionaryTheme {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(HomeDestination.titleRes),
                    canNavigateBack = false,
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onAddWord,
                    shape = MaterialTheme.shapes.small,
                    text = { Text(text = "Add New Word") },
                    icon = { Icon(Icons.Filled.Add, "Extended floating action button.") }
                )
            }
        ) { innerPadding ->

            // فقط یک تابع، با پارامتر deviceType
            ResponsiveHomeContent(
                innerPadding = innerPadding,
                deviceType = deviceType,
                onAllWord = onAllWord,
                onQuiz = onQuiz,
                onAbout = onAbout,
                gridCards = gridCards
            )
        }
    }
}

@Composable
fun ResponsiveHomeContent(
    innerPadding: PaddingValues,
    deviceType: DeviceType,
    onAllWord: () -> Unit,
    onQuiz: () -> Unit,
    onAbout: () -> Unit,
    gridCards: List<CategoryItem>
) {
    // Determine device type
    val horizontalPadding = when (deviceType) {
        DeviceType.Phone -> 8.dp
        DeviceType.Foldable -> 12.dp
        DeviceType.Tablet -> 16.dp
    }

    val verticalPadding = when (deviceType) {
        DeviceType.Phone -> 8.dp
        DeviceType.Foldable -> 12.dp
        DeviceType.Tablet -> 16.dp
    }

    val cardSpacing = when (deviceType) {
        DeviceType.Phone -> 8.dp
        DeviceType.Foldable -> 12.dp
        DeviceType.Tablet -> 16.dp
    }

    val allWordsMinHeight = when (deviceType) {
        DeviceType.Phone -> 180.dp
        DeviceType.Foldable -> 180.dp
        DeviceType.Tablet -> 200.dp
    }

    val allWordsMaxHeight = when (deviceType) {
        DeviceType.Phone -> 220.dp
        DeviceType.Foldable -> 240.dp
        DeviceType.Tablet -> 300.dp
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = horizontalPadding),
        contentPadding = PaddingValues(vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(cardSpacing)
    ) {
        // cart All Words
        item {
            AllWords(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = allWordsMinHeight, max = allWordsMaxHeight),
                onAllWord = onAllWord
            )
        }

        // cart Quiz
        item {
            AdaptiveStatCard(
                titleRes = R.string.quiz,
                description = R.string.description_quiz,
                image = R.drawable.quiz,
                color = Color(0xFFE0F2F1),
                onClick = onQuiz,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // کارت‌های دوتایی (لایتنر و اکسل)
        items(gridCards.chunked(2)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                rowItems.forEach { card ->
                    AdaptiveStatCard(
                        titleRes = card.titleRes,
                        description = card.descriptionRes,
                        image = card.icon,
                        color = card.color,
                        onClick = card.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // cart Report
        item {
            AdaptiveStatCard(
                titleRes = R.string.report,
                description = R.string.description_report,
                image = R.drawable.bar_chart,
                color = Color(0xFFE0F2F1),
                onClick = onAbout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
fun AdaptiveStatCard(
    @StringRes titleRes: Int,
    @StringRes description: Int,
    @DrawableRes image: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // recognize screen device type
    val deviceType = rememberDeviceType()

    // Determine the height
    val cardHeight = when (deviceType) {
        DeviceType.Phone -> 170.dp
        DeviceType.Foldable -> 170.dp
        DeviceType.Tablet -> 200.dp
    }

    // Determine icon size
    val iconSize = when (deviceType) {
        DeviceType.Phone -> 90.dp
        DeviceType.Foldable -> 120.dp
        DeviceType.Tablet -> 150.dp
    }

    // Determine font size
    val titleFontSize = when (deviceType) {
        DeviceType.Phone -> 16.sp
        DeviceType.Foldable -> 18.sp
        DeviceType.Tablet -> 20.sp
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.BottomEnd)
                    .rotate(-18f),
                alpha = 0.08f,
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(titleRes),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun AllWords(
    modifier: Modifier = Modifier,
    onAllWord: () -> Unit
) {
    val deviceType = rememberDeviceType()

    val iconSize = when (deviceType) {
        DeviceType.Phone -> 120.dp
        DeviceType.Foldable -> 150.dp
        DeviceType.Tablet -> 200.dp
    }

    val titleFontSize = when (deviceType) {
        DeviceType.Phone -> 18.sp
        DeviceType.Foldable -> 20.sp
        DeviceType.Tablet -> 24.sp
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAllWord),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.list),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.TopEnd)
                    .rotate(-15f),
                alpha = 0.08f,
            )

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "All Words",
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.list_of_all_words),
                    textAlign = TextAlign.Justify,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color.Black
                )
            }
        }
    }
}

data class CategoryItem(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val color: Color,
    @DrawableRes val icon: Int,
    val onClick: () -> Unit
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun HomeScreenPreview() {
    MyDictionaryTheme {
        HomeScreen(
            onLeitnerBox = {},
            onAllWord = {},
            onExcelWord = {},
            onQuiz = {},
            onAbout = {},
            onAddWord = {}
        )
    }
}