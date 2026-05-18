package com.example.mydictionary.ui.wordlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.example.mydictionary.ui.wordlist.DictionaryWord

object WordListDestination : NavigationDestination {
    override val route = "wordlist"
    override val titleRes = R.string.list_of_words
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    navigateToAddNewWord: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigateToEditScreen: (Int) -> Unit,
    wordListViewModel: WordListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val listUiState by wordListViewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val deviceType = rememberDeviceType()

    LaunchedEffect(Unit) {
        wordListViewModel.initializeTts(context)
    }

    MyDictionaryTheme {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(WordListDestination.titleRes),
                    canNavigateBack = true,
                    scrollBehavior = scrollBehavior,
                    navigateUp = navigateBack
                )
            },
            floatingActionButton = {
                // در تبلت FAB را کمی بزرگتر کن
                FloatingActionButton(
                    onClick = navigateToAddNewWord,
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
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_newword),
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
        ) { innerPadding ->
            AdaptiveWordListBody(
                wordsList = listUiState.wordsList,
                skippedWordsList = listUiState.skippedWords,
                onUpdateWord = { word ->
                    navigateToEditScreen(word.id)
                },
                contentPadding = innerPadding,
                onDelete = { word -> wordListViewModel.deleteWord(word) },
                onSpeakWord = { word -> wordListViewModel.speakWord(word.english) },
                deviceType = deviceType,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AdaptiveWordListBody(
    wordsList: List<Word>,
    onUpdateWord: (Word) -> Unit,
    modifier: Modifier = Modifier,
    skippedWordsList: List<Word>,
    onDelete: (Word) -> Unit,
    onSpeakWord: (Word) -> Unit,
    deviceType: DeviceType,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    // show dialog and selected word when open options
    var showDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<Word?>(null) }

    // تعیین پدینگ داخلی لیست بر اساس نوع دستگاه
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

    // تعیین سایز فونت برای عنوان‌های بخش
    val sectionTitleFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    if (showDialog && selectedWord != null) {
        AdaptiveRepairAlterDialog(
            onDismiss = { showDialog = false },
            onDelete = {
                onDelete(selectedWord!!)
                showDialog = false
            },
            onRepair = {
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
        if (wordsList.isEmpty() && skippedWordsList.isEmpty()) {
            Text(
                text = stringResource(R.string.empity_list),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            AdaptiveListWords(
                wordsList = wordsList,
                contentPadding = PaddingValues(
                    horizontal = listHorizontalPadding,
                    vertical = 8.dp
                ),
                skippedWords = skippedWordsList,
                onSpeakWord = onSpeakWord,
                onOpenOptions = { word ->
                    selectedWord = word
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
private fun AdaptiveListWords(
    wordsList: List<Word>,
    contentPadding: PaddingValues,
    skippedWords: List<Word>,
    onSpeakWord: (Word) -> Unit,
    onOpenOptions: (Word) -> Unit,
    deviceType: DeviceType,
    itemSpacing: androidx.compose.ui.unit.Dp,
    sectionTitleFontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        if (skippedWords.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.skipped_words),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = sectionTitleFontSize,
                    modifier = Modifier.padding(
                        start = when (deviceType) {
                            DeviceType.Phone -> 8.dp
                            else -> 0.dp
                        },
                        bottom = 4.dp,
                        top = 8.dp
                    )
                )
            }
            items(skippedWords) { item ->
                AdaptiveSkippedWordItem(
                    word = item,
                    onSpeakWord = { onSpeakWord(item) },
                    onOpenOptions = { onOpenOptions(item) },
                    deviceType = deviceType,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.the_rest_of_words),
                style = MaterialTheme.typography.titleMedium,
                fontSize = sectionTitleFontSize,
                modifier = Modifier.padding(
                    start = when (deviceType) {
                        DeviceType.Phone -> 8.dp
                        else -> 0.dp
                    },
                    bottom = 4.dp,
                    top = if (skippedWords.isNotEmpty()) 16.dp else 8.dp
                )
            )
        }
        items(wordsList) { item ->
            AdaptiveDictionaryWord(
                word = item,
                onSpeakWord = { onSpeakWord(item) },
                onOpenOptions = { onOpenOptions(item) },
                deviceType = deviceType,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun AdaptiveSkippedWordItem(
    word: Word,
    onSpeakWord: (Word) -> Unit,
    onOpenOptions: () -> Unit,
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

    val iconSize = when (deviceType) {
        DeviceType.Phone -> 20.dp
        DeviceType.Foldable -> 24.dp
        DeviceType.Tablet -> 28.dp
    }

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

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = when (deviceType) {
            DeviceType.Phone -> 0.dp
            else -> 2.dp
        })
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = cardHorizontalPadding, vertical = cardVerticalPadding)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    word.english,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = englishFontSize
                )
                Icon(
                    Icons.Outlined.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onSpeakWord(word) }
                )
            }
            Text(
                word.persian,
                style = MaterialTheme.typography.titleMedium,
                fontSize = persianFontSize,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onOpenOptions,
                modifier = Modifier.size(iconSize + 8.dp)
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
private fun AdaptiveDictionaryWord(
    word: Word,
    onSpeakWord: (Word) -> Unit,
    onOpenOptions: () -> Unit,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    // تعیین سایز فونت‌ها بر اساس نوع دستگاه
    val englishFontSize = when (deviceType) {
        DeviceType.Phone -> 14.sp
        DeviceType.Foldable -> 16.sp
        DeviceType.Tablet -> 18.sp
    }

    val persianFontSize = when (deviceType) {
        DeviceType.Phone -> 12.sp
        DeviceType.Foldable -> 14.sp
        DeviceType.Tablet -> 16.sp
    }

    val iconSize = when (deviceType) {
        DeviceType.Phone -> 18.dp
        DeviceType.Foldable -> 20.dp
        DeviceType.Tablet -> 24.dp
    }

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
                    word.english,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = englishFontSize
                )
                Icon(
                    Icons.Outlined.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onSpeakWord(word) }
                )
            }
            Text(
                word.persian,
                style = MaterialTheme.typography.titleSmall,
                fontSize = persianFontSize,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onOpenOptions,
                modifier = Modifier.size(iconSize + 8.dp)
            ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
fun AdaptiveRepairAlterDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRepair: () -> Unit,
    deviceType: DeviceType
) {
    // در تبلت دیالوگ را بزرگتر نشان بده
    AlertDialog(
        onDismissRequest = onDismiss,
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
                        when (deviceType) {
                            DeviceType.Phone -> 36.dp
                            else -> 48.dp
                        }
                    )
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = when (deviceType) {
                            DeviceType.Phone -> 14.sp
                            else -> 16.sp
                        }
                    )
                }
                TextButton(
                    onClick = onRepair,
                    modifier = Modifier.height(
                        when (deviceType) {
                            DeviceType.Phone -> 36.dp
                            else -> 48.dp
                        }
                    )
                ) {
                    Text(
                        "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = when (deviceType) {
                            DeviceType.Phone -> 14.sp
                            else -> 16.sp
                        }
                    )
                }
            }
        }
    )
}

// نگه داشتن کدهای قدیمی برای Preview
@Composable
private fun WordListBody(
    wordsList: List<Word>,
    onUpdateWord: (Word) -> Unit,
    modifier: Modifier = Modifier,
    skippedWordsList: List<Word>,
    onDelete: (Word) -> Unit,
    onSpeakWord: (Word) -> Unit,
    contentPading: PaddingValues = PaddingValues(0.dp)
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<Word?>(null) }

    if (showDialog && selectedWord != null) {
        RepairAlterDialog(
            onDismiss = { showDialog = false },
            onDelete = {
                onDelete(selectedWord!!)
                showDialog = false
            },
            onRepair = {
                onUpdateWord(selectedWord!!)
                showDialog = false
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(contentPading),
    ) {
        if (wordsList.isEmpty() && skippedWordsList.isEmpty()) {
            Text(
                text = stringResource(R.string.empity_list),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            ListWords(
                wordsList = wordsList,
                contentPadding = PaddingValues(0.dp),
                skippedWords = skippedWordsList,
                onSpeakWord = onSpeakWord,
                onOpenOptions = { word ->
                    selectedWord = word
                    showDialog = true
                }
            )
        }
    }
}

@Composable
private fun ListWords(
    wordsList: List<Word>,
    contentPadding: PaddingValues,
    skippedWords: List<Word>,
    onSpeakWord: (Word) -> Unit,
    onOpenOptions: (Word) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        if (skippedWords.isNotEmpty()) {
            item {
                Text("Skipped Words", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
            }
            items(skippedWords) { item ->
                SkippedWordItem(
                    word = item,
                    onSpeakWord = { onSpeakWord(item) },
                    onOpenOptions = { onOpenOptions(item) },
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        item {
            Text("The rest of the words", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
        }
        items(wordsList) { item ->
            DictionaryWord(
                word = item,
                onSpeakWord = { onSpeakWord(item) },
                onOpenOptions = { onOpenOptions(item) },
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
private fun SkippedWordItem(
    word: Word,
    onSpeakWord: (Word) -> Unit,
    onOpenOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(word.english, style = MaterialTheme.typography.titleLarge)
                Icon(
                    Icons.Outlined.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).clickable { onSpeakWord(word) }
                )
            }
            Text(word.persian, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            IconButton(onClick = onOpenOptions) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DictionaryWord(
    word: Word,
    onSpeakWord: (Word) -> Unit,
    onOpenOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(word.english, style = MaterialTheme.typography.titleMedium)
                Icon(
                    Icons.Outlined.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).clickable { onSpeakWord(word) }
                )
            }
            Text(word.persian, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            IconButton(onClick = onOpenOptions) {
                Icon(Icons.Outlined.MoreVert, contentDescription = null)
            }
        }
    }
}

@Composable
fun RepairAlterDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRepair: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.repair)) },
        text = { Text(text = stringResource(R.string.repair_description)) },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onRepair) {
                    Text("Edit" , color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun OneWordDictionaryPreview(){
    MyDictionaryTheme {
        AdaptiveDictionaryWord(
            Word(english = "Apple" , persian = "سیب"),
            onSpeakWord = {},
            onOpenOptions = {},
            deviceType = DeviceType.Phone,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Preview(showBackground = true, widthDp = 840, heightDp = 1000)
@Composable
fun WordListBodyPreview(){
    MyDictionaryTheme {
        AdaptiveWordListBody(
            wordsList = listOf(
                Word(english = "Apple" , persian = "سیب"),
                Word(english = "Cat" , persian = "گربه") ,
                Word(english = "Red" , persian = "قرمز")
            ),
            onUpdateWord = {},
            onSpeakWord = {},
            skippedWordsList = listOf(
                Word(english = "ball" , persian = "توپ"),
                Word(english = "Cat" , persian = "گربه")
            ),
            onDelete = {},
            deviceType = DeviceType.Phone,
            contentPadding = PaddingValues(0.dp)
        )
    }
}