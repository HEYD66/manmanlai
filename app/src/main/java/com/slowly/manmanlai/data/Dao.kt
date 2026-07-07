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
}

@Dao
interface CompletedCardDao {
    @Query("SELECT * FROM completed_cards ORDER BY completedAt DESC")
    fun observeCards(): Flow<List<CompletedCard>>

    @Query("SELECT * FROM completed_cards ORDER BY completedAt DESC")
    suspend fun allCards(): List<CompletedCard>

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
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): PlanTaskDao
    abstract fun cardDao(): CompletedCardDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "manmanlai.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
