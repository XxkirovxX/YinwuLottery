package org.XxkirovxX.plugins.yinwulottery.command;

import java.util.ArrayList;
import java.util.List;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.XxkirovxX.plugins.yinwulottery.lottery.LotteryService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class YinwuLotteryCommand implements CommandExecutor, TabCompleter {
    private final PluginConfig config;
    private final LotteryService lotteryService;
    private final Runnable reloadAction;

    public YinwuLotteryCommand(PluginConfig config, LotteryService lotteryService, Runnable reloadAction) {
        this.config = config;
        this.lotteryService = lotteryService;
        this.reloadAction = reloadAction;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("lottery")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(config.message("player-only"));
                return true;
            }
            if (!player.hasPermission("yinwulottery.command.lottery")) {
                player.sendMessage(config.message("no-permission"));
                return true;
            }
            lotteryService.open(player);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("yinwulottery.admin.reload")) {
                sender.sendMessage(config.message("no-permission"));
                return true;
            }
            reloadAction.run();
            sender.sendMessage(config.message("reloaded"));
            return true;
        }
        sender.sendMessage(config.message("usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> completions = new ArrayList<>();
        if ("lottery".startsWith(args[0].toLowerCase()) && sender.hasPermission("yinwulottery.command.lottery")) {
            completions.add("lottery");
        }
        if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("yinwulottery.admin.reload")) {
            completions.add("reload");
        }
        return completions;
    }
}
