package com.example.mydictionary.data.repository

import com.example.mydictionary.data.daos.WordDao
import com.example.mydictionary.data.entities.Word
import kotlinx.coroutines.flow.Flow

class OfflineWordsRepository(private val wordDao: WordDao) : WordsRepository {

    override fun getAllWordsDictionary(): Flow<List<Word>> = wordDao.getAllWord()

    override fun getWordDictionary(id: Long): Flow<Word?> = wordDao.getWord(id)

    override suspend fun insertWord(word: Word): Long = wordDao.insert(word)

    override suspend fun insertWords(words: List<Word>): List<Long> = wordDao.insertAll(words)

    override suspend fun getWordIdByEnglish(english: String): Int? {
        return wordDao.getWordIdByEnglish(english)
    }

    override suspend fun updateWord(word: Word) = wordDao.update(word)

    override suspend fun deleteWord(word: Word) = wordDao.delete(word)

    override fun getAllDateAdded(): Flow<List<Long>> = wordDao.getAllDateAdded()
}