package com.slowly.manmanlai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckLogicTest {
    @Test
    fun dragThresholdWorksInEveryDirection() {
        assertTrue(DeckLogic.shouldCycleCard(35f, 0f, 100f, 200f))
        assertTrue(DeckLogic.shouldCycleCard(-35f, 0f, 100f, 200f))
        assertTrue(DeckLogic.shouldCycleCard(0f, 49f, 100f, 200f))
        assertTrue(DeckLogic.shouldCycleCard(0f, -49f, 100f, 200f))
        assertFalse(DeckLogic.shouldCycleCard(34f, 48f, 100f, 200f))
    }

    @Test
    fun cyclingMovesOnlyTheTopCardToTheEnd() {
        val tasks = listOf(
            PlanTask(id = 1, title = "A", deckOrder = 1),
            PlanTask(id = 2, title = "B", deckOrder = 2),
            PlanTask(id = 3, title = "C", deckOrder = 3),
        )

        val cycled = DeckLogic.cycleTopCard(tasks)

        assertEquals(listOf(2L, 3L, 1L), cycled.map { it.id })
        assertEquals(4, cycled.last().deckOrder)
    }
}
