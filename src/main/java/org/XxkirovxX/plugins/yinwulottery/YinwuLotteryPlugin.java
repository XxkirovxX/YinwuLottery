package org.XxkirovxX.plugins.yinwulottery;

import org.XxkirovxX.plugins.yinwulottery.command.YinwuLotteryCommand;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.XxkirovxX.plugins.yinwulottery.economy.EconomyService;
import org.XxkirovxX.plugins.yinwulottery.lottery.LotteryService;
import org.XxkirovxX.plugins.yinwulottery.recipe.BreweryRecipeService;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeBookFactory;
import org.XxkirovxX.plugins.yinwulottery.scheduler.PluginScheduler;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class YinwuLotteryPlugin extends JavaPlugin {
    private LotteryService lotteryService;
    private PluginConfig pluginConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initializeServices();
    }

    @Override
    public void onDisable() {
        if (lotteryService != null) {
            lotteryService.shutdown();
        }
    }

    private void initializeServices() {
        pluginConfig = new PluginConfig(getConfig());
        PluginScheduler scheduler = new PluginScheduler(this);
        BreweryRecipeService recipeService = new BreweryRecipeService(getLogger());
        RecipeBookFactory bookFactory = new RecipeBookFactory(pluginConfig);
        EconomyService economyService = new EconomyService(getServer().getServicesManager());
        lotteryService = new LotteryService(pluginConfig, recipeService, bookFactory, economyService, scheduler);
        getServer().getPluginManager().registerEvents(lotteryService, this);
        registerCommand();
    }

    private void registerCommand() {
        PluginCommand command = getCommand("yw");
        if (command == null) {
            throw new IllegalStateException("Command 'yw' is missing from plugin.yml");
        }
        YinwuLotteryCommand executor = new YinwuLotteryCommand(pluginConfig, lotteryService, this::reload);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void reload() {
        reloadConfig();
        if (lotteryService != null) {
            lotteryService.shutdown();
            HandlerList.unregisterAll(lotteryService);
        }
        initializeServices();
    }
}
