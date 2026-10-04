package org.XxkirovxX.plugins.yinwulottery.lottery;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

interface LotteryUiSession {
    UUID playerId();

    Inventory inventory();

    boolean isRunning();

    boolean isStopping();

    boolean hasPendingPrize();

    void start();

    void requestStop();

    boolean handleClick(Player player, InventoryClickEvent event);

    void close(Player player);

    void cancelTask();

    boolean shouldExpire();
}
