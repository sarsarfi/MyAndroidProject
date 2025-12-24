package com.example.mydictionary.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "word" , indices = [androidx.room.Index(value = ["english"] , unique = true)])
data class Word(
   @PrimaryKey(autoGenerate = true)
   val id : Int = 0  ,
   @ColumnInfo()
   val english: String,
   val persian: String ,
   val isSkipped: Boolean = false ,
   val isDeleted : Boolean = false ,
   val dateAdded: Long = System.currentTimeMillis() ,
   val leitnerBox : Int = 1 ,
   val nextReviewDate : Long = System.currentTimeMillis()
)

@Entity(tableName = "game_state" ,
   foreignKeys = [ForeignKey(
      entity = Word::class,
      parentColumns = ["id"],
      childColumns = ["word_id"],
      onDelete = ForeignKey.CASCADE
   )] ,
   indices = [androidx.room.Index(value = ["word_id"] , unique = true)]
)
data class GameState(
   @PrimaryKey(autoGenerate = true) val id: Int = 0,
   @ColumnInfo(name = "word_id") val wordId: Int, // مطمئن شوید نام ستون در دیتابیس word_id است
   val correctAnswer: Int = 0,
   val wrongAnswer: Int = 0
)



