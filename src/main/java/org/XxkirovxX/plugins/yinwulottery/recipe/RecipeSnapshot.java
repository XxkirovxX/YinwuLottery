package org.XxkirovxX.plugins.yinwulottery.recipe;

import java.util.List;

public record RecipeSnapshot(
    String id,
    String displayName,
    List<String> qualityNames,
    List<String> ingredients,
    int difficulty,
    int cookingTime,
    int distillRuns,
    int distillTimeTicks,
    List<String> barrelTypes,
    int age,
    String color,
    int alcohol,
    List<String> lore
) {
}
