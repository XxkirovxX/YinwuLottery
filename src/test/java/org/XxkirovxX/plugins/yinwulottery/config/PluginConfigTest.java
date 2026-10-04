package org.XxkirovxX.plugins.yinwulottery.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PluginConfigTest {
    @Test
    void stopCycleSettingsDefaultToFastVisibleResult() {
        PluginConfig config = new PluginConfig(new YamlConfiguration());

        assertEquals(0, config.stopMinFullCycles());
        assertEquals(0, config.stopExtraRandomCycles());
    }

    @Test
    void messageDefaultsUseYinwuBrandAndUtf8ChineseText() {
        PluginConfig config = new PluginConfig(new YamlConfiguration());

        assertEquals("酒方：", config.bookTitlePrefix());
        assertEquals("§6[Yinwu抽奖]§r §c只有玩家可以使用该命令。", config.message("player-only"));
        assertEquals("§6[Yinwu抽奖]§r §c余额不足，需要 100.0。", config.message("not-enough-money").replace("{cost}", "100.0"));
    }
}
