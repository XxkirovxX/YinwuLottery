package org.XxkirovxX.plugins.yinwulottery.util;

import org.bukkit.Material;

public final class Materials {
    private Materials() {
    }

    public static Material firstAvailable(Material fallback, String... names) {
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                return material;
            }
        }
        return fallback;
    }
}
