package com.example.mydictionary.data

import WordsRepository
import android.content.Context

interface AppContainer {

    val wordsRepository : WordsRepository
}

class AppDataContainer (private val context: Context) : AppContainer{


    override val wordsRepository : WordsRepository by lazy {
        offlineWordsRepository(DictionaryDatabase.getDatabase(context).wordDao())
    }
}