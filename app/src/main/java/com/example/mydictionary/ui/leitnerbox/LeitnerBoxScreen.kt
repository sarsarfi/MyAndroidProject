package com.example.mydictionary.ui.leitnerbox

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.data.Word
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.adaptive.DeviceType
import com.example.mydictionary.ui.adaptive.rememberDeviceType
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme

object LeitnerBoxScreenDestination : NavigationDestination {
    override val route = "LeitnerBoxScreen"
    override val titleRes: Int = R.string.leitner_box
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeitnerScreen(
    navigateBack: () -> Unit,
    leitnerBoxViewModel: LeitnerBoxViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val leitnerUiState by leitnerBoxViewModel.uiState.collectAsState()
    val currentWord = leitnerBoxViewModel.getNextWordToReview(leitnerUiState)
    val meaningWord by leitnerBoxViewModel.isMeaningVisible.collectAsState()
    val context = LocalContext.current
    val deviceType = rememberDeviceType()

    // فراخوانی initializeTts فقط یک بار هنگام ورود به صفحه
    LaunchedEffect(Unit) {
        leitnerBoxViewModel.initializeTts(context)
    }
    LaunchedEffect(currentWord?.id) {
        leitnerBoxViewModel.resetMeaning()
    }

    MyDictionaryTheme {
        Scaffold(
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(LeitnerBoxScreenDestination.titleRes),
                    canNavigateBack = true,
                    navigateUp = navigateBack
                )
            }
        ) { innerPadding ->

            // تعیین حداکثر عرض کارت بر اساس نوع دستگاه
            val cardMaxWidth = when (deviceType) {
                DeviceType.Phone -> 0.9f  // 90% صفحه
                DeviceType.Foldable -> 0.7f  // 70% صفحه
                DeviceType.Tablet -> 0.6f  // 60% صفحه (کوچکتر برای تبلت)
            }

            // تعیین حداقل و حداکثر ارتفاع کارت
            val cardMinHeight = when (deviceType) {
                DeviceType.Phone -> 300.dp
                DeviceType.Foldable -> 350.dp
                DeviceType.Tablet -> 400.dp
            }

            val cardMaxHeight = when (deviceType) {
                DeviceType.Phone -> 400.dp
                DeviceType.Foldable -> 450.dp
                DeviceType.Tablet -> 500.dp
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (currentWord != null) {
                    val isHighPriority = leitnerUiState.highPriorityWords.contains(currentWord)

                    // تعیین سایز آیکون‌ها بر اساس نوع دستگاه
                    val iconSize = when (deviceType) {
                        DeviceType.Phone -> 48.dp
                        DeviceType.Foldable -> 56.dp
                        DeviceType.Tablet -> 64.dp
                    }

                    val volumeIconSize = when (deviceType) {
                        DeviceType.Phone -> 32.dp
                        DeviceType.Foldable -> 36.dp
                        DeviceType.Tablet -> 40.dp
                    }

                    // تعیین سایز فونت‌ها
                    val englishFontSize = when (deviceType) {
                        DeviceType.Phone -> 30.sp
                        DeviceType.Foldable -> 34.sp
                        DeviceType.Tablet -> 38.sp
                    }

                    val persianFontSize = when (deviceType) {
                        DeviceType.Phone -> 20.sp
                        DeviceType.Foldable -> 24.sp
                        DeviceType.Tablet -> 28.sp
                    }

                    val hintFontSize = when (deviceType) {
                        DeviceType.Phone -> 16.sp
                        DeviceType.Foldable -> 18.sp
                        DeviceType.Tablet -> 20.sp
                    }

                    val buttonTextSize = when (deviceType) {
                        DeviceType.Phone -> 14.sp
                        DeviceType.Foldable -> 15.sp
                        DeviceType.Tablet -> 16.sp
                    }

                    val buttonHeight = when (deviceType) {
                        DeviceType.Phone -> 48.dp
                        DeviceType.Foldable -> 52.dp
                        DeviceType.Tablet -> 56.dp
                    }

                    if (isHighPriority) {
                        AdaptiveCartLayoutReview(
                            word = currentWord,
                            onKnow = { leitnerBoxViewModel.markWordAsLearned(it) },
                            onDontKnow = { leitnerBoxViewModel.markWordAsForgotten(it) },
                            onClickToShowMeaning = { leitnerBoxViewModel.onClickToShowMeaning() },
                            isMeaningVisible = meaningWord,
                            onSpeakWord = { leitnerBoxViewModel.speakWord(currentWord.english) },
                            deviceType = deviceType,
                            cardMaxWidth = cardMaxWidth,
                            cardMinHeight = cardMinHeight,
                            cardMaxHeight = cardMaxHeight,
                            iconSize = iconSize,
                            volumeIconSize = volumeIconSize,
                            englishFontSize = englishFontSize,
                            persianFontSize = persianFontSize,
                            hintFontSize = hintFontSize,
                            buttonTextSize = buttonTextSize,
                            buttonHeight = buttonHeight,
                            modifier = Modifier.fillMaxWidth(cardMaxWidth)
                        )
                    } else {
                        AdaptiveCartLayout(
                            word = currentWord,
                            onKnow = { leitnerBoxViewModel.markWordAsLearned(it) },
                            onDontKnow = { leitnerBoxViewModel.markWordAsForgotten(it) },
                            onClickToShowMeaning = { leitnerBoxViewModel.onClickToShowMeaning() },
                            isMeaningVisible = meaningWord,
                            onSpeakWord = { leitnerBoxViewModel.speakWord(currentWord.english) },
                            deviceType = deviceType,
                            cardMaxWidth = cardMaxWidth,
                            cardMinHeight = cardMinHeight,
                            cardMaxHeight = cardMaxHeight,
                            iconSize = iconSize,
                            volumeIconSize = volumeIconSize,
                            englishFontSize = englishFontSize,
                            persianFontSize = persianFontSize,
                            hintFontSize = hintFontSize,
                            buttonTextSize = buttonTextSize,
                            buttonHeight = buttonHeight,
                            modifier = Modifier.fillMaxWidth(cardMaxWidth)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "!!!همه کلمات مرور شدند",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = when (deviceType) {
                                DeviceType.Phone -> 16.sp
                                DeviceType.Foldable -> 18.sp
                                DeviceType.Tablet -> 20.sp
                            },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdaptiveCartLayout(
    word: Word,
    onKnow: (Word) -> Unit,
    onDontKnow: (Word) -> Unit,
    onClickToShowMeaning: () -> Unit,
    isMeaningVisible: Boolean,
    onSpeakWord: () -> Unit,
    deviceType: DeviceType,
    cardMaxWidth: Float,
    cardMinHeight: androidx.compose.ui.unit.Dp,
    cardMaxHeight: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    volumeIconSize: androidx.compose.ui.unit.Dp,
    englishFontSize: androidx.compose.ui.unit.TextUnit,
    persianFontSize: androidx.compose.ui.unit.TextUnit,
    hintFontSize: androidx.compose.ui.unit.TextUnit,
    buttonTextSize: androidx.compose.ui.unit.TextUnit,
    buttonHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // تعیین فاصله‌ها بر اساس نوع دستگاه
    val topSpacing = when (deviceType) {
        DeviceType.Phone -> 74.dp
        DeviceType.Foldable -> 60.dp
        DeviceType.Tablet -> 50.dp
    }

    val bottomSpacing = when (deviceType) {
        DeviceType.Phone -> 85.dp
        DeviceType.Foldable -> 70.dp
        DeviceType.Tablet -> 60.dp
    }

    val cardPadding = when (deviceType) {
        DeviceType.Phone -> 16.dp
        DeviceType.Foldable -> 20.dp
        DeviceType.Tablet -> 24.dp
    }

    Card(
        modifier = modifier
            .heightIn(cardMinHeight, cardMaxHeight)
            .padding(cardPadding),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(iconSize)
                        .padding(top = 16.dp, start = 16.dp)
                        .clickable {
                            val url = "https://www.google.com/search?tbm=isch&q=${word.english}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                )
            }

            Spacer(modifier = Modifier.height(topSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = englishFontSize,
                    modifier = Modifier.alignByBaseline()
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = "Pronounce word",
                    modifier = Modifier
                        .size(volumeIconSize)
                        .clickable { onSpeakWord() }
                        .alignByBaseline()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isMeaningVisible) {
                Text(
                    text = word.persian,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = persianFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "نمایش معنی کلمه",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = hintFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onClickToShowMeaning)
                )
            }

            Spacer(modifier = Modifier.height(bottomSpacing))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 34.dp, bottom = 21.dp)
            ) {
                OutlinedButton(
                    onClick = { onDontKnow(word) },
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "I don't know",
                        fontSize = buttonTextSize,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { onKnow(word) },
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "I know",
                        fontSize = buttonTextSize,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AdaptiveCartLayoutReview(
    word: Word,
    onKnow: (Word) -> Unit,
    onDontKnow: (Word) -> Unit,
    onClickToShowMeaning: () -> Unit,
    onSpeakWord: () -> Unit,
    isMeaningVisible: Boolean,
    deviceType: DeviceType,
    cardMaxWidth: Float,
    cardMinHeight: androidx.compose.ui.unit.Dp,
    cardMaxHeight: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    volumeIconSize: androidx.compose.ui.unit.Dp,
    englishFontSize: androidx.compose.ui.unit.TextUnit,
    persianFontSize: androidx.compose.ui.unit.TextUnit,
    hintFontSize: androidx.compose.ui.unit.TextUnit,
    buttonTextSize: androidx.compose.ui.unit.TextUnit,
    buttonHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // تعیین فاصله‌ها بر اساس نوع دستگاه
    val topSpacing = when (deviceType) {
        DeviceType.Phone -> 74.dp
        DeviceType.Foldable -> 60.dp
        DeviceType.Tablet -> 50.dp
    }

    val bottomSpacing = when (deviceType) {
        DeviceType.Phone -> 85.dp
        DeviceType.Foldable -> 70.dp
        DeviceType.Tablet -> 60.dp
    }

    val cardPadding = when (deviceType) {
        DeviceType.Phone -> 16.dp
        DeviceType.Foldable -> 20.dp
        DeviceType.Tablet -> 24.dp
    }

    Card(
        modifier = modifier
            .heightIn(cardMinHeight, cardMaxHeight)
            .padding(cardPadding),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(iconSize)
                        .padding(top = 16.dp, start = 16.dp)
                        .clickable {
                            val url = "https://www.google.com/search?tbm=isch&q=${word.english}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                )
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(iconSize)
                        .padding(top = 16.dp, end = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(topSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = englishFontSize,
                    modifier = Modifier.alignByBaseline()
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = "Pronounce word",
                    modifier = Modifier
                        .size(volumeIconSize)
                        .clickable { onSpeakWord() }
                        .alignByBaseline()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isMeaningVisible) {
                Text(
                    text = word.persian,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = persianFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onClickToShowMeaning)
                )
            } else {
                Text(
                    text = "نمایش معنی کلمه",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = hintFontSize,
                    modifier = Modifier.clickable(onClick = onClickToShowMeaning),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(bottomSpacing))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 34.dp, bottom = 21.dp)
            ) {
                OutlinedButton(
                    onClick = { onDontKnow(word) },
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "I don't know",
                        fontSize = buttonTextSize,
                        fontWeight = FontWeight.Medium
                    )
                }
                Button(
                    onClick = { onKnow(word) },
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "I know",
                        fontSize = buttonTextSize,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// نگه داشتن کدهای قدیمی برای Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun CartLayoutReviewPreview() {
    MyDictionaryTheme {
        AdaptiveCartLayoutReview(
            word = Word(1, english = "Search", persian = "جست و جو"),
            onKnow = {},
            onDontKnow = {},
            onClickToShowMeaning = {},
            onSpeakWord = {},
            isMeaningVisible = false,
            deviceType = DeviceType.Phone,
            cardMaxWidth = 0.9f,
            cardMinHeight = 300.dp,
            cardMaxHeight = 400.dp,
            iconSize = 48.dp,
            volumeIconSize = 32.dp,
            englishFontSize = 30.sp,
            persianFontSize = 20.sp,
            hintFontSize = 16.sp,
            buttonTextSize = 14.sp,
            buttonHeight = 48.dp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun CartLayoutPreview() {
    MyDictionaryTheme {
        AdaptiveCartLayout(
            word = Word(id = 2, english = "Car", persian = "ماشین"),
            onKnow = {},
            onDontKnow = {},
            onClickToShowMeaning = {},
            onSpeakWord = {},
            isMeaningVisible = false,
            deviceType = DeviceType.Phone,
            cardMaxWidth = 0.9f,
            cardMinHeight = 300.dp,
            cardMaxHeight = 400.dp,
            iconSize = 48.dp,
            volumeIconSize = 32.dp,
            englishFontSize = 30.sp,
            persianFontSize = 20.sp,
            hintFontSize = 16.sp,
            buttonTextSize = 14.sp,
            buttonHeight = 48.dp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun LeitnerBoxPreview() {
    MyDictionaryTheme {
        LeitnerScreen(
            navigateBack = {},
            leitnerBoxViewModel = viewModel()
        )
    }
}