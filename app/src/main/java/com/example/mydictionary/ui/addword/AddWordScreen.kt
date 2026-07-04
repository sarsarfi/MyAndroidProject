package com.example.mydictionary.ui.addword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
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

object AddWordDestination : NavigationDestination {
    override val route = "addnewword"
    override val titleRes = R.string.add_new_word
}

// ۱. این کامپوزبل فقط در ناوبری برنامه (Navigation) صدا زده می‌شود
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val deviceType = rememberDeviceType()

    // ارسال داده‌ها به کامپوزبل پایینی بدون درگیر کردن کدهای دیتابیس در Preview
    AddWordScreenContent(
        uiState = uiState,
        deviceType = deviceType,
        onValueChange = viewModel::update,
        onSaveWord = {
            viewModel.saveWord {
                navigateBack()
            }
        },
        navigateBack = navigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreenContent(
    uiState: AddWordUiState,
    deviceType: DeviceType,
    onValueChange: (AddWordDetails) -> Unit,
    onSaveWord: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            DictionaryTopAppBar(
                title = stringResource(AddWordDestination.titleRes),
                canNavigateBack = true,
                scrollBehavior = scrollBehavior,
                navigateUp = navigateBack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveWordEntryBody(
            addWordUiState = uiState,
            onValueChange = onValueChange,
            onSaveWord = onSaveWord,
            deviceType = deviceType,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
private fun AdaptiveWordEntryBody(
    addWordUiState: AddWordUiState,
    onValueChange: (AddWordDetails) -> Unit,
    onSaveWord: () -> Unit,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {

    val horizontalPadding = when (deviceType) {
        DeviceType.Phone -> 16.dp
        DeviceType.Foldable -> 24.dp
        DeviceType.Tablet -> 32.dp
    }

    val elementSpacing = when (deviceType) {
        DeviceType.Phone -> 16.dp
        DeviceType.Foldable -> 24.dp
        DeviceType.Tablet -> 32.dp
    }

    val maxFormWidth = when (deviceType) {
        DeviceType.Phone -> Modifier.fillMaxWidth()
        DeviceType.Foldable -> Modifier.widthIn(max = 500.dp)
        DeviceType.Tablet -> Modifier.widthIn(max = 600.dp)
    }

    val buttonHeight = when (deviceType) {
        DeviceType.Phone -> 48.dp
        DeviceType.Foldable -> 56.dp
        DeviceType.Tablet -> 64.dp
    }

    val buttonTextSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
            .padding(top = 16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Top
    ) {

        Column(
            modifier = Modifier.then(maxFormWidth),

            verticalArrangement = Arrangement.spacedBy(elementSpacing),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // title for tablet/foldable
            if (deviceType != DeviceType.Phone) {

                Text(
                    text = stringResource(R.string.add_new_word),

                    fontSize = when (deviceType) {
                        DeviceType.Foldable -> 24.sp
                        DeviceType.Tablet -> 28.sp
                        else -> 20.sp
                    },

                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // textfields
            AdaptiveWordInputForm(
                wordDetails = addWordUiState.addWordDetails,
                onValueChange = onValueChange,
                deviceType = deviceType,
                modifier = Modifier.fillMaxWidth()
            )

            // save button
            Button(
                onClick = onSaveWord,

                enabled = addWordUiState.isValid,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),

                shape = MaterialTheme.shapes.small,

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {

                Text(
                    text = stringResource(R.string.save_vocabulary),
                    fontSize = buttonTextSize
                )
            }
        }
    }
}

@Composable
private fun AdaptiveWordInputForm(
    wordDetails: AddWordDetails,
    onValueChange: (AddWordDetails) -> Unit,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {

    val textFieldHeight = when (deviceType) {
        DeviceType.Phone -> 65.dp
        DeviceType.Foldable -> 64.dp
        DeviceType.Tablet -> 72.dp
    }

    val labelFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    val fieldSpacing = when (deviceType) {
        DeviceType.Phone -> 12.dp
        DeviceType.Foldable -> 20.dp
        DeviceType.Tablet -> 24.dp
    }

    Column(
        modifier = modifier.fillMaxWidth(),

        verticalArrangement = Arrangement.spacedBy(fieldSpacing)
    ) {

        // English field
        OutlinedTextField(
            value = wordDetails.englishWord,

            onValueChange = { newValue ->

                val filteredValue = newValue.filter {
                    it.isLetter() || it == ' '
                }

                onValueChange(
                    wordDetails.copy(
                        englishWord = filteredValue.lowercase()
                    )
                )
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(textFieldHeight),

            singleLine = true,

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.None
            ),

            label = {
                Text(
                    text = stringResource(R.string.enter_your_word),
                    fontSize = labelFontSize
                )
            },

            shape = MaterialTheme.shapes.small,

            colors = TextFieldDefaults.colors(
                focusedContainerColor =
                    MaterialTheme.colorScheme.secondaryContainer,

                unfocusedContainerColor =
                    MaterialTheme.colorScheme.secondaryContainer,

                disabledContainerColor =
                    MaterialTheme.colorScheme.secondaryContainer,

                errorContainerColor =
                    MaterialTheme.colorScheme.errorContainer
            )
        )

        // Persian field
        OutlinedTextField(
            value = wordDetails.meaningWord,

            onValueChange = {
                onValueChange(
                    wordDetails.copy(
                        meaningWord = it
                    )
                )
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(textFieldHeight),

            singleLine = true,

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),

            label = {
                Text(
                    text = stringResource(R.string.persian_meaning),
                    fontSize = labelFontSize
                )
            },

            shape = MaterialTheme.shapes.small,

            colors = TextFieldDefaults.colors(
                focusedContainerColor =
                    MaterialTheme.colorScheme.secondaryContainer,

                unfocusedContainerColor =
                    MaterialTheme.colorScheme.secondaryContainer,

                disabledContainerColor =
                    MaterialTheme.colorScheme.secondaryContainer,

                errorContainerColor =
                    MaterialTheme.colorScheme.errorContainer
            )
        )
    }
}

// نگه داشتن کدهای قدیمی برای Preview
@Composable
private fun WordEntryBody(
    addWordUiState: AddWordUiState,
    onValueChange: (AddWordDetails) -> Unit,
    onSaveWord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                contentColor = MaterialTheme.colorScheme.onPrimary
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
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isLetter() || it == ' ' }
                val lowercaseValue = filteredValue.lowercase()
                onValueChange(wordDetails.copy(englishWord = lowercaseValue))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.None
            ),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640, name = "Phone")
@Preview(showBackground = true, widthDp = 600, heightDp = 800, name = "Foldable")
@Preview(showBackground = true, widthDp = 840, heightDp = 1000, name = "Tablet")
@Composable
fun AddWordScreenPreview() {
    MyDictionaryTheme {
        val mockUiState = AddWordUiState(
            addWordDetails = AddWordDetails(
                englishWord = "developer",
                meaningWord = "توسعه دهنده"
            ),
            isValid = true
        )

        AddWordScreenContent(
            uiState = mockUiState,
            deviceType = DeviceType.Phone,
            onValueChange = {},
            onSaveWord = {},
            navigateBack = {}
        )
    }
}