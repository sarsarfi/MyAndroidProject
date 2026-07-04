package com.example.mydictionary.data.repository

import com.example.mydictionary.data.dao.WordCategoryCrossRefDao
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.entities.WordCategoryCrossRef

class OfflineWordCategoryCrossRefRepository(
    private val wordCategoryCrossRefDao: WordCategoryCrossRefDao ,
    private val wordsRepository: WordsRepository
): WordCategoryCrossRefRepository {

    override suspend fun insertRelation(crossRef: WordCategoryCrossRef) = wordCategoryCrossRefDao.insertRelation(crossRef)

    override suspend fun insertRelations(relations: List<WordCategoryCrossRef>) = wordCategoryCrossRefDao.insertRelations(relations)

    override suspend fun deleteRelation(crossRef: WordCategoryCrossRef) = wordCategoryCrossRefDao.deleteRelation(crossRef)

    override fun getWordsForCategory(categoryId: Int) = wordCategoryCrossRefDao.getWordsForCategory(categoryId)

    override suspend fun isWordInCategory(wordId: Int, categoryId: Int) = wordCategoryCrossRefDao.isWordInCategory(wordId, categoryId)

    override suspend fun insertWordToCategory(word: Word, categoryId: Int) {
        // ۱. تلاش برای درج لغت
        val wordId = wordsRepository.insertWord(word)

        // اگر کلمه تکراری بود و دیتابیس 1- برگرداند، از همان آیدی واقعی که همراه کلمه آمده بود (word.id) استفاده کن.
        // در غیر این صورت، از آیدی جدیدی که دیتابیس تولید کرده استفاده کن.
        val finalWordId = if (wordId == -1L) {
            word.id
        } else {
            wordId.toInt()
        }

        //  ساخت رابطه با آیدی واقعی و تضمین‌شده
        val crossRef = WordCategoryCrossRef(finalWordId, categoryId)

        //  ثبت بدون کرش و بدون نقص در جدول واسط
        wordCategoryCrossRefDao.insertRelation(crossRef)
    }

    override suspend fun insertExcelWordsToCategory(words: List<Word>, categoryId: Int) {
        //  فرستادن لیست لغات به دیتابیس و گرفتن لیست آیدی‌ها (که تکراری‌ها در آن 1- هستند)
        val wordIds = wordsRepository.insertWords(words)

        //  با استفاده از یک حلقه  آیدی‌های واقعی را جایگزین 1- می‌کنیم
        val crossRefs = wordIds.mapIndexed { index, wordId ->
            val finalWordId = if (wordId == -1L) {
                words[index].id // اگر تکراری بود، آیدی خودش را که از قبل پیدا کرده بودیم بردار
            } else {
                wordId.toInt() // اگر جدید بود، آیدی دیتابیس را بردار
            }
            WordCategoryCrossRef(finalWordId, categoryId)
        }

        //  ثبت گروهی در جدول واسط
        wordCategoryCrossRefDao.insertRelations(crossRefs)
    }

}