package com.example.mydictionary.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.ui.navigation.NavigationDestination
import com.example.mydictionary.ui.theme.MyDictionaryTheme

object HomeDestination : NavigationDestination{

    override val route = "home"

    override val titleRes = R.string.app_name

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddWordClicked: () -> Unit,
    onWordListClicked: () -> Unit,
    onQuizClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    MyDictionaryTheme {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
                DictionaryTopAppBar(
                    title = stringResource(HomeDestination.titleRes),
                    canNavigateBack = false,
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPading ->
            val cardData = listOf(
                Triple(
                    R.string.add_newword,
                    R.string.description_statrt_screen,
                    R.drawable.add_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                ),
                Triple(
                    R.string.list_of_words,
                    R.string.description_statrt_screen,
                    R.drawable.list_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                ),
                Triple(
                    R.string.quize,
                    R.string.description_statrt_screen,
                    R.drawable.quiz_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                )
            )

            // تعریف تصاویر خام به صورت جداگانه (چون Triple فقط از 3 آیتم پشتیبانی می‌کند)
            val imageResources = listOf(
                R.drawable._850917,
                R.drawable._081047,
                R.drawable._3128_ojm4kv_79
            )

            // تعریف توابع کلیک به ترتیب
            val clickActions = listOf(
                onAddWordClicked,
                onWordListClicked,
                onQuizClicked
            )

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPading),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // استفاده از index برای دسترسی به لیست‌های مختلف
                    items(cardData.size) { index ->
                        MenuCard(
                            title = cardData[index].first,       // title
                            description = cardData[index].second,    // description
                            iconImage = cardData[index].third,       // iconImage
                            image = imageResources[index],           // image
                            onClick = clickActions[index],           // onClick
                            modifier = Modifier.fillMaxWidth()
                                .padding(innerPading)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MenuCard(
    @StringRes title: Int,
    @StringRes description: Int,
    @DrawableRes iconImage : Int,
    onClick:() -> Unit,
    @DrawableRes image : Int ,
    modifier: Modifier = Modifier
) {
    val cardHeight = 300.dp

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(description),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        painter = painterResource(iconImage) ,
                        contentDescription = stringResource(title),
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onAddWordClicked = {},
            onWordListClicked = {},
            onQuizClicked = {}
        )
    }
}