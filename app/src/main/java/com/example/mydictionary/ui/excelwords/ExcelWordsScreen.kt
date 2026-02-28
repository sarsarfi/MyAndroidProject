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
import androidx.compose.foundation.layout.padding
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
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme

object ExcelWordsScreenDestination : NavigationDestination{

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

    val context = LocalContext.current // ✅ دسترسی به Context برای ViewModel

    // ۱. تعریف لانچر برای انتخاب فایل
    val excelPickerLauncher = rememberLauncherForActivityResult(
        // قرارداد: درخواست یک سند (فایل)
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                // 1. دریافت پرچم‌های دسترسی از Intent
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION

                // 2. درخواست مجوز دسترسی پایدار
                context.contentResolver.takePersistableUriPermission(it, flag)

                // 3. فراخوانی تابع ViewModel
                excelWordsViewModel.readExcelFile(context, it)
                // ****** 👆 تا اینجا 👆 ******
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
                    // ✅ فراخوانی لانچر هنگام کلیک
                    onClick = {
                        // درخواست فایل‌های اکسل XLSX
                        excelPickerLauncher.launch(arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel" // برای XLS قدیمی‌تر (اختیاری)
                        ))
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Import",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold)
                }
            }
        ) { innerPadding ->
            WordListBody(
                wordsList = ExcelUiState.words,
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun WordListBody(
    wordsList: List<Word>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
)
{
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
    wordsList : List<Word> ,
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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
@Composable
fun EmptyListPreview() {
    MyDictionaryTheme {
        ExcelWordListScreen(
            navigateToExcel = {},
            navigateBack = {} ,
        )
    }
}