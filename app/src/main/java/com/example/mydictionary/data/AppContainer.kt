package com.example.mydictionary.data

import WordsRepository
import android.content.Context
import com.example.mydictionary.data.DictionaryDatabase
import com.example.mydictionary.data.GameStateRepository

interface AppContainer {

    val wordsRepository : WordsRepository
    val gameStateRepository : GameStateRepository
}

class AppDataContainer (private val context: Context) : AppContainer{


    override val wordsRepository : WordsRepository by lazy {
        offlineWordsRepository(DictionaryDatabase.getDatabase(context).wordDao())
    }
    override val gameStateRepository : GameStateRepository by lazy {
        OfflineGameStateRepository(DictionaryDatabase.getDatabase(context).gameStateDao())
    }
}