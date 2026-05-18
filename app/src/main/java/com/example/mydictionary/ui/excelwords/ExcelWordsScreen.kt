package com.example.mydictionary.ui.excelwords

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

object ExcelWordsScreenDestination : NavigationDestination {
    override val route: String = "ExcelWordsScreen"
    override val titleRes: Int = R.string.list_words_excel
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelWordListScreen(
    navigateToExcel: () -> Unit,
    navigateBack: () -> Unit,
    excelWordsViewModel: ExcelWordsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val ExcelUiState by excelWordsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val deviceType = rememberDeviceType()

    // لانچر برای انتخاب فایل
    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flag)
                excelWordsViewModel.readExcelFile(context, it)
            }
        }
    )

    MyDictionaryTheme {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(ExcelWordsScreenDestination.titleRes),
                    canNavigateBack = true,
                    scrollBehavior = scrollBehavior,
                    navigateUp = navigateBack
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        excelPickerLauncher.launch(arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel"
                        ))
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(
                        when (deviceType) {
                            DeviceType.Phone -> 56.dp
                            DeviceType.Foldable -> 64.dp
                            DeviceType.Tablet -> 72.dp
                        }
                    )
                ) {
                    Text(
                        text = "Import",
                        fontSize = when (deviceType) {
                            DeviceType.Phone -> 12.sp
                            DeviceType.Foldable -> 14.sp
                            DeviceType.Tablet -> 16.sp
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { innerPadding ->
            AdaptiveWordListBody(
                wordsList = ExcelUiState.words,
                contentPadding = innerPadding,
                deviceType = deviceType,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun AdaptiveWordListBody(
    wordsList: List<Word>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    deviceType: DeviceType = DeviceType.Phone
) {
    // تعیین پدینگ کناری لیست بر اساس نوع دستگاه
    val listHorizontalPadding = when (deviceType) {
        DeviceType.Phone -> 0.dp
        DeviceType.Foldable -> 16.dp
        DeviceType.Tablet -> 32.dp
    }

    // تعیین فاصله بین آیتم‌ها
    val itemSpacing = when (deviceType) {
        DeviceType.Phone -> 4.dp
        DeviceType.Foldable -> 8.dp
        DeviceType.Tablet -> 12.dp
    }

    // تعیین سایز فونت برای متن خالی
    val emptyTextFontSize = when (deviceType) {
        DeviceType.Phone -> 16.sp
        DeviceType.Foldable -> 20.sp
        DeviceType.Tablet -> 24.sp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (wordsList.isEmpty()) {
            Text(
                text = stringResource(R.string.empity_list_excel),
                style = MaterialTheme.typography.titleLarge,
                fontSize = emptyTextFontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(contentPadding)
            )
        } else {
            AdaptiveListWords(
                wordsList = wordsList,
                contentPadding = PaddingValues(
                    horizontal = listHorizontalPadding,
                    vertical = 8.dp
                ),
                deviceType = deviceType,
                itemSpacing = itemSpacing,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun AdaptiveListWords(
    wordsList: List<Word>,
    contentPadding: PaddingValues,
    deviceType: DeviceType,
    itemSpacing: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        items(items = wordsList) { item ->
            AdaptiveDictionaryWord(
                word = item,
                deviceType = deviceType,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AdaptiveDictionaryWord(
    word: Word,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    // تعیین سایز فونت‌ها بر اساس نوع دستگاه
    val englishFontSize = when (deviceType) {
        DeviceType.Phone -> 16.sp
        DeviceType.Foldable -> 18.sp
        DeviceType.Tablet -> 20.sp
    }

    val persianFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    // تعیین پدینگ داخلی کارت
    val cardHorizontalPadding = when (deviceType) {
        DeviceType.Phone -> 20.dp
        DeviceType.Foldable -> 24.dp
        DeviceType.Tablet -> 32.dp
    }

    val cardVerticalPadding = when (deviceType) {
        DeviceType.Phone -> 10.dp
        DeviceType.Foldable -> 12.dp
        DeviceType.Tablet -> 16.dp
    }

    // تعیین ارتفاع کارت
    val cardHeight = when (deviceType) {
        DeviceType.Phone -> null  // ارتفاع خودکار
        DeviceType.Foldable -> 64.dp
        DeviceType.Tablet -> 72.dp
    }

    // تعیین سایه کارت
    val elevation = when (deviceType) {
        DeviceType.Phone -> 2.dp
        DeviceType.Foldable -> 3.dp
        DeviceType.Tablet -> 4.dp
    }

    Card(
        modifier = modifier
            .then(if (cardHeight != null) Modifier.height(cardHeight) else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = cardHorizontalPadding, vertical = cardVerticalPadding)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = word.english,
                style = MaterialTheme.typography.titleLarge,
                fontSize = englishFontSize,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = word.persian,
                style = MaterialTheme.typography.titleMedium,
                fontSize = persianFontSize,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// نگه داشتن کدهای قدیمی برای Preview
@Composable
private fun WordListBody(
    wordsList: List<Word>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (wordsList.isEmpty()) {
            Text(
                text = stringResource(R.string.empity_list_excel),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(contentPadding)
            )
        } else {
            ListWords(
                wordsList = wordsList,
                contentPadding = contentPadding,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ListWords(
    wordsList: List<Word>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(items = wordsList) { item ->
            DictionaryWord(
                word = item,
                modifier = Modifier
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun DictionaryWord(
    word: Word,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = word.english,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = word.persian,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun ExcelWordsScreenPreview() {
    MyDictionaryTheme {
        ExcelWordListScreen(
            excelWordsViewModel = viewModel(),
            navigateToExcel = {},
            navigateBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun EmptyListPreview() {
    MyDictionaryTheme {
        ExcelWordListScreen(
            navigateToExcel = {},
            navigateBack = {},
        )
    }
}