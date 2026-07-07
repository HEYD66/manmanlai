package com.slowly.manmanlai

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun tagsToString(value: List<String>): String = value.joinToString("|") { it.replace("|", " ") }

    @TypeConverter
    fun stringToTags(value: String): List<String> = value.split("|").filter { it.isNotBlank() }

    @TypeConverter
    fun longListToString(value: List<Long>): String = value.joinToString(",")

    @TypeConverter
    fun stringToLongList(value: String): List<Long> =
        value.split(",").mapNotNull { it.toLongOrNull() }

    @TypeConverter
    fun statusToString(value: TaskStatus): String = value.name

    @TypeConverter
    fun stringToStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun priorityToString(value: Priority): String = value.name

    @TypeConverter
    fun stringToPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun achievementTypeToString(value: AchievementType): String = value.name

    @TypeConverter
    fun stringToAchievementType(value: String): AchievementType = AchievementType.valueOf(value)
}
