package com.example.mydictionary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.addword.AddWordDestination
import com.example.mydictionary.ui.addword.AddWordScreen
import com.example.mydictionary.ui.home.HomeDestination
import com.example.mydictionary.ui.home.HomeScreen
import com.example.mydictionary.ui.quiz.GameScreen
import com.example.mydictionary.ui.quiz.QuizDestination
import com.example.mydictionary.ui.wordlist.WordListDestination
import com.example.mydictionary.ui.wordlist.WordListScreen

@Composable
fun DictionaryNavHostApp(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(HomeDestination.route) {
            HomeScreen(
                onAddWordClicked = { navController.navigate(AddWordDestination.route) },
                onWordListClicked = { navController.navigate(WordListDestination.route) },
                onQuizClicked = { navController.navigate(QuizDestination.route) }
            )
        }

        composable(AddWordDestination.route) {
            AddWordScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(WordListDestination.route) {
            WordListScreen(
                navigateToAddNewWord = { navController.navigate(AddWordDestination.route) },
                wordUpdate = {} ,
                navigateBack = {navController.popBackStack()} ,
                viewModelList = viewModel(factory = AppViewModelProvider.Factory)

            )
        }

        composable(QuizDestination.route) {
            GameScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}