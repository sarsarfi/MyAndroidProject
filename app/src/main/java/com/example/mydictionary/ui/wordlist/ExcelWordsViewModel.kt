package com.example.mydictionary.ui.wordlist

import WordsRepository
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
// ✅ ایمپورت های POI را نیاز دارید، اما چون قبلاً حذف کردیم،
// فعلاً فقط این یکی را اضافه کنید و در مرحله بعد وابستگی‌ها را نصب می‌کنیم.
import org.apache.poi.ss.usermodel.WorkbookFactory

data class ExcelWordUiState(
    val words: List<Word> = emptyList(),
    val isLoadedSuccessfully: Boolean = false,
    val isLoading: Boolean = false,
    val loadError: String? = null
)

class ExcelWordsViewModel(
    private val repository: WordsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExcelWordUiState())
    val uiState: StateFlow<ExcelWordUiState> = _uiState.asStateFlow()

    fun readExcelFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, loadError = null)

            runCatching {
                val tempList = mutableListOf<Word>()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    if (inputStream == null) throw IOException("Could not open input stream.")

                    // ✅ خواندن فایل به عنوان XLSX/ZIP با استفاده از POI
                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)

                    for (row in sheet.drop(1)) { // شروع از ردیف ۱ برای رد شدن از هدر
                        // استفاده از toString() برای ساده‌سازی خواندن داده‌ها بدون توجه به نوع Cell
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
                    words = words,
                    isLoadedSuccessfully = words.isNotEmpty(),
                    isLoading = false,
                    loadError = null
                )
                Log.d("EXCEL_SUCCESS", "Successfully loaded ${words.size} words.")
            }.onFailure { e ->
                Log.e("EXCEL_DEBUG", "FINAL ERROR reading XLSX file: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadedSuccessfully = false,
                    loadError = "خطا در پردازش فایل XLSX: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }
}