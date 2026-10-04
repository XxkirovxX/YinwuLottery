package org.XxkirovxX.plugins.yinwulottery.lottery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class LotteryLayoutTest {
    @Test
    void recipeSlotsMatchRequestedPaperPositions() {
        assertEquals(18, LotteryLayout.recipeSlots().size());
        assertTrue(LotteryLayout.recipeSlots().contains(LotteryLayout.slot(1, 3)));
        assertTrue(LotteryLayout.recipeSlots().contains(LotteryLayout.slot(1, 7)));
        assertTrue(LotteryLayout.recipeSlots().contains(LotteryLayout.slot(2, 1)));
        assertTrue(LotteryLayout.recipeSlots().contains(LotteryLayout.slot(5, 9)));
        assertTrue(LotteryLayout.recipeSlots().contains(LotteryLayout.slot(6, 3)));
        assertTrue(LotteryLayout.recipeSlots().contains(LotteryLayout.slot(6, 7)));
    }

    @Test
    void copperSlotsMatchCornerBlocks() {
        assertEquals(8, LotteryLayout.copperSlots().size());
        assertTrue(LotteryLayout.copperSlots().contains(LotteryLayout.slot(1, 1)));
        assertTrue(LotteryLayout.copperSlots().contains(LotteryLayout.slot(1, 2)));
        assertTrue(LotteryLayout.copperSlots().contains(LotteryLayout.slot(1, 8)));
        assertTrue(LotteryLayout.copperSlots().contains(LotteryLayout.slot(6, 9)));
    }

    @Test
    void trackStartsBelowCampfireAndHasUniqueSlots() {
        assertEquals(LotteryLayout.INITIAL_POINTER_SLOT, LotteryLayout.trackSlots().get(0));
        assertEquals(18, LotteryLayout.trackSlots().size());
        assertEquals(18, new HashSet<>(LotteryLayout.trackSlots()).size());
    }

    @Test
    void pointerIndexesResolveToTheRecipeTheyVisuallyPointAt() {
        assertEquals(LotteryLayout.recipeSlots().get(11), LotteryLayout.recipeSlotForPointerIndex(0));
        assertEquals(LotteryLayout.recipeSlots().get(8), LotteryLayout.recipeSlotForPointerIndex(3));
        assertEquals(LotteryLayout.recipeSlots().get(5), LotteryLayout.recipeSlotForPointerIndex(6));
        assertEquals(LotteryLayout.recipeSlots().get(2), LotteryLayout.recipeSlotForPointerIndex(9));
        assertEquals(LotteryLayout.recipeSlots().get(17), LotteryLayout.recipeSlotForPointerIndex(12));
        assertEquals(LotteryLayout.recipeSlots().get(14), LotteryLayout.recipeSlotForPointerIndex(15));
    }
}
