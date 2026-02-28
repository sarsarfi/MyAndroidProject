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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<Word>)//get from excel file


   @Update
    suspend fun update(word: Word)

    @Delete
    suspend fun delete(word: Word)

    @Query("SELECT * FROM word WHERE id = :id")
    fun getWord(id: Long): Flow<Word?>

    @Query("SELECT * FROM word ORDER BY dateAdded DESC")
    fun getAllWord(): Flow<List<Word>> //get all words from database with date added order

    @Query("SELECT * FROM word WHERE isSkipped = 1 ORDER BY dateAdded DESC")
    fun getSkippedWords(): Flow<List<Word>> //get all skipped words from database with date added order


    @Query("UPDATE word SET isSkipped = :isSkipped WHERE id = :wordId")
    suspend fun updateSkipStatus(wordId: Int, isSkipped: Boolean)//update skip status of word in database


    @Query("UPDATE word SET leitnerBox = :newLeitnerBox WHERE id = :wordId")
    suspend fun updateWordBox(wordId: Int, newLeitnerBox: Int) //update leitner box of word in database


    @Query("UPDATE word SET nextReviewDate = :nextReviewDate WHERE id = :wordId")
    suspend fun updateNextReviewDate(wordId: Int, nextReviewDate: Long)//update next review date of word in database

    @Query("SELECT * FROM word WHERE leitnerBox <= 5 AND nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC, leitnerBox ASC")
    fun getAllWordForReview(currentTime: Long): Flow<List<Word>> //get all words for review from database with leitner box and next review date order

    @Query("SELECT dateAdded FROM word ORDER BY dateAdded DESC") //get all date added from database with date added order to show in report
    fun getAllDateAdded(): Flow<List<Long>>

}