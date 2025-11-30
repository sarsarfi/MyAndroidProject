import com.example.mydictionary.data.Word
import kotlinx.coroutines.flow.Flow

interface WordsRepository {

    fun getAllWordsDictionary(): Flow<List<Word>>

    fun getWordDictionary(id: Long): Flow<Word?>

    suspend fun insertWord(word: Word)

    // ✅ اضافه کردن متد insertAll
    suspend fun insertWords(words: List<Word>)

    suspend fun updateWord(word: Word)

    suspend fun deleteWord(word: Word)

    fun getAllSkippedWords(): Flow<List<Word>>

    suspend fun updateLeitnerBox(wordId: Int, newLeitnerBox: Int)

    suspend fun updateNextReviewDate(wordId: Int, nextReviewDate: Long)

    fun getAllWordForReview(currentTime: Long): Flow<List<Word>>

    suspend fun updateSkipStatus(wordId: Int, isSkipped: Boolean)


}
