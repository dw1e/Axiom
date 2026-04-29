package me.dw1e.axiom.command;

import me.dw1e.axiom.command.subcommand.BaseCommand;
import me.dw1e.axiom.command.subcommand.impl.*;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.misc.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AxiomCommand implements TabExecutor {

    private static final String BASE_PERMISSION = "axiom.command.";

    private static final BaseCommand[] SUB_COMMANDS = {
            new AlertsCommand(),
            new GuiCommand(),
            new InfoCommand(),
            new ReloadCommand(),
            new ResetVLCommand()
    };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();

            for (BaseCommand sub : SUB_COMMANDS) {
                if (hasPermission(sender, sub)) {
                    list.add(sub.getName());
                }
            }

            return StringUtil.filterStartingWith(list, args[0]);
        }

        BaseCommand sub = getSubCommand(args[0]);
        if (sub != null) return sub.onTabComplete(sender, cmd, label, args);

        // PS: 返回空列表 = 什么补全也没有, 返回 null = 由客户端自动补全在线玩家的名字
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ConfigValue.PREFIX + " 使用 §f/" + label + " help §7查看帮助.");
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ConfigValue.PREFIX + " 可用指令列表:");

            boolean found = false;

            for (BaseCommand sub : SUB_COMMANDS) {
                if (!hasPermission(sender, sub)) continue;

                found = true;

                sender.sendMessage("§8- §f/" + label + " " + sub.getName() + " §8>> §7" + sub.getDescription());
            }

            if (!found) sender.sendMessage(ConfigValue.PREFIX + " §c你没有权限使用任何指令.");
            return true;
        }

        BaseCommand sub = getSubCommand(args[0]);

        if (sub == null) {
            sender.sendMessage(ConfigValue.PREFIX + " §c未知子指令: " + args[0]);
            return true;
        }

        if (!hasPermission(sender, sub)) {
            sender.sendMessage(ConfigValue.PREFIX + " §c你没有此指令的使用权限.");
            return true;
        }

        return sub.onCommand(sender, cmd, label, args);
    }

    private BaseCommand getSubCommand(String name) {
        for (BaseCommand sub : SUB_COMMANDS) {
            if (sub.getName().equalsIgnoreCase(name)) {
                return sub;
            }
        }
        return null;
    }

    private boolean hasPermission(CommandSender sender, BaseCommand cmd) {
        return sender.hasPermission(BASE_PERMISSION + cmd.getName());
    }
}