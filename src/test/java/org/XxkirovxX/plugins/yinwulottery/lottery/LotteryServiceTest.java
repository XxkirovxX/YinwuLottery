package org.XxkirovxX.plugins.yinwulottery.lottery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import org.XxkirovxX.plugins.yinwulottery.config.PluginConfig;
import org.XxkirovxX.plugins.yinwulottery.recipe.RecipeSnapshot;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;

class LotteryServiceTest {
    @Test
    void reopenReusesExistingPreviewSessionInsteadOfRerollingPool() {
        PluginConfig config = new PluginConfig(new YamlConfiguration());
        FakeSession session = new FakeSession();
        CountingSessionFactory factory = new CountingSessionFactory(session);
        LotteryService service = new LotteryService(
            config,
            () -> List.of(recipe("a"), recipe("b")),
            null,
            null,
            (targetPlayer, task) -> task.run(),
            factory
        );
        FakePlayer player = new FakePlayer();

        service.open(player.proxy());
        service.open(player.proxy());

        assertEquals(1, factory.createCalls);
        assertEquals(2, player.openCalls);
        assertSame(session.inventory(), player.lastOpenedInventory);
    }

    private static RecipeSnapshot recipe(String id) {
        return new RecipeSnapshot(id, id, List.of(), List.of(), 0, 0, 0, 0, List.of(), 0, "WHITE", 0, List.of());
    }

    private static final class CountingSessionFactory implements LotteryService.SessionFactory {
        private final LotteryUiSession session;
        private int createCalls;

        private CountingSessionFactory(LotteryUiSession session) {
            this.session = session;
        }

        @Override
        public LotteryUiSession create(Player player, List<RecipeSnapshot> recipes) {
            createCalls++;
            return session;
        }
    }

    private static final class FakeSession implements LotteryUiSession {
        private final UUID playerId = UUID.randomUUID();
        private final Inventory inventory = (Inventory) Proxy.newProxyInstance(
            Inventory.class.getClassLoader(),
            new Class<?>[]{Inventory.class},
            (proxy, method, args) -> null
        );

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
            return false;
        }

        @Override
        public boolean isStopping() {
            return false;
        }

        @Override
        public boolean hasPendingPrize() {
            return false;
        }

        @Override
        public void start() {
        }

        @Override
        public void requestStop() {
        }

        @Override
        public boolean handleClick(Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            return true;
        }

        @Override
        public void close(Player player) {
        }

        @Override
        public void cancelTask() {
        }

        @Override
        public boolean shouldExpire() {
            return false;
        }
    }

    private static final class FakePlayer {
        private final UUID uuid = UUID.randomUUID();
        private int openCalls;
        private Inventory lastOpenedInventory;

        private Player proxy() {
            return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "openInventory" -> {
                        openCalls++;
                        lastOpenedInventory = (Inventory) args[0];
                        yield null;
                    }
                    case "sendMessage" -> null;
                    default -> defaultValue(method.getReturnType());
                }
            );
        }

        private Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) {
                return false;
            }
            if (type == byte.class) {
                return (byte) 0;
            }
            if (type == short.class) {
                return (short) 0;
            }
            if (type == int.class) {
                return 0;
            }
            if (type == long.class) {
                return 0L;
            }
            if (type == float.class) {
                return 0F;
            }
            if (type == double.class) {
                return 0D;
            }
            if (type == char.class) {
                return '\0';
            }
            return null;
        }
    }
}
