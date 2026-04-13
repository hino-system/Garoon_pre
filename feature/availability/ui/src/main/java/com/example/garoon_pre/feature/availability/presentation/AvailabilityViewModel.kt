package com.example.garoon_pre.feature.availability.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.data.currentWeekStartDateString
import com.example.core.common.data.shiftWeekDate
import com.example.core.common.data.todayString
import com.example.core.common.data.weekDatesFrom
import com.example.core.common.data.weekStartDateString
import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.availability.data.preference.AvailabilityPreferenceRepository
import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import com.example.garoon_pre.core.model.user.GaroonUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AvailabilityUiState(
    val loading: Boolean = true,
    val weekStartDate: String = currentWeekStartDateString(),
    val weekDates: List<String> = weekDatesFrom(currentWeekStartDateString()),
    val selectedDate: String = todayString(),
    val users: List<GaroonUser> = emptyList(),
    val selectedUserIds: List<String> = emptyList(),
    val itemsByDate: Map<String, List<Schedule>> = emptyMap(),
    val errorMessage: String? = null
)

private data class AvailabilityLoadResult(
    val weekStartDate: String,
    val weekDates: List<String>,
    val selectedDate: String,
    val users: List<GaroonUser>,
    val selectedUserIds: List<String>
)

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val sessionStore: SessionStore,
    private val preferenceRepository: AvailabilityPreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailabilityUiState())
    val uiState = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var syncJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (_uiState.value.users.isEmpty()) {
            loadUsersAndWeek()
        } else {
            loadWeek(
                weekStartDate = _uiState.value.weekStartDate,
                selectedDate = _uiState.value.selectedDate,
                selectedUserIds = _uiState.value.selectedUserIds
            )
        }
    }

    fun moveWeek(weekOffset: Int) {
        val current = _uiState.value
        val currentSelectedIndex = current.weekDates.indexOf(current.selectedDate).coerceAtLeast(0)
        val nextWeekStartDate = shiftWeekDate(current.weekStartDate, weekOffset)
        val nextWeekDates = weekDatesFrom(nextWeekStartDate)
        val nextSelectedDate = nextWeekDates.getOrElse(currentSelectedIndex) { nextWeekDates.first() }

        loadWeek(
            weekStartDate = nextWeekStartDate,
            selectedDate = nextSelectedDate,
            selectedUserIds = current.selectedUserIds
        )
    }

    fun jumpToToday() {
        val today = todayString()
        loadWeek(
            weekStartDate = weekStartDateString(today),
            selectedDate = today,
            selectedUserIds = _uiState.value.selectedUserIds
        )
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        persistCurrentPreference()
    }

    fun addUser(userId: String) {
        val current = _uiState.value
        if (userId in current.selectedUserIds) return

        loadWeek(
            weekStartDate = current.weekStartDate,
            selectedDate = current.selectedDate,
            selectedUserIds = current.selectedUserIds + userId
        )
    }

    fun removeUser(userId: String) {
        val current = _uiState.value
        loadWeek(
            weekStartDate = current.weekStartDate,
            selectedDate = current.selectedDate,
            selectedUserIds = current.selectedUserIds.filterNot { it == userId }
        )
    }

    private fun loadUsersAndWeek() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = true,
                    errorMessage = null
                )
            }

            runCatching {
                val currentUserId = sessionStore.userIdFlow.first()
                val savedPreference = preferenceRepository.load(currentUserId)

                val users = repository.getUsers().sortedWith(
                    compareBy<GaroonUser>(
                        { it.department1.orEmpty() },
                        { it.department2.orEmpty() },
                        { it.displayName }
                    )
                )

                val resolvedWeekStartDate = savedPreference.weekStartDate
                    ?.takeIf { it.isNotBlank() }
                    ?: currentWeekStartDateString()

                val resolvedWeekDates = weekDatesFrom(resolvedWeekStartDate)
                val resolvedSelectedDate = resolveSelectedDate(
                    requestedDate = savedPreference.selectedDate,
                    weekDates = resolvedWeekDates
                )
                val resolvedSelectedUserIds = resolveSelectedUserIds(
                    users = users,
                    requestedSelectedUserIds = savedPreference.selectedUserIds
                )

                preferenceRepository.save(
                    ownerUserId = currentUserId,
                    preference = AvailabilityPreference(
                        selectedUserIds = resolvedSelectedUserIds,
                        selectedDate = resolvedSelectedDate,
                        weekStartDate = resolvedWeekStartDate
                    )
                )

                AvailabilityLoadResult(
                    weekStartDate = resolvedWeekStartDate,
                    weekDates = resolvedWeekDates,
                    selectedDate = resolvedSelectedDate,
                    users = users,
                    selectedUserIds = resolvedSelectedUserIds
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        loading = true,
                        weekStartDate = result.weekStartDate,
                        weekDates = result.weekDates,
                        selectedDate = result.selectedDate,
                        users = result.users,
                        selectedUserIds = result.selectedUserIds,
                        errorMessage = null
                    )
                }

                startObserveAndSync(
                    weekDates = result.weekDates,
                    selectedUserIds = result.selectedUserIds,
                    errorMessage = "空き時間の取得に失敗しました"
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "空き時間の取得に失敗しました"
                    )
                }
            }
        }
    }

    private fun loadWeek(
        weekStartDate: String,
        selectedDate: String,
        selectedUserIds: List<String>
    ) {
        viewModelScope.launch {
            val currentUsers = _uiState.value.users
            if (currentUsers.isEmpty()) {
                loadUsersAndWeek()
                return@launch
            }

            val weekDates = weekDatesFrom(weekStartDate)
            val normalizedSelectedDate = resolveSelectedDate(
                requestedDate = selectedDate,
                weekDates = weekDates
            )
            val validSelectedUserIds = selectedUserIds
                .filter { selectedId -> currentUsers.any { user -> user.id == selectedId } }
                .distinct()

            _uiState.update {
                it.copy(
                    loading = true,
                    weekStartDate = weekStartDate,
                    weekDates = weekDates,
                    selectedDate = normalizedSelectedDate,
                    selectedUserIds = validSelectedUserIds,
                    errorMessage = null
                )
            }

            runCatching {
                val currentUserId = sessionStore.userIdFlow.first()
                preferenceRepository.save(
                    ownerUserId = currentUserId,
                    preference = AvailabilityPreference(
                        selectedUserIds = validSelectedUserIds,
                        selectedDate = normalizedSelectedDate,
                        weekStartDate = weekStartDate
                    )
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "空き時間の取得に失敗しました"
                    )
                }
                return@launch
            }

            startObserveAndSync(
                weekDates = weekDates,
                selectedUserIds = validSelectedUserIds,
                errorMessage = "空き時間の取得に失敗しました"
            )
        }
    }

    private fun startObserveAndSync(
        weekDates: List<String>,
        selectedUserIds: List<String>,
        errorMessage: String
    ) {
        observeJob?.cancel()
        syncJob?.cancel()

        if (weekDates.isEmpty() || selectedUserIds.isEmpty()) {
            _uiState.update {
                it.copy(
                    loading = false,
                    itemsByDate = weekDates.associateWith { emptyList() },
                    errorMessage = null
                )
            }
            return
        }

        observeJob = viewModelScope.launch {
            repository.observeSchedulesInRange(
                startDate = weekDates.first(),
                endDate = weekDates.last(),
                userIds = selectedUserIds
            ).collect { schedules ->
                val grouped = schedules.groupBy { it.startAt.take(8) }
                val itemsByDate = weekDates.associateWith { date ->
                    grouped[date].orEmpty()
                        .sortedWith(compareBy<Schedule>({ it.startAt }, { it.organizerName }, { it.title }))
                }

                _uiState.update {
                    it.copy(
                        loading = false,
                        itemsByDate = itemsByDate,
                        errorMessage = null
                    )
                }
            }
        }

        syncJob = viewModelScope.launch {
            runCatching {
                weekDates.forEach { date ->
                    repository.syncSchedulesByDate(
                        date = date,
                        userIds = selectedUserIds
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: errorMessage
                    )
                }
            }
        }
    }

    private suspend fun resolveSelectedUserIds(
        users: List<GaroonUser>,
        requestedSelectedUserIds: List<String>
    ): List<String> {
        val validRequested = requestedSelectedUserIds
            .filter { requestedId -> users.any { user -> user.id == requestedId } }
            .distinct()

        if (validRequested.isNotEmpty()) {
            return validRequested
        }

        val currentUserId = sessionStore.userIdFlow.first()
        val currentUser = users.firstOrNull { user -> user.userId == currentUserId }

        return listOfNotNull(currentUser?.id ?: users.firstOrNull()?.id)
    }

    private fun resolveSelectedDate(
        requestedDate: String?,
        weekDates: List<String>
    ): String {
        if (weekDates.isEmpty()) return requestedDate ?: todayString()
        return requestedDate?.takeIf { it in weekDates } ?: weekDates.first()
    }

    private fun persistCurrentPreference() {
        viewModelScope.launch {
            val currentUserId = sessionStore.userIdFlow.first()
            preferenceRepository.save(
                ownerUserId = currentUserId,
                preference = AvailabilityPreference(
                    selectedUserIds = _uiState.value.selectedUserIds,
                    selectedDate = _uiState.value.selectedDate,
                    weekStartDate = _uiState.value.weekStartDate
                )
            )
        }
    }
}