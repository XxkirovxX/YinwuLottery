package org.XxkirovxX.plugins.yinwulottery.lottery;

import java.util.List;

public final class LotteryLayout {
    public static final int SIZE = 54;
    public static final int START_BUTTON_SLOT = slot(4, 5);
    public static final int RESULT_SLOT = slot(3, 5);
    public static final int INITIAL_POINTER_SLOT = slot(5, 5);

    private static final List<Integer> RECIPE_SLOTS = List.of(
        slot(1, 3), slot(1, 4), slot(1, 5), slot(1, 6), slot(1, 7),
        slot(2, 9), slot(3, 9), slot(4, 9), slot(5, 9),
        slot(6, 7), slot(6, 6), slot(6, 5), slot(6, 4), slot(6, 3),
        slot(5, 1), slot(4, 1), slot(3, 1), slot(2, 1)
    );

    private static final List<Integer> COPPER_SLOTS = List.of(
        slot(1, 1), slot(1, 2), slot(1, 8), slot(1, 9),
        slot(6, 1), slot(6, 2), slot(6, 8), slot(6, 9)
    );

    private static final List<Integer> TRACK_SLOTS = List.of(
        slot(5, 5), slot(5, 6), slot(5, 7), slot(5, 8),
        slot(4, 8), slot(3, 8), slot(2, 8),
        slot(2, 7), slot(2, 6), slot(2, 5), slot(2, 4), slot(2, 3), slot(2, 2),
        slot(3, 2), slot(4, 2),
        slot(5, 2), slot(5, 3), slot(5, 4)
    );

    private LotteryLayout() {
    }

    public static int slot(int row, int column) {
        if (row < 1 || row > 6 || column < 1 || column > 9) {
            throw new IllegalArgumentException("Inventory coordinates are 1-based and must be within 6x9");
        }
        return (row - 1) * 9 + (column - 1);
    }

    public static List<Integer> recipeSlots() {
        return RECIPE_SLOTS;
    }

    public static List<Integer> copperSlots() {
        return COPPER_SLOTS;
    }

    public static List<Integer> trackSlots() {
        return TRACK_SLOTS;
    }

    public static int recipeSlotForPointerIndex(int pointerIndex) {
        return RECIPE_SLOTS.get(recipeIndexForPointerIndex(pointerIndex));
    }

    public static int recipeIndexForPointerIndex(int pointerIndex) {
        int normalized = Math.floorMod(pointerIndex, TRACK_SLOTS.size());
        int pointerSlot = TRACK_SLOTS.get(normalized);
        int pointerRow = pointerSlot / 9;
        int pointerColumn = pointerSlot % 9;
        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < RECIPE_SLOTS.size(); i++) {
            int recipeSlot = RECIPE_SLOTS.get(i);
            int recipeRow = recipeSlot / 9;
            int recipeColumn = recipeSlot % 9;
            int distance = Math.abs(recipeRow - pointerRow) + Math.abs(recipeColumn - pointerColumn);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public static int pointerIndexForRecipeIndex(int recipeIndex) {
        int normalized = Math.floorMod(recipeIndex, RECIPE_SLOTS.size());
        int recipeSlot = RECIPE_SLOTS.get(normalized);
        int recipeRow = recipeSlot / 9;
        int recipeColumn = recipeSlot % 9;
        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < TRACK_SLOTS.size(); i++) {
            int trackSlot = TRACK_SLOTS.get(i);
            int trackRow = trackSlot / 9;
            int trackColumn = trackSlot % 9;
            int distance = Math.abs(trackRow - recipeRow) + Math.abs(trackColumn - recipeColumn);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public static boolean isControlledSlot(int slot) {
        return slot >= 0 && slot < SIZE;
    }
}
