package com.example.mydictionary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordCategoryCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCategoryCrossRefDao {

    //  متصل کردن یک کلمه به یک دسته‌بندی خاص (ثبت در جدول واسط)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRelation(crossRef: WordCategoryCrossRef)

    //  متصل کردن گروهی کلمات به یک دسته (فوق‌العاده حیاتی برای زمان Import فایل اکسل)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRelations(relations: List<WordCategoryCrossRef>)

    //  قطع رابطه یک کلمه با یک دسته خاص (برداشتن کلمه از یک دسته‌بندی)
    @Delete
    suspend fun deleteRelation(crossRef: WordCategoryCrossRef)

    //   (JOIN رابطه‌ای): گرفتن تمام لغات متعلق به یک دسته‌بندی خاص برای صفحه ۲
    // این کوئری جدول Word را با جدول واسط متصل می‌کند و فقط لغاتی را می‌آورد که آیدی دسته آن‌ها با ورودی یکی باشد
    @Transaction
    @Query("""
        SELECT word.* FROM word 
        INNER JOIN word_category_cross_ref ON word.id = word_category_cross_ref.word_id 
        WHERE word_category_cross_ref.category_id = :categoryId AND word.isDeleted = 0 
        ORDER BY word.dateAdded DESC
    """)
    fun getWordsForCategory(categoryId: Int): Flow<List<Word>>

    //  چک کردن اینکه آیا یک کلمه قبلاً در این دسته وجود داشته یا خیر (برای جلوگیری از ثبت مجدد لغت در یک صفحه)
    @Query("SELECT EXISTS(SELECT 1 FROM word_category_cross_ref WHERE word_id = :wordId AND category_id = :categoryId)")
    suspend fun isWordInCategory(wordId: Int, categoryId: Int): Boolean
}