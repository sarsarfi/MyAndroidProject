package com.example.mydictionary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow



@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    @Update
    suspend fun update(word: Word)

    @Delete
    suspend fun delete(word: Word)


    @Query("SELECT * FROM word WHERE id =:id ")
    fun getWord(id : Long): Flow<Word?>

    @Query("SELECT * FROM word ORDER BY dateAdded DESC")
    fun getAllWord(): Flow<List<Word>>

    // --- متد جدید ۱: دریافت کلمات رد شده ---
    @Query("SELECT * FROM word WHERE isSkipped = 1 ORDER BY dateAdded DESC")
    fun getSkippedWords(): Flow<List<Word>>
    // ----------------------------------------

    // متد update موجود برای به‌روزرسانی فیلد isSkipped کفایت می‌کند.

    // در WordDao.kt - این کوئری را اضافه کنید
    @Query("UPDATE word SET isSkipped = :isSkipped WHERE id = :wordId")
    suspend fun updateSkipStatus(wordId: Int, isSkipped: Boolean)

    @Query("UPDATE word SET leitnerBox = :newLeitnerBox WHERE id = :wordId")
    suspend fun updateWordBox(wordId: Int , newLeitnerBox : Int)

    @Query("UPDATE word SET nextReviewDate = :nextReviewDate WHERE  id = :wordId")
    suspend fun updateNextReviewDate(wordId : Int , nextReviewDate : Long)

    @Query("SELECT * FROM word WHERE leitnerBox < 5 AND nextReviewDate <= :currentTime ORDER BY leitnerBox ASC, nextReviewDate ASC")
    fun getAllWordForReview(currentTime : Long): Flow<List<Word>>

}