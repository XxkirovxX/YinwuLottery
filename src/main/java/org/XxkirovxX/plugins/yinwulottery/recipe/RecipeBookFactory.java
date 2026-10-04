package org.XxkirovxX.plugins.yinwulottery.recipe;

import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public final class RecipeBookFactory {
    private final RecipeBookTextFormatter formatter;

    public RecipeBookFactory(PluginConfig config) {
        this.formatter = new RecipeBookTextFormatter(config);
    }

    public ItemStack createBook(RecipeSnapshot recipe) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle(formatter.title(recipe));
        meta.setAuthor(formatter.author());
        for (String page : formatter.pages(recipe)) {
            meta.addPage(page);
        }
        item.setItemMeta(meta);
        return item;
    }
}
