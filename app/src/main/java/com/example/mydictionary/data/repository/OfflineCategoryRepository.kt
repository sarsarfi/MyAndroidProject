package com.example.mydictionary.data.repository

import com.example.mydictionary.data.daos.CategoryDao
import com.example.mydictionary.data.entities.Category
import kotlinx.coroutines.flow.Flow

class OfflineCategoryRepository(
    private val categoryDao: CategoryDao
): CategoryRepository {

    override suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)

    override suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

}