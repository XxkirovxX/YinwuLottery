package org.XxkirovxX.plugins.yinwulottery.lottery;

import java.util.List;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeSnapshot;

final class LotteryTextFormatter {
    private final PluginConfig config;

    LotteryTextFormatter(PluginConfig config) {
        this.config = config;
    }

    String inventoryTitle() {
        return config.color("&0Yinwu酒方抽奖");
    }

    String decorationName() {
        return config.color("&6装饰");
    }

    String startButtonName() {
        return config.color("&a开始抽奖");
    }

    List<String> startButtonLore() {
        return List.of(config.color("&7点击支付 &e" + config.cost() + " &7并开始抽奖。"));
    }

    String stopButtonName() {
        return config.color("&c停止转盘");
    }

    List<String> stopButtonLore() {
        return List.of(config.color("&7点击后转盘将在一圈内停止。"));
    }

    List<String> recipeLore() {
        return List.of(config.color("&7本轮可抽中这个展示格中的酒方。"));
    }

    List<String> recipeLore(RecipeSnapshot recipe) {
        if (recipe.lore().isEmpty()) {
            return recipeLore();
        }
        return recipe.lore().stream().map(config::color).toList();
    }

    String pointerName() {
        return config.color("&a当前指针");
    }
}
