package com.example.mydictionary.ui.categorywords

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydictionary.DictionaryTopAppBar
import com.example.mydictionary.R
import com.example.mydictionary.data.entities.Category
import com.example.mydictionary.ui.AppViewModelProvider
import com.example.mydictionary.ui.adaptive.DeviceType
import com.example.mydictionary.ui.adaptive.rememberDeviceType
import com.example.mydictionary.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object CategoryScreenDestination : NavigationDestination {
    override val route: String = "CategoryScreen"
    override val titleRes: Int = R.string.word_list_in_category
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navigateBack: () -> Unit,
    navigateToCategoryWordsScreen: (Int) -> Unit,
    viewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val deviceType = rememberDeviceType()

    CategoryScreenContent(
        uiState = uiState,
        deviceType = deviceType,
        navigateBack = navigateBack,
        navigateToCategoryWordsScreen = navigateToCategoryWordsScreen,
        onUpdateCategoryName = viewModel::update,
        onSaveCategory = viewModel::saveCategory,
        onDeleteCategory = { category ->
            viewModel.viewModelScope.launch {
                viewModel.deleteCategory(category)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryScreenContent(
    uiState: CategoryUiState,
    deviceType: DeviceType,
    navigateBack: () -> Unit,
    navigateToCategoryWordsScreen: (Int) -> Unit,
    onUpdateCategoryName: (AddCategoryName) -> Unit,
    onSaveCategory: (onSuccess: () -> Unit) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DictionaryTopAppBar(
                title = stringResource(CategoryScreenDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onUpdateCategoryName(AddCategoryName())
                    showDialog = true
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(text = stringResource(R.string.create_new_category))
            }
        },
        modifier = modifier
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            CategoryList(
                gridCards = uiState.categoriesList,
                onCategoryClick = { selectedCategory ->
                    navigateToCategoryWordsScreen(selectedCategory.id)
                },
                innerPadding = paddingValues,
                deviceType = deviceType,
                onDeleteClick = onDeleteCategory
            )
        }

        if (showDialog) {
            AddCategoryDialog(
                uiState = uiState,
                onValueChange = onUpdateCategoryName,
                onDismissRequest = { showDialog = false },
                onSaveClick = {
                    onSaveCategory {
                        showDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun AddCategoryDialog(
    uiState: CategoryUiState,
    onValueChange: (AddCategoryName) -> Unit,
    onDismissRequest: () -> Unit,
    onSaveClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.create_new_category),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = uiState.addCategoryName.nameCategory,
                    onValueChange = { newValue ->
                        onValueChange(AddCategoryName(nameCategory = newValue))
                    },
                    label = { Text(stringResource(R.string.category_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = onSaveClick,
                        enabled = uiState.isValid,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(stringResource(R.string.save) )

                    }
                }
            }
        }
    }
}

@Composable
fun CategoryList(
    gridCards: List<Category>,
    onCategoryClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    innerPadding: PaddingValues,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = when (deviceType) {
        DeviceType.Phone -> 12.dp
        DeviceType.Foldable -> 16.dp
        DeviceType.Tablet -> 24.dp
    }
    val verticalPadding = when (deviceType) {
        DeviceType.Phone -> 12.dp
        DeviceType.Foldable -> 16.dp
        DeviceType.Tablet -> 24.dp
    }
    val cardSpacing = when (deviceType) {
        DeviceType.Phone -> 12.dp
        DeviceType.Foldable -> 16.dp
        DeviceType.Tablet -> 20.dp
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = horizontalPadding),
        contentPadding = PaddingValues(vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(cardSpacing)
    ) {
        items(gridCards.chunked(2)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                rowItems.forEach { card ->
                    AdaptiveCategoryCard(
                        category = card,
                        onClick = { onCategoryClick(card) },
                        onDeleteClick = { onDeleteClick(card) }, //
                        deviceType = deviceType,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AdaptiveCategoryCard(
    category: Category,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    deviceType: DeviceType,
    modifier: Modifier = Modifier
) {
    val cardHeight = when (deviceType) {
        DeviceType.Phone -> 110.dp
        DeviceType.Foldable -> 130.dp
        DeviceType.Tablet -> 150.dp
    }
    val titleFontSize = when (deviceType) {
        DeviceType.Phone -> 15.sp
        DeviceType.Foldable -> 17.sp
        DeviceType.Tablet -> 19.sp
    }
    val iconSize = when (deviceType) {
        DeviceType.Phone -> 70.dp
        DeviceType.Foldable -> 90.dp
        DeviceType.Tablet -> 110.dp
    }
    val elevation = when (deviceType) {
        DeviceType.Phone -> 2.dp
        DeviceType.Foldable -> 3.dp
        DeviceType.Tablet -> 4.dp
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.categories),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp, end = 4.dp),
                alpha = 0.08f,
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 52.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = category.name,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp, end = 4.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف دسته",
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640, name = "Phone")
@Preview(showBackground = true, widthDp = 600, heightDp = 800, name = "Foldable")
@Preview(showBackground = true, widthDp = 840, heightDp = 1000, name = "Tablet")
@Composable
fun CategoryScreenPreview() {
    val mockCategories = listOf(
        Category(id = 1, name = "لغات عمومی"),
        Category(id = 2, name = "اصطلاحات مهندسی"),
        Category(id = 3, name = "سفر و مهاجرت")
    )

    val mockUiState = CategoryUiState(
        categoriesList = mockCategories,
        addCategoryName = AddCategoryName(nameCategory = "دسته جدید"),
        isValid = true
    )

    com.example.mydictionary.ui.theme.MyDictionaryTheme {
        CategoryScreenContent(
            uiState = mockUiState,
            deviceType = DeviceType.Phone,
            navigateBack = {},
            navigateToCategoryWordsScreen = {},
            onUpdateCategoryName = {},
            onSaveCategory = {},
            onDeleteCategory = {}
        )
    }
}