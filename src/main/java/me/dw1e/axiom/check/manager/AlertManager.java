package me.dw1e.axiom.check.manager;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.config.CheckValue;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.TaskUtil;
import org.bukkit.Bukkit;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AlertManager {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");
    private static final Set<UUID> INTERVAL_PLAYERS = new HashSet<>();

    public static void handleAlert(PlayerData data, CheckMeta checkMeta, CheckValue checkValue,
                                   double violations, String debug) {

        String thresholds = checkValue.getPunishType() == PunishType.NONE
                ? "∞" : DECIMAL_FORMAT.format(checkMeta.getThresholds());

        String formatted = ConfigValue.ALERTS_FORMAT
                .replace("%prefix%", ConfigValue.PREFIX)
                .replace("%player%", data.getPlayer().getName())
                .replace("%check%", checkMeta.getCategory().getName())
                .replace("%type%", checkMeta.getType())
                .replace("%vl%", DECIMAL_FORMAT.format(violations))
                .replace("%thresholds%", thresholds)
                .replace("%debug%", debug);

        UUID uuid = data.getPlayer().getUniqueId();

        if (!INTERVAL_PLAYERS.contains(uuid)) {
            for (PlayerData staff : Axiom.getPlugin().getDataManager().getAllData()) {
                if (staff.isToggleAlert()) {
                    staff.getPlayer().sendMessage(formatted);
                }
            }

            if (ConfigValue.ALERTS_PRINT_TO_CONSOLE) {
                Bukkit.getConsoleSender().sendMessage(formatted);
            }

            if (ConfigValue.ALERTS_COOLDOWN_ENABLED) {
                INTERVAL_PLAYERS.add(uuid);

                TaskUtil.runTaskLater(() -> INTERVAL_PLAYERS.remove(uuid), ConfigValue.ALERTS_COOLDOWN_INTERVAL);
            }
        }
    }

}
