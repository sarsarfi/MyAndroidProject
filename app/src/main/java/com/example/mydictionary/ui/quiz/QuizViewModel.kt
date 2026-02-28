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
import com.example.mydictionary.data.GameStateRepository
import kotlinx.coroutines.delay
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

    fun speakCurrentCorrectWord() {
        currentWordObject?.english?.let { word ->
            speakWord(word)
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
            _uiState.value = _uiState.value.copy(
                currentWord = "No words available",
                isLoading = false
            )
            return
        }

        if (usedWords.size >= words.size || usedWords.size >= WORD_COUNT_QUIZ) {
            _uiState.value = _uiState.value.copy(isGameOver = true, isLoading = false)
            return
        }

        val availableWords = words.filter { !usedWords.contains(it.english) && !it.isDeleted }
        if (availableWords.isEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        val weightWords = availableWords.map { word ->
            val state = gameStateRepository.getGameStateByWordId(word.id)
            val countWrong = state?.wrongAnswer ?: 0
            Pair(word, countWrong + 1)
        }

        val totalWeight = weightWords.sumOf { it.second }
        val rnd = randomGenerator(1..totalWeight)

        var startLoop = 0
        var selectedWord: Word? = null

        for ((word, weight) in weightWords) {
            startLoop += weight
            if (rnd <= startLoop) {
                selectedWord = word
                currentWordObject = word
                break
            }
        }

        selectedWord?.let {
            usedWords.add(it.english)
            val shuffled = shuffleWord(it.english)

            _uiState.value = _uiState.value.copy(
                currentWord = shuffled,
                currentWordCount = usedWords.size,
                inputUserGuess = "",
                isLoading = false,
                message = "Word ${usedWords.size} of $WORD_COUNT_QUIZ"
            )

            delay(50)
            speakWord(it.english)
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
            _uiState.value = _uiState.value.copy(
                score = updateScore,
                isGuess = true,
                inputUserGuess = "",
                message = "Correct! +$SCORE_QUIZ points"
            )
            viewModelScope.launch {  wordRandom() }
        }
    }

    fun skip() {
        val word = currentWordObject ?: return
        val wordId = word.id

        viewModelScope.launch {

            // update state (the word)
            gameStateRepository.updateStats(wordId, false)

            kotlinx.coroutines.delay(200)

            // ۴. خواندن مستقیم از دیتابیس بلافاصله بعد از ذخیره
            val checkData = gameStateRepository.getGameStateByWordId(wordId)
            if (checkData == null) {
                android.util.Log.e("QUIZ_SAVE", "FAILED! No record found in GameState for ID: $wordId")
            } else {
                android.util.Log.d("QUIZ_SAVE", "SUCCESS! DB now has -> Correct: ${checkData.correctAnswer}, Wrong: ${checkData.wrongAnswer}")
            }

            wordsRepository.updateSkipStatus(wordId, true)
            wordRandom()
        }
    }

    fun restartGame() {
        usedWords.clear()
        currentWordObject = null
        _uiState.value = QuizUiState(isLoading = true)

        viewModelScope.launch {
            kotlinx.coroutines.delay(300) // delay to load data

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