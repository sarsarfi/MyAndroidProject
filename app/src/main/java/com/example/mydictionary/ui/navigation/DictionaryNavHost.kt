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
import com.example.mydictionary.ui.wordlist.ExcelWordListScreen
import com.example.mydictionary.ui.wordlist.ExcelWordsScreenDestination
import com.example.mydictionary.ui.wordlist.LeitnerBoxScreenDestination
import com.example.mydictionary.ui.wordlist.LeitnerScreen
import com.example.mydictionary.ui.wordlist.WordCategoryListScreen
import com.example.mydictionary.ui.wordlist.WordListCategoryScreenDestinatin
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
                onWordListClicked = { navController.navigate(WordListCategoryScreenDestinatin.route) },
                onQuizClicked = { navController.navigate(QuizDestination.route) }
            )
        }

        composable(AddWordDestination.route) {
            AddWordScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(WordListCategoryScreenDestinatin.route) {
            WordCategoryListScreen(
                onLightnerBox = {navController.navigate(LeitnerBoxScreenDestination.route)},
                onAllWord = {navController.navigate(WordListDestination.route)} ,
                onExcelWord = {navController.navigate(ExcelWordsScreenDestination.route)} ,
                onNavigateBack = {navController.popBackStack()}
            )
        }
        composable(LeitnerBoxScreenDestination.route){
            LeitnerScreen(navigateBack = {navController.popBackStack(
                WordListCategoryScreenDestinatin.route , inclusive = false)},
                leitnerBoxViewModel = viewModel(factory = AppViewModelProvider.Factory))
        }
        composable(WordListDestination.route){
            WordListScreen(
                navigateToAddNewWord = {navController.navigate(AddWordDestination.route)},
                navigateBack = {navController.popBackStack(
                    WordListCategoryScreenDestinatin.route , inclusive = false)} ,
                wordListViewModel = viewModel(factory = AppViewModelProvider.Factory) ,
                wordUpdate = {}
            )
        }
        composable(ExcelWordsScreenDestination.route){
            ExcelWordListScreen(
                wordsList = listOf(),
                navigateBack = {navController.popBackStack(
                    WordListCategoryScreenDestinatin.route , inclusive = false)} ,
                navigateToExcel = {}
            )
        }

        composable(QuizDestination.route) {
            GameScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}