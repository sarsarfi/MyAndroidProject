package com.example.mydictionary.ui.addword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme

object AddWordDestination : NavigationDestination {
    override val route = "addnewword"
    override val titleRes = R.string.add_new_word
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    modifier: Modifier = Modifier,
    viewModel: AddWordViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    MyDictionaryTheme {
        Scaffold(
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(AddWordDestination.titleRes),
                    canNavigateBack = true,
                    scrollBehavior = scrollBehavior,
                    navigateUp = navigateBack
                )
            }
        ) { innerPadding ->
            WordEntryBody(
                addWordUiState = uiState,
                onValueChange = viewModel::update,
                onSaveWord = {
                    viewModel.saveWord {
                        navigateBack()
                    }
                },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun WordEntryBody(
    addWordUiState: AddWordUiState,
    onValueChange: (AddWordDetails) -> Unit,
    onSaveWord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WordInputForm(
            wordDetails = addWordUiState.addWordDetails,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveWord,
            enabled = addWordUiState.isValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        ) {
            Text(text = stringResource(R.string.save_vocabulary))
        }
    }
}

@Composable
private fun WordInputForm(
    wordDetails: AddWordDetails,
    onValueChange: (AddWordDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = wordDetails.englishWord,
            onValueChange = { onValueChange(wordDetails.copy(englishWord = it)) },
            label = { Text(stringResource(R.string.enter_your_word)) },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = wordDetails.meaningWord,
            onValueChange = { onValueChange(wordDetails.copy(meaningWord = it)) },
            label = { Text(stringResource(R.string.persian_meaning)) },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddWordScreenPreview() {
    MyDictionaryTheme {
        AddWordScreen(navigateBack = {})
    }
}