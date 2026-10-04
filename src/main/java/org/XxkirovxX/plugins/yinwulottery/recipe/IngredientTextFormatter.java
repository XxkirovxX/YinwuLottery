package org.XxkirovxX.plugins.yinwulottery.recipe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class IngredientTextFormatter {
    private static final Map<String, String> MATERIAL_NAMES = createMaterialNames();

    private IngredientTextFormatter() {
    }

    static String format(String ingredient) {
        if (ingredient == null || ingredient.isBlank()) {
            return "";
        }
        String trimmed = ingredient.trim();
        int amount = 1;
        String itemId = trimmed;
        int separator = trimmed.lastIndexOf('/');
        if (separator > 0 && separator < trimmed.length() - 1) {
            itemId = trimmed.substring(0, separator);
            try {
                amount = Integer.parseInt(trimmed.substring(separator + 1));
            } catch (NumberFormatException ignored) {
                itemId = trimmed;
                amount = 1;
            }
        }
        return displayName(itemId) + " x" + amount;
    }

    private static String displayName(String itemId) {
        String normalized = itemId.toLowerCase(Locale.ROOT);
        String translated = MATERIAL_NAMES.get(normalized);
        if (translated != null) {
            return translated;
        }
        return itemId;
    }

    private static Map<String, String> createMaterialNames() {
        Map<String, String> names = new HashMap<>();
        names.put("wheat", "小麦");
        names.put("sweet_berries", "甜浆果");
        names.put("water_bucket", "水桶");
        names.put("apple", "苹果");
        names.put("sugar_cane", "甘蔗");
        names.put("potato", "马铃薯");
        names.put("red_mushroom", "红色蘑菇");
        names.put("brown_mushroom", "棕色蘑菇");
        names.put("cactus", "仙人掌");
        names.put("short_grass", "矮草丛");
        names.put("grass", "草");
        names.put("poisonous_potato", "毒马铃薯");
        names.put("cocoa_beans", "可可豆");
        names.put("milk_bucket", "牛奶桶");
        names.put("egg", "鸡蛋");
        names.put("sugar", "糖");
        names.put("gold_nugget", "金粒");
        names.put("blaze_powder", "烈焰粉");
        names.put("cookie", "曲奇");
        names.put("snowball", "雪球");
        return names;
    }
}
