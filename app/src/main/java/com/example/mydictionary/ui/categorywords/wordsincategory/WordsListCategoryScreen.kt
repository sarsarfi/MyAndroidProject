package com.example.mydictionary.ui.categorywords.wordsincategory

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordCategoryCrossRef
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.adaptive.DeviceType
import com.example.mydictionary.ui.adaptive.rememberDeviceType
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme

object WordsListCategoryScreenDestination : NavigationDestination {
    override val route: String = "words_list_category_screen"
    override val titleRes = R.string.list_of_words

    const val categoryIdArg = "categoryId"
    val routeWithArgs = "$route/{$categoryIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordsListCategoryScreen(
    navigateToAddNewWord: (Int) -> Unit,
    onNavigateUp: () -> Unit,
    navigateToEditScreen: (Int) -> Unit,
    navigateToExcel : (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WordListCategoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val deviceType = rememberDeviceType()

    LaunchedEffect(Unit) {
        viewModel.initializeTts(context)
    }

    MyDictionaryTheme {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(WordsListCategoryScreenDestination.titleRes),
                    canNavigateBack = true,
                    navigateUp = onNavigateUp,
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                var isMenuExpanded by remember { mutableStateOf(false) }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isMenuExpanded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = stringResource(R.string.import_from_excel),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            FloatingActionButton(
                                onClick = {
                                    isMenuExpanded = false
                                    navigateToExcel(viewModel.categoryId)
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.size(
                                    when (deviceType) {
                                        DeviceType.Phone -> 44.dp
                                        DeviceType.Foldable -> 52.dp
                                        DeviceType.Tablet -> 60.dp
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ImportExport,
                                    contentDescription = "Import Excel",
                                    modifier = Modifier.size(
                                        when (deviceType) {
                                            DeviceType.Phone -> 20.dp
                                            DeviceType.Foldable -> 24.dp
                                            DeviceType.Tablet -> 28.dp
                                        }
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = stringResource(R.string.add_manual_vocabulary),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            FloatingActionButton(
                                onClick = {
                                    isMenuExpanded = false
                                    navigateToAddNewWord(viewModel.categoryId)
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.size(
                                    when (deviceType) {
                                        DeviceType.Phone -> 44.dp
                                        DeviceType.Foldable -> 52.dp
                                        DeviceType.Tablet -> 60.dp
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_newword),
                                    modifier = Modifier.size(
                                        when (deviceType) {
                                            DeviceType.Phone -> 20.dp
                                            DeviceType.Foldable -> 24.dp
                                            DeviceType.Tablet -> 28.dp
                                        }
                                    )
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { isMenuExpanded = !isMenuExpanded },
                        containerColor = if (isMenuExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.size(
                            when (deviceType) {
                                DeviceType.Phone -> 56.dp
                                DeviceType.Foldable -> 64.dp
                                DeviceType.Tablet -> 72.dp
                            }
                        )
                    ) {
                        Icon(
                            imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Toggle Menu",
                            modifier = Modifier.size(
                                when (deviceType) {
                                    DeviceType.Phone -> 24.dp
                                    DeviceType.Foldable -> 28.dp
                                    DeviceType.Tablet -> 32.dp
                                }
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            AdaptiveWordListCategoryBody(
                categoryId = viewModel.categoryId,
                wordsList = uiState.wordsListCategory,
                totalWords = uiState.totalWords,
                onUpdateWord = { crossRef -> navigateToEditScreen(crossRef.wordId) }, // 🎯 اصلاح به این شکل
                onSpeakWord = { word -> viewModel.speakWord(word.english) },
                deviceType = deviceType,
                contentPadding = innerPadding,
                onDelete = { crossRef -> viewModel.deleteWord(crossRef) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AdaptiveWordListCategoryBody(
    categoryId: Int,
    totalWords: Int,
    wordsList: List<Word>,
    onUpdateWord: (WordCategoryCrossRef) -> Unit,
    onSpeakWord : (Word) -> Unit,
    onDelete : (WordCategoryCrossRef) -> Unit,
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<WordCategoryCrossRef?>(null) }

    val listHorizontalPadding = when (deviceType) {
        DeviceType.Phone -> 0.dp
        DeviceType.Foldable -> 16.dp
        DeviceType.Tablet -> 32.dp
    }

    val itemSpacing = when (deviceType) {
        DeviceType.Phone -> 4.dp
        DeviceType.Foldable -> 8.dp
        DeviceType.Tablet -> 12.dp
    }

    val sectionTitleFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    if (showDialog && selectedWord != null) {
        AdaptiveCategoryWordRepairDialog(
            onDismiss = {
                showDialog = false // زدن روی فضای بیرون یا دکمه‌های غیر از دیلیت فقط دیالوگ را می‌بندد
            },
            onDelete = {
                onDelete(selectedWord!!) // دیت دیتابیس فقط اینجا اجرا می‌شود
                showDialog = false
            },
            onEdit = {
                onUpdateWord(selectedWord!!)
                showDialog = false
            },
            deviceType = deviceType
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(contentPadding),
    ) {
        if (wordsList.isEmpty()) {
            Text(
                text = stringResource(R.string.empity_list),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            WordsListCategory(
                categoryId = categoryId,
                wordsList = wordsList,
                totalWords = totalWords,
                contentPadding = PaddingValues(
                    horizontal = listHorizontalPadding,
                    vertical = 8.dp
                ),
                onSpeakWord = onSpeakWord,
                onOpenOptions = { crossRef ->
                    selectedWord = crossRef
                    showDialog = true
                },
                deviceType = deviceType,
                itemSpacing = itemSpacing,
                sectionTitleFontSize = sectionTitleFontSize,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun WordsListCategory(
    categoryId: Int,
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    wordsList: List<Word>,
    totalWords: Int,
    onSpeakWord: (Word) -> Unit,
    onOpenOptions: (WordCategoryCrossRef) -> Unit,
    itemSpacing: Dp,
    sectionTitleFontSize: TextUnit,
) {
    val dictEnglishFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    val dictPersianFontSize = when (deviceType) {
        DeviceType.Phone -> 12.sp
        DeviceType.Foldable -> 14.sp
        DeviceType.Tablet -> 16.sp
    }

    val iconSize = when (deviceType) {
        DeviceType.Phone -> 18.dp
        DeviceType.Foldable -> 22.dp
        DeviceType.Tablet -> 26.dp
    }

    val isPhone = deviceType == DeviceType.Phone

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        item(key = "header_rest_of_words") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isPhone) 12.dp else 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.rest_word_category),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = sectionTitleFontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "($totalWords)",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = sectionTitleFontSize,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(
            items = wordsList,
            key = { "category_word_${it.id}" }
        ) { item ->
            AdaptiveWordsCardLayoutCategory(
                categoryId = categoryId,
                word = item,
                englishFontSize = dictEnglishFontSize,
                persianFontSize = dictPersianFontSize,
                iconSize = iconSize,
                onSpeakWord = onSpeakWord,
                onOpenOptions = onOpenOptions,
                deviceType = deviceType,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun AdaptiveWordsCardLayoutCategory(
    categoryId: Int,
    deviceType: DeviceType,
    modifier: Modifier = Modifier,
    word: Word,
    englishFontSize: TextUnit,
    persianFontSize: TextUnit,
    iconSize: Dp,
    onSpeakWord: (Word) -> Unit,
    onOpenOptions: (WordCategoryCrossRef) -> Unit
) {
    val cardHorizontalPadding = when (deviceType) {
        DeviceType.Phone -> 16.dp
        DeviceType.Foldable -> 20.dp
        DeviceType.Tablet -> 24.dp
    }

    val cardVerticalPadding = when (deviceType) {
        DeviceType.Phone -> 8.dp
        DeviceType.Foldable -> 10.dp
        DeviceType.Tablet -> 12.dp
    }

    val elevation = when (deviceType) {
        DeviceType.Phone -> 2.dp
        DeviceType.Foldable -> 3.dp
        DeviceType.Tablet -> 4.dp
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = cardHorizontalPadding, vertical = cardVerticalPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = englishFontSize
                )
                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onSpeakWord(word) }
                )
            }
            Text(
                text = word.persian,
                style = MaterialTheme.typography.titleSmall,
                fontSize = persianFontSize,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { onOpenOptions(WordCategoryCrossRef(word.id , categoryId )) },
                modifier = Modifier.size(iconSize + 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
fun AdaptiveCategoryWordRepairDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    deviceType: DeviceType
) {
    AlertDialog(
        onDismissRequest = onDismiss, // کلیک روی فضای بیرون صرفا متد dismiss را صدا می‌زند که دیالوگ بسته شود
        title = {
            Text(
                text = stringResource(R.string.repair),
                fontSize = when (deviceType) {
                    DeviceType.Phone -> 16.sp
                    DeviceType.Foldable -> 18.sp
                    DeviceType.Tablet -> 20.sp
                }
            )
        },
        text = {
            Text(
                text = stringResource(R.string.repair_description),
                fontSize = when (deviceType) {
                    DeviceType.Phone -> 14.sp
                    DeviceType.Foldable -> 15.sp
                    DeviceType.Tablet -> 16.sp
                }
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.height(
                        if (deviceType == DeviceType.Phone) 36.dp else 48.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = if (deviceType == DeviceType.Phone) 14.sp else 16.sp
                    )
                }
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.height(
                        if (deviceType == DeviceType.Phone) 36.dp else 48.dp
                    )
                ) {
                    Text(
                        text = "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = if (deviceType == DeviceType.Phone) 14.sp else 16.sp
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CategoryWordListBodyPreview() {
    MyDictionaryTheme {
        AdaptiveWordListCategoryBody(
            categoryId = 1,
            wordsList = listOf(
                Word(id = 1, english = "Apple", persian = "سیب"),
                Word(id = 2, english = "Cat", persian = "گربه"),
                Word(id = 3, english = "Red", persian = "قرمز")
            ),
            totalWords = 3,
            onUpdateWord = {},
            deviceType = DeviceType.Phone,
            contentPadding = PaddingValues(0.dp) ,
            onSpeakWord = {} ,
            onDelete = {}
        )
    }
}