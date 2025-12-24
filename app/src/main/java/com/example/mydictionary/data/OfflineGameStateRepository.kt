package com.example.mydictionary.data

import kotlinx.coroutines.flow.Flow


class OfflineGameStateRepository(private val gameStateDao: GameStateDao) : GameStateRepository{
    override suspend fun insertGameState(gameState: GameState) = gameStateDao.insert(gameState)
    override suspend fun getGameStateByWordId(wordId: Int): GameState? = gameStateDao.getByWordId(wordId)
    override suspend fun increaseCorrect(wordId: Int) = gameStateDao.increaseCorrect(wordId)
    override suspend fun increaseWrong(wordId: Int) = gameStateDao.increaseWrong(wordId)
    override  fun getAllGameState(): Flow<List<GameState>> = gameStateDao.getAll()
    override suspend fun deleteByWordId(wordId: Int) = gameStateDao.deleteByWordId(wordId)
    override fun getFullReport(): Flow<List<WordReport>> = gameStateDao.getFullReport()
    override suspend fun delete(gameState: GameState) = gameStateDao.delete(gameState)
    override suspend fun updateStats(wordId: Int, isCorrect: Boolean) = gameStateDao.updateStats(wordId, isCorrect)
}