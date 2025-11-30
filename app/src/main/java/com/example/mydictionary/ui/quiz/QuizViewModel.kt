package com.example.mydictionary.ui.quiz

import WordsRepository
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

class QuizViewModel(private val wordsRepository: WordsRepository) : ViewModel() {
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

        _uiState.value = _uiState.value.copy(
            currentWord = shuffleWord(wordToUseObject.english),
            currentWordCount = usedWords.size,
            inputUserGuess = "",
            isGuess = false,
//            message = "Word ${usedWords.size} of $WORD_COUNT_QUIZ"
        )
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
            _uiState.value = _uiState.value.copy(
                message = "Please enter your guess"
            )
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
        currentWordObject?.let { word ->
            viewModelScope.launch {
                // اگر کلمه قبلاً اسکیپ شده بود، از حالت اسکیپ خارجش کن
                if (word.isSkipped) {
                    wordsRepository.updateSkipStatus(word.id, false)
                }
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
            wordRandom()
            _uiState.value = _uiState.value.copy(
                score = updateScore,
                isGuess = true,
                inputUserGuess = "",
                message = "Correct! +$SCORE_QUIZ points"
            )
        }
    }

    fun skip() {
        currentWordObject?.let { word ->
            viewModelScope.launch {
                // ✅ اصلاح: فقط وضعیت اسکیپ را آپدیت کن
                wordsRepository.updateSkipStatus(word.id, true)

                // ✅ همچنین کلمه را به جعبه اول لایتنر برگردان
                wordsRepository.updateLeitnerBox(word.id, 1)
                wordsRepository.updateNextReviewDate(word.id, System.currentTimeMillis())
            }
        }

        if (usedWords.size >= WORD_COUNT_QUIZ) {
            _uiState.value = _uiState.value.copy(
                isGameOver = true,
                message = "Game over! Final score: ${_uiState.value.score}"
            )
        } else {
            wordRandom()
            _uiState.value = _uiState.value.copy(
                isGuess = false,
                inputUserGuess = "",
                message = "Word skipped - will be reviewed in Leitner box"
            )
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