package com.example.mydictionary.ui.quiz

import WordsRepository
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import com.example.mydictionary.data.GameState
import com.example.mydictionary.data.GameStateRepository
import java.util.Locale

data class QuizUiState(
    val currentWord: String = "",
    val inputUserGuess: String = "",
    val currentWordCount: Int = 0,
    val score: Int = 0,
    val isGuess: Boolean = false,
    val isGameOver: Boolean = false,
    val isLoading: Boolean = true,
    val message: String = ""
)

data class WordListUiState(val wordList: List<Word> = listOf())

private const val SCORE_QUIZ = 20
private const val WORD_COUNT_QUIZ = 10

class QuizViewModel(private val wordsRepository: WordsRepository ,
    private val gameStateRepository: GameStateRepository
) : ViewModel() , OnInitListener {

    // Add pronunciation logic
    private var tts: TextToSpeech? = null
    var isTtsInitialized = false

    fun initializeTts(context: Context) {
        if(tts == null){
            tts = TextToSpeech(context.applicationContext , this)
        }
    }

    override fun onInit(status : Int) {
        if (status == TextToSpeech.SUCCESS){
            val result = tts?.setLanguage(Locale.ENGLISH)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                isTtsInitialized = false
            }else{
                isTtsInitialized = true
            }
        }
    }

    fun speakWord(word: String) {

        if (isTtsInitialized) {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
        }

    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    // ✅ تابع جدید: پخش کلمه اصلی ذخیره شده
    fun speakCurrentCorrectWord() {
        currentWordObject?.english?.let { word ->
            speakWord(word)
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private val _uiState = MutableStateFlow(QuizUiState())

    val uiState : StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var currentWordObject : Word? = null

    private val usedWords : MutableSet<String> = mutableSetOf()

    private var availableWordsCount : Int = 0

    val allWords : StateFlow<WordListUiState> = wordsRepository.getAllWordsDictionary()
        .map { wordList ->
            val filteredWords = wordList.filter { !it.isDeleted }
            WordListUiState(filteredWords)
        }
        .stateIn(
            scope = viewModelScope ,
            started = SharingStarted.WhileSubscribed(5_000L) ,
            initialValue = WordListUiState()
        )

    init {
        viewModelScope.launch {
            allWords.collect { wordListState ->
                availableWordsCount  = wordListState.wordList.size

                if (wordListState.wordList.isNotEmpty() && usedWords.isEmpty()){
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    wordRandom()
                }else if (usedWords.isNotEmpty()){
                    refreshAvailableWords()
                }
            }
        }
    }

    private fun refreshAvailableWords() {
        val currentWords = allWords.value.wordList.map { it.english }
        usedWords.removeAll { usedWord ->
            !currentWords.contains(usedWord)
        }

        // اگر کلمه فعلی حذف شده، کلمه جدید انتخاب کن
        currentWordObject?.let { current ->
            if (!currentWords.contains(current.english)) {
                wordRandom()
            }
        }
    }

    private fun wordRandom() {
        val words = allWords.value.wordList
        availableWordsCount = words.size

        if (words.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                currentWord = "No words available",
                inputUserGuess = "",
                message = "Please add some words to wordlist!!"
            )
            return
        }

        if (usedWords.size >= availableWordsCount || usedWords.size >= WORD_COUNT_QUIZ) {
            _uiState.value = _uiState.value.copy(
                isGameOver = true,
                message = "Game completed! Score: ${_uiState.value.score}"
            )
            return
        }

        val availableWords = words.filter {
            !usedWords.contains(it.english) && !it.isDeleted
        }

        if (availableWords.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isGameOver = true,
                message = "No more words available"
            )
            return
        }

        val wordToUseObject = availableWords.random()
        usedWords.add(wordToUseObject.english)
        currentWordObject = wordToUseObject

        val shuffledWord = shuffleWord(wordToUseObject.english)
        val correctWord = wordToUseObject.english

        // 🟢 تغییر کلیدی: از Coroutine برای مدیریت پخش و به‌روزرسانی UI استفاده می‌کنیم
        viewModelScope.launch {
            // 1. ابتدا TTS را متوقف کنید تا هر پخش قبلی متوقف شود.
            tts?.stop()

            // 2. UI را با کلمه شافل شده به‌روزرسانی کنید.
            _uiState.value = _uiState.value.copy(
                currentWord = shuffledWord,
                currentWordCount = usedWords.size,
                inputUserGuess = "",
                isGuess = false,
                // پیام را اینجا به‌روزرسانی کنید تا سریعتر ظاهر شود
                message = "Word ${usedWords.size} of $WORD_COUNT_QUIZ"
            )

            // 3. با یک تأخیر کوتاه (اختیاری) کلمه صحیح را پخش کنید.
            kotlinx.coroutines.delay(50) // اگر مشکل ادامه داشت، این خط را فعال کنید

            speakWord(correctWord) // 🟢 پخش کلمه صحیح
        }
    }

    private fun shuffleWord(word: String): String {
        if (word.length <= 1) return word

        val chars = word.toCharArray()
        do {
            chars.shuffle()
        } while (String(chars) == word && word.length > 1)

        return String(chars)
    }

    fun userGuess(input: String) {
        _uiState.value = _uiState.value.copy(inputUserGuess = input)
    }

    fun checkGuessUser(): Boolean {
        val userInput = _uiState.value.inputUserGuess.trim()
        val correctWord = currentWordObject?.english ?: ""

        // اگر ورودی خالی بود، اصلاً به عنوان جواب درست قبول نکن
        if (userInput.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Please enter your guess")
            return false
        }

        // مقایسه دقیق (بدون حساسیت به حروف بزرگ و کوچک)
        return if (userInput.equals(correctWord, ignoreCase = true)) {
            submit() // فقط اگر کاملاً یکی بود
            true
        } else {
            skip() // اگر غلط بود یا اسکیپ شد، این تابع اجرا شود
            false
        }
    }

    fun submit() {
        val wordId = currentWordObject?.id ?: return

        // ابتدا آمار را در دیتابیس ثبت کن
        viewModelScope.launch {
            gameStateRepository.updateStats(wordId, true)
            if (currentWordObject?.isSkipped == true) {
                wordsRepository.updateSkipStatus(wordId, false)
            }
        }

        val updateScore = _uiState.value.score + SCORE_QUIZ

        if (usedWords.size >= WORD_COUNT_QUIZ) {
            _uiState.value = _uiState.value.copy(
                score = updateScore,
                isGameOver = true,
                isGuess = true,
                message = "Congratulations! Final score: $updateScore"
            )
        } else {
            // اول پیام موفقیت را نشان بده، بعد کلمه را عوض کن
            _uiState.value = _uiState.value.copy(
                score = updateScore,
                isGuess = true,
                inputUserGuess = "",
                message = "Correct! +$SCORE_QUIZ points"
            )
            wordRandom() // حالا کلمه بعدی
        }
    }

    fun skip() {
        val word = currentWordObject ?: return
        val wordId = word.id

        viewModelScope.launch {
            // ۱. چاپ آیدی کلمه برای اطمینان
            android.util.Log.d("QUIZ_SAVE", "Attempting to save WRONG for Word: ${word.english} with ID: $wordId")

            // ۲. انجام عملیات بروزرسانی
            gameStateRepository.updateStats(wordId, false)

            // ۳. وقفه کوتاه برای اطمینان از پایان تراکنش دیتابیس
            kotlinx.coroutines.delay(200)

            // ۴. خواندن مستقیم از دیتابیس بلافاصله بعد از ذخیره
            val checkData = gameStateRepository.getGameStateByWordId(wordId)
            if (checkData == null) {
                android.util.Log.e("QUIZ_SAVE", "FAILED! No record found in GameState for ID: $wordId")
            } else {
                android.util.Log.d("QUIZ_SAVE", "SUCCESS! DB now has -> Correct: ${checkData.correctAnswer}, Wrong: ${checkData.wrongAnswer}")
            }

            // ادامه کارهای دیگر...
            wordsRepository.updateSkipStatus(wordId, true)
            wordRandom()
        }
    }

    fun restartGame() {
        usedWords.clear()
        currentWordObject = null
        _uiState.value = QuizUiState(isLoading = true)

        viewModelScope.launch {
            kotlinx.coroutines.delay(300) // کمی تاخیر برای لود مجدد داده‌ها

            val words = allWords.value.wordList
            if (words.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                wordRandom()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentWord = "No words available",
                    message = "Please add some words first"
                )
            }
        }
    }
}