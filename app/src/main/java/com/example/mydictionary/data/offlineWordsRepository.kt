package com.example.mydictionary.data

import kotlinx.coroutines.flow.Flow

class offlineWordsRepository(private val wordDao: WordDao) : WordsRepository {

    override fun getAllWordsDictionary(): Flow<List<Word>> = wordDao.getAllWord()

    override fun getWordDictionary(id: Long): Flow<Word?> = wordDao.getWord(id)

    override suspend fun insertWord(word: Word) = wordDao.insert(word)

    override suspend fun updateWord(word: Word) = wordDao.update(word)

    override suspend fun deleteWord(word: Word) = wordDao.delete(word)
}