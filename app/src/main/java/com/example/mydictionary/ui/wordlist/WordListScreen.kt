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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.data.Word
import com.example.mydictionary.ui.AppViewModelProvider
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
                FloatingActionButton(
                    onClick = navigateToAddNewWord,
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_newword))
                }
            }
        ) { innerPadding ->
            WordListBody(
                wordsList = listUiState.wordsList,
                skippedWordsList = listUiState.skippedWords,
                onUpdateWord = { word ->
                    navigateToEditScreen(word.id)
                },
                contentPading = innerPadding,
                onDelete = { word -> wordListViewModel.deleteWord(word) },
                onSpeakWord = { word -> wordListViewModel.speakWord(word.english) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun WordListBody(
    wordsList: List<Word>,
    onUpdateWord: (Word) -> Unit,
    modifier: Modifier = Modifier,
    skippedWordsList: List<Word>,
    onDelete: (Word) -> Unit,
    onSpeakWord: (Word) -> Unit,
    contentPading: PaddingValues = PaddingValues(0.dp)
) {
    // show dialog and selected word when open options
    var showDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<Word?>(null) } // remembers the selected word

    // show dialog if selectedWord is not null
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

@Preview(showBackground = true)
@Composable
fun OneWordDictionaryPreview(){
    MyDictionaryTheme {
        DictionaryWord(Word(english = "Apple" , persian = "سیب") ,
            onSpeakWord = {} ,
            onOpenOptions = {} ,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WordListBodyPreview(){
    MyDictionaryTheme {
        WordListBody(wordsList =listOf(
            Word(english = "Apple" , persian = "سیب"),
            Word(english = "Cat" , persian = "گربه") ,
            Word(english = "Red" , persian = "قرمز")),
            onUpdateWord = {} ,
            onSpeakWord = {},
            skippedWordsList = listOf(
                Word(english = "ball" , persian = "توپ"),
                Word(english = "Cat" , persian = "گربه")
            ) ,
            onDelete = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun WordListPreview(){
    MyDictionaryTheme {
        WordListScreen(navigateToAddNewWord = {} ,
            navigateBack = {} ,
            wordListViewModel = viewModel() ,
            navigateToEditScreen = {}
        )

    }
}