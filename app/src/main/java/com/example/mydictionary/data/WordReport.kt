package com.example.mydictionary.data

//virtual table
data class WordReport(
    val englishWord: String, // from Word Entity
    val correctCount: Int,   // from GameState Entity
    val wrongCount: Int      // from GameState Entity
)