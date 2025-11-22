package com.example.mydictionary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word")
data class Word(
   @PrimaryKey(autoGenerate = true)
   val id : Int = 0  ,
   val english: String,
   val persian: String ,
   val isSkipped: Boolean = false ,
   val isDeleted : Boolean = false ,
   val dateAdded: Long = System.currentTimeMillis() ,
   val leitnerBox : Int = 1 ,
   val nextReviewDate : Long = System.currentTimeMillis()
)
