package com.example.mydictionary.data.repository

import com.example.mydictionary.data.daos.WordDao
import com.example.mydictionary.data.daos.WordsStateDao
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordReport
import com.example.mydictionary.data.entities.WordsState
import kotlinx.coroutines.flow.Flow

class OfflineWordStatsRepository(
    private val wordsStateDao: WordsStateDao,
    private val wordDao: WordDao
) : WordStatsRepository {

    override suspend fun insertGameState(wordsState: WordsState) = wordsStateDao.insert(wordsState)

    override suspend fun getGameStateByWordId(wordId: Int): WordsState? = wordsStateDao.getByWordId(wordId)

    override suspend fun increaseCorrect(wordId: Int) = wordsStateDao.increaseCorrect(wordId)

    override suspend fun increaseWrong(wordId: Int) = wordsStateDao.increaseWrong(wordId)

    override fun getAllGameState(): Flow<List<WordsState>> = wordsStateDao.getAll()

    override suspend fun deleteByWordId(wordId: Int) = wordsStateDao.deleteByWordId(wordId)

    override suspend fun delete(wordsState: WordsState) = wordsStateDao.delete(wordsState)

    override fun getFullReport(): Flow<List<WordReport>> = wordsStateDao.getFullReport()

    override suspend fun updateStats(wordId: Int, isCorrect: Boolean) = wordsStateDao.updateStats(wordId, isCorrect)

    override suspend fun updateLeitnerBox(wordId: Int, newLeitnerBox: Int) {
        wordsStateDao.updateWordBox(wordId, newLeitnerBox)
    }

    override suspend fun updateNextReviewDate(wordId: Int, nextReviewDate: Long) {
        wordsStateDao.updateNextReviewDate(wordId, nextReviewDate)
    }

    override suspend fun updateSkipStatus(wordId: Int, isSkipped: Boolean) {
        wordsStateDao.updateSkipStatus(wordId, isSkipped)
    }

    override fun getAllWordForReview(currentTime: Long): Flow<List<Word>> {
        return wordDao.getAllWordsForReview(currentTime)
    }

    // 🎯 متد جدید اضافه شد تا زنجیره متدهای لایتنر و کلمات فراموش‌شده کامل شود
    override fun getAllSkippedWords(): Flow<List<Word>> {
        return wordsStateDao.getAllSkippedWords()
    }
}