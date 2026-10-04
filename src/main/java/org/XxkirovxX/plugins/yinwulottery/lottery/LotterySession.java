package org.XxkirovxX.plugins.yinwulottery.lottery;

import java.util.List;
import java.util.UUID;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeBookFactory;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeSnapshot;
import org.XxkirovxX.plugins.yinwulottery.scheduler.PluginScheduler;
import org.XxkirovxX.plugins.yinwulottery.util.ItemBuilder;
import org.XxkirovxX.plugins.yinwulottery.util.Materials;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class LotterySession implements LotteryUiSession {
    private final UUID playerId;
    private final Player player;
    private final Inventory inventory;
    private final PluginConfig config;
    private final PluginScheduler scheduler;
    private final RecipeBookFactory bookFactory;
    private final List<RecipeSnapshot> displayedRecipes;
    private final LotterySpinState spinState;
    private final LotteryTextFormatter textFormatter;

    private PluginScheduler.TaskHandle taskHandle;
    private long currentPeriod;
    private boolean running;
    private ItemStack winningBook;
    private boolean delivered;

    public LotterySession(
        Player player,
        PluginConfig config,
        PluginScheduler scheduler,
        RecipeBookFactory bookFactory,
        List<RecipeSnapshot> recipes
    ) {
        this(player, player.getServer(), config, scheduler, bookFactory, recipes, new ThreadLocalLotteryRandom());
    }

    LotterySession(
        Player player,
        Server server,
        PluginConfig config,
        PluginScheduler scheduler,
        RecipeBookFactory bookFactory,
        List<RecipeSnapshot> recipes,
        LotteryRandom random
    ) {
        this.playerId = player.getUniqueId();
        this.player = player;
        this.config = config;
        this.scheduler = scheduler;
        this.bookFactory = bookFactory;
        this.displayedRecipes = LotterySpinState.buildDisplayedRecipes(recipes, LotteryLayout.recipeSlots().size(), random);
        this.spinState = new LotterySpinState(displayedRecipes, random, config.stopMinFullCycles(), config.stopExtraRandomCycles());
        this.textFormatter = new LotteryTextFormatter(config);
        this.inventory = server.createInventory(null, LotteryLayout.SIZE, textFormatter.inventoryTitle());
        this.currentPeriod = config.initialTickPeriod();
        drawBase();
    }

    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    public Inventory inventory() {
        return inventory;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isStopping() {
        return spinState.isStopping();
    }

    @Override
    public boolean hasPendingPrize() {
        return winningBook != null && !delivered;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        winningBook = null;
        delivered = false;
        spinState.start();
        currentPeriod = config.initialTickPeriod();
        running = true;
        inventory.setItem(LotteryLayout.RESULT_SLOT, null);
        inventory.setItem(LotteryLayout.START_BUTTON_SLOT, stopButton());
        scheduleNext();
    }

    @Override
    public void requestStop() {
        if (!running || spinState.isStopping()) {
            return;
        }
        spinState.requestStop();
    }

    @Override
    public boolean handleClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot == LotteryLayout.START_BUTTON_SLOT) {
            return false;
        }
        if (slot == LotteryLayout.RESULT_SLOT && winningBook != null && !delivered) {
            deliver(player);
            inventory.setItem(LotteryLayout.RESULT_SLOT, null);
        }
        return true;
    }

    @Override
    public void close(Player player) {
        cancelTask();
        if (running && winningBook == null) {
            finish();
        }
        if (!delivered && winningBook != null && config.autoDeliverOnClose()) {
            deliver(player);
        }
    }

    @Override
    public void cancelTask() {
        if (taskHandle != null) {
            taskHandle.cancel();
            taskHandle = null;
        }
    }

    @Override
    public boolean shouldExpire() {
        return delivered;
    }

    private void tick() {
        clearPointer();
        boolean complete = spinState.advance();
        drawPointer();
        if (spinState.isStopping()) {
            currentPeriod = Math.min(config.maxTickPeriod(), currentPeriod + 1);
            if (complete) {
                finish();
                return;
            }
        }
        scheduleNext();
    }

    private void finish() {
        running = false;
        winningBook = bookFactory.createBook(spinState.winningRecipe());
        inventory.setItem(LotteryLayout.RESULT_SLOT, winningBook);
        inventory.setItem(LotteryLayout.START_BUTTON_SLOT, startButton());
        player.sendMessage(config.message("won").replace("{recipe}", spinState.winningRecipe().displayName()));
    }

    private void scheduleNext() {
        taskHandle = scheduler.runEntityLater(player, this::tick, currentPeriod);
    }

    private void drawBase() {
        ItemStack copper = ItemBuilder.of(Materials.firstAvailable(Material.COPPER_BLOCK, "COPPER_BARS", "COPPER_GRATE", "WAXED_COPPER_GRATE"))
            .name(textFormatter.decorationName())
            .build();
        ItemStack black = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int slot : LotteryLayout.copperSlots()) {
            inventory.setItem(slot, copper);
        }
        for (int slot : LotteryLayout.trackSlots()) {
            inventory.setItem(slot, black);
        }
        for (int i = 0; i < LotteryLayout.recipeSlots().size(); i++) {
            inventory.setItem(LotteryLayout.recipeSlots().get(i), recipeIcon(displayedRecipes.get(i)));
        }
        inventory.setItem(LotteryLayout.START_BUTTON_SLOT, startButton());
        drawPointer();
    }

    private ItemStack recipeIcon(RecipeSnapshot recipe) {
        return ItemBuilder.of(Material.PAPER)
            .name(config.color("&f" + recipe.displayName()))
            .lore(textFormatter.recipeLore(recipe))
            .build();
    }

    private ItemStack startButton() {
        return ItemBuilder.of(Material.CAMPFIRE)
            .name(textFormatter.startButtonName())
            .lore(textFormatter.startButtonLore())
            .build();
    }

    private ItemStack stopButton() {
        return ItemBuilder.of(Material.SOUL_CAMPFIRE)
            .name(textFormatter.stopButtonName())
            .lore(textFormatter.stopButtonLore())
            .build();
    }

    private void clearPointer() {
        inventory.setItem(
            LotteryLayout.trackSlots().get(spinState.pointerIndex()),
            ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build()
        );
    }

    private void drawPointer() {
        inventory.setItem(
            LotteryLayout.trackSlots().get(spinState.pointerIndex()),
            ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).name(textFormatter.pointerName()).build()
        );
    }

    private void deliver(Player player) {
        if (winningBook == null || delivered) {
            return;
        }
        ItemStack prize = winningBook.clone();
        scheduler.runEntity(player, () -> deliverOnEntityThread(player, prize));
    }

    private void deliverOnEntityThread(Player player, ItemStack prize) {
        var leftovers = player.getInventory().addItem(prize);
        if (!leftovers.isEmpty()) {
            for (ItemStack item : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            player.sendMessage(config.message("inventory-full-drop"));
        } else {
            player.sendMessage(config.message("delivered"));
        }
        delivered = true;
    }
}
