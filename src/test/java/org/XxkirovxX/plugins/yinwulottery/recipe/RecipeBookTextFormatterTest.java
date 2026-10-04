package org.XxkirovxX.plugins.yinwulottery.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class RecipeBookTextFormatterTest {
    @Test
    void formatterUsesUtf8ChineseLabelsAndCarriesRecipeLore() {
        RecipeBookTextFormatter formatter = new RecipeBookTextFormatter(new PluginConfig(new YamlConfiguration()));
        RecipeSnapshot recipe = new RecipeSnapshot(
            "rice-wine",
            "清酒",
            List.of("佳酿", "极品"),
            List.of("rice/3", "water_bucket/1"),
            4,
            120,
            2,
            400,
            List.of("OAK"),
            7,
            "WHITE",
            12,
            List.of("入口绵柔", "回甘明显")
        );

        assertEquals("酒方：清酒", formatter.title(recipe));
        assertEquals("YinwuLottery", formatter.author());

        List<String> pages = formatter.pages(recipe);
        assertEquals(2, pages.size());
        String content = String.join("\n", pages);
        assertTrue(content.contains("酒名: 清酒"));
        assertTrue(content.contains("品质名: 佳酿 / 极品"));
        assertTrue(content.contains("蒸馏次数: 2"));
        assertTrue(content.contains("- 水桶 x1"));
        assertTrue(content.contains("备注:"));
        assertTrue(content.contains("- 入口绵柔"));
    }

    @Test
    void formatterTranslatesVanillaIngredientConfigNamesToChineseDisplayNames() {
        RecipeBookTextFormatter formatter = new RecipeBookTextFormatter(new PluginConfig(new YamlConfiguration()));
        RecipeSnapshot recipe = new RecipeSnapshot(
            "beer",
            "啤酒",
            List.of(),
            List.of("wheat/3", "sweet_berries/5", "Brewery:Grape/2"),
            1,
            60,
            0,
            0,
            List.of(),
            0,
            "YELLOW",
            4,
            List.of()
        );

        String content = String.join("\n", formatter.pages(recipe));

        assertTrue(content.contains("- 小麦 x3"));
        assertTrue(content.contains("- 甜浆果 x5"));
        assertTrue(content.contains("- Brewery:Grape x2"));
    }
}
