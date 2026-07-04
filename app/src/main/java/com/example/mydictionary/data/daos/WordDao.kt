package com.example.mydictionary.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mydictionary.data.entities.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<Word>): List<Long> // دریافت از فایل اکسل

    @Update
    suspend fun update(word: Word)

    @Delete
    suspend fun delete(word: Word)

    @Query("SELECT * FROM word WHERE id = :id")
    fun getWord(id: Long): Flow<Word?>

    //  متد  برای پیدا کردن آیدی کلماتی که از قبل در دیتابیس وجود دارند
    @Query("SELECT id FROM word WHERE english = :english LIMIT 1")
    suspend fun getWordIdByEnglish(english: String): Int?

    @Query("SELECT * FROM word WHERE isDeleted = 0 ORDER BY dateAdded DESC")
    fun getAllWord(): Flow<List<Word>>

    @Query("SELECT dateAdded FROM word WHERE isDeleted = 0 ORDER BY dateAdded DESC")
    fun getAllDateAdded(): Flow<List<Long>>

    @Query("""
    SELECT word.* FROM word 
    INNER JOIN word_stats ON word.id = word_stats.word_id 
    WHERE word.isDeleted = 0 
      AND word_stats.leitnerBox >= 1 
      AND word_stats.leitnerBox <= 5 
      AND word_stats.nextReviewDate > 0 
      AND word_stats.nextReviewDate <= :currentTime 
      
    ORDER BY word_stats.leitnerBox ASC, word_stats.nextReviewDate ASC
""")
    fun getAllWordsForReview(currentTime: Long): Flow<List<Word>>
}