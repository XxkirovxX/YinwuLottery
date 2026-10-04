package org.XxkirovxX.plugins.yinwulottery.config;

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

@SuppressWarnings("deprecation")
public final class PluginConfig {
    private static final String DEFAULT_PREFIX = "&6[Yinwu抽奖]&r ";
    private static final Map<String, String> DEFAULT_MESSAGES = Map.ofEntries(
        Map.entry("player-only", "&c只有玩家可以使用该命令。"),
        Map.entry("no-permission", "&c你没有权限使用该命令。"),
        Map.entry("usage", "&e用法：/yw lottery"),
        Map.entry("reloaded", "&a配置已重载。"),
        Map.entry("no-recipes", "&cBreweryX 当前没有可抽取的酒方。"),
        Map.entry("economy-unavailable", "&c未找到可用的 Vault 经济服务。"),
        Map.entry("not-enough-money", "&c余额不足，需要 {cost}。"),
        Map.entry("charge-failed", "&c扣款失败：{reason}"),
        Map.entry("charged", "&a已支付 {cost}，抽奖开始。"),
        Map.entry("already-running", "&c你已经有一个正在进行的抽奖。"),
        Map.entry("click-stop", "&e再次点击灵魂营火停止转盘。"),
        Map.entry("stopping", "&e转盘正在减速..."),
        Map.entry("won", "&a你抽中了：{recipe}"),
        Map.entry("delivered", "&a酒方已发放。"),
        Map.entry("inventory-full-drop", "&e背包已满，酒方已掉落在你脚边。")
    );

    private final FileConfiguration config;

    public PluginConfig(FileConfiguration config) {
        this.config = config;
    }

    public double cost() {
        return config.getDouble("lottery.cost", 100.0D);
    }

    public long initialTickPeriod() {
        return Math.max(1L, config.getLong("lottery.initial-tick-period", 3L));
    }

    public int stoppingIncrements() {
        return Math.max(1, config.getInt("lottery.stopping-increments", 14));
    }

    public int stopMinFullCycles() {
        return Math.max(0, config.getInt("lottery.stop-min-full-cycles", 0));
    }

    public int stopExtraRandomCycles() {
        return Math.max(0, config.getInt("lottery.stop-extra-random-cycles", 0));
    }

    public long maxTickPeriod() {
        return Math.max(initialTickPeriod(), config.getLong("lottery.max-tick-period", 18L));
    }

    public boolean autoDeliverOnClose() {
        return config.getBoolean("lottery.auto-deliver-on-close", true);
    }

    public String bookTitlePrefix() {
        return color(config.getString("book.title-prefix", "酒方："));
    }

    public String bookAuthor() {
        return color(config.getString("book.author", "YinwuLottery"));
    }

    public int maxLinesPerPage() {
        return Math.max(6, config.getInt("book.max-lines-per-page", 12));
    }

    public String message(String key) {
        String prefix = color(config.getString("messages.prefix", DEFAULT_PREFIX));
        String fallback = DEFAULT_MESSAGES.getOrDefault(key, key);
        return prefix + color(config.getString("messages." + key, fallback));
    }

    public String message(String key, String placeholder, String value) {
        return message(key).replace(placeholder, value);
    }

    public String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
