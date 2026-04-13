package com.example.garoon_pre.feature.availability.presentation

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.garoon_pre.core.model.user.GaroonUser
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import com.example.core.common.data.isToday
import com.example.core.common.data.minuteOfDay
import com.example.core.common.data.toDayOfMonthLabel
import com.example.core.common.data.toDayOfWeekLabel
import com.example.core.common.data.toMonthLabel
import kotlin.math.abs

private const val CALENDAR_START_HOUR = 8
private const val CALENDAR_END_HOUR = 21

private val GridLineColor = Color(0xFFD8E0EB)
private val HeaderBg = Color(0xFFF7FAFF)
private val SelectedDayColor = Color(0xFF4A90E2)
private val TodayOutline = Color(0xFF9CC2F0)
private val BusyShadeColor = Color(0x18000000)

private data class UserColorScheme(
    val fill: Color,
    val border: Color,
    val chip: Color
)

private val UserPalette = listOf(
    UserColorScheme(Color(0xFFFFF4BF), Color(0xFFE6C454), Color(0xFFF8E594)),
    UserColorScheme(Color(0xFFE4F0FF), Color(0xFF8AB6F3), Color(0xFFC8DEF9)),
    UserColorScheme(Color(0xFFEFE5FF), Color(0xFFB995E5), Color(0xFFD9C2F6)),
    UserColorScheme(Color(0xFFFFE3EA), Color(0xFFEC9BB0), Color(0xFFF7C7D4)),
    UserColorScheme(Color(0xFFE8F7EA), Color(0xFF8CCB96), Color(0xFFCDEED2)),
    UserColorScheme(Color(0xFFFFEEE0), Color(0xFFE3A45C), Color(0xFFF7D5AF))
)

@Composable
fun AvailabilityRoute(
    onBack: () -> Unit,
    onOpenDetail: (String, String, String) -> Unit,
    viewModel: AvailabilityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AvailabilityScreen(
        uiState = uiState,
        onBack = onBack,
        onPreviousWeek = { viewModel.moveWeek(-1) },
        onNextWeek = { viewModel.moveWeek(1) },
        onToday = viewModel::jumpToToday,
        onSelectDate = viewModel::selectDate,
        onAddUser = viewModel::addUser,
        onRemoveUser = viewModel::removeUser,
        onItemClick = { schedule ->
            onOpenDetail(
                Uri.encode(schedule.id),
                Uri.encode(schedule.startAt),
                Uri.encode(schedule.endAt)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    uiState: AvailabilityUiState,
    onBack: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
    onSelectDate: (String) -> Unit,
    onAddUser: (String) -> Unit,
    onRemoveUser: (String) -> Unit,
    onItemClick: (Schedule) -> Unit
) {
    val userColors = remember(uiState.users) {
        buildUserColorMap(uiState.users)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("空き時間の確認") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("戻る")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F7FC))
                .padding(padding)
        ) {
            if (uiState.loading && uiState.itemsByDate.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    WeekToolbar(
                        monthLabel = toMonthLabel(uiState.selectedDate),
                        onPreviousWeek = onPreviousWeek,
                        onToday = onToday,
                        onNextWeek = onNextWeek
                    )

                    UserSelectionSection(
                        users = uiState.users,
                        selectedUserIds = uiState.selectedUserIds,
                        userColors = userColors,
                        onAddUser = onAddUser,
                        onRemoveUser = onRemoveUser
                    )

                    Text(
                        text = "網掛け: 選択したユーザーのうち誰か1人でも予定がある時間",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5F6F86),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (uiState.selectedUserIds.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("表示したいユーザーを選択してください。")
                            }
                        }
                    } else {
                        AvailabilityWeekCalendar(
                            weekDates = uiState.weekDates,
                            selectedDate = uiState.selectedDate,
                            itemsByDate = uiState.itemsByDate,
                            userColors = userColors,
                            onSelectDate = onSelectDate,
                            onItemClick = onItemClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (uiState.loading && uiState.itemsByDate.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun WeekToolbar(
    monthLabel: String,
    onPreviousWeek: () -> Unit,
    onToday: () -> Unit,
    onNextWeek: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = onPreviousWeek,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("◀")
            }
            TextButton(onClick = onToday, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("今日")
            }
            TextButton(onClick = onNextWeek, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("▶")
            }
        }
    }
}

@Composable
private fun UserSelectionSection(
    users: List<GaroonUser>,
    selectedUserIds: List<String>,
    userColors: Map<String, UserColorScheme>,
    onAddUser: (String) -> Unit,
    onRemoveUser: (String) -> Unit
) {
    var showUserPicker by rememberSaveable { mutableStateOf(false) }

    val selectedUsers = remember(users, selectedUserIds) {
        users.filter { it.id in selectedUserIds }
    }
    val addableUsers = remember(users, selectedUserIds) {
        users.filterNot { it.id in selectedUserIds }
    }

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "表示するユーザー",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            selectedUsers.forEach { user ->
                val scheme = userColors[user.id] ?: fallbackColorScheme(user.id)

                InputChip(
                    selected = true,
                    onClick = { onRemoveUser(user.id) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(scheme.border)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${user.label} ×")
                        }
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = scheme.chip,
                        selectedLabelColor = Color(0xFF253447)
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        enabled = true,
                        selected = true,
                        borderColor = scheme.border,
                        selectedBorderColor = scheme.border
                    )
                )
            }

            AssistChip(
                onClick = { showUserPicker = true },
                label = { Text("＋") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.White,
                    labelColor = Color(0xFF253447)
                ),
                border = BorderStroke(1.dp, Color(0xFFB8C5D6))
            )
        }

        if (selectedUsers.isEmpty()) {
            Text(
                text = "＋ ボタンから表示するユーザーを追加してください。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5F6F86),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (showUserPicker) {
            UserPickerDialog(
                users = addableUsers,
                onDismiss = { showUserPicker = false },
                onSelectUser = { userId ->
                    onAddUser(userId)
                    showUserPicker = false
                }
            )
        }
    }
}

