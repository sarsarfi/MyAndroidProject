package com.example.mydictionary.ui.excelwords

import WordsRepository
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import org.apache.poi.ss.usermodel.WorkbookFactory


data class ExcelWordUiState(
    val words: List<Word> = emptyList()
)

class ExcelWordsViewModel(
    private val repository: WordsRepository ,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExcelWordUiState())
    val uiState: StateFlow<ExcelWordUiState> = _uiState.asStateFlow()

    fun readExcelFile(context: Context, uri: Uri) {
        viewModelScope.launch(ioDispatcher) {
           // _uiState.value = _uiState.value.copy(isLoading = true, loadError = null)

            runCatching {
                val tempList = mutableListOf<Word>()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    if (inputStream == null) throw IOException("Could not open input stream.")

                    //  خواندن فایل به عنوان XLSX/ZIP با استفاده از POI
                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)

                    for (row in sheet.drop(1)) { // drop first row (header)

                        val eng = row.getCell(0)?.toString()?.trim() ?: ""
                        val per = row.getCell(1)?.toString()?.trim() ?: ""

                        if (eng.isBlank() || per.isBlank() || eng == ".") continue

                        tempList.add(
                            Word(
                                english = eng,
                                persian = per
                            )
                        )
                    }
                }

                if (tempList.isNotEmpty()) {
                    repository.insertWords(tempList)
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

