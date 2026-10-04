package org.XxkirovxX.plugins.yinwulottery.lottery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeSnapshot;
import org.junit.jupiter.api.Test;

class LotterySpinStateTest {
    @Test
    void startPreselectsTentativeSlotAndStopReplacesWinnerWithLandingSlot() {
        List<RecipeSnapshot> displayed = displayedRecipes(18);
        LotterySpinState state = new LotterySpinState(displayed, new SequenceRandom(List.of(5, 1)), 0, 0);

        state.start();
        RecipeSnapshot preselected = state.winningRecipe();

        state.requestStop();

        assertNotNull(preselected);
        assertSame(displayed.get(LotteryLayout.recipeIndexForPointerIndex(1)), state.winningRecipe());
        assertEquals(LotteryLayout.recipeIndexForPointerIndex(1), state.winningRecipeIndex());
        assertTrue(state.isStopping());
    }

    @Test
    void stopIgnoresConfiguredFullCyclesAndFinishesWithinOnePointerCycle() {
        List<RecipeSnapshot> displayed = displayedRecipes(18);
        LotterySpinState state = new LotterySpinState(displayed, new SequenceRandom(List.of(3, 1)), 1, 1);

        state.start();
        state.requestStop();

        assertEquals(1, state.remainingStepsToTarget());

        int safety = 0;
        while (!state.advance()) {
            safety++;
            assertTrue(safety < LotteryLayout.trackSlots().size(), "spin should finish within one pointer cycle");
        }

        assertEquals(state.targetPointerIndex(), state.pointerIndex());
        assertSame(displayed.get(LotteryLayout.recipeIndexForPointerIndex(state.pointerIndex())), state.winningRecipe());
    }

    @Test
    void stopLandsOnPointerThatVisuallyTargetsWinningRecipe() {
        List<RecipeSnapshot> displayed = displayedRecipes(18);
        LotterySpinState state = new LotterySpinState(displayed, new SequenceRandom(List.of(8)), 0, 0);

        state.start();
        state.requestStop();

        int safety = 0;
        while (!state.advance()) {
            safety++;
            assertTrue(safety < 30, "spin should finish without full stop cycles");
        }

        int pointedRecipeSlot = LotteryLayout.recipeSlotForPointerIndex(state.pointerIndex());
        int winningRecipeSlot = LotteryLayout.recipeSlots().get(state.winningRecipeIndex());
        assertEquals(winningRecipeSlot, pointedRecipeSlot);
        assertSame(displayed.get(state.winningRecipeIndex()), state.winningRecipe());
    }

    @Test
    void stopRandomizesLandingWithinOneVisiblePointerCycle() {
        List<RecipeSnapshot> displayed = displayedRecipes(18);
        LotterySpinState state = new LotterySpinState(displayed, new SequenceRandom(List.of(3, 17)), 0, 0);

        state.start();
        state.requestStop();

        assertEquals(17, state.remainingStepsToTarget());

        int safety = 0;
        while (!state.advance()) {
            safety++;
            assertTrue(safety < LotteryLayout.trackSlots().size(), "stop should finish within one pointer cycle");
        }

        assertEquals(17, state.pointerIndex());
        int winningRecipeSlot = LotteryLayout.recipeSlots().get(state.winningRecipeIndex());
        int pointedRecipeSlot = LotteryLayout.recipeSlotForPointerIndex(state.pointerIndex());
        assertEquals(pointedRecipeSlot, winningRecipeSlot);
        assertSame(displayed.get(state.winningRecipeIndex()), state.winningRecipe());
    }

    @Test
    void stopWithoutFullCyclesUsesShortestForwardPathToTarget() {
        List<RecipeSnapshot> displayed = displayedRecipes(18);
        LotterySpinState state = new LotterySpinState(displayed, new SequenceRandom(List.of(11, 0)), 0, 0);

        state.start();
        state.requestStop();

        assertEquals(0, state.remainingStepsToTarget());
        assertTrue(state.advance());
        assertEquals(0, state.pointerIndex());
    }

    @Test
    void visibleSlotSelectionKeepsDuplicateDisplayProbability() {
        RecipeSnapshot common = recipe("common");
        RecipeSnapshot rare = recipe("rare");
        List<RecipeSnapshot> displayed = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            displayed.add(common);
        }
        displayed.add(rare);

        LotterySpinState state = new LotterySpinState(displayed, new SequenceRandom(List.of(17, 0)), 1, 0);

        state.start();

        assertSame(rare, state.winningRecipe());
        assertEquals(17, state.winningRecipeIndex());
    }

    @Test
    void poolBuilderRepeatsRecipesToFillVisibleSlots() {
        List<RecipeSnapshot> pool = List.of(recipe("a"), recipe("b"), recipe("c"));
        List<RecipeSnapshot> displayed = LotterySpinState.buildDisplayedRecipes(pool, 18, new SequenceRandom(List.of()));

        assertEquals(18, displayed.size());
        assertSame(pool.get(0), displayed.get(0));
        assertSame(pool.get(1), displayed.get(1));
        assertSame(pool.get(2), displayed.get(2));
        assertSame(pool.get(0), displayed.get(3));
    }

    private static List<RecipeSnapshot> displayedRecipes(int count) {
        List<RecipeSnapshot> recipes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            recipes.add(recipe("recipe-" + i));
        }
        return recipes;
    }

    private static RecipeSnapshot recipe(String id) {
        return new RecipeSnapshot(id, id, List.of(), List.of(), 0, 0, 0, 0, List.of(), 0, "WHITE", 0, List.of());
    }

    private static final class SequenceRandom implements LotteryRandom {
        private final Deque<Integer> values;

        private SequenceRandom(List<Integer> values) {
            this.values = new ArrayDeque<>(values);
        }

        @Override
        public int nextInt(int bound) {
            if (values.isEmpty()) {
                return 0;
            }
            return Math.floorMod(values.removeFirst(), bound);
        }

        @Override
        public <T> void shuffle(List<T> values) {
        }
    }
}
