package com.example.mydictionary.data

import WordsRepository
import kotlinx.coroutines.flow.Flow

class offlineWordsRepository(private val wordDao: WordDao) : WordsRepository {

    override fun getAllWordsDictionary(): Flow<List<Word>> = wordDao.getAllWord()

    override fun getWordDictionary(id: Long): Flow<Word?> = wordDao.getWord(id)

    override suspend fun insertWord(word: Word) = wordDao.insert(mutableListOf(word))

    // ✅ اضافه کردن متد برای درج چند کلمه‌ای
    override suspend fun insertWords(words: List<Word>) = wordDao.insertAll(words.toMutableList())

    override suspend fun updateWord(word: Word) = wordDao.update(word)

    override suspend fun deleteWord(word: Word) = wordDao.delete(word)

    override fun getAllSkippedWords(): Flow<List<Word>> = wordDao.getSkippedWords()

    override suspend fun updateLeitnerBox(wordId: Int, newLeitnerBox: Int) =
        wordDao.updateWordBox(wordId, newLeitnerBox)

    override suspend fun updateNextReviewDate(wordId: Int, nextReviewDate: Long) =
        wordDao.updateNextReviewDate(wordId, nextReviewDate)

    override fun getAllWordForReview(currentTime: Long): Flow<List<Word>> =
        wordDao.getAllWordForReview(currentTime)

    override suspend fun updateSkipStatus(wordId: Int, isSkipped: Boolean) =
        wordDao.updateSkipStatus(wordId, isSkipped)
}
