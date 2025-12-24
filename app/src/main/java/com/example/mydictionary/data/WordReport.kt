package com.example.mydictionary.data

// این یک کلاس معمولی است و @Entity ندارد چون جدول نیست
data class WordReport(
    val englishWord: String, // از جدول Word می‌آید
    val correctCount: Int,   // از جدول GameState می‌آید
    val wrongCount: Int      // از جدول GameState می‌آید
)