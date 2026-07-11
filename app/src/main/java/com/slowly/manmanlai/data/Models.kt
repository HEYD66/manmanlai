package com.slowly.manmanlai

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskStatus { TODO, DONE, ARCHIVED, DELETED }
enum class Priority { LOW, NORMAL, HIGH }
enum class AchievementType { FIRST_DONE, STREAK, ON_TIME, RECOVERY }

@Entity(tableName = "plan_tasks")
data class PlanTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dueAt: Long? = null,
    val remindAt: Long? = null,
    val reminderCycleMinutes: Int? = null,
    val completedAt: Long? = null,
    val priority: Priority = Priority.NORMAL,
    val status: TaskStatus = TaskStatus.TODO,
    val tags: List<String> = emptyList(),
    val postponeCount: Int = 0,
    val delayReason: String = "",
    val deckOrder: Int = 0,
)

@Entity(
    tableName = "completed_cards",
    indices = [Index(value = ["sourceTaskId"], unique = true)],
)
data class CompletedCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceTaskId: Long,
    val title: String,
    val createdAt: Long,
    val completedAt: Long,
    val summary: String,
    val templateId: String,
    val description: String = "",
    val dueAt: Long? = null,
    val remindAt: Long? = null,
    val reminderCycleMinutes: Int? = null,
    val priority: Priority = Priority.NORMAL,
    val tags: List<String> = emptyList(),
    val postponeCount: Int = 0,
    val delayReason: String = "",
    val achievementIds: List<Long> = emptyList(),
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: AchievementType,
    val title: String,
    val description: String,
    val progress: Int,
    val target: Int,
    val unlockedAt: Long? = null,
)

data class DailySummary(
    val dateStart: Long,
    val createdCount: Int,
    val completedCount: Int,
    val delayedCount: Int,
    val focusMinutes: Int = 0,
    val mood: String = "\u5e73\u7a33",
)

data class ThemeTemplate(
    val id: String,
    val name: String,
    val colorScheme: TemplateColors,
)

data class TemplateColors(
    val background: Long,
    val cardFront: Long,
    val cardBack: Long,
    val accent: Long,
    val text: Long,
)
