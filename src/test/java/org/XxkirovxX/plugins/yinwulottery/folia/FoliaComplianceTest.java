package org.XxkirovxX.plugins.yinwulottery.folia;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class FoliaComplianceTest {
    @Test
    void productionCodeDoesNotUseLegacySchedulingOrUnsafeInternals() throws IOException {
        String source = productionSource();

        assertFalse(source.contains("Bukkit.getScheduler("));
        assertFalse(source.contains("runTask("));
        assertFalse(source.contains("runTaskLater("));
        assertFalse(source.contains("runTaskTimer("));
        assertFalse(source.contains("runTaskAsynchronously("));
        assertFalse(source.contains(".runTask"));
        assertFalse(source.contains("Bukkit."));
        assertFalse(source.contains("java.lang.reflect"));
        assertFalse(source.contains("net.minecraft"));
        assertFalse(source.contains("craftbukkit"));
    }

    @Test
    void mavenDependencyUsesPaperApiInsteadOfSpigotApi() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);

        assertTrue(pom.contains("<artifactId>paper-api</artifactId>"));
        assertFalse(pom.contains("<artifactId>spigot-api</artifactId>"));
    }

    @Test
    void pluginMetadataDeclaresFoliaAndRequiredPluginAdapters() throws IOException {
        String pluginYml = Files.readString(Path.of("src/main/resources/plugin.yml"), StandardCharsets.UTF_8);

        assertTrue(pluginYml.contains("folia-supported: true"));
        assertTrue(pluginYml.contains("  - BreweryX"));
        assertTrue(pluginYml.contains("  - Vault"));
    }

    @Test
    void schedulerUsesEntitySchedulerAndNeverSchedulesZeroDelay() throws IOException {
        String scheduler = Files.readString(
            Path.of("src/main/java/org/XxkirovxX/plugins/yinwulottery/scheduler/PluginScheduler.java"),
            StandardCharsets.UTF_8
        );

        assertTrue(scheduler.contains("entity.getScheduler().runDelayed"));
        assertTrue(scheduler.contains("entity.getScheduler().run"));
        assertTrue(scheduler.contains("Math.max(1L, delayTicks)"));
        assertFalse(scheduler.contains("getGlobalRegionScheduler()"));
        assertFalse(scheduler.contains("getRegionScheduler()"));
    }

    @Test
    void economyUsesVaultUnlockedApiBeforeLegacyVault() throws IOException {
        String economy = Files.readString(
            Path.of("src/main/java/org/XxkirovxX/plugins/yinwulottery/economy/EconomyService.java"),
            StandardCharsets.UTF_8
        );

        int modernIndex = economy.indexOf("modernEconomy()");
        int legacyIndex = economy.indexOf("legacyEconomy()");

        assertTrue(modernIndex >= 0);
        assertTrue(legacyIndex > modernIndex);
        assertTrue(economy.contains("net.milkbowl.vault2.economy.Economy"));
        assertTrue(economy.contains("net.milkbowl.vault.economy.Economy"));
    }

    private String productionSource() throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Stream<Path> paths = Files.walk(Path.of("src/main/java"))) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                builder.append(Files.readString(path, StandardCharsets.UTF_8)).append('\n');
            }
        }
        return builder.toString();
    }
}
