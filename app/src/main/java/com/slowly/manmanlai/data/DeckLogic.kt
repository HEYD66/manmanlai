package com.slowly.manmanlai

object DeckLogic {
    fun shouldCycleCard(offsetX: Float, offsetY: Float, width: Float, height: Float): Boolean {
        val horizontal = kotlin.math.abs(offsetX) > width * 0.34f
        val vertical = kotlin.math.abs(offsetY) > height * 0.24f
        return horizontal || vertical
    }

    fun cycleTopCard(tasks: List<PlanTask>): List<PlanTask> {
        if (tasks.size < 2) return tasks
        val maxOrder = tasks.maxOf { it.deckOrder }
        val top = tasks.first().copy(deckOrder = maxOrder + 1)
        return (tasks.drop(1) + top).sortedWith(compareBy<PlanTask> { it.deckOrder }.thenBy { it.createdAt })
    }
}
