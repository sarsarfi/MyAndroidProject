package com.example.mydictionary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mydictionary.data.dao.WordCategoryCrossRefDao
import com.example.mydictionary.data.daos.CategoryDao
import com.example.mydictionary.data.daos.WordDao
import com.example.mydictionary.data.daos.WordsStateDao
import com.example.mydictionary.data.entities.Category
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordCategoryCrossRef
import com.example.mydictionary.data.entities.WordsState

@Database(
    entities = [Word::class, WordsState::class , Category::class , WordCategoryCrossRef::class],
    version = 13,
    exportSchema = false
)
abstract class DictionaryDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun wordStatsDao(): WordsStateDao

    abstract fun categoryDao(): CategoryDao

    abstract fun wordCategoryCrossRefDao(): WordCategoryCrossRefDao

    companion object {
        @Volatile
        private var Instance: DictionaryDatabase? = null

        fun getDatabase(context: Context): DictionaryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    DictionaryDatabase::class.java,
                    "dictionary_database"
                )
                    .fallbackToDestructiveMigration() // دیتابیس قدیمی را پاک و با ساختار جدید ورژن x می‌سازد
                    .build()
                    .also { Instance = it }
            }
        }
    }
}