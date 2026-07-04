package com.example.mydictionary.data.entities

//virtual table
data class WordReport(
    val englishWord: String, // from Word Entity
    val correctCount: Int,   // from WordStats Entity
    val wrongCount: Int      // from WordStats Entity
)