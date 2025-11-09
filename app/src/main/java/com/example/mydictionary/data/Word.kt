package com.example.mydictionary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word")
class Word(
   @PrimaryKey(autoGenerate = true)
   val id : Long = 0L   ,
   val english: String,
   val persian: String ,
   val dateAdded: Long = System.currentTimeMillis()
)
