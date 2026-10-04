package org.XxkirovxX.plugins.yinwulottery.lottery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeSnapshot;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class LotteryTextFormatterTest {
    @Test
    void formatterUsesYinwuBrandAndUtf8ChineseUiText() {
        LotteryTextFormatter formatter = new LotteryTextFormatter(new PluginConfig(new YamlConfiguration()));

        assertEquals("§0Yinwu酒方抽奖", formatter.inventoryTitle());
        assertEquals("§6装饰", formatter.decorationName());
        assertEquals("§a开始抽奖", formatter.startButtonName());
        assertEquals(List.of("§7点击支付 §e100.0 §7并开始抽奖。"), formatter.startButtonLore());
        assertEquals("§c停止转盘", formatter.stopButtonName());
        assertEquals(List.of("§7点击后转盘将在一圈内停止。"), formatter.stopButtonLore());
        assertEquals(List.of("§7本轮可抽中这个展示格中的酒方。"), formatter.recipeLore());
        assertEquals("§a当前指针", formatter.pointerName());
    }

    @Test
    void recipePaperLoreUsesBreweryRecipeLoreWhenPresent() {
        LotteryTextFormatter formatter = new LotteryTextFormatter(new PluginConfig(new YamlConfiguration()));
        RecipeSnapshot recipe = new RecipeSnapshot(
            "test",
            "test",
            List.of(),
            List.of(),
            0,
            0,
            0,
            0,
            List.of(),
            0,
            "WHITE",
            0,
            List.of("&7清爽", "&e入口柔和")
        );

        assertEquals(List.of("§7清爽", "§e入口柔和"), formatter.recipeLore(recipe));
    }
}
