package com.example.mydictionary.data

import android.content.Context
import com.example.mydictionary.data.repository.CategoryRepository
import com.example.mydictionary.data.repository.OfflineCategoryRepository
import com.example.mydictionary.data.repository.OfflineWordCategoryCrossRefRepository
import com.example.mydictionary.data.repository.OfflineWordStatsRepository
import com.example.mydictionary.data.repository.OfflineWordsRepository
import com.example.mydictionary.data.repository.WordCategoryCrossRefRepository
import com.example.mydictionary.data.repository.WordStatsRepository
import com.example.mydictionary.data.repository.WordsRepository

interface AppContainer {
    val wordsRepository: WordsRepository
    val wordStatsRepository: WordStatsRepository

    val categoryRepository: CategoryRepository

    val wordCategoryCrossRefRepository: WordCategoryCrossRefRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val database by lazy { DictionaryDatabase.getDatabase(context) }

    override val wordsRepository: WordsRepository by lazy {
        OfflineWordsRepository(wordDao = database.wordDao())
    }
    override val wordStatsRepository: WordStatsRepository by lazy {
        OfflineWordStatsRepository(
            wordsStateDao = database.wordStatsDao(),
            wordDao = database.wordDao()
        )
    }
    override val categoryRepository: CategoryRepository by lazy {
        OfflineCategoryRepository(
            categoryDao = database.categoryDao()
        )
    }

    override val wordCategoryCrossRefRepository : WordCategoryCrossRefRepository by lazy {
        OfflineWordCategoryCrossRefRepository(
            wordCategoryCrossRefDao = database.wordCategoryCrossRefDao() ,
            wordsRepository = wordsRepository
        )
    }


}