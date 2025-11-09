package com.example.mydictionary

import android.app.Application
import com.example.mydictionary.data.AppContainer
import com.example.mydictionary.data.AppDataContainer

class DictionaryApplication : Application() {

    val container: AppContainer by lazy {
        AppDataContainer(this)
    }

}