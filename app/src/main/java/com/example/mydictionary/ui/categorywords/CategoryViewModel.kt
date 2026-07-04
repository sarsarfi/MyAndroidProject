package com.example.mydictionary.ui.categorywords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydictionary.data.entities.Category
import com.example.mydictionary.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categoriesList: List<Category> = emptyList(),
    val addCategoryName: AddCategoryName = AddCategoryName(),
    val isValid: Boolean = false
)

data class AddCategoryName(val nameCategory: String = "")

fun AddCategoryName.toCategory(): Category = Category(
    name = nameCategory.trim()
)

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        //  گوش دادن به دیتابیس به صورت زنده و ریختن آن در UiState
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { list ->
                _uiState.update { it.copy(categoriesList = list) }
            }
        }
    }

    /**
     * update
     */
    fun update(addCategoryName: AddCategoryName) {
        _uiState.update { currentState ->
            currentState.copy(
                addCategoryName = addCategoryName,
                isValid = isValidInput(addCategoryName)
            )
        }
    }

    fun isValidInput(details: AddCategoryName): Boolean = with(details) {
        nameCategory.trim().isNotBlank()
    }

    fun saveCategory(onSuccess: () -> Unit = {}) {
        val currentState = _uiState.value
        if (!currentState.isValid) return

        viewModelScope.launch {
            categoryRepository.insertCategory(currentState.addCategoryName.toCategory())
            onSuccess()
        }
    }

    suspend fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }
}