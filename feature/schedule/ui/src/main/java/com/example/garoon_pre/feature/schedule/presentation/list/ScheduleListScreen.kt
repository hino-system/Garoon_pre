package com.example.garoon_pre.feature.schedule.presentation.list

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.core.common.data.isToday
import com.example.core.common.data.toDayOfMonthLabel
import com.example.core.common.data.toDayOfWeekLabel
import com.example.core.common.data.toMonthLabel
import com.example.garoon_pre.core.designsystem.theme.AppDivider
import com.example.garoon_pre.core.designsystem.theme.AppSurfaceMuted
import com.example.garoon_pre.core.designsystem.theme.CalendarSelected
import com.example.garoon_pre.core.designsystem.theme.CalendarTodayOutlineColor
import kotlinx.coroutines.flow.collectLatest
import com.example.garoon_pre.core.designsystem.theme.CalendarSelectedColumnBackground
import com.example.garoon_pre.core.designsystem.theme.EventBlueBg
import com.example.garoon_pre.core.designsystem.theme.EventBlueBorder
import com.example.garoon_pre.core.designsystem.theme.EventGreenBg
import com.example.garoon_pre.core.designsystem.theme.EventGreenBorder
import com.example.garoon_pre.core.designsystem.theme.EventPinkBg
import com.example.garoon_pre.core.designsystem.theme.EventPinkBorder
import com.example.garoon_pre.core.designsystem.theme.EventPurpleBg
import com.example.garoon_pre.core.designsystem.theme.EventPurpleBorder
import com.example.garoon_pre.core.designsystem.theme.EventYellowBg
import com.example.garoon_pre.core.designsystem.theme.EventYellowBorder
import com.example.garoon_pre.core.designsystem.theme.TextMuted
import com.example.garoon_pre.core.designsystem.theme.TextPrimary
import com.example.garoon_pre.core.designsystem.theme.TextSecondary
import com.example.garoon_pre.core.designsystem.theme.WeekendSaturday
import com.example.garoon_pre.core.designsystem.theme.WeekendSunday
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.presentation.common.CALENDAR_END_HOUR
import com.example.garoon_pre.feature.schedule.presentation.common.CALENDAR_START_HOUR
import com.example.garoon_pre.feature.schedule.presentation.common.buildCalendarPlacements

private val CalendarGridLineColor = AppDivider
private val CalendarHeaderBackground = AppSurfaceMuted
private val CalendarSelectedColor = CalendarSelected
private val CalendarTodayOutline = CalendarTodayOutlineColor

@Composable
fun ScheduleListRoute(
    onBack: () -> Unit,
    onOpenDetail: (String, String, String) -> Unit,
    onOpenCreate: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ScheduleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.loggedOut.collectLatest {
            onLoggedOut()
        }
    }

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

    ScheduleListScreen(
        uiState = uiState,
        onBack = onBack,
        onPreviousWeek = { viewModel.moveWeek(-1) },
        onNextWeek = { viewModel.moveWeek(1) },
        onToday = viewModel::jumpToToday,
        onSelectDate = viewModel::selectDate,
        onOpenCreate = onOpenCreate,
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
fun ScheduleListScreen(
    uiState: ScheduleListUiState,
    onBack: () -> Unit,
//    onRefresh: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
    onSelectDate: (String) -> Unit,
//    onLogout: () -> Unit,
    onOpenCreate: () -> Unit,
    onItemClick: (Schedule) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("スケジュール") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("戻る")
                    }
                },
                actions = {
                    TextButton(onClick = onOpenCreate) {
                        Text("＋")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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

                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    WeekCalendar(
                        weekDates = uiState.weekDates,
                        selectedDate = uiState.selectedDate,
                        itemsByDate = uiState.itemsByDate,
                        onSelectDate = onSelectDate,
                        onItemClick = onItemClick,
                        modifier = Modifier.weight(1f)
                    )
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
            TextButton(onClick = onPreviousWeek, contentPadding = PaddingValues(horizontal = 8.dp)) {
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
private fun WeekCalendar(
    weekDates: List<String>,
    selectedDate: String,
    itemsByDate: Map<String, List<Schedule>>,
    onSelectDate: (String) -> Unit,
    onItemClick: (Schedule) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val containerWidth = this@BoxWithConstraints.maxWidth
            val dayCount = weekDates.size.coerceAtLeast(1)

            val timeColumnWidth = 40.dp
            val hourHeight = 64.dp
            val gridHeight = hourHeight * (CALENDAR_END_HOUR - CALENDAR_START_HOUR)
            val dayColumnWidth = ((containerWidth - timeColumnWidth) / dayCount)

            val placementsByDate = remember(weekDates, itemsByDate) {
                weekDates.associateWith { date ->
                    buildCalendarPlacements(itemsByDate[date].orEmpty())
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                WeekHeaderRow(
                    weekDates = weekDates,
                    selectedDate = selectedDate,
                    timeColumnWidth = timeColumnWidth,
                    dayColumnWidth = dayColumnWidth,
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
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surface),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        Text(
                                            text = hour.toString(),
                                            modifier = Modifier.padding(top = 2.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextMuted
                                        )
                                    }

                                    weekDates.forEach { date ->
                                        Box(
                                            modifier = Modifier
                                                .width(dayColumnWidth)
                                                .fillMaxSize()
                                                .background(
                                                    if (date == selectedDate) {
                                                        CalendarSelectedColumnBackground
                                                    } else {
                                                        Color.White
                                                    }
                                                )
                                                .border(0.5.dp, CalendarGridLineColor)
                                        )
                                    }
                                }
                            }
                        }

                        weekDates.forEachIndexed { dayIndex, date ->
                            placementsByDate[date].orEmpty().forEach { placement ->
                                val eventWidth = (dayColumnWidth / placement.totalColumns) - 4.dp
                                val eventOffsetX =
                                    timeColumnWidth +
                                            (dayColumnWidth * dayIndex) +
                                            ((dayColumnWidth / placement.totalColumns) * placement.column) + 2.dp

                                EventCard(
                                    schedule = placement.schedule,
                                    modifier = Modifier
                                        .width(eventWidth)
                                        .height(placement.heightDp.coerceAtLeast(32.dp))
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
            .background(CalendarHeaderBackground)
            .border(0.5.dp, CalendarGridLineColor)
    ) {
        Box(
            modifier = Modifier
                .width(timeColumnWidth)
                .height(62.dp)
        )

        weekDates.forEach { date ->
            val isSelected = date == selectedDate
            val weekdayLabel = toDayOfWeekLabel(date)
            val dayNumber = toDayOfMonthLabel(date)
            val weekdayColor = when (weekdayLabel) {
                "日" -> WeekendSunday
                "土" -> WeekendSaturday
                else -> TextSecondary
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
                    text = weekdayLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = weekdayColor
                )

                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> CalendarSelectedColor
                                isToday(date) -> Color.White
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (isToday(date) && !isSelected) 1.dp else 0.dp,
                            color = CalendarTodayOutline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    schedule: Schedule,
    modifier: Modifier = Modifier
) {
    val backgroundColor = eventBackgroundColor(schedule)
    val borderColor = eventBorderColor(schedule)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = schedule.title,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF253447),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun eventBackgroundColor(schedule: Schedule): Color {
    return when (stableColorIndex(schedule)) {
        0 -> EventYellowBg
        1 -> EventBlueBg
        2 -> EventPurpleBg
        3 -> EventPinkBg
        else -> EventGreenBg
    }
}

private fun eventBorderColor(schedule: Schedule): Color {
    return when (stableColorIndex(schedule)) {
        0 -> EventYellowBorder
        1 -> EventBlueBorder
        2 -> EventPurpleBorder
        3 -> EventPinkBorder
        else -> EventGreenBorder
    }
}

private fun stableColorIndex(schedule: Schedule): Int {
    return (schedule.title.hashCode().absoluteValue + schedule.organizerName.hashCode().absoluteValue) % 5
}

private val Int.absoluteValue: Int
    get() = if (this < 0) -this else this