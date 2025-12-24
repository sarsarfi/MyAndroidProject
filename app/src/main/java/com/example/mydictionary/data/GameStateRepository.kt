package com.example.mydictionary.data

import kotlinx.coroutines.flow.Flow

interface GameStateRepository{

    suspend fun insertGameState(gameState: GameState)
    suspend fun getGameStateByWordId(wordId: Int): GameState?
    suspend fun increaseCorrect(wordId: Int)
    suspend fun increaseWrong(wordId: Int)
    fun getAllGameState(): Flow<List<GameState>>
    suspend fun deleteByWordId(wordId: Int)
    suspend fun delete(gameState: GameState)

    fun getFullReport(): Flow<List<WordReport>>

    suspend fun updateStats(wordId: Int, isCorrect: Boolean)


}