package com.example.mydictionary.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mydictionary.data.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long //add new category

    @Delete
    suspend fun deleteCategory(category: Category) // delete category

    @Query("SELECT * FROM category ORDER BY dateAdded DESC")
    fun getAllCategories(): Flow<List<Category>>

}
