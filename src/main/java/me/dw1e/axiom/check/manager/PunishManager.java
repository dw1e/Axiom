package me.dw1e.axiom.check.manager;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.config.CheckValue;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class PunishManager {

    public static void handlePunish(PlayerData data, CheckMeta checkMeta, CheckValue checkValue) {
        if (!ConfigValue.PUNISH_ENABLED) return;

        if (data.isBanned()) return;

        PunishType punishType = checkValue.getPunishType();
        if (punishType == PunishType.NONE) return;

        Player player = data.getPlayer();
        if (player == null || !player.isOnline()) return;

        if (punishType == PunishType.KICK && data.isKicked()) return;

        if (punishType == PunishType.BAN) {
            data.setBanned(true);
            data.setKicked(true);
        } else if (punishType == PunishType.KICK) {
            data.setKicked(true);
        }

        List<String> commands = getCommands(punishType);
        if (commands == null || commands.isEmpty()) return;

        String playerName = player.getName();
        String reason = checkValue.getPunishReason();

        if (reason == null || reason.isEmpty()) {
            reason = ConfigValue.PUNISH_DEFAULT_REASON;
        }

        String punishMessage = ConfigValue.PUNISH_MESSAGE;

        boolean shouldSendMessage = punishMessage != null && !punishMessage.isEmpty();
        if (shouldSendMessage) {
            punishMessage = punishMessage
                    .replace("%prefix%", ConfigValue.PREFIX)
                    .replace("%player%", data.getPlayer().getName())
                    .replace("%check%", checkMeta.getCategory().getName())
                    .replace("%type%", checkMeta.getType());
        }

        for (PlayerData staff : Axiom.getPlugin().getDataManager().getAllData()) {
            if (shouldSendMessage && staff.isToggleAlert()) {
                staff.getPlayer().sendMessage(punishMessage);
            }
        }

        if (shouldSendMessage && ConfigValue.ALERTS_PRINT_TO_CONSOLE) {
            Bukkit.getConsoleSender().sendMessage(punishMessage);
        }

        for (String command : commands) {
            if (command == null || command.isEmpty()) continue;

            String formatted = command
                    .replace("%prefix%", ConfigValue.PREFIX)
                    .replace("%player%", playerName)
                    .replace("%reason%", reason);

            TaskUtil.runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted));
        }
    }

    private static List<String> getCommands(PunishType type) {
        switch (type) {
            case KICK:
                return ConfigValue.PUNISH_COMMANDS_KICK;
            case BAN:
                return ConfigValue.PUNISH_COMMANDS_BAN;
            default:
                return null;
        }
    }

}