package com.example.mydictionary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Word::class , GameState::class], // ← اضافه کردن View
    version = 9,
    exportSchema = false
)
abstract class DictionaryDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun gameStateDao(): GameStateDao


    companion object {
        @Volatile
        private var Instance: DictionaryDatabase? = null

        fun getDatabase(context: Context): DictionaryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    DictionaryDatabase::class.java,
                    "dictionary_database"
                )
                    .fallbackToDestructiveMigration() // برای مرحله توسعه
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
