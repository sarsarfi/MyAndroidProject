package com.example.mydictionary.ui.wordlist

import WordsRepository
import com.example.mydictionary.data.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeWordsRepository : WordsRepository {

    override fun getAllWordsDictionary(): Flow<List<Word>> = flowOf(emptyList())

    override fun getWordDictionary(id: Long): Flow<Word?> = flowOf(null)
    override suspend fun insertWord(word: Word) { /* do nothing */
    }

    override suspend fun insertWords(words: List<Word>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWord(word: Word) { /* do nothing */
    }

    override suspend fun updateWord(word: Word) { /* do nothing */
    }

    override fun getAllSkippedWords(): Flow<List<Word>>  = flowOf(emptyList())

    override suspend fun updateLeitnerBox(wordId: Int, newLeitnerBox: Int){}

    override suspend fun updateNextReviewDate(wordId: Int , nextReviewDate : Long){}

    override fun getAllWordForReview(currentTime : Long ) : Flow<List<Word>> = flowOf(emptyList())


    override suspend fun updateSkipStatus(wordId: Int, isSkipped: Boolean) {}

    override fun getAllDateAdded(): Flow<List<Long>> {
        TODO("Not yet implemented")
    }

}