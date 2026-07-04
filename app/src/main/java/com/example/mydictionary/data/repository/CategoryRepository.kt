package com.example.mydictionary.data.repository

import com.example.mydictionary.data.entities.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    suspend fun insertCategory(category: Category): Long

    suspend fun deleteCategory(category: Category)

    fun getAllCategories(): Flow<List<Category>>

}