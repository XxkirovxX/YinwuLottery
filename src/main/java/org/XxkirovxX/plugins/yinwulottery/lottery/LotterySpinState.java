package org.XxkirovxX.plugins.yinwulottery.lottery;

import java.util.ArrayList;
import java.util.List;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeSnapshot;

final class LotterySpinState {
    private final List<RecipeSnapshot> displayedRecipes;
    private final LotteryRandom random;
    private final int stopMinFullCycles;
    private final int stopExtraRandomCycles;

    private int pointerIndex;
    private boolean stopping;
    private int remainingStepsToTarget;
    private int winningRecipeIndex = -1;
    private int targetPointerIndex = -1;
    private RecipeSnapshot winningRecipe;

    LotterySpinState(
        List<RecipeSnapshot> displayedRecipes,
        LotteryRandom random,
        int stopMinFullCycles,
        int stopExtraRandomCycles
    ) {
        this.displayedRecipes = List.copyOf(displayedRecipes);
        this.random = random;
        this.stopMinFullCycles = Math.max(0, stopMinFullCycles);
        this.stopExtraRandomCycles = Math.max(0, stopExtraRandomCycles);
    }

    static List<RecipeSnapshot> buildDisplayedRecipes(List<RecipeSnapshot> recipes, int visibleSlots, LotteryRandom random) {
        List<RecipeSnapshot> shuffled = new ArrayList<>(recipes);
        random.shuffle(shuffled);
        List<RecipeSnapshot> result = new ArrayList<>(visibleSlots);
        for (int i = 0; i < visibleSlots; i++) {
            result.add(shuffled.get(i % shuffled.size()));
        }
        return result;
    }

    void start() {
        if (winningRecipe != null) {
            return;
        }
        winningRecipeIndex = random.nextInt(displayedRecipes.size());
        winningRecipe = displayedRecipes.get(winningRecipeIndex);
        targetPointerIndex = LotteryLayout.pointerIndexForRecipeIndex(winningRecipeIndex);
    }

    void requestStop() {
        if (stopping) {
            return;
        }
        start();
        stopping = true;
        remainingStepsToTarget = random.nextInt(LotteryLayout.trackSlots().size());
        targetPointerIndex = (pointerIndex + remainingStepsToTarget) % LotteryLayout.trackSlots().size();
        winningRecipeIndex = LotteryLayout.recipeIndexForPointerIndex(targetPointerIndex);
        winningRecipe = displayedRecipes.get(winningRecipeIndex);
    }

    boolean advance() {
        if (stopping && remainingStepsToTarget <= 0) {
            return true;
        }
        pointerIndex = (pointerIndex + 1) % LotteryLayout.trackSlots().size();
        if (!stopping) {
            return false;
        }
        remainingStepsToTarget--;
        return remainingStepsToTarget <= 0;
    }

    int pointerIndex() {
        return pointerIndex;
    }

    boolean isStopping() {
        return stopping;
    }

    int remainingStepsToTarget() {
        return remainingStepsToTarget;
    }

    int winningRecipeIndex() {
        return winningRecipeIndex;
    }

    RecipeSnapshot winningRecipe() {
        return winningRecipe;
    }

    int targetPointerIndex() {
        return targetPointerIndex;
    }
}
