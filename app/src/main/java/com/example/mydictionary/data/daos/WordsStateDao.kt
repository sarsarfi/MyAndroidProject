package com.example.mydictionary.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordReport
import com.example.mydictionary.data.entities.WordsState
import kotlinx.coroutines.flow.Flow

@Dao
interface WordsStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wordsState: WordsState)

    @Query("SELECT * FROM word_stats WHERE word_id = :wordId LIMIT 1")
    suspend fun getByWordId(wordId: Int): WordsState?

    @Transaction
    suspend fun updateStats(wId: Int, isCorrect: Boolean) {
        val existing = getByWordId(wId)

        if (existing == null) {
            // اگر کلمه جدید بود و کاربر درست جواب داد، جعبه آن را 0 می‌گذاریم تا وارد لایتنر نشود
            // اما اگر غلط جواب داد، طبق خواسته شما مستقیم وارد جعبه 1 لایتنر می‌شود
            val newState = WordsState(
                wordId = wId,
                leitnerBox = if (isCorrect) 0 else 1,
                nextReviewDate = if (isCorrect) 0L else System.currentTimeMillis(),
                correctAnswer = if (isCorrect) 1 else 0,
                wrongAnswer = if (isCorrect) 0 else 1
            )
            insert(newState)
        } else {
            // اگر کلمه از قبل رکورد داشت (چه در لایتنر باشد چه کلمه عمومی با جعبه 0)
            val updatedState = if (isCorrect) {
                existing.copy(correctAnswer = existing.correctAnswer + 1)
            } else {
                // اگر قبلاً توی جعبه‌های بالا بوده یا عمومی بوده، با پاسخ غلط مستقیم به جعبه ۱ سقوط می‌کند
                existing.copy(
                    wrongAnswer = existing.wrongAnswer + 1,
                    leitnerBox = 1,
                    nextReviewDate = System.currentTimeMillis()
                )
            }
            insert(updatedState)
        }
    }


    @Query("UPDATE word_stats SET isSkipped = :isSkipped WHERE word_id = :wordId")
    suspend fun updateSkipStatus(wordId: Int, isSkipped: Boolean)

    @Query("UPDATE word_stats SET leitnerBox = :newLeitnerBox WHERE word_id = :wordId")
    suspend fun updateWordBox(wordId: Int, newLeitnerBox: Int)

    @Query("UPDATE word_stats SET nextReviewDate = :nextReviewDate WHERE word_id = :wordId")
    suspend fun updateNextReviewDate(wordId: Int, nextReviewDate: Long)

    @Query("UPDATE word_stats SET correctAnswer = correctAnswer + 1 WHERE word_id = :wordId")
    suspend fun increaseCorrect(wordId: Int)

    @Query("UPDATE word_stats SET wrongAnswer = wrongAnswer + 1 WHERE word_id = :wordId")
    suspend fun increaseWrong(wordId: Int)

    @Delete
    suspend fun delete(wordsState: WordsState)

    @Query("DELETE FROM word_stats WHERE word_id = :wordId")
    suspend fun deleteByWordId(wordId: Int)

    @Query("SELECT * FROM word_stats")
    fun getAll(): Flow<List<WordsState>>

    @Query("""
        SELECT word.* FROM word 
        INNER JOIN word_stats ON word.id = word_stats.word_id 
        WHERE word.isDeleted = 0 AND word_stats.isSkipped = 1
    """)
    fun getAllSkippedWords(): Flow<List<Word>>

    @Query("""
        SELECT 
            word.english as englishWord, 
            COALESCE(word_stats.correctAnswer, 0) as correctCount, 
            COALESCE(word_stats.wrongAnswer, 0) as wrongCount 
        FROM word 
        LEFT JOIN word_stats ON word.id = word_stats.word_id
        WHERE word.isDeleted = 0
    """)
    fun getFullReport(): Flow<List<WordReport>>
}