package org.XxkirovxX.plugins.yinwulottery.recipe;

import com.dre.brewery.BarrelWoodType;
import com.dre.brewery.recipe.BRecipe;
import com.dre.brewery.recipe.RecipeItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BreweryRecipeService {
    private final Logger logger;

    public BreweryRecipeService(Logger logger) {
        this.logger = logger;
    }

    public List<RecipeSnapshot> snapshots() {
        List<BRecipe> recipes = BRecipe.getAllRecipes();
        if (recipes == null || recipes.isEmpty()) {
            return Collections.emptyList();
        }
        List<RecipeSnapshot> snapshots = new ArrayList<>(recipes.size());
        for (BRecipe recipe : recipes) {
            try {
                snapshots.add(snapshot(recipe));
            } catch (RuntimeException ex) {
                logger.log(Level.WARNING, "Failed to snapshot BreweryX recipe: " + recipe, ex);
            }
        }
        return snapshots;
    }

    private RecipeSnapshot snapshot(BRecipe recipe) {
        List<String> lore = recipe.getLoreForQuality(10);
        if (lore == null) {
            lore = Collections.emptyList();
        }
        return new RecipeSnapshot(
            nullToEmpty(recipe.getId()),
            recipe.getRecipeName(),
            qualityNames(recipe),
            ingredients(recipe),
            recipe.getDifficulty(),
            recipe.getCookingTime(),
            recipe.getDistillruns(),
            recipe.getDistillTime(),
            barrelTypes(recipe),
            recipe.getAge(),
            recipe.getColor().toString(),
            recipe.getAlcohol(),
            lore
        );
    }

    private List<String> qualityNames(BRecipe recipe) {
        String[] names = recipe.getName();
        if (names == null || names.length == 0) {
            return List.of(recipe.getRecipeName());
        }
        List<String> result = new ArrayList<>(names.length);
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                result.add(name);
            }
        }
        return result.isEmpty() ? List.of(recipe.getRecipeName()) : result;
    }

    private List<String> ingredients(BRecipe recipe) {
        List<RecipeItem> raw = recipe.getIngredients();
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        return raw.stream().filter(Objects::nonNull).map(RecipeItem::toConfigString).toList();
    }

    private List<String> barrelTypes(BRecipe recipe) {
        List<BarrelWoodType> raw = recipe.getBarrelTypes();
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        return raw.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
