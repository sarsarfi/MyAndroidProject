package com.example.mydictionary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.addword.AddWordDestination
import com.example.mydictionary.ui.addword.AddWordScreen
import com.example.mydictionary.ui.quiz.GameScreen
import com.example.mydictionary.ui.quiz.QuizDestination
import com.example.mydictionary.ui.wordlist.ExcelWordListScreen
import com.example.mydictionary.ui.wordlist.ExcelWordsScreenDestination
import com.example.mydictionary.ui.home.HomeDestination
import com.example.mydictionary.ui.home.HomeScreen
import com.example.mydictionary.ui.report.ReportScreen
import com.example.mydictionary.ui.report.ReportScreenDestination
import com.example.mydictionary.ui.wordlist.LeitnerBoxScreenDestination
import com.example.mydictionary.ui.wordlist.LeitnerScreen
import com.example.mydictionary.ui.wordlist.WordEditDestination
import com.example.mydictionary.ui.wordlist.WordEditScreen
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
                onAllWord = { navController.navigate(WordListDestination.route) },
                onQuiz = { navController.navigate(QuizDestination.route) },
                onLeitnerBox = { navController.navigate(LeitnerBoxScreenDestination.route) } ,
                onExcelWord = {navController.navigate(ExcelWordsScreenDestination.route)} ,
                onAbout = {navController.navigate(ReportScreenDestination.route)},
                onAddWord = {navController.navigate(AddWordDestination.route)}
            )
        }

        composable(AddWordDestination.route) {
            AddWordScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(LeitnerBoxScreenDestination.route) {
            LeitnerScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                leitnerBoxViewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }

        composable(WordListDestination.route) {
            WordListScreen(
                navigateToAddNewWord = { navController.navigate(AddWordDestination.route) },
                navigateBack = {
                    navController.popBackStack()
                },
                wordListViewModel = viewModel(factory = AppViewModelProvider.Factory),
                navigateToEditScreen = { wordId ->
                    navController.navigate("${WordEditDestination.route}/$wordId") }
            )
        }

        composable(ExcelWordsScreenDestination.route) {
            ExcelWordListScreen(
                excelWordsViewModel = viewModel(factory = AppViewModelProvider.Factory),
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToExcel = {}
            )
        }

        composable(QuizDestination.route) {
            GameScreen(
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(route = ReportScreenDestination.route) {
            ReportScreen(
                navigateBack = { navController.popBackStack() } ,
                viewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }
        composable(
            route = WordEditDestination.routeWithArgs,
            arguments = listOf(navArgument(WordEditDestination.wordIdArg) {
                type = NavType.IntType // حتما این را وارد کن تا برنامه بفهمد ورودی عدد است
            })
        ) {
            WordEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
                // viewModel به صورت خودکار خودش ساخته می‌شود، نیازی به نوشتن دستی نیست
            )
        }
    }
}