package org.XxkirovxX.plugins.yinwulottery.recipe;

import java.util.ArrayList;
import java.util.List;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;

final class RecipeBookTextFormatter {
    private final PluginConfig config;

    RecipeBookTextFormatter(PluginConfig config) {
        this.config = config;
    }

    String title(RecipeSnapshot recipe) {
        String title = config.bookTitlePrefix() + recipe.displayName();
        return title.length() <= 32 ? title : title.substring(0, 32);
    }

    String author() {
        return config.bookAuthor();
    }

    List<String> pages(RecipeSnapshot recipe) {
        List<String> pages = new ArrayList<>();
        int maxLines = config.maxLinesPerPage();
        StringBuilder page = new StringBuilder();
        int lineCount = 0;
        for (String line : lines(recipe)) {
            if (lineCount >= maxLines) {
                pages.add(page.toString());
                page = new StringBuilder();
                lineCount = 0;
            }
            page.append(line).append('\n');
            lineCount++;
        }
        if (!page.isEmpty()) {
            pages.add(page.toString());
        }
        return pages;
    }

    private List<String> lines(RecipeSnapshot recipe) {
        List<String> lines = new ArrayList<>();
        lines.add("酒名: " + recipe.displayName());
        if (!recipe.id().isBlank()) {
            lines.add("ID: " + recipe.id());
        }
        if (!recipe.qualityNames().isEmpty()) {
            lines.add("品质名: " + String.join(" / ", recipe.qualityNames()));
        }
        lines.add("颜色: " + recipe.color());
        lines.add("酒精度: " + recipe.alcohol());
        lines.add("难度: " + recipe.difficulty());
        lines.add("煮锅时间: " + recipe.cookingTime() + " 秒");
        lines.add("蒸馏次数: " + recipe.distillRuns());
        lines.add("单次蒸馏: " + (recipe.distillTimeTicks() / 20) + " 秒");
        lines.add("陈酿天数: " + recipe.age());
        lines.add("木桶: " + (recipe.barrelTypes().isEmpty() ? "任意" : String.join(", ", recipe.barrelTypes())));
        lines.add("原料:");
        for (String ingredient : recipe.ingredients()) {
            lines.add("- " + IngredientTextFormatter.format(ingredient));
        }
        if (!recipe.lore().isEmpty()) {
            lines.add("备注:");
            for (String lore : recipe.lore()) {
                lines.add("- " + lore);
            }
        }
        return lines;
    }
}
