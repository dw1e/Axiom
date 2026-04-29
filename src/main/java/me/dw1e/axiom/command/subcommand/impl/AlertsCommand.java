package me.dw1e.axiom.command.subcommand.impl;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.command.subcommand.BaseCommand;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.data.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collections;
import java.util.List;

public final class AlertsCommand extends BaseCommand {

    public static final String META_ALERTS = "axiom:alerts";

    public AlertsCommand() {
        super("alerts", "切换警报状态");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigValue.PREFIX + " §c此指令仅玩家可使用.");
            return true;
        }

        Player player = (Player) sender;

        PlayerData data = Axiom.getPlugin().getDataManager().getData(player.getUniqueId());
        if (data == null) {
            sender.sendMessage(ConfigValue.PREFIX + " §c未查询到你的数据");
            return true;
        }

        data.setToggleAlert(!data.isToggleAlert());
        sender.sendMessage(ConfigValue.PREFIX + " §7警报现已" + (data.isToggleAlert() ? "§a开启" : "§c关闭"));

        if (data.isToggleAlert()) {
            player.setMetadata(META_ALERTS, new FixedMetadataValue(Axiom.getPlugin(), true));
        } else {
            player.removeMetadata(META_ALERTS, Axiom.getPlugin());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}