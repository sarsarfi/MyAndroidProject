package com.example.mydictionary.ui.wordlist



import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.mydictionary.ui.wordlist.WordListBody

object WordListDestination : NavigationDestination{
    override val route = "wordlist"
    override val titleRes = R.string.list_of_words
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    navigateToAddNewWord : () -> Unit,
    wordUpdate : (Int) -> Unit,
    navigateBack : () -> Unit,
    modifier : Modifier = Modifier,
    wordListViewModel: WordListViewModel = viewModel(factory = AppViewModelProvider.Factory)
    ) {
    val listUiState by wordListViewModel.uiState.collectAsState()


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val context = LocalContext.current

    // ✅ 2. فراخوانی initializeTts فقط یک بار هنگام ورود به صفحه
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
                    scrollBehavior = scrollBehavior ,
                    navigateUp = navigateBack
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = navigateToAddNewWord,
                    shape = MaterialTheme.shapes.small,
                    modifier = modifier
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_newword)
                    )
                }
            }
        ) { innerPadding ->
            WordListBody(
                wordsList = listUiState.wordsList,
                skippedWordsList = listUiState.skippedWords ,
                /*onWordClick = wordUpdate,*/
                contentPading = innerPadding,
                onDelete = {currentWord : Word -> wordListViewModel.deleteWord(currentWord)},
                onSpeakWord = { currentWord: Word -> wordListViewModel.speakWord(currentWord.english) },                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun WordListBody(
    wordsList: List<Word>,
    //onWordClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    skippedWordsList : List<Word>,
    onDelete : (Word) -> Unit,
    onSpeakWord: (Word) -> Unit,
    contentPading: PaddingValues = PaddingValues(0.dp)
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (wordsList.isEmpty()) {
            Text(
                text = stringResource(R.string.empity_list),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(contentPading)
            )
        } else
            ListWords(
                wordsList = wordsList,
                //onWordClick = onWordClick,
                contentPadding = contentPading,
                skippedWords = skippedWordsList ,
                onDelete = onDelete,
                onSpeakWord = onSpeakWord,
                modifier = modifier
            )
    }
}
@Composable
private fun ListWords(
    wordsList: List<Word>,
    //onWordClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    skippedWords: List<Word>,
    onDelete : (Word) -> Unit,
    onSpeakWord: (Word) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        if (skippedWords.isNotEmpty()) {
            item {
                Text(
                    text = "Skipped Words",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            items(items = skippedWords) { item ->
                SkippedWordItem(
                    word = item,
                    onSpeakWord = {onSpeakWord(item)},
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { /*onWordClick*/ }
                )
            }
        }
        item {
            Divider(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        item {
            Text(
                text = "The rest of the words in the list",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
        // اول نمایش کلمات معمولی
        items(items = wordsList) { item ->
            DictionaryWord(
                word = item,
                onDelete = {onDelete(item)},
                onSpeakWord = {onSpeakWord(item)},
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { /*onWordClick*/ } // ✅ اینجا باید تابع فراخوانی شود!
            )
        }
    }
}


// یک کامپوزبل جدید در انتهای فایل WordListScreen.kt اضافه کنید
@Composable
private fun SkippedWordItem(
    word: Word,
    onSpeakWord: (Word) -> Unit,
    modifier: Modifier = Modifier
){
    Card(
        modifier = modifier.fillMaxWidth() ,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) ,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = "Pronounce word",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {onSpeakWord(word)}
                        .alignByBaseline()
                )
            }
            Text(
                text = word.persian ,
                style = MaterialTheme.typography.titleMedium ,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Row() {
//                IconButton(onClick = {onDelete(word)}) {
//                    Icon(
//                        imageVector = Icons.Filled.Delete,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onErrorContainer
//                    )
//                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Warning, // استفاده از آیکون پیش‌فرض Warning
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DictionaryWord(
    word: Word,
    onDelete : (Word) -> Unit,
    onSpeakWord : (Word) -> Unit,
    modifier: Modifier = Modifier
){
    Card(
        modifier = modifier ,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) ,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = "Pronounce word",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {onSpeakWord(word)}
                        .alignByBaseline()
                )
            }
                Text(
                    text = word.persian,
                    style = MaterialTheme.typography.titleSmall ,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {onDelete(word)}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete ,
                        contentDescription = null
                    )
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OneWordDictionaryPreview(){
    MyDictionaryTheme {
        DictionaryWord(Word(english = "Apple" , persian = "سیب") ,
            onDelete = {} ,
            onSpeakWord = {}
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
            /*onWordClick = {}*/
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
            wordUpdate = {} ,
            navigateBack = {} ,
            wordListViewModel = viewModel()
            )

    }
}