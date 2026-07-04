package com.example.mydictionary.data.repository

import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordCategoryCrossRef
import kotlinx.coroutines.flow.Flow

interface WordCategoryCrossRefRepository {

    suspend fun insertRelation(crossRef: WordCategoryCrossRef)

    suspend fun insertRelations(relations: List<WordCategoryCrossRef>)

    suspend fun deleteRelation(crossRef: WordCategoryCrossRef)

    fun getWordsForCategory(categoryId: Int): Flow<List<Word>>

    suspend fun isWordInCategory(wordId: Int, categoryId: Int): Boolean

    //  برای اضافه کردن لغت دستی به یک دسته خاص
    suspend fun insertWordToCategory(word: Word, categoryId: Int)

    //  برای اضافه کردن گروهی لغات (اکسل) به یک دسته خاص
    suspend fun insertExcelWordsToCategory(words: List<Word>, categoryId: Int)
}