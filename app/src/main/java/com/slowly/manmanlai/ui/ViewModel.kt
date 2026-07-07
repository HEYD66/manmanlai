package com.slowly.manmanlai.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.slowly.manmanlai.Achievement
import com.slowly.manmanlai.CompletedCard
import com.slowly.manmanlai.ManManLaiRepository
import com.slowly.manmanlai.PlanTask
import com.slowly.manmanlai.SettingsRepository
import com.slowly.manmanlai.worker.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ManManLaiUiState(
    val deck: List<PlanTask> = emptyList(),
    val trash: List<PlanTask> = emptyList(),
    val cards: List<CompletedCard> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val themeId: String = "fresh",
    val cardStyleId: String = "fresh_ai",
)

class ManManLaiViewModel(
    private val repository: ManManLaiRepository,
    private val settings: SettingsRepository,
    private val context: Context,
) : ViewModel() {
    private val contentState = combine(
        repository.deck,
        repository.trash,
        repository.cards,
        repository.achievements,
    ) { deck, trash, cards, achievements ->
        ManManLaiUiState(deck = deck, trash = trash, cards = cards, achievements = achievements)
    }

    val uiState: StateFlow<ManManLaiUiState> = combine(
        contentState,
        settings.themeId,
        settings.cardStyleId,
    ) { content, themeId, cardStyleId ->
        content.copy(
            themeId = themeId,
            cardStyleId = cardStyleId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManManLaiUiState())

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }

    fun addTask(title: String, description: String, tags: String, reminderMinutes: Int?) {
        viewModelScope.launch {
            val remindAt = reminderMinutes?.let { System.currentTimeMillis() + it.coerceAtLeast(1) * 60 * 1000L }
            val taskTitle = title.ifBlank { "\u4eca\u5929\u7684\u4e00\u5c0f\u6b65" }
            val id = repository.addTask(
                title = taskTitle,
                description = description,
                dueAt = null,
                remindAt = remindAt,
                tags = tags.split(" ", "\uff0c", ",").filter { it.isNotBlank() },
                reminderCycleMinutes = reminderMinutes?.coerceAtLeast(1),
            )
            NotificationHelper.schedule(context, id, taskTitle, remindAt, reminderMinutes, "#$id")
        }
    }

    fun editTask(task: PlanTask, title: String, description: String, tags: String, reminderMinutes: Int?) {
        viewModelScope.launch {
            NotificationHelper.cancel(context, task.id)
            val nextReminder = reminderMinutes?.let { System.currentTimeMillis() + it.coerceAtLeast(1) * 60 * 1000L }
            val updated = task.copy(
                title = title.ifBlank { task.title },
                description = description,
                tags = tags.split(" ", "\uff0c", ",").filter { it.isNotBlank() },
                remindAt = nextReminder,
                reminderCycleMinutes = reminderMinutes?.coerceAtLeast(1),
            )
            repository.updateTask(updated)
            NotificationHelper.schedule(context, updated.id, updated.title, nextReminder, updated.reminderCycleMinutes, "#${updated.id}")
        }
    }

    fun cycleTask(task: PlanTask) {
        viewModelScope.launch { repository.cycleTask(task) }
    }

    fun postpone(task: PlanTask) {
        viewModelScope.launch { repository.postponeTask(task) }
    }

    fun delete(task: PlanTask) {
        viewModelScope.launch {
            NotificationHelper.cancel(context, task.id)
            repository.deleteTask(task)
        }
    }

    fun restore(task: PlanTask) {
        viewModelScope.launch { repository.restoreTask(task) }
    }

    fun purge(task: PlanTask) {
        viewModelScope.launch {
            NotificationHelper.cancel(context, task.id)
            repository.purgeTask(task)
        }
    }

    fun deleteCompleted(card: CompletedCard) {
        viewModelScope.launch { repository.deleteCompletedCard(card) }
    }

    fun returnCardToDeck(card: CompletedCard) {
        viewModelScope.launch { repository.returnCardToDeck(card) }
    }

    fun complete(task: PlanTask) {
        viewModelScope.launch {
            NotificationHelper.cancel(context, task.id)
            repository.completeTask(task, uiState.value.themeId)
        }
    }

    fun changeTheme(id: String) {
        viewModelScope.launch { settings.setTheme(id) }
    }

    fun changeCardStyle(id: String) {
        viewModelScope.launch { settings.setCardStyle(id) }
    }

    fun exportBackup(onReady: (String) -> Unit) {
        viewModelScope.launch { onReady(repository.exportJson()) }
    }

    fun importBackup(json: String) {
        viewModelScope.launch { repository.importJson(json) }
    }
}

class ManManLaiViewModelFactory(
    private val repository: ManManLaiRepository,
    private val settings: SettingsRepository,
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ManManLaiViewModel(repository, settings, context) as T
}
