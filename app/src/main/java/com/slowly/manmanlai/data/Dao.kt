package com.slowly.manmanlai

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanTaskDao {
    @Query("SELECT * FROM plan_tasks WHERE status = 'TODO' ORDER BY deckOrder ASC, createdAt ASC")
    fun observeDeck(): Flow<List<PlanTask>>

    @Query("SELECT * FROM plan_tasks WHERE status = 'DELETED' ORDER BY createdAt DESC")
    fun observeTrash(): Flow<List<PlanTask>>

    @Query("SELECT * FROM plan_tasks ORDER BY createdAt DESC")
    suspend fun allTasks(): List<PlanTask>

    @Query("SELECT * FROM plan_tasks WHERE id = :id LIMIT 1")
    suspend fun taskById(id: Long): PlanTask?

    @Query("SELECT COUNT(*) FROM plan_tasks")
    suspend fun taskCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: PlanTask): Long

    @Query("DELETE FROM plan_tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COALESCE(MAX(deckOrder), 0) FROM plan_tasks")
    suspend fun maxDeckOrder(): Int

    @Query("SELECT COALESCE(MIN(deckOrder), 0) FROM plan_tasks WHERE status = 'TODO'")
    suspend fun minDeckOrder(): Int
}

@Dao
interface CompletedCardDao {
    @Query("SELECT * FROM completed_cards ORDER BY completedAt DESC")
    fun observeCards(): Flow<List<CompletedCard>>

    @Query("SELECT * FROM completed_cards ORDER BY completedAt DESC")
    suspend fun allCards(): List<CompletedCard>

    @Query("SELECT * FROM completed_cards WHERE sourceTaskId = :sourceTaskId LIMIT 1")
    suspend fun cardBySourceTaskId(sourceTaskId: Long): CompletedCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CompletedCard): Long

    @Query("DELETE FROM completed_cards WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC, id ASC")
    fun observeAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements")
    suspend fun allAchievements(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(achievement: Achievement): Long
}

@Database(
    entities = [PlanTask::class, CompletedCard::class, Achievement::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): PlanTaskDao
    abstract fun cardDao(): CompletedCardDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE completed_cards ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE completed_cards ADD COLUMN dueAt INTEGER")
                db.execSQL("ALTER TABLE completed_cards ADD COLUMN remindAt INTEGER")
                db.execSQL("ALTER TABLE completed_cards ADD COLUMN reminderCycleMinutes INTEGER")
                db.execSQL("ALTER TABLE completed_cards ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'")
                db.execSQL("ALTER TABLE completed_cards ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE completed_cards ADD COLUMN postponeCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE completed_cards ADD COLUMN delayReason TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    """
                    UPDATE completed_cards
                    SET description = COALESCE((SELECT description FROM plan_tasks WHERE id = sourceTaskId), ''),
                        dueAt = (SELECT dueAt FROM plan_tasks WHERE id = sourceTaskId),
                        remindAt = (SELECT remindAt FROM plan_tasks WHERE id = sourceTaskId),
                        reminderCycleMinutes = (SELECT reminderCycleMinutes FROM plan_tasks WHERE id = sourceTaskId),
                        priority = COALESCE((SELECT priority FROM plan_tasks WHERE id = sourceTaskId), 'NORMAL'),
                        tags = COALESCE((SELECT tags FROM plan_tasks WHERE id = sourceTaskId), ''),
                        postponeCount = COALESCE((SELECT postponeCount FROM plan_tasks WHERE id = sourceTaskId), 0),
                        delayReason = COALESCE((SELECT delayReason FROM plan_tasks WHERE id = sourceTaskId), '')
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    UPDATE completed_cards
                    SET templateId = CASE templateId
                        WHEN 'fresh' THEN 'fresh_ai'
                        WHEN 'dark' THEN 'dark_ai'
                        WHEN 'eye' THEN 'eye_ai'
                        WHEN 'coral' THEN 'coral_ai'
                        ELSE 'fresh_ai'
                    END
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    DELETE FROM completed_cards
                    WHERE id NOT IN (
                        SELECT MAX(id) FROM completed_cards GROUP BY sourceTaskId
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_completed_cards_sourceTaskId ON completed_cards(sourceTaskId)",
                )
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "manmanlai.db")
                .addMigrations(MIGRATION_2_3)
                .build()
    }
}
