package com.example.mydictionary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Word::class], version = 2) // نسخه جدید
abstract class DictionaryDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var Instance: DictionaryDatabase? = null

        // تعریف Migration
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // اضافه کردن ستون dateAdded با مقدار پیش‌فرض 0
                database.execSQL(
                    "ALTER TABLE word ADD COLUMN dateAdded INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): DictionaryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    DictionaryDatabase::class.java,
                    "dictionary_database"
                )
                    .addMigrations(MIGRATION_1_2) // اعمال Migration
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
