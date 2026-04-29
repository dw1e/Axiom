package me.dw1e.axiom.command.subcommand.impl;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.command.subcommand.BaseCommand;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class InfoCommand extends BaseCommand {

    public InfoCommand() {
        super("info", "显示一名玩家的信息");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ConfigValue.PREFIX + " §c用法: /" + label + " info <玩家>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ConfigValue.PREFIX + " §c未找到玩家: " + args[1]);
            return true;
        }

        PlayerData targetData = Axiom.getPlugin().getDataManager().getData(target.getUniqueId());
        if (targetData == null) {
            sender.sendMessage(ConfigValue.PREFIX + " §c未查询到 §f" + args[1] + " §c的数据");
            return true;
        }

        sender.sendMessage(ConfigValue.PREFIX + " §7玩家 §f" + target.getName() + " §7的信息:");
        sender.sendMessage("§8§m--------------------");
        sender.sendMessage("§f基础信息:");
        sender.sendMessage(" §f● Ping: §b" + targetData.getPingProcessor().getPing());
        sender.sendMessage(" §f● Last CPS: §b" + targetData.getActionProcessor().getLastCps());
        sender.sendMessage(" §f● FastMath: §b" + targetData.getEmulationProcessor().isFastMath());
        sender.sendMessage("§8§m--------------------");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
}