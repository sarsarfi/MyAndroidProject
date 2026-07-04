package com.example.mydictionary.data.repository

import com.example.mydictionary.data.entities.Word
import kotlinx.coroutines.flow.Flow

interface WordsRepository {

    fun getAllWordsDictionary(): Flow<List<Word>>

    fun getWordDictionary(id: Long): Flow<Word?>

    suspend fun insertWord(word: Word): Long

    suspend fun insertWords(words: List<Word>) : List<Long>

    //  متد جدید برای یافتن آیدی کلمات تکراری بر اساس متن انگلیسی
    suspend fun getWordIdByEnglish(english: String): Int?

    suspend fun updateWord(word: Word)

    suspend fun deleteWord(word: Word)

    fun getAllDateAdded(): Flow<List<Long>>
}