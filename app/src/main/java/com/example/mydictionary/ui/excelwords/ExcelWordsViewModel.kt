package com.example.mydictionary.ui.excelwords

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.entities.Word
import com.example.mydictionary.data.repository.WordCategoryCrossRefRepository
import com.example.mydictionary.data.repository.WordsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.IOException

data class ExcelWordUiState(
    val words: List<Word> = emptyList()
)

class ExcelWordsViewModel(
    private val repository: WordsRepository,
    private val wordCategoryCrossRefRepository: WordCategoryCrossRefRepository,
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    //  استخراج آیدی دسته‌بندی فرستاده شده از نویگیشن (اگر از داخل دسته‌ای نیامده باشد، پیش‌فرض ۱- می‌گیرد)
    private val categoryId: Int = savedStateHandle.get<Int>("categoryId") ?: -1

    private val _uiState = MutableStateFlow(ExcelWordUiState())
    val uiState: StateFlow<ExcelWordUiState> = _uiState.asStateFlow()

    fun readExcelFile(context: Context, uri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val tempList = mutableListOf<Word>()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    if (inputStream == null) throw IOException("Could not open input stream.")

                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)

                    for (row in sheet.drop(1)) { // drop first row (header)
                        val eng = row.getCell(0)?.toString()?.trim() ?: ""
                        val per = row.getCell(1)?.toString()?.trim() ?: ""

                        if (eng.isBlank() || per.isBlank() || eng == ".") continue
                        val generatedUrl = "https://www.google.com/search?tbm=isch&q=$eng"
                        tempList.add(
                            Word(
                                english = eng.lowercase(), // برای یکدستی داده‌ها بهتر است لغات انگلیسی lowercase شوند
                                persian = per ,
                                searchUrl = generatedUrl
                            )
                        )
                    }
                }

                if (tempList.isNotEmpty()) {
                    //  اگر از داخل دسته‌بندی اقدام به ایمپورت اکسل کرده است
                    if (categoryId != -1) {
                        wordCategoryCrossRefRepository.insertExcelWordsToCategory(
                            words = tempList,
                            categoryId = categoryId
                        )
                    } else {
                        //  اگر از منوی عمومی اقدام به ایمپورت کرده است
                        repository.insertWords(tempList)
                    }
                }

                tempList.toList()

            }.onSuccess { words ->
                _uiState.value = _uiState.value.copy(
                    words = words
                )
            }
        }
    }
}