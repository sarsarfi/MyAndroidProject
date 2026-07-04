package com.example.mydictionary.ui.quiz

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.adaptive.DeviceType
import com.example.mydictionary.ui.adaptive.rememberDeviceType
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme

object QuizDestination : NavigationDestination {
    override val route = "quiz"
    override val titleRes = R.string.quiz
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    quizViewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val quizUiState by quizViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val deviceType = rememberDeviceType()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        quizViewModel.initializeTts(context)
    }

    if (quizUiState.message.contains("No words available")) {
        AlertDialog(
            onDismissRequest = { navigateBack() },
            title = {
                Text(
                    text = stringResource(R.string.no_words_title),
                    fontSize = when (deviceType) {
                        DeviceType.Phone -> 18.sp
                        DeviceType.Foldable -> 20.sp
                        DeviceType.Tablet -> 22.sp
                    }
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.no_words_body),
                    fontSize = when (deviceType) {
                        DeviceType.Phone -> 14.sp
                        DeviceType.Foldable -> 15.sp
                        DeviceType.Tablet -> 16.sp
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = navigateBack,
                    modifier = Modifier.height(
                        when (deviceType) {
                            DeviceType.Phone -> 36.dp
                            else -> 48.dp
                        }
                    )
                ) {
                    Text(
                        text = stringResource(R.string.back),
                        fontSize = when (deviceType) {
                            DeviceType.Phone -> 14.sp
                            else -> 16.sp
                        }
                    )
                }
            }
        )
        return
    }

    val cardMaxWidth = when (deviceType) {
        DeviceType.Phone -> Modifier.fillMaxWidth()
        DeviceType.Foldable -> Modifier.widthIn(max = 500.dp)
        DeviceType.Tablet -> Modifier.widthIn(max = 600.dp)
    }

    MyDictionaryTheme {
        Scaffold(
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(QuizDestination.titleRes),
                    canNavigateBack = true,
                    scrollBehavior = scrollBehavior,
                    navigateUp = navigateBack
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .safeDrawingPadding()
                        .then(cardMaxWidth)
                        .padding(horizontal = when (deviceType) {
                            DeviceType.Phone -> 0.dp
                            DeviceType.Foldable -> 16.dp
                            DeviceType.Tablet -> 32.dp
                        }),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AdaptiveGameLayout(
                        quizUiState = quizUiState,
                        onDone = { quizViewModel.checkGuessUser() },
                        onValueChange = { quizViewModel.userGuess(it) },
                        onSpeakWord = { quizViewModel.speakCurrentCorrectWord() },
                        deviceType = deviceType,
                        isLandscape = isLandscape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(mediumPadding)
                    )

                    Text(
                        text = quizUiState.message,
                        color = if (quizUiState.isGuess) colorScheme.primary else colorScheme.error, // اصلاح رنگ بر اساس درستی یا نادرستی پاسخ
                        style = typography.bodyLarge,
                        fontSize = when (deviceType) {
                            DeviceType.Phone -> 14.sp
                            DeviceType.Foldable -> 15.sp
                            DeviceType.Tablet -> 16.sp
                        },
                        modifier = Modifier.padding(bottom = if (isLandscape) 4.dp else mediumPadding)
                    )

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = mediumPadding, vertical = if (isLandscape) 4.dp else mediumPadding),
                            horizontalArrangement = Arrangement.spacedBy(mediumPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val buttonHeight = when (deviceType) {
                                DeviceType.Phone -> if (isLandscape) 40.dp else 48.dp
                                DeviceType.Foldable -> 52.dp
                                DeviceType.Tablet -> 56.dp
                            }

                            val buttonTextSize = when (deviceType) {
                                DeviceType.Phone -> 14.sp
                                DeviceType.Foldable -> 15.sp
                                DeviceType.Tablet -> 16.sp
                            }

                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(buttonHeight),
                                onClick = { quizViewModel.checkGuessUser() },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = stringResource(R.string.submit),
                                    fontSize = buttonTextSize
                                )
                            }

                            OutlinedButton(
                                onClick = { quizViewModel.skip() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(buttonHeight),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = stringResource(R.string.skip),
                                    fontSize = buttonTextSize
                                )
                            }
                        }
                    }

                    AdaptiveGameStatus(
                        score = quizUiState.score,
                        deviceType = deviceType,
                        isLandscape = isLandscape,
                        modifier = Modifier.padding(if (isLandscape) 8.dp else 20.dp)
                    )
                }
            }

            if (quizUiState.isGameOver) {
                MyDictionaryTheme {
                    AdaptiveFinalScoreDialog(
                        score = quizUiState.score,
                        onPlayAgain = { quizViewModel.restartGame() },
                        onExist = { navigateBack() },
                        deviceType = deviceType
                    )
                }
            }
        }
    }
}

@Composable
fun AdaptiveGameStatus(
    score: Int,
    deviceType: DeviceType,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val scoreFontSize = when (deviceType) {
        DeviceType.Phone -> if (isLandscape) 20.sp else 24.sp
        DeviceType.Foldable -> 28.sp
        DeviceType.Tablet -> 32.sp
    }

    val cardPadding = when (deviceType) {
        DeviceType.Phone -> 8.dp
        DeviceType.Foldable -> 10.dp
        DeviceType.Tablet -> 12.dp
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = when (deviceType) {
            DeviceType.Phone -> 4.dp
            else -> 6.dp
        })
    ) {
        Text(
            text = stringResource(R.string.score, score),
            style = typography.headlineMedium,
            fontSize = scoreFontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(cardPadding)
        )
    }
}

@Composable
fun AdaptiveGameLayout(
    modifier: Modifier = Modifier,
    quizUiState: QuizUiState,
    onValueChange: (String) -> Unit,
    onSpeakWord: () -> Unit,
    onDone: () -> Unit,
    deviceType: DeviceType,
    isLandscape: Boolean
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    val wordFontSize = when (deviceType) {
        DeviceType.Phone -> if (isLandscape) 22.sp else 28.sp
        DeviceType.Foldable -> 32.sp
        DeviceType.Tablet -> 36.sp
    }

    val instructionFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 15.sp
        DeviceType.Tablet -> 16.sp
    }

    val wordCountFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 15.sp
        DeviceType.Tablet -> 16.sp
    }

    val iconSize = when (deviceType) {
        DeviceType.Phone -> if (isLandscape) 28.dp else 32.dp
        DeviceType.Foldable -> 36.dp
        DeviceType.Tablet -> 40.dp
    }

    val textFieldHeight = when (deviceType) {
        DeviceType.Phone -> if (isLandscape) 56.dp else 65.dp
        DeviceType.Foldable -> 64.dp
        DeviceType.Tablet -> 72.dp
    }

    val cardPadding = when (deviceType) {
        DeviceType.Phone -> if (isLandscape) 12.dp else mediumPadding
        DeviceType.Foldable -> 24.dp
        DeviceType.Tablet -> 32.dp
    }

    // بررسی پویا برای نمایش خطا (فقط اگر حدس زده شده باشد و نادرست باشد خطا نشان می‌دهد)
    val isInputError = quizUiState.inputUserGuess.isNotEmpty() && !quizUiState.isGuess

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(cardPadding)
        ) {
            Text(
                modifier = Modifier
                    .clip(shapes.medium)
                    .background(colorScheme.surfaceTint)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .align(alignment = Alignment.Start),
                text = stringResource(R.string.word_count, quizUiState.currentWordCount),
                style = typography.titleMedium,
                fontSize = wordCountFontSize,
                color = colorScheme.onPrimary,
            )

            Text(
                text = quizUiState.currentWord,
                fontSize = wordFontSize,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = Icons.Filled.VolumeUp,
                contentDescription = null,
                tint = if (quizUiState.isGuess) colorScheme.primary else colorScheme.onSurface,
                modifier = Modifier
                    .clickable { onSpeakWord() }
                    .size(iconSize)
            )

            if (!isLandscape) { // حذف دستورالعمل در حالت افقی گوشی برای باز شدن فضا
                Text(
                    text = stringResource(R.string.instructions),
                    textAlign = TextAlign.Center,
                    style = typography.titleMedium,
                    fontSize = instructionFontSize
                )
            }

            OutlinedTextField(
                value = quizUiState.inputUserGuess,
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(textFieldHeight),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = { newValue ->
                    val filteredValue = newValue.filter { it.isLetter() || it == ' ' }
                    val lowercaseValue = filteredValue.lowercase()
                    onValueChange(lowercaseValue)
                },
                label = {
                    Text(
                        stringResource(R.string.enter_your_word),
                        fontSize = when (deviceType) {
                            DeviceType.Phone -> 14.sp
                            else -> 16.sp
                        }
                    )
                },
                isError = isInputError, // ✅ پویا و اصلاح شد
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDone() }
                ),
            )
        }
    }
}

@Composable
private fun AdaptiveFinalScoreDialog(
    score: Int,
    onPlayAgain: () -> Unit,
    onExist: () -> Unit,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    val titleFontSize = when (deviceType) {
        DeviceType.Phone -> 18.sp
        DeviceType.Foldable -> 20.sp
        DeviceType.Tablet -> 22.sp
    }

    val textFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 15.sp
        DeviceType.Tablet -> 16.sp
    }

    val buttonTextSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 15.sp
        DeviceType.Tablet -> 16.sp
    }

    val buttonHeight = when (deviceType) {
        DeviceType.Phone -> 36.dp
        DeviceType.Foldable -> 40.dp
        DeviceType.Tablet -> 48.dp
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(R.string.congratulations),
                fontSize = titleFontSize
            )
        },
        text = {
            Text(
                text = stringResource(R.string.you_scored, score),
                fontSize = textFontSize
            )
        },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = { onExist() },
                modifier = Modifier.height(buttonHeight)
            ) {
                Text(
                    text = stringResource(R.string.exit),
                    fontSize = buttonTextSize
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPlayAgain() },
                modifier = Modifier.height(buttonHeight)
            ) {
                Text(
                    text = stringResource(R.string.play_again),
                    fontSize = buttonTextSize
                )
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun GameScreenPreview() {
    MyDictionaryTheme {
        GameScreen(navigateBack = {}, quizViewModel = viewModel())
    }
}