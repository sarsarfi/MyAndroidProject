package com.example.mydictionary.data.repository

import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordReport
import com.example.mydictionary.data.entities.WordsState
import kotlinx.coroutines.flow.Flow

interface WordStatsRepository {

    suspend fun insertGameState(wordsState: WordsState)

    suspend fun getGameStateByWordId(wordId: Int): WordsState?

    suspend fun updateStats(wordId: Int, isCorrect: Boolean)

    suspend fun increaseCorrect(wordId: Int)

    suspend fun increaseWrong(wordId: Int)

    fun getAllGameState(): Flow<List<WordsState>>

    suspend fun deleteByWordId(wordId: Int)

    suspend fun delete(wordsState: WordsState)

    fun getFullReport(): Flow<List<WordReport>>

    suspend fun updateLeitnerBox(wordId: Int, newLeitnerBox: Int)

    suspend fun updateNextReviewDate(wordId: Int, nextReviewDate: Long)

    fun getAllWordForReview(currentTime: Long): Flow<List<Word>>

    suspend fun updateSkipStatus(wordId: Int, isSkipped: Boolean)

    fun getAllSkippedWords(): Flow<List<Word>>
}