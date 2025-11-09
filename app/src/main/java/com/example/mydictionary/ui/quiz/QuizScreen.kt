package com.example.mydictionary.ui.quiz


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme

object QuizDestination : NavigationDestination{
    override val route = "quiz"
    override val titleRes = R.string.quize
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(modifier: Modifier = Modifier,
               navigateBack : () -> Unit,
               quizViewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val quizUiState by quizViewModel.uiState.collectAsState()
    MyDictionaryTheme {
        Scaffold(
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(QuizDestination.titleRes),
                    canNavigateBack = true,
                    scrollBehavior = scrollBehavior ,
                    navigateUp = navigateBack
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .safeDrawingPadding()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameLayout(
                    quizUiState =quizUiState ,
                    onDone = {quizViewModel.checkGuessUser()},
                    onValueChange = {quizViewModel.userGuess(it)},
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(mediumPadding)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(mediumPadding),
                    verticalArrangement = Arrangement.spacedBy(mediumPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {quizViewModel.submit()} ,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(R.string.submit),
                            fontSize = 16.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {quizViewModel.skip()},
                        modifier = Modifier.fillMaxWidth() ,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(R.string.skip),
                            fontSize = 16.sp
                        )
                    }
                }

                GameStatus(score = quizUiState.score, modifier = Modifier.padding(20.dp))
            }
        }
    }
}
@Composable
fun GameStatus(score: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier ,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = stringResource(R.string.score, score),
            style = typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun GameLayout(modifier: Modifier = Modifier,
               quizUiState: QuizUiState,
               onValueChange : (String) -> Unit,
               onDone : () -> Unit
){
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp) ,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(mediumPadding)
        ) {
            Text(
                modifier = Modifier
                    .clip(shapes.medium)
                    .background(colorScheme.surfaceTint)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .align(alignment = Alignment.End),
                text = stringResource(R.string.word_count, quizUiState.currentWordCount),
                style = typography.titleMedium,
                color = colorScheme.onPrimary ,
            )
            Text(
                text = quizUiState.currentWord,
                style = typography.displayMedium
            )
            Text(
                text = stringResource(R.string.instructions),
                textAlign = TextAlign.Center,
                style = typography.titleMedium
            )
            OutlinedTextField(
                value = quizUiState.inputUserGuess,
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = {onValueChange(it)},
                label = { Text(stringResource(R.string.enter_your_word)) },
                isError = false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {onDone()}
                ) ,
            )
        }
    }
}

/*
 * Creates and shows an AlertDialog with final score.
 */
@Composable
private fun FinalScoreDialog(
    score: Int,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {

    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onCloseRequest.
        },
        title = { Text(text = stringResource(R.string.congratulations)) },
        text = { Text(text = stringResource(R.string.you_scored, score)) },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = {}
            ) {
                Text(text = stringResource(R.string.exit))
            }
        },
        confirmButton = {
            TextButton(onClick = {  }) {
                Text(text = stringResource(R.string.play_again))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    MyDictionaryTheme {
        GameScreen(navigateBack = {} , quizViewModel = viewModel())
    }
}