@Composable
private fun AvailabilityWeekCalendar(
    weekDates: List<String>,
    selectedDate: String,
    itemsByDate: Map<String, List<Schedule>>,
    userColors: Map<String, UserColorScheme>,
    onSelectDate: (String) -> Unit,
    onItemClick: (Schedule) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val hourHeight = 64.dp
    val timeColumnWidth = 40.dp

    Card(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            val containerWidth = this@BoxWithConstraints.maxWidth
            val dayCount = weekDates.size.coerceAtLeast(1)
            val dayWidth = (containerWidth - timeColumnWidth) / dayCount
            val gridHeight = hourHeight * (CALENDAR_END_HOUR - CALENDAR_START_HOUR)

            val placementsByDate = remember(weekDates, itemsByDate) {
                weekDates.associateWith { date ->
                    buildCalendarPlacements(itemsByDate[date].orEmpty(), hourHeight)
                }
            }
            val busyRangesByDate = remember(weekDates, itemsByDate) {
                weekDates.associateWith { date ->
                    mergeBusyRanges(itemsByDate[date].orEmpty())
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                WeekHeaderRow(
                    weekDates = weekDates,
                    selectedDate = selectedDate,
                    timeColumnWidth = timeColumnWidth,
                    dayColumnWidth = dayWidth,
                    onSelectDate = onSelectDate
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            for (hour in CALENDAR_START_HOUR until CALENDAR_END_HOUR) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(hourHeight)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(timeColumnWidth)
                                            .height(hourHeight)
                                            .border(0.5.dp, GridLineColor),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        Text(
                                            text = "%02d".format(hour),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF7A8A9D),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    repeat(dayCount) {
                                        Box(
                                            modifier = Modifier
                                                .width(dayWidth)
                                                .height(hourHeight)
                                                .border(0.5.dp, GridLineColor)
                                        )
                                    }
                                }
                            }
                        }

                        weekDates.forEachIndexed { dayIndex, date ->
                            busyRangesByDate[date].orEmpty().forEach { range ->
                                Box(
                                    modifier = Modifier
                                        .width(dayWidth)
                                        .height(hourHeight * (range.durationMinutes / 60f))
                                        .absoluteOffset(
                                            x = timeColumnWidth + (dayWidth * dayIndex),
                                            y = hourHeight * ((range.startMinute - (CALENDAR_START_HOUR * 60)) / 60f)
                                        )
                                        .background(BusyShadeColor)
                                )
                            }
                        }

                        weekDates.forEachIndexed { dayIndex, date ->
                            placementsByDate[date].orEmpty().forEach { placement ->
                                val eventWidth = (dayWidth / placement.totalColumns) - 4.dp
                                val eventOffsetX =
                                    timeColumnWidth +
                                            (dayWidth * dayIndex) +
                                            ((dayWidth / placement.totalColumns) * placement.column) + 2.dp

                                AvailabilityEventCard(
                                    schedule = placement.schedule,
                                    colorScheme = placement.schedule.ownerUserId
                                        ?.let { userColors[it] }
                                        ?: fallbackColorScheme(
                                            placement.schedule.ownerUserId
                                                ?: placement.schedule.organizerName
                                        ),
                                    modifier = Modifier
                                        .width(eventWidth)
                                        .height(placement.heightDp.coerceAtLeast(36.dp))
                                        .absoluteOffset(
                                            x = eventOffsetX,
                                            y = placement.topDp
                                        )
                                        .clickable { onItemClick(placement.schedule) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekHeaderRow(
    weekDates: List<String>,
    selectedDate: String,
    timeColumnWidth: Dp,
    dayColumnWidth: Dp,
    onSelectDate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .border(0.5.dp, GridLineColor)
    ) {
        Box(
            modifier = Modifier
                .width(timeColumnWidth)
                .height(62.dp)
        )

        weekDates.forEach { date ->
            val isSelected = date == selectedDate
            val weekLabel = toDayOfWeekLabel(date)
            val dayNumber = toDayOfMonthLabel(date)
            val weekColor = when (weekLabel) {
                "日" -> Color(0xFFD65A5A)
                "土" -> Color(0xFF4A90E2)
                else -> Color(0xFF5F6F86)
            }

            Column(
                modifier = Modifier
                    .width(dayColumnWidth)
                    .height(62.dp)
                    .clickable { onSelectDate(date) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = weekLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = weekColor
                )

                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> SelectedDayColor
                                isToday(date) -> Color.White
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (isToday(date) && !isSelected) 1.dp else 0.dp,
                            color = TodayOutline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else Color(0xFF1D2A3A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailabilityEventCard(
    schedule: Schedule,
    colorScheme: UserColorScheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorScheme.fill)
            .border(1.dp, colorScheme.border, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = schedule.title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF253447),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = schedule.organizerName,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4E5D70),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPickerDialog(
    users: List<GaroonUser>,
    onDismiss: () -> Unit,
    onSelectUser: (String) -> Unit
) {
    val departmentOptions = remember(users) {
        buildDepartmentOptions(users)
    }

    var selectedDepartment by remember(departmentOptions) {
        mutableStateOf(departmentOptions.firstOrNull().orEmpty())
    }

    val sectionOptions = remember(users, selectedDepartment) {
        buildSectionOptions(users, selectedDepartment)
    }

    var selectedSection by remember(selectedDepartment, sectionOptions) {
        mutableStateOf(sectionOptions.firstOrNull().orEmpty())
    }

    val filteredUsers = remember(users, selectedDepartment, selectedSection) {
        filterUsersForPicker(
            users = users,
            selectedDepartment = selectedDepartment,
            selectedSection = selectedSection
        )
    }

    val groupedUsers = remember(filteredUsers) {
        listOf("社長", "部長", "課長", "一般")
            .map { position ->
                position to filteredUsers.filter { user ->
                    normalizedPosition(user.position) == position
                }
            }
            .filter { (_, members) -> members.isNotEmpty() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("表示するユーザーを追加")
        },
        text = {
            if (users.isEmpty()) {
                Text("追加できるユーザーはいません。")
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DepartmentDropdown(
                        label = "部",
                        selectedValue = selectedDepartment,
                        options = departmentOptions,
                        onSelected = { selectedDepartment = it }
                    )

                    DepartmentDropdown(
                        label = "課",
                        selectedValue = selectedSection,
                        options = sectionOptions,
                        onSelected = { selectedSection = it }
                    )

                    if (groupedUsers.isEmpty()) {
                        Text(
                            text = "該当するユーザーがいません。",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5F6F86)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            groupedUsers.forEach { (position, members) ->
                                item(key = "header-$position") {
                                    Text(
                                        text = position,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF253447)
                                    )
                                }

                                items(
                                    items = members,
                                    key = { it.id }
                                ) { user ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectUser(user.id) }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = user.label,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            val detail = buildUserPickerDetail(user)
                                            if (detail.isNotBlank()) {
                                                Text(
                                                    text = detail,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF5F6F86)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepartmentDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun buildDepartmentOptions(users: List<GaroonUser>): List<String> {
    val hasExecutiveUsers = users.any { user ->
        normalizedPosition(user.position) == "社長" || user.department1.isNullOrBlank()
    }

    val departments = users
        .mapNotNull { user -> user.department1?.takeIf { it.isNotBlank() } }
        .distinct()
        .sorted()

    return buildList {
        if (hasExecutiveUsers) {
            add(EXECUTIVE_DEPARTMENT_LABEL)
        }
        addAll(departments)
    }
}

private fun buildSectionOptions(
    users: List<GaroonUser>,
    selectedDepartment: String
): List<String> {
    if (selectedDepartment.isBlank()) return emptyList()

    if (selectedDepartment == EXECUTIVE_DEPARTMENT_LABEL) {
        return listOf(ALL_SECTIONS_LABEL)
    }

    val sections = users
        .filter { user -> user.department1 == selectedDepartment }
        .mapNotNull { user -> user.department2?.takeIf { it.isNotBlank() } }
        .distinct()
        .sorted()

    return buildList {
        add(ALL_SECTIONS_LABEL)
        addAll(sections)
    }
}

private fun filterUsersForPicker(
    users: List<GaroonUser>,
    selectedDepartment: String,
    selectedSection: String
): List<GaroonUser> {
    val filtered = when {
        selectedDepartment.isBlank() -> emptyList()

        selectedDepartment == EXECUTIVE_DEPARTMENT_LABEL -> {
            users.filter { user ->
                normalizedPosition(user.position) == "社長" || user.department1.isNullOrBlank()
            }
        }

        selectedSection == ALL_SECTIONS_LABEL || selectedSection.isBlank() -> {
            users.filter { user ->
                user.department1 == selectedDepartment
            }
        }

        else -> {
            users.filter { user ->
                when {
                    user.department1 != selectedDepartment -> false
                    normalizedPosition(user.position) == "部長" -> true
                    else -> user.department2 == selectedSection
                }
            }
        }
    }

    return filtered.sortedWith(
        compareBy<GaroonUser>(
            { positionSortKey(normalizedPosition(it.position)) },
            { it.department2.orEmpty() },
            { it.displayName }
        )
    )
}

private fun buildUserPickerDetail(user: GaroonUser): String {
    return listOfNotNull(
        user.department1?.takeIf { it.isNotBlank() },
        user.department2?.takeIf { it.isNotBlank() },
        user.position.takeIf { it.isNotBlank() }
    ).joinToString(" / ")
}

private fun normalizedPosition(position: String): String {
    return when (position.trim()) {
        "社長" -> "社長"
        "部長" -> "部長"
        "課長" -> "課長"
        else -> "一般"
    }
}

private fun positionSortKey(position: String): Int {
    return when (position) {
        "社長" -> 0
        "部長" -> 1
        "課長" -> 2
        else -> 3
    }
}

private const val ALL_SECTIONS_LABEL = "すべて"
private const val EXECUTIVE_DEPARTMENT_LABEL = "役員"

private data class CalendarPlacement(
    val schedule: Schedule,
    val startMinute: Int,
    val endMinute: Int,
    val column: Int,
    val totalColumns: Int,
    val topDp: Dp,
    val heightDp: Dp
)

private data class AssignedItem(
    val schedule: Schedule,
    val startMinute: Int,
    val endMinute: Int,
    val column: Int
)

private data class BusyRange(
    val startMinute: Int,
    val endMinute: Int
) {
    val durationMinutes: Int
        get() = endMinute - startMinute
}

private fun buildCalendarPlacements(
    items: List<Schedule>,
    hourHeight: Dp
): List<CalendarPlacement> {
    val visibleItems = items
        .mapNotNull { schedule ->
            val startMinute = minuteOfDay(schedule.startAt) ?: return@mapNotNull null
            val endMinute = minuteOfDay(schedule.endAt) ?: return@mapNotNull null
            val clampedStart = startMinute.coerceAtLeast(CALENDAR_START_HOUR * 60)
            val clampedEnd = endMinute.coerceAtMost(CALENDAR_END_HOUR * 60)
            if (clampedEnd <= clampedStart) return@mapNotNull null
            schedule to (clampedStart to clampedEnd)
        }
        .sortedBy { it.second.first }

    val active = mutableListOf<AssignedItem>()
    val assigned = mutableListOf<AssignedItem>()

    visibleItems.forEach { (schedule, minutes) ->
        val startMinute = minutes.first
        val endMinute = minutes.second

        active.removeAll { it.endMinute <= startMinute }

        val usedColumns = active.map { it.column }.toSet()
        val nextColumn = generateSequence(0) { it + 1 }
            .first { it !in usedColumns }

        val item = AssignedItem(
            schedule = schedule,
            startMinute = startMinute,
            endMinute = endMinute,
            column = nextColumn
        )

        active += item
        assigned += item
    }

    return assigned.map { item ->
        val overlappingItems = assigned.filter { other ->
            other.startMinute < item.endMinute && other.endMinute > item.startMinute
        }

        val totalColumns = ((overlappingItems.maxOfOrNull { it.column } ?: 0) + 1)
            .coerceAtLeast(1)

        val topMinutes = item.startMinute - (CALENDAR_START_HOUR * 60)
        val durationMinutes = (item.endMinute - item.startMinute).coerceAtLeast(30)

        CalendarPlacement(
            schedule = item.schedule,
            startMinute = item.startMinute,
            endMinute = item.endMinute,
            column = item.column,
            totalColumns = totalColumns,
            topDp = hourHeight * (topMinutes / 60f),
            heightDp = hourHeight * (durationMinutes / 60f)
        )
    }
}

private fun mergeBusyRanges(items: List<Schedule>): List<BusyRange> {
    val ranges = items
        .mapNotNull { schedule ->
            val startMinute = minuteOfDay(schedule.startAt) ?: return@mapNotNull null
            val endMinute = minuteOfDay(schedule.endAt) ?: return@mapNotNull null
            val clampedStart = startMinute.coerceAtLeast(CALENDAR_START_HOUR * 60)
            val clampedEnd = endMinute.coerceAtMost(CALENDAR_END_HOUR * 60)
            if (clampedEnd <= clampedStart) return@mapNotNull null
            BusyRange(clampedStart, clampedEnd)
        }
        .sortedBy { it.startMinute }

    if (ranges.isEmpty()) return emptyList()

    val merged = mutableListOf(ranges.first())
    ranges.drop(1).forEach { range ->
        val last = merged.last()
        if (range.startMinute <= last.endMinute) {
            merged[merged.lastIndex] = BusyRange(
                startMinute = last.startMinute,
                endMinute = maxOf(last.endMinute, range.endMinute)
            )
        } else {
            merged += range
        }
    }

    return merged
}

private fun buildUserColorMap(users: List<GaroonUser>): Map<String, UserColorScheme> {
    return users
        .sortedBy { it.label }
        .mapIndexed { index, user ->
            user.id to UserPalette[index % UserPalette.size]
        }
        .toMap()
}

private fun fallbackColorScheme(seed: String): UserColorScheme {
    val safeHash = seed.hashCode().let { if (it == Int.MIN_VALUE) 0 else abs(it) }
    return UserPalette[safeHash % UserPalette.size]
}