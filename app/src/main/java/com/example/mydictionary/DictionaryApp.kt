package com.example.mydictionary


import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mydictionary.ui.navigation.DictionaryNavHostApp

@Composable
fun DictionaryApp(navController: NavController = rememberNavController()){
    DictionaryNavHostApp(
        navController = navController as NavHostController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {}
) {
        CenterAlignedTopAppBar(
            title =
                { Text(title,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.displayLarge
                ) }
            ,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                if (canNavigateBack) {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }
        )
}