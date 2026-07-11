package com.slowly.manmanlai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryDataTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ManManLaiRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ManManLaiRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completionIsDeduplicatedAndKeepsTaskSnapshot() = runTest {
        val task = insertDetailedTask()

        val first = repository.completeTask(task, "dark_ai")
        val second = repository.completeTask(task, "dark_ai")

        assertEquals(first.id, second.id)
        assertEquals(1, database.cardDao().allCards().size)
        assertEquals(TaskStatus.DONE, database.taskDao().taskById(task.id)?.status)
        assertEquals("dark_ai", first.templateId)
        assertEquals("完整描述", first.description)
        assertEquals(listOf("工作", "启动"), first.tags)
        assertEquals(Priority.HIGH, first.priority)
        assertEquals(2, first.postponeCount)
    }

    @Test
    fun returningCardReusesOriginalTaskAndRemovesCompletedCard() = runTest {
        val task = insertDetailedTask()
        val card = repository.completeTask(task, "eye_ai")

        val restored = repository.returnCardToDeck(card)

        assertEquals(task.id, restored.id)
        assertEquals(TaskStatus.TODO, restored.status)
        assertEquals("完整描述", restored.description)
        assertEquals(Priority.HIGH, restored.priority)
        assertTrue(database.cardDao().allCards().isEmpty())
        assertNotNull(restored.remindAt)
    }

    @Test
    fun removingCompletedCardMovesSourceTaskToTrash() = runTest {
        val task = insertDetailedTask()
        val card = repository.completeTask(task, "fresh_ai")

        repository.deleteCompletedCard(card)

        assertEquals(TaskStatus.DELETED, database.taskDao().taskById(task.id)?.status)
        assertTrue(database.cardDao().allCards().isEmpty())
    }

    @Test
    fun postponingMovesCardAndRearmsReminder() = runTest {
        val task = insertDetailedTask()
        val before = System.currentTimeMillis()

        val postponed = repository.postponeTask(task, "\u9700\u8981\u62c6\u5c0f")

        assertEquals(3, postponed.postponeCount)
        assertEquals("\u9700\u8981\u62c6\u5c0f", postponed.delayReason)
        assertTrue(requireNotNull(postponed.remindAt) >= before + 10 * 60_000L)
        assertTrue(postponed.deckOrder > task.deckOrder)
    }

    private suspend fun insertDetailedTask(): PlanTask {
        val id = repository.addTask(
            title = "测试任务",
            description = "完整描述",
            dueAt = System.currentTimeMillis() + 60_000L,
            remindAt = System.currentTimeMillis() + 30_000L,
            tags = listOf("工作", "启动"),
            reminderCycleMinutes = 10,
        )
        val task = requireNotNull(database.taskDao().taskById(id)).copy(
            priority = Priority.HIGH,
            postponeCount = 2,
            delayReason = "需要拆小",
        )
        database.taskDao().upsert(task)
        return task
    }
}
