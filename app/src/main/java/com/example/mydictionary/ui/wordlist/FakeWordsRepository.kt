package com.example.mydictionary.ui.wordlist

import com.example.mydictionary.data.Word
import com.example.mydictionary.data.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeWordsRepository : WordsRepository{
    // چون در Preview فقط به لیست کلمات نیاز داریم،
    // تابع getAllWordsStream را طوری پیاده‌سازی می‌کنیم که یک Flow از لیست خالی بدهد.
    override fun getAllWordsDictionary(): Flow<List<Word>> = flowOf(emptyList())

    // بقیه توابع suspend باید پیاده‌سازی شوند، هرچند که در Preview استفاده نمی‌شوند.
    override fun getWordDictionary(id: Long): Flow<Word?> = flowOf(null)
    override suspend fun insertWord(word: Word) { /* do nothing */ }
    override suspend fun deleteWord(word: Word) { /* do nothing */ }
    override suspend fun updateWord(word: Word) { /* do nothing */ }
}