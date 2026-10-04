package org.XxkirovxX.plugins.yinwulottery.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@SuppressWarnings("deprecation")
public final class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    private ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(color(name));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        List<String> colored = new ArrayList<>(lore.size());
        for (String line : lore) {
            colored.add(color(line));
        }
        meta.setLore(colored);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    private static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
