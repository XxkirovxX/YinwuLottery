package org.XxkirovxX.plugins.yinwulottery.economy;

import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public final class EconomyService {
    private static final String PLUGIN_NAME = "YinwuLottery";

    private final ServicesManager servicesManager;

    public EconomyService(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public PaymentResult withdraw(Player player, double amount) {
        if (amount <= 0.0D) {
            return PaymentResult.paid();
        }
        Optional<net.milkbowl.vault2.economy.Economy> modern = modernEconomy();
        if (modern.isPresent()) {
            PaymentResult result = withdrawModern(modern.get(), player, amount);
            if (result.available()) {
                return result;
            }
        }
        Optional<net.milkbowl.vault.economy.Economy> legacy = legacyEconomy();
        return legacy.map(economy -> withdrawLegacy(economy, player, amount))
            .orElseGet(() -> PaymentResult.unavailable("No Vault economy provider is registered."));
    }

    private Optional<net.milkbowl.vault2.economy.Economy> modernEconomy() {
        RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> registration =
            servicesManager.getRegistration(net.milkbowl.vault2.economy.Economy.class);
        return registration == null ? Optional.empty() : Optional.ofNullable(registration.getProvider());
    }

    private Optional<net.milkbowl.vault.economy.Economy> legacyEconomy() {
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> registration =
            servicesManager.getRegistration(net.milkbowl.vault.economy.Economy.class);
        return registration == null ? Optional.empty() : Optional.ofNullable(registration.getProvider());
    }

    private PaymentResult withdrawModern(net.milkbowl.vault2.economy.Economy economy, Player player, double amount) {
        BigDecimal decimal = BigDecimal.valueOf(amount);
        try {
            net.milkbowl.vault2.economy.EconomyResponse check = economy.canWithdraw(PLUGIN_NAME, player.getUniqueId(), decimal);
            if (check.type != net.milkbowl.vault2.economy.EconomyResponse.ResponseType.NOT_IMPLEMENTED && !check.transactionSuccess()) {
                return PaymentResult.failure(message(check.errorMessage, "Cannot withdraw."));
            }
            if (check.type == net.milkbowl.vault2.economy.EconomyResponse.ResponseType.NOT_IMPLEMENTED
                && !economy.has(PLUGIN_NAME, player.getUniqueId(), decimal)) {
                return PaymentResult.failure("Insufficient balance.");
            }
            net.milkbowl.vault2.economy.EconomyResponse response = economy.withdraw(PLUGIN_NAME, player.getUniqueId(), decimal);
            return response.transactionSuccess()
                ? PaymentResult.paid()
                : PaymentResult.failure(message(response.errorMessage, "Withdraw failed."));
        } catch (UnsupportedOperationException | NoSuchMethodError ex) {
            return PaymentResult.unavailable(ex.getMessage());
        }
    }

    private PaymentResult withdrawLegacy(net.milkbowl.vault.economy.Economy economy, Player player, double amount) {
        if (!economy.has(player, amount)) {
            return PaymentResult.failure("Insufficient balance.");
        }
        net.milkbowl.vault.economy.EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess()
            ? PaymentResult.paid()
            : PaymentResult.failure(message(response.errorMessage, "Withdraw failed."));
    }

    private String message(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }

    public record PaymentResult(boolean available, boolean success, String reason) {
        public static PaymentResult paid() {
            return new PaymentResult(true, true, "");
        }

        public static PaymentResult failure(String reason) {
            return new PaymentResult(true, false, reason);
        }

        public static PaymentResult unavailable(String reason) {
            return new PaymentResult(false, false, reason);
        }
    }
}
