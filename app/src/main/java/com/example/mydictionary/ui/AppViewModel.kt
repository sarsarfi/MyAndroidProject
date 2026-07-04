package com.example.mydictionary.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mydictionary.DictionaryApplication
import com.example.mydictionary.ui.addword.AddWordViewModel
import com.example.mydictionary.ui.categorywords.CategoryViewModel
import com.example.mydictionary.ui.categorywords.wordsincategory.WordListCategoryViewModel
import com.example.mydictionary.ui.editword.WordEditViewModel
import com.example.mydictionary.ui.excelwords.ExcelWordsViewModel
import com.example.mydictionary.ui.leitnerbox.LeitnerBoxViewModel
import com.example.mydictionary.ui.quiz.QuizViewModel
import com.example.mydictionary.ui.report.ReportViewModel
import com.example.mydictionary.ui.wordlist.WordListViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")

            val repository = application.container.wordsRepository
            val wordCategoryCrossRefRepository = application.container.wordCategoryCrossRefRepository
            AddWordViewModel(repository, wordCategoryCrossRefRepository, this.createSavedStateHandle())
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")

            val repository = application.container.wordsRepository
            val stateRepository = application.container.wordStatsRepository
            WordListViewModel(repository , stateRepository)
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordStatsRepository

            LeitnerBoxViewModel(repository)
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordsRepository
            val gameStateRepository = application.container.wordStatsRepository
            QuizViewModel(repository, gameStateRepository)
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordsRepository
            val wordCategoryCrossRefRepository = application.container.wordCategoryCrossRefRepository
            ExcelWordsViewModel(repository , wordCategoryCrossRefRepository, this.createSavedStateHandle())
        }
        initializer { val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordsRepository

            WordEditViewModel(this.createSavedStateHandle(), repository)
        }

        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordsRepository
            val gameStateRepository = application.container.wordStatsRepository
            ReportViewModel(repository, gameStateRepository)
        }

        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.categoryRepository
            CategoryViewModel(repository)
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordCategoryCrossRefRepository
            WordListCategoryViewModel(repository, this.createSavedStateHandle())
        }
    }
}