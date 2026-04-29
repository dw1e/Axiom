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

public final class ResetVLCommand extends BaseCommand {

    public ResetVLCommand() {
        super("resetvl", "重置违规次数");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ConfigValue.PREFIX + " §c用法: /" + label + " resetvl <玩家/*>");
            return true;
        }

        if (args[1].equals("*")) {
            Axiom.getPlugin().getCheckManager().manualResetVL();
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

        targetData.resetVL();
        sender.sendMessage(ConfigValue.PREFIX + " §a已重置玩家 §f" + target.getName() + " §a的违规次数.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
}