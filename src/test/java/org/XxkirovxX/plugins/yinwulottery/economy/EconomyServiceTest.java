package org.XxkirovxX.plugins.yinwulottery.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.Test;

class EconomyServiceTest {
    @Test
    void withdrawPrefersVaultUnlockedApiBeforeLegacyVault() {
        RecordingVault2Economy modern = new RecordingVault2Economy();
        RecordingLegacyEconomy legacy = new RecordingLegacyEconomy();
        EconomyService service = new EconomyService(services(modern.proxy(), legacy.proxy()));

        EconomyService.PaymentResult result = service.withdraw(player(), 25.5D);

        assertTrue(result.success());
        assertEquals(1, modern.withdrawCalls);
        assertEquals(0, legacy.withdrawCalls);
    }

    @Test
    void withdrawUsesLegacyVaultWhenVaultUnlockedApiIsNotRegistered() {
        RecordingLegacyEconomy legacy = new RecordingLegacyEconomy();
        EconomyService service = new EconomyService(services(null, legacy.proxy()));

        EconomyService.PaymentResult result = service.withdraw(player(), 8.0D);

        assertTrue(result.success());
        assertEquals(1, legacy.withdrawCalls);
    }

    @Test
    void withdrawUsesHasWhenVaultUnlockedCanWithdrawIsNotImplemented() {
        RecordingVault2Economy modern = new RecordingVault2Economy();
        modern.canWithdrawType = net.milkbowl.vault2.economy.EconomyResponse.ResponseType.NOT_IMPLEMENTED;
        modern.hasResult = false;
        EconomyService service = new EconomyService(services(modern.proxy(), null));

        EconomyService.PaymentResult result = service.withdraw(player(), 3.0D);

        assertTrue(result.available());
        assertEquals(false, result.success());
        assertEquals(1, modern.canWithdrawCalls);
        assertEquals(1, modern.hasCalls);
        assertEquals(0, modern.withdrawCalls);
    }

    private ServicesManager services(
        net.milkbowl.vault2.economy.Economy modern,
        net.milkbowl.vault.economy.Economy legacy
    ) {
        return (ServicesManager) Proxy.newProxyInstance(
            ServicesManager.class.getClassLoader(),
            new Class<?>[]{ServicesManager.class},
            (proxy, method, args) -> {
                if (method.getName().equals("getRegistration") && args.length == 1) {
                    if (args[0] == net.milkbowl.vault2.economy.Economy.class && modern != null) {
                        return registration(net.milkbowl.vault2.economy.Economy.class, modern);
                    }
                    if (args[0] == net.milkbowl.vault.economy.Economy.class && legacy != null) {
                        return registration(net.milkbowl.vault.economy.Economy.class, legacy);
                    }
                    return null;
                }
                return defaultValue(method.getReturnType());
            }
        );
    }

    private <T> RegisteredServiceProvider<T> registration(Class<T> service, T provider) {
        return new RegisteredServiceProvider<>(service, provider, ServicePriority.Normal, plugin());
    }

    private Plugin plugin() {
        return (Plugin) Proxy.newProxyInstance(
            Plugin.class.getClassLoader(),
            new Class<?>[]{Plugin.class},
            (proxy, method, args) -> method.getName().equals("getName") ? "TestPlugin" : defaultValue(method.getReturnType())
        );
    }

    private Player player() {
        UUID uuid = UUID.randomUUID();
        return (Player) Proxy.newProxyInstance(
            Player.class.getClassLoader(),
            new Class<?>[]{Player.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getUniqueId" -> uuid;
                case "getName" -> "TestPlayer";
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

    private final class RecordingVault2Economy {
        private net.milkbowl.vault2.economy.EconomyResponse.ResponseType canWithdrawType =
            net.milkbowl.vault2.economy.EconomyResponse.ResponseType.SUCCESS;
        private boolean hasResult = true;
        private int canWithdrawCalls;
        private int hasCalls;
        private int withdrawCalls;

        private net.milkbowl.vault2.economy.Economy proxy() {
            return (net.milkbowl.vault2.economy.Economy) Proxy.newProxyInstance(
                net.milkbowl.vault2.economy.Economy.class.getClassLoader(),
                new Class<?>[]{net.milkbowl.vault2.economy.Economy.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "canWithdraw" -> {
                        canWithdrawCalls++;
                        yield response(canWithdrawType);
                    }
                    case "has" -> {
                        hasCalls++;
                        yield hasResult;
                    }
                    case "withdraw" -> {
                        withdrawCalls++;
                        yield response(net.milkbowl.vault2.economy.EconomyResponse.ResponseType.SUCCESS);
                    }
                    case "isEnabled" -> true;
                    case "getName" -> "ModernEconomy";
                    default -> defaultValue(method.getReturnType());
                }
            );
        }

        private net.milkbowl.vault2.economy.EconomyResponse response(
            net.milkbowl.vault2.economy.EconomyResponse.ResponseType type
        ) {
            return new net.milkbowl.vault2.economy.EconomyResponse(BigDecimal.ONE, BigDecimal.TEN, type, "");
        }
    }

    private final class RecordingLegacyEconomy {
        private int withdrawCalls;

        private net.milkbowl.vault.economy.Economy proxy() {
            return (net.milkbowl.vault.economy.Economy) Proxy.newProxyInstance(
                net.milkbowl.vault.economy.Economy.class.getClassLoader(),
                new Class<?>[]{net.milkbowl.vault.economy.Economy.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "has" -> true;
                    case "withdrawPlayer" -> {
                        withdrawCalls++;
                        yield new net.milkbowl.vault.economy.EconomyResponse(
                            1.0D,
                            10.0D,
                            net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS,
                            ""
                        );
                    }
                    case "isEnabled" -> true;
                    case "getName" -> "LegacyEconomy";
                    default -> {
                        if (args != null && args.length > 0 && args[0] instanceof OfflinePlayer) {
                            yield defaultValue(method.getReturnType());
                        }
                        yield defaultValue(method.getReturnType());
                    }
                }
            );
        }
    }
}
