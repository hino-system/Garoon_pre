package com.example.garoon_pre.feature.board.presentation.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.common.data.calendarFromApiDateTime
import com.example.core.common.data.currentCalendar
import com.example.core.common.data.formatApiDateTime
import com.example.garoon_pre.feature.board.domain.model.BoardCategory
import com.example.garoon_pre.feature.board.presentation.common.formatBoardDateTime
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest

private val DepartmentOptions = listOf(
    "",
    "営業部",
    "総務部",
    "人事部",
    "情報システム部"
)

@Composable
fun CreateBoardPostRoute(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateBoardPostViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.saved.collectLatest { onSaved() }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BoardPostEditorScreen(
        snackbarHostState = snackbarHostState,
        screenTitle = "掲示を作成",
        actionLabel = "投稿",
        categories = viewModel.categories,
        selectedCategoryId = viewModel.selectedCategoryId,
        categoryNameLocked = null,
        targetDepartment1 = viewModel.targetDepartment1,
        showDepartmentSelector = viewModel.selectedCategoryId == "cat-department",
        title = viewModel.title,
        body = viewModel.body,
        startAt = viewModel.startAt,
        endAt = viewModel.endAt,
        allowComments = viewModel.allowComments,
        loading = viewModel.loading,
        onCategoryChanged = viewModel::onCategoryChanged,
        onTargetDepartment1Changed = viewModel::onTargetDepartment1Changed,
        onTitleChanged = viewModel::onTitleChanged,
        onBodyChanged = viewModel::onBodyChanged,
        onStartAtChanged = viewModel::onStartAtChanged,
        onEndAtChanged = viewModel::onEndAtChanged,
        onAllowCommentsChanged = viewModel::onAllowCommentsChanged,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@Composable
fun EditBoardPostRoute(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditBoardPostViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.saved.collectLatest { onSaved() }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BoardPostEditorScreen(
        snackbarHostState = snackbarHostState,
        screenTitle = "掲示を編集",
        actionLabel = "保存",
        categories = emptyList(),
        selectedCategoryId = "",
        categoryNameLocked = viewModel.categoryName,
        targetDepartment1 = viewModel.targetDepartment1,
        showDepartmentSelector = viewModel.isDepartmentCategory,
        title = viewModel.title,
        body = viewModel.body,
        startAt = viewModel.startAt,
        endAt = viewModel.endAt,
        allowComments = viewModel.allowComments,
        loading = viewModel.loading,
        onCategoryChanged = {},
        onTargetDepartment1Changed = viewModel::onTargetDepartment1Changed,
        onTitleChanged = viewModel::onTitleChanged,
        onBodyChanged = viewModel::onBodyChanged,
        onStartAtChanged = viewModel::onStartAtChanged,
        onEndAtChanged = viewModel::onEndAtChanged,
        onAllowCommentsChanged = viewModel::onAllowCommentsChanged,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardPostEditorScreen(
    snackbarHostState: SnackbarHostState,
    screenTitle: String,
    actionLabel: String,
    categories: List<BoardCategory>,
    selectedCategoryId: String,
    categoryNameLocked: String?,
    targetDepartment1: String,
    showDepartmentSelector: Boolean,
    title: String,
    body: String,
    startAt: String,
    endAt: String,
    allowComments: Boolean,
    loading: Boolean,
    onCategoryChanged: (String) -> Unit,
    onTargetDepartment1Changed: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onStartAtChanged: (String) -> Unit,
    onEndAtChanged: (String) -> Unit,
    onAllowCommentsChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val openDateTimePicker: (String, (String) -> Unit) -> Unit = { initialValue, onSelected ->
        val initialCalendar = calendarFromApiDateTime(initialValue) ?: currentCalendar()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = (initialCalendar.clone() as Calendar).apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedCalendar.apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onSelected(formatApiDateTime(selectedCalendar))
                    },
                    initialCalendar.get(Calendar.HOUR_OF_DAY),
                    initialCalendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val selectedCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name.orEmpty()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("キャンセル")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = !loading
                    ) {
                        Text(actionLabel)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (categoryNameLocked != null) {
                OutlinedTextField(
                    value = categoryNameLocked,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("カテゴリー") },
                    readOnly = true,
                    singleLine = true
                )
            } else {
                CategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    selectedCategoryName = selectedCategoryName,
                    onSelected = onCategoryChanged
                )
            }

            if (showDepartmentSelector) {
                DepartmentDropdown(
                    selectedDepartment = targetDepartment1,
                    onSelected = onTargetDepartment1Changed
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("タイトル") },
                singleLine = true
            )

            OutlinedTextField(
                value = body,
                onValueChange = onBodyChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("本文") },
                minLines = 8
            )

            DateTimeField(
                label = "掲載開始",
                value = formatBoardDateTime(startAt),
                onClick = { openDateTimePicker(startAt, onStartAtChanged) }
            )

            DateTimeField(
                label = "掲載終了",
                value = formatBoardDateTime(endAt),
                onClick = { openDateTimePicker(endAt, onEndAtChanged) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("コメントを許可")
                Switch(
                    checked = allowComments,
                    onCheckedChange = onAllowCommentsChanged
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<BoardCategory>,
    selectedCategoryId: String,
    selectedCategoryName: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategoryName,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text("カテゴリー") },
            readOnly = true,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepartmentDropdown(
    selectedDepartment: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selectedDepartment.ifBlank { "自部門" }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text("対象部門（空欄なら自部門）") },
            readOnly = true,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DepartmentOptions.forEach { department ->
                DropdownMenuItem(
                    text = { Text(if (department.isBlank()) "自部門" else department) },
                    onClick = {
                        onSelected(department)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateTimeField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        label = { Text(label) },
        readOnly = true,
        singleLine = true
    )
}