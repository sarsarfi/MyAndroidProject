package com.example.mydictionary.ui.editword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
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
import kotlinx.coroutines.launch

object WordEditDestination : NavigationDestination {
    override val route = "word_edit"
    override val titleRes = R.string.edit_screen
    const val wordIdArg = "wordId"
    val routeWithArgs = "$route/{$wordIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WordEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val deviceType = rememberDeviceType()

    Scaffold(
        topBar = {
            DictionaryTopAppBar(
                title = stringResource(WordEditDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        modifier = modifier
    ) { innerPadding ->

        AdaptiveWordEntryBody(
            wordUiState = uiState,
            onWordValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateWord()
                    navigateBack()
                }
            },
            deviceType = deviceType,
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding()
                )
                .fillMaxSize()
        )
    }
}

@Composable
private fun AdaptiveWordEntryBody(
    wordUiState: WordUiState,
    onWordValueChange: (WordDetails) -> Unit,
    onSaveClick: () -> Unit,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    // تعیین پدینگ بر اساس نوع دستگاه
    val horizontalPadding = when (deviceType) {
        DeviceType.Phone -> 16.dp
        DeviceType.Foldable -> 24.dp
        DeviceType.Tablet -> 32.dp
    }

    // تعیین فاصله بین المان‌ها
    val elementSpacing = when (deviceType) {
        DeviceType.Phone -> 16.dp
        DeviceType.Foldable -> 24.dp
        DeviceType.Tablet -> 32.dp
    }

    // تعیین حداکثر عرض فرم (برای تبلت که خیلی پهن نشود)
    val maxFormWidth = when (deviceType) {
        DeviceType.Phone -> Modifier.fillMaxWidth()
        DeviceType.Foldable -> Modifier.widthIn(max = 500.dp)
        DeviceType.Tablet -> Modifier.widthIn(max = 600.dp)
    }

    // سایز فونت برای متن دکمه
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
            // عنوان (برای تبلت و گوشی تاشو)
            if (deviceType != DeviceType.Phone) {
                Text(
                    text = stringResource(R.string.edit_screen),
                    fontSize = when (deviceType) {
                        DeviceType.Foldable -> 24.sp
                        DeviceType.Tablet -> 28.sp
                        else -> 20.sp
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // فرم ورودی adaptive
            AdaptiveWordInputForm(
                wordDetails = wordUiState.wordDetails,
                onValueChange = onWordValueChange,
                deviceType = deviceType,
                modifier = Modifier.fillMaxWidth()
            )

            // دکمه ذخیره
            Button(
                onClick = onSaveClick,
                enabled = wordUiState.isEntryValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(when (deviceType) {
                        DeviceType.Phone -> 48.dp
                        DeviceType.Foldable -> 56.dp
                        DeviceType.Tablet -> 64.dp
                    }),
                shape = MaterialTheme.shapes.small
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
    wordDetails: WordDetails,
    onValueChange: (WordDetails) -> Unit,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    // تعیین ارتفاع فیلدها
    val textFieldHeight = when (deviceType) {
        DeviceType.Phone -> 65.dp
        DeviceType.Foldable -> 64.dp
        DeviceType.Tablet -> 72.dp
    }

    // تعیین سایز فونت برچسب
    val labelFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            when (deviceType) {
                DeviceType.Phone -> 16.dp
                DeviceType.Foldable -> 20.dp
                DeviceType.Tablet -> 24.dp
            }
        )
    ) {
        // فیلد کلمه انگلیسی
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
            label = {
                Text(
                    stringResource(R.string.enter_your_word),
                    fontSize = labelFontSize
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(textFieldHeight)
        )

        // فیلد معنی فارسی
        OutlinedTextField(
            value = wordDetails.meaningWord,
            onValueChange = { onValueChange(wordDetails.copy(meaningWord = it)) },
            label = {
                Text(
                    stringResource(R.string.persian_meaning),
                    fontSize = labelFontSize
                )
            },
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
            modifier = Modifier
                .fillMaxWidth()
                .height(textFieldHeight)
        )
    }
}
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun WordEditScreenPreview() {
    MyDictionaryTheme {
        WordEditScreen(
            navigateBack = {},
            onNavigateUp = {}
        )
    }
}