package com.example.mydictionary.data

import kotlinx.coroutines.flow.Flow

interface WordsRepository {

    fun getAllWordsDictionary(): Flow<List<Word>>

    fun getWordDictionary(id : Long) : Flow<Word?>

    suspend fun insertWord(word: Word)

    suspend fun updateWord(word: Word)

    suspend fun deleteWord(word: Word)

}