package com.example.mydictionary.ui.quiz

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.repository.WordStatsRepository
import com.example.mydictionary.data.repository.WordsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

class QuizViewModel(
    private val wordsRepository: WordsRepository,
    private val wordStatsRepository: WordStatsRepository
) : ViewModel(), OnInitListener {

    private var tts: TextToSpeech? = null
    var isTtsInitialized = false

    fun initializeTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.ENGLISH)
            isTtsInitialized = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
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

    fun speakCurrentCorrectWord() {
        currentWordObject?.english?.let { speakWord(it) }
    }

    var randomGenerator: (IntRange) -> Int = { it.random() }
    internal val usedWords: MutableSet<String> = mutableSetOf()
    private var currentWordObject: Word? = null

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    val allWords: StateFlow<WordListUiState> = wordsRepository.getAllWordsDictionary()
        .map { wordList ->
            val filteredWords = wordList.filter { !it.isDeleted }
            WordListUiState(filteredWords)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = WordListUiState()
        )

    init {
        viewModelScope.launch {
            allWords.collect { wordListState ->
                if (wordListState.wordList.isNotEmpty() && usedWords.isEmpty()) {
                    wordRandom()
                }
            }
        }
    }

    suspend fun wordRandom() {
        val words = allWords.value.wordList

        if (words.isEmpty()) {
            _uiState.value = _uiState.value.copy(currentWord = "No words available", isLoading = false)
            return
        }

        if (usedWords.size >= words.size || usedWords.size >= WORD_COUNT_QUIZ) {
            _uiState.value = _uiState.value.copy(isGameOver = true, isLoading = false)
            return
        }

        val availableWords = words.filter { !usedWords.contains(it.english) && !it.isDeleted }
        if (availableWords.isEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        val wordsWithState = availableWords.map { word ->
            val state = wordStatsRepository.getGameStateByWordId(word.id)
            Triple(word, state?.wrongAnswer ?: 0, state?.correctAnswer ?: 0)
        }

        val priority1 = wordsWithState.filter { (_, wrong, correct) ->
            wrong == 0 && correct == 0
        }.map { it.first }

        val priority2 = wordsWithState.filter { (_, wrong, correct) ->
            wrong > correct
        }.map { it.first }

        val priority3 = availableWords - priority1.toSet() - priority2.toSet()

        val weightedList = mutableListOf<Pair<Word, Int>>()

        if (priority1.isNotEmpty()) priority1.forEach { weightedList.add(it to 60) }
        if (priority2.isNotEmpty()) priority2.forEach { weightedList.add(it to 30) }
        if (priority3.isNotEmpty()) priority3.forEach { weightedList.add(it to 10) }

        val totalWeight = weightedList.sumOf { it.second }

        if (totalWeight == 0) {
            val fallbackWord = availableWords.random()
            setupSelectedWord(fallbackWord)
            return
        }

        val rnd = randomGenerator(1..totalWeight)
        var startLoop = 0
        var selectedWord: Word? = null

        for ((word, weight) in weightedList) {
            startLoop += weight
            if (rnd <= startLoop) {
                selectedWord = word
                break
            }
        }

        val finalWord = selectedWord ?: availableWords.firstOrNull()
        finalWord?.let { setupSelectedWord(it) }
    }

    private suspend fun setupSelectedWord(word: Word) {
        currentWordObject = word
        usedWords.add(word.english)
        val shuffled = shuffleWord(word.english)

        _uiState.value = _uiState.value.copy(
            currentWord = shuffled,
            currentWordCount = usedWords.size,
            inputUserGuess = "",
            isLoading = false,
            message = "Word ${usedWords.size} of $WORD_COUNT_QUIZ"
        )

        delay(50)
        speakWord(word.english)
    }

    fun shuffleWord(word: String): String {
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

        if (userInput.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Please enter your guess")
            return false
        }

        return if (userInput.equals(correctWord, ignoreCase = true)) {
            submit()
            true
        } else {
            skip()
            false
        }
    }

    fun submit() {
        val wordId = currentWordObject?.id ?: return

        viewModelScope.launch {
            // ۱. ثبت آمار پاسخ صحیح در دیتابیس (بدون هیچ کد مخرب اضافه)
            wordStatsRepository.updateStats(wordId, true)

            // ۲. اگر کلمه قبلاً در حالت اسکیپ/هشدار بود، وضعیت آن را ریست کن
            val currentStats = wordStatsRepository.getGameStateByWordId(wordId)
            if (currentStats?.isSkipped == true) {
                wordStatsRepository.updateSkipStatus(wordId, false)
            }
        }

        // ۳. محاسبه و به‌روزرسانی امتیاز کوئیز
        val updateScore = _uiState.value.score + SCORE_QUIZ

        if (usedWords.size >= WORD_COUNT_QUIZ) {
            _uiState.value = _uiState.value.copy(
                score = updateScore,
                isGameOver = true,
                isGuess = true,
                message = "Congratulations! Final score: $updateScore"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                score = updateScore,
                isGuess = true,
                inputUserGuess = "",
                message = "Correct! +$SCORE_QUIZ points"
            )
            // رفتن به کلمه تصادفی بعدی
            viewModelScope.launch { wordRandom() }
        }
    }

    fun skip() {
        val word = currentWordObject ?: return
        val wordId = word.id

        viewModelScope.launch {
            //  افزایش تعداد پاسخ‌های غلط در آمار بازی/کوئیز
            wordStatsRepository.updateStats(wordId, false)

            //  ارسال کلمه به لایتنر ۱ و تنظیم تاریخ مرور برای همین الان
            // این کار باعث می‌شود کلمه حتماً در کوئری لایتنر باکس بالا بیاید
            wordStatsRepository.updateLeitnerBox(wordId, 1)
            wordStatsRepository.updateNextReviewDate(wordId, System.currentTimeMillis())
            wordStatsRepository.updateSkipStatus(wordId, true) // فعال کردن علامت هشدار (کلمه رد شده)

            // ۳. رفتن به کلمه تصادفی بعدی در بازی
            wordRandom()
        }
    }

    fun restartGame() {
        usedWords.clear()
        currentWordObject = null
        _uiState.value = QuizUiState(isLoading = true)

        viewModelScope.launch {
            delay(300)
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