package com.example.mydictionary.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mydictionary.DictionaryApplication
import com.example.mydictionary.ui.addword.AddWordViewModel
import com.example.mydictionary.ui.quiz.QuizViewModel
import com.example.mydictionary.ui.wordlist.ExcelWordsViewModel
import com.example.mydictionary.ui.wordlist.LeitnerBoxViewModel
import com.example.mydictionary.ui.wordlist.WordListViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")

            val repository = application.container.wordsRepository
            AddWordViewModel(repository)
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")

            val repository = application.container.wordsRepository
            WordListViewModel(repository)
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordsRepository

            LeitnerBoxViewModel(repository)
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordsRepository
            QuizViewModel(repository)
        }
        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? DictionaryApplication
                ?: throw IllegalStateException("DictionaryApplication is not registered in AndroidManifest.xml")
            val repository = application.container.wordsRepository
            ExcelWordsViewModel(repository)
        }
    }
}