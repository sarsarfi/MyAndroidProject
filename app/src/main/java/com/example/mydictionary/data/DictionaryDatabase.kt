package com.example.mydictionary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Word::class], version = 5, exportSchema = false)
abstract class DictionaryDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

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
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
