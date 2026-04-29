package me.dw1e.axiom.command.subcommand.impl;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.command.subcommand.BaseCommand;
import me.dw1e.axiom.config.ConfigValue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class ReloadCommand extends BaseCommand {

    public ReloadCommand() {
        super("reload", "重新加载配置文件");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Axiom.getPlugin().reload();

        sender.sendMessage(ConfigValue.PREFIX + " §a已重新加载配置文件.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}