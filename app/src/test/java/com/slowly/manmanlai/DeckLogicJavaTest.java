package com.slowly.manmanlai;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeckLogicJavaTest {
    @Test
    public void dragPastHorizontalThresholdCyclesCard() {
        assertTrue(DeckLogic.INSTANCE.shouldCycleCard(180f, 0f, 400f, 700f));
    }

    @Test
    public void smallDragDoesNotCycleCard() {
        assertFalse(DeckLogic.INSTANCE.shouldCycleCard(40f, 40f, 400f, 700f));
    }
}
