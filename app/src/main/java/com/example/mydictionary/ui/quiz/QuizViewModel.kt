package com.example.mydictionary.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
import com.example.mydictionary.data.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizUiState(
    val currentWord: String = "",
    var inputUserGuess: String = "",
    val currentWordCount: Int = 1,
    val score: Int = 0,
    val isGuess: Boolean = false,
    val isGameOver: Boolean = false
)

data class WordListUiState(val wordList: List<Word> = listOf())

private const val SCORE_QUIZ = 20
private const val WORD_COUNT_QUIZ = 10

class QuizViewModel(wordsRepository: WordsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var currentWord: String = ""
    private val usedWords: MutableSet<String> = mutableSetOf()

    val allWords: StateFlow<WordListUiState> = wordsRepository.getAllWordsDictionary()
        .map { WordListUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = WordListUiState()
        )

    init {
        // وقتی ViewModel ساخته می‌شود، کلمه اول را آماده کن
        viewModelScope.launch {
            allWords.collect { wordListState ->
                if (wordListState.wordList.isNotEmpty() && currentWord.isEmpty()) {
                    wordRandom()
                }
            }
        }
    }

    private fun wordRandom() {
        val words = allWords.value.wordList
        if (words.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                currentWord = "No words available",
                inputUserGuess = ""
            )
            return
        }

        // اگر همه کلمات استفاده شدند، مجموعه را پاک کن
        if (usedWords.size >= words.size) {
            usedWords.clear()
        }

        // کلمه تصادفی که قبلاً استفاده نشده باشد
        val availableWords = words.map { it.english }.filter { !usedWords.contains(it) }
        if (availableWords.isEmpty()) {
            usedWords.clear()
            wordRandom()
            return
        }

        val wordToUse = availableWords.random()
        usedWords.add(wordToUse)
        currentWord = wordToUse

        _uiState.value = _uiState.value.copy(
            currentWord = shuffleWord(currentWord),
            currentWordCount = usedWords.size,
            inputUserGuess = ""
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

    // بقیه متدها بدون تغییر...
    fun userGuess(input: String) {
        _uiState.value = _uiState.value.copy(inputUserGuess = input)
    }

    fun checkGuessUser(): Boolean {
        return if (_uiState.value.inputUserGuess.equals(currentWord, ignoreCase = true)) {
            submit()
            true
        } else {
            skip()
            false
        }
    }

    fun submit() {
        val updateScore = _uiState.value.score + SCORE_QUIZ
        if (usedWords.size >= WORD_COUNT_QUIZ) {
            _uiState.value = _uiState.value.copy(
                score = updateScore,
                isGameOver = true
            )
        } else {
            wordRandom()
            _uiState.value = _uiState.value.copy(
                score = updateScore,
                isGuess = true,
                inputUserGuess = ""
            )
        }
    }

    fun skip() {
        if (usedWords.size >= WORD_COUNT_QUIZ) {
            _uiState.value = _uiState.value.copy(isGameOver = true)
        } else {
            wordRandom()
            _uiState.value = _uiState.value.copy(
                isGuess = false,
                inputUserGuess = ""
            )
        }
    }
}
