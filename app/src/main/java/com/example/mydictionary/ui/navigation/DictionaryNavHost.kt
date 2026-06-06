package com.example.mydictionary.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.addword.AddWordDestination
import com.example.mydictionary.ui.addword.AddWordScreen
import com.example.mydictionary.ui.editword.WordEditDestination
import com.example.mydictionary.ui.editword.WordEditScreen
import com.example.mydictionary.ui.excelwords.ExcelWordListScreen
import com.example.mydictionary.ui.excelwords.ExcelWordsScreenDestination
import com.example.mydictionary.ui.quiz.GameScreen
import com.example.mydictionary.ui.quiz.QuizDestination
import com.example.mydictionary.ui.home.HomeDestination
import com.example.mydictionary.ui.home.HomeScreen
import com.example.mydictionary.ui.leitnerbox.LeitnerBoxScreenDestination
import com.example.mydictionary.ui.leitnerbox.LeitnerScreen
import com.example.mydictionary.ui.report.ReportScreen
import com.example.mydictionary.ui.report.ReportScreenDestination
import com.example.mydictionary.ui.wordlist.WordListDestination
import com.example.mydictionary.ui.wordlist.WordListScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            // مدیریت دکمه Back در صفحه اصلی
            var backPressedOnce by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            BackHandler(enabled = true) {
                if (backPressedOnce) {
                    // دفعه دوم: خارج شدن از برنامه
                    (context as? Activity)?.finish()
                } else {
                    // دفعه اول: پیام نشان بده
                    backPressedOnce = true
                    Toast.makeText(context, "برای خروج دوباره کلیک کنید", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        delay(2000)
                        backPressedOnce = false
                    }
                }
            }

            HomeScreen(
                onAllWord = { navController.navigate(WordListDestination.route) },
                onQuiz = { navController.navigate(QuizDestination.route) },
                onLeitnerBox = { navController.navigate(LeitnerBoxScreenDestination.route) },
                onExcelWord = { navController.navigate(ExcelWordsScreenDestination.route) },
                onAbout = { navController.navigate(ReportScreenDestination.route) },
                onAddWord = { navController.navigate(AddWordDestination.route) }
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
                type = NavType.IntType
            })
        ) {
            WordEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}