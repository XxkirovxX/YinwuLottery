package org.XxkirovxX.plugins.yinwulottery.lottery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.XxkirovxX.plugins.yinwulottery.economy.EconomyService;
import org.XxkirovxX.plugins.yinwulottery.recipe.BreweryRecipeService;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeBookFactory;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeSnapshot;
import org.XxkirovxX.plugins.yinwulottery.scheduler.PluginScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public final class LotteryService implements Listener {
    private final PluginConfig config;
    private final EconomyService economyService;
    private final PluginScheduler scheduler;
    private final Supplier<List<RecipeSnapshot>> recipeSupplier;
    private final SessionFactory sessionFactory;
    private final PlayerExecutor playerExecutor;
    private final Map<UUID, LotteryUiSession> sessions = new HashMap<>();

    public LotteryService(
        PluginConfig config,
        BreweryRecipeService recipeService,
        RecipeBookFactory bookFactory,
        EconomyService economyService,
        PluginScheduler scheduler
    ) {
        this(
            config,
            recipeService::snapshots,
            economyService,
            scheduler,
            scheduler::runEntity,
            (player, recipes) -> new LotterySession(player, config, scheduler, bookFactory, recipes)
        );
    }

    LotteryService(
        PluginConfig config,
        Supplier<List<RecipeSnapshot>> recipeSupplier,
        EconomyService economyService,
        PluginScheduler scheduler,
        PlayerExecutor playerExecutor,
        SessionFactory sessionFactory
    ) {
        this.config = config;
        this.economyService = economyService;
        this.scheduler = scheduler;
        this.recipeSupplier = recipeSupplier;
        this.sessionFactory = sessionFactory;
        this.playerExecutor = playerExecutor;
    }

    public void open(Player player) {
        playerExecutor.execute(player, () -> openOnEntityThread(player));
    }

    public void shutdown() {
        for (LotteryUiSession session : sessions.values()) {
            session.cancelTask();
        }
        sessions.clear();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        LotteryUiSession session = sessions.get(player.getUniqueId());
        if (session == null || !sameInventory(session.inventory(), event.getView().getTopInventory())) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot >= session.inventory().getSize()) {
            return;
        }
        if (slot == LotteryLayout.START_BUTTON_SLOT) {
            if (session.hasPendingPrize()) {
                return;
            }
            if (!session.isRunning()) {
                EconomyService.PaymentResult payment = economyService.withdraw(player, config.cost());
                if (!payment.available()) {
                    player.sendMessage(config.message("economy-unavailable"));
                    return;
                }
                if (!payment.success()) {
                    String key = payment.reason().toLowerCase().contains("insufficient") ? "not-enough-money" : "charge-failed";
                    player.sendMessage(config.message(key).replace("{cost}", String.valueOf(config.cost())).replace("{reason}", payment.reason()));
                    return;
                }
                player.sendMessage(config.message("charged").replace("{cost}", String.valueOf(config.cost())));
                session.start();
            } else if (!session.isStopping()) {
                player.sendMessage(config.message("stopping"));
                session.requestStop();
            } else {
                player.sendMessage(config.message("click-stop"));
            }
            return;
        }
        session.handleClick(player, event);
        if (session.shouldExpire()) {
            sessions.remove(player.getUniqueId());
            scheduler.runEntity(player, player::closeInventory);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        LotteryUiSession session = sessions.get(player.getUniqueId());
        if (session != null && sameInventory(session.inventory(), event.getInventory())) {
            session.close(player);
            if (session.shouldExpire()) {
                sessions.remove(player.getUniqueId());
            }
        }
    }

    private boolean sameInventory(Inventory first, Inventory second) {
        return first == second;
    }

    private void openOnEntityThread(Player player) {
        LotteryUiSession existing = sessions.get(player.getUniqueId());
        if (existing != null && !existing.shouldExpire()) {
            player.openInventory(existing.inventory());
            return;
        }
        sessions.remove(player.getUniqueId());
        List<RecipeSnapshot> recipes = recipeSupplier.get();
        if (recipes.isEmpty()) {
            player.sendMessage(config.message("no-recipes"));
            return;
        }
        LotteryUiSession session = sessionFactory.create(player, recipes);
        sessions.put(player.getUniqueId(), session);
        player.openInventory(session.inventory());
    }

    interface SessionFactory {
        LotteryUiSession create(Player player, List<RecipeSnapshot> recipes);
    }

    interface PlayerExecutor {
        void execute(Player player, Runnable task);
    }
}
