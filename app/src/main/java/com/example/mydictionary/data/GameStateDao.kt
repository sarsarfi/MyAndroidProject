package com.example.mydictionary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameState: GameState)

    @Query("SELECT * FROM game_state WHERE word_id = :wordId LIMIT 1")
    suspend fun getByWordId(wordId: Int): GameState?

    @androidx.room.Transaction
    suspend fun updateStats(wId: Int, isCorrect: Boolean) {

        val existing = getByWordId(wId)

        if (existing == null) {
            // ۲. اگر نبوده، یک ردیف جدید بساز
            val newState = GameState(
                wordId = wId,
                correctAnswer = if (isCorrect) 1 else 0,
                wrongAnswer = if (isCorrect) 0 else 1
            )
            insert(newState)
        } else {
            // ۳. اگر بوده، مقدار قبلی را بگیر و یکی اضافه کن و دوباره آپدیت کن
            val updatedState = if (isCorrect) {
                existing.copy(correctAnswer = existing.correctAnswer + 1)
            } else {
                existing.copy(wrongAnswer = existing.wrongAnswer + 1)
            }
            insert(updatedState) // چون OnConflictStrategy.REPLACE داریم، آپدیت انجام می‌شود
        }
    }

    @Query("UPDATE game_state SET correctAnswer = correctAnswer + 1 WHERE word_id = :wordId")
    suspend fun increaseCorrect(wordId: Int)

    @Query("UPDATE game_state SET wrongAnswer = wrongAnswer + 1 WHERE word_id = :wordId")
    suspend fun increaseWrong(wordId: Int)

    @Delete
    suspend fun delete(gameState: GameState)

    @Query("DELETE FROM game_state WHERE word_id = :wordId")
    suspend fun deleteByWordId(wordId: Int)

    @Query("SELECT * FROM game_state")
    fun getAll(): Flow<List<GameState>>

    @Query("""
    SELECT 
        word.english as englishWord, 
        COALESCE(game_state.correctAnswer, 0) as correctCount, 
        COALESCE(game_state.wrongAnswer, 0) as wrongCount 
    FROM word 
    LEFT JOIN game_state ON word.id = game_state.word_id
""")
    fun getFullReport(): Flow<List<WordReport>>
}