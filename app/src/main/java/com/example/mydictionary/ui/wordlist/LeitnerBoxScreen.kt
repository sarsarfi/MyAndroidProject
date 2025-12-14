package com.example.mydictionary.ui.wordlist

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.data.Word
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.ui.AppViewModelProvider


object LeitnerBoxScreenDestination : NavigationDestination{
    override val route = "LeitnerBoxScreen"

    override val titleRes: Int = R.string.leitner_box
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeitnerScreen(
    navigateBack: () -> Unit,
    leitnerBoxViewModel: LeitnerBoxViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val leitnerUiState by leitnerBoxViewModel.uiState.collectAsState()
    val currentWord = leitnerBoxViewModel.getNextWordToReview(leitnerUiState)
    val meaningWord by leitnerBoxViewModel.isMeaningVisible.collectAsState()
    LaunchedEffect (currentWord?.id){//وقتی id  کلمه تغییر کرد معنی ریست شود (isMeaning = false)
        leitnerBoxViewModel.resetMeaning()
    }

    MyDictionaryTheme {
        Scaffold(
            topBar = {
                DictionaryTopAppBar(
                    title = stringResource(LeitnerBoxScreenDestination.titleRes),
                    canNavigateBack = true,
                    navigateUp = navigateBack
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (currentWord != null) {
                    val isHighPriority = leitnerUiState.highPriorityWords.contains(currentWord)
                    if (isHighPriority) {
                        CartLayoutReview(
                            word = currentWord,
                            onKnow = { leitnerBoxViewModel.markWordAsLearned(it) },
                            onDontKnow = { leitnerBoxViewModel.markWordAsForgotten(it) },
                            onClickToShowMeaning = {leitnerBoxViewModel.onClickToShowMeaning()},
                            isMeaningVisible = meaningWord,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    } else {
                        CartLayout(
                            word = currentWord,
                            onKnow = { leitnerBoxViewModel.markWordAsLearned(it) },
                            onDontKnow = { leitnerBoxViewModel.markWordAsForgotten(it) },
                            onClickToShowMeaning = {leitnerBoxViewModel.onClickToShowMeaning()},
                            isMeaningVisible = meaningWord,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "!!!همه کلمات مرور شدند",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center ,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun CartLayout(
    word: Word,
    onKnow : (Word) -> Unit,
    onDontKnow : (Word) -> Unit,
    onClickToShowMeaning : () -> Unit,
    isMeaningVisible: Boolean,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth(0.9f)  // ✅ 90% عرض صفحه
            .heightIn(300.dp, 400.dp) // ✅ محدوده انعطاف‌پذیر
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier
                .fillMaxWidth() ,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(top = 16.dp, start = 16.dp)
                        .clickable {
                            val url = "https://www.google.com/search?tbm=isch&q=${word.english}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                )
            }
            Spacer(modifier = Modifier.height(74.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.alignByBaseline() // هم‌ترازی متنی
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = "Pronounce word",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { /* اجرای تابع پخش تلفظ */ }
                        .alignByBaseline()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (isMeaningVisible) {
                Text(
                    text = word.persian,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else{
                Text(
                    text = "نمایش معنی کلمه" ,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onClickToShowMeaning)
                )
            }
            Spacer(modifier = Modifier.height(85.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 34.dp, bottom = 21.dp)
            ) {
                OutlinedButton(
                    onClick = {onDontKnow(word)},
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "I don't know",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {onKnow(word)},
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = MaterialTheme.shapes.small,
                     contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
               ) {
                    Text(
                        text = "I know",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

        }
    }
}
@Composable
private fun CartLayoutReview(
    word: Word,
    onKnow: (Word) -> Unit,
    onDontKnow: (Word) -> Unit,
    onClickToShowMeaning: () -> Unit,
    isMeaningVisible: Boolean,
    modifier: Modifier = Modifier
){
    val  context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .heightIn(300.dp, 400.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier
                .fillMaxWidth() ,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(top = 16.dp, start = 16.dp)
                        .clickable{
                            val url = "https://www.google.com/search?tbm=isch&q=${word.english}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                )
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(top = 16.dp, end = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(74.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.alignByBaseline()
                )

                Spacer(modifier = Modifier.width(8.dp))

                // آیکون تلفظ (🔊)
                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = "Pronounce word",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { /* اجرای تابع پخش تلفظ */ }
                        .alignByBaseline()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (isMeaningVisible) {
                Text(
                    text = word.persian,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onClickToShowMeaning)
                )
            }else{
                Text(
                    text = "نمایش معنی کلمه" ,
                    style = MaterialTheme.typography.titleLarge ,
                    modifier = Modifier.clickable(onClick = onClickToShowMeaning) ,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(85.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 34.dp, bottom = 21.dp)
            ) {
                OutlinedButton(
                    onClick = {onDontKnow(word)},
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "I don't know",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Button(
                    onClick = {onKnow(word)},
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "I know",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

        }
    }
}
@Preview(showBackground = true)
@Composable
fun CartLayoutReviewPreview(){
    MyDictionaryTheme {
        CartLayoutReview(
            word = Word(1 , english = "Search" , persian = "جست و جو") ,
            onKnow = {},
            onDontKnow = {} ,
            onClickToShowMeaning = {} ,
            isMeaningVisible = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CartLayoutPreview() {
    MyDictionaryTheme {
        CartLayout(word = Word(id = 2, english = "Car", persian = "ماشین"),
            onKnow = {},
            onDontKnow = {} ,
            onClickToShowMeaning = {} ,
            isMeaningVisible = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LeitnerBoxPreview(){
    MyDictionaryTheme {
        LeitnerScreen(
            navigateBack = {},
            leitnerBoxViewModel = viewModel()
        )
    }
}

