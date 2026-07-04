package com.example.mydictionary.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "category")
data class Category(
   @PrimaryKey(autoGenerate = true) val id: Int = 0,
   val name: String,
   val dateAdded: Long = System.currentTimeMillis()
)

@Entity(
   tableName = "word_category_cross_ref",
   primaryKeys = ["word_id", "category_id"], //  (Composite Key)
   foreignKeys = [
      ForeignKey(
         entity = Word::class,
         parentColumns = ["id"],
         childColumns = ["word_id"],
         onDelete = ForeignKey.CASCADE // اگر کلمه از دیتابیس پاک شد، رابطه‌اش هم پاک شود
      ),
      ForeignKey(
         entity = Category::class,
         parentColumns = ["id"],
         childColumns = ["category_id"],
         onDelete = ForeignKey.CASCADE // اگر دسته پاک شد، رابطه‌اش پاک شود (خود کلمه باقی می‌ماند)
      )
   ],
   indices = [
      Index(value = ["category_id"]) // ایندکس برای بالا بردن سرعت لود کلماتِ یک دسته‌بندی خاص
   ]
)
data class WordCategoryCrossRef(
   @ColumnInfo(name = "word_id") val wordId: Int,
   @ColumnInfo(name = "category_id") val categoryId: Int
)

@Entity(tableName = "word", indices = [Index(value = ["english"], unique = true)])
data class Word(
   @PrimaryKey(autoGenerate = true) val id: Int = 0,
   val english: String,
   val persian: String,
   val isDeleted: Boolean = false,
   val dateAdded: Long = System.currentTimeMillis() ,
   val searchUrl: String = "https://www.google.com/search?tbm=isch&q=$english"
)

@Entity(
   tableName = "word_stats",
   foreignKeys = [
      //1 to 1
      ForeignKey(
         entity = Word::class,
         parentColumns = ["id"],
         childColumns = ["word_id"],
         onDelete = ForeignKey.CASCADE
      )
   ],
   indices = [
      Index(value = ["word_id"], unique = true),
   ]
)
data class WordsState(
   @PrimaryKey(autoGenerate = true) val id: Int = 0,
   @ColumnInfo(name = "word_id") val wordId: Int,
   val correctAnswer: Int = 0,
   val wrongAnswer: Int = 0,
   val isSkipped: Boolean = false,
   val leitnerBox: Int = 0,
   val nextReviewDate: Long = 0L
)