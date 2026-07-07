package com.slowly.manmanlai

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Context.dataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_template")
    private val cardStyleKey = stringPreferencesKey("card_style")
    val themeId: Flow<String> = context.dataStore.data.map { it[themeKey] ?: "fresh" }
    val cardStyleId: Flow<String> = context.dataStore.data.map { it[cardStyleKey] ?: "fresh_ai" }
    suspend fun setTheme(id: String) {
        context.dataStore.edit { it[themeKey] = id }
    }

    suspend fun setCardStyle(id: String) {
        context.dataStore.edit { it[cardStyleKey] = id }
    }
}

class ManManLaiRepository(private val db: AppDatabase) {
    val deck = db.taskDao().observeDeck()
    val trash = db.taskDao().observeTrash()
    val cards = db.cardDao().observeCards()
    val achievements = db.achievementDao().observeAchievements()

    suspend fun seedIfEmpty() {
        if (db.taskDao().taskCount() > 0) return
        addTask(
            title = "\u5199\u4e0b\u4eca\u5929\u6700\u5c0f\u7684\u4e00\u6b65",
            description = "\u4e0d\u7528\u5b8c\u6574\u89e3\u51b3\uff0c\u53ea\u8981\u8ba9\u4e8b\u60c5\u5f00\u59cb\u79fb\u52a8\u3002",
            dueAt = null,
            remindAt = null,
            tags = listOf("\u542f\u52a8"),
        )
        addTask(
            title = "\u590d\u76d8\u6700\u8fd1\u4e09\u5929",
            description = "\u627e\u4e00\u4e2a\u4e0d\u8d23\u5907\u81ea\u5df1\u7684\u89d2\u5ea6\uff0c\u770b\u770b\u5361\u4f4f\u5728\u54ea\u91cc\u3002",
            dueAt = null,
            remindAt = null,
            tags = listOf("\u603b\u7ed3"),
        )
    }

    suspend fun addTask(
        title: String,
        description: String,
        dueAt: Long?,
        remindAt: Long?,
        tags: List<String>,
        reminderCycleMinutes: Int? = null,
    ): Long {
        val nextOrder = db.taskDao().maxDeckOrder() + 1
        return db.taskDao().upsert(
            PlanTask(
                title = title.ifBlank { "\u4eca\u5929\u7684\u4e00\u5c0f\u6b65" },
                description = description,
                dueAt = dueAt,
                remindAt = remindAt,
                reminderCycleMinutes = reminderCycleMinutes,
                tags = tags,
                deckOrder = nextOrder,
            ),
        )
    }

    suspend fun updateTask(task: PlanTask) = db.taskDao().upsert(task)

    suspend fun deleteTask(task: PlanTask) = db.taskDao().upsert(task.copy(status = TaskStatus.DELETED))

    suspend fun restoreTask(task: PlanTask) {
        db.taskDao().upsert(task.copy(status = TaskStatus.TODO, deckOrder = db.taskDao().maxDeckOrder() + 1))
    }

    suspend fun purgeTask(task: PlanTask) = db.taskDao().delete(task.id)

    suspend fun deleteCompletedCard(card: CompletedCard) = db.cardDao().delete(card.id)

    suspend fun returnCardToDeck(card: CompletedCard) {
        addTask(
            title = card.title,
            description = card.summary,
            dueAt = null,
            remindAt = null,
            tags = listOf("\u5361\u5305\u8fd4\u56de"),
        )
        db.cardDao().delete(card.id)
    }

    suspend fun cycleTask(task: PlanTask) {
        db.taskDao().upsert(task.copy(deckOrder = db.taskDao().maxDeckOrder() + 1))
    }

    suspend fun postponeTask(task: PlanTask, reason: String = "\u5148\u7f13\u4e00\u7f13") {
        db.taskDao().upsert(task.copy(postponeCount = task.postponeCount + 1, delayReason = reason))
    }

    suspend fun completeTask(task: PlanTask, templateId: String): CompletedCard {
        val now = System.currentTimeMillis()
        val done = task.copy(status = TaskStatus.DONE, completedAt = now)
        db.taskDao().upsert(done)
        val achievements = unlockAchievements(done)
        val card = CompletedCard(
            sourceTaskId = task.id,
            title = task.title,
            createdAt = task.createdAt,
            completedAt = now,
            summary = if (task.postponeCount > 0) {
                "\u7ed5\u4e86\u4e00\u70b9\u8def\uff0c\u4f46\u8fd8\u662f\u5b8c\u6210\u4e86\u3002"
            } else {
                "\u6309\u81ea\u5df1\u7684\u8282\u594f\u5b8c\u6210\u3002"
            },
            templateId = templateId,
            achievementIds = achievements,
        )
        db.cardDao().insert(card)
        return card
    }

    suspend fun dailySummary(date: LocalDate = LocalDate.now()): DailySummary {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val tasks = db.taskDao().allTasks()
        return DailySummary(
            dateStart = start,
            createdCount = tasks.count { it.createdAt in start until end },
            completedCount = tasks.count { (it.completedAt ?: 0) in start until end },
            delayedCount = tasks.count { it.postponeCount > 0 && it.createdAt <= end },
        )
    }

    suspend fun exportJson(): String {
        val root = JSONObject()
        root.put("tasks", JSONArray(db.taskDao().allTasks().map { it.toJson() }))
        root.put("cards", JSONArray(db.cardDao().allCards().map { it.toJson() }))
        root.put("achievements", JSONArray(db.achievementDao().allAchievements().map { it.toJson() }))
        return root.toString(2)
    }

    suspend fun importJson(json: String) {
        val root = JSONObject(json)
        root.optJSONArray("tasks")?.let { array ->
            for (i in 0 until array.length()) db.taskDao().upsert(array.getJSONObject(i).toPlanTask())
        }
        root.optJSONArray("cards")?.let { array ->
            for (i in 0 until array.length()) db.cardDao().insert(array.getJSONObject(i).toCompletedCard())
        }
        root.optJSONArray("achievements")?.let { array ->
            for (i in 0 until array.length()) db.achievementDao().upsert(array.getJSONObject(i).toAchievement())
        }
    }

    private suspend fun unlockAchievements(task: PlanTask): List<Long> {
        val existing = db.achievementDao().allAchievements()
        val unlocked = mutableListOf<Long>()
        if (existing.none { it.type == AchievementType.FIRST_DONE }) {
            unlocked += db.achievementDao().upsert(
                Achievement(
                    type = AchievementType.FIRST_DONE,
                    title = "\u7b2c\u4e00\u5f20\u5b8c\u6210\u5361",
                    description = "\u4f60\u5df2\u7ecf\u5f00\u59cb\u628a\u8ba1\u5212\u53d8\u6210\u6536\u85cf\u3002",
                    progress = 1,
                    target = 1,
                    unlockedAt = System.currentTimeMillis(),
                ),
            )
        }
        if (task.dueAt != null && (task.completedAt ?: Long.MAX_VALUE) <= task.dueAt) {
            unlocked += db.achievementDao().upsert(
                Achievement(
                    type = AchievementType.ON_TIME,
                    title = "\u521a\u521a\u597d",
                    description = "\u5728\u622a\u6b62\u65f6\u95f4\u524d\u5b8c\u6210\u4e86\u4e00\u4ef6\u4e8b\u3002",
                    progress = 1,
                    target = 1,
                    unlockedAt = System.currentTimeMillis(),
                ),
            )
        }
        if (task.postponeCount > 0) {
            unlocked += db.achievementDao().upsert(
                Achievement(
                    type = AchievementType.RECOVERY,
                    title = "\u56de\u6765\u4e5f\u7b97\u8d62",
                    description = "\u5ef6\u671f\u4e4b\u540e\u91cd\u65b0\u5b8c\u6210\uff0c\u503c\u5f97\u88ab\u8bb0\u5f55\u3002",
                    progress = 1,
                    target = 1,
                    unlockedAt = System.currentTimeMillis(),
                ),
            )
        }
        return unlocked
    }
}

private fun PlanTask.toJson() = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("description", description)
    .put("createdAt", createdAt)
    .put("dueAt", dueAt)
    .put("remindAt", remindAt)
    .put("reminderCycleMinutes", reminderCycleMinutes)
    .put("completedAt", completedAt)
    .put("priority", priority.name)
    .put("status", status.name)
    .put("tags", JSONArray(tags))
    .put("postponeCount", postponeCount)
    .put("delayReason", delayReason)
    .put("deckOrder", deckOrder)

private fun CompletedCard.toJson() = JSONObject()
    .put("id", id)
    .put("sourceTaskId", sourceTaskId)
    .put("title", title)
    .put("createdAt", createdAt)
    .put("completedAt", completedAt)
    .put("summary", summary)
    .put("templateId", templateId)
    .put("achievementIds", JSONArray(achievementIds))

private fun Achievement.toJson() = JSONObject()
    .put("id", id)
    .put("type", type.name)
    .put("title", title)
    .put("description", description)
    .put("progress", progress)
    .put("target", target)
    .put("unlockedAt", unlockedAt)

private fun JSONObject.toPlanTask() = PlanTask(
    id = optLong("id"),
    title = optString("title"),
    description = optString("description"),
    createdAt = optLong("createdAt"),
    dueAt = nullableLong("dueAt"),
    remindAt = nullableLong("remindAt"),
    reminderCycleMinutes = nullableInt("reminderCycleMinutes"),
    completedAt = nullableLong("completedAt"),
    priority = Priority.valueOf(optString("priority", Priority.NORMAL.name)),
    status = TaskStatus.valueOf(optString("status", TaskStatus.TODO.name)),
    tags = optJSONArray("tags")?.toStringList().orEmpty(),
    postponeCount = optInt("postponeCount"),
    delayReason = optString("delayReason"),
    deckOrder = optInt("deckOrder"),
)

private fun JSONObject.toCompletedCard() = CompletedCard(
    id = optLong("id"),
    sourceTaskId = optLong("sourceTaskId"),
    title = optString("title"),
    createdAt = optLong("createdAt"),
    completedAt = optLong("completedAt"),
    summary = optString("summary"),
    templateId = optString("templateId", "fresh"),
    achievementIds = optJSONArray("achievementIds")?.toLongList().orEmpty(),
)

private fun JSONObject.toAchievement() = Achievement(
    id = optLong("id"),
    type = AchievementType.valueOf(optString("type", AchievementType.FIRST_DONE.name)),
    title = optString("title"),
    description = optString("description"),
    progress = optInt("progress"),
    target = optInt("target"),
    unlockedAt = nullableLong("unlockedAt"),
)

private fun JSONObject.nullableLong(key: String): Long? = if (isNull(key)) null else optLong(key)
private fun JSONObject.nullableInt(key: String): Int? = if (isNull(key)) null else optInt(key)
private fun JSONArray.toStringList(): List<String> = List(length()) { getString(it) }
private fun JSONArray.toLongList(): List<Long> = List(length()) { getLong(it) }

fun Long.formatDateTime(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
