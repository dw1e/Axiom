package me.dw1e.axiom.command.subcommand.impl;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.command.subcommand.BaseCommand;
import me.dw1e.axiom.config.ConfigValue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class GuiCommand extends BaseCommand {

    public GuiCommand() {
        super("gui", "打开管理界面");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigValue.PREFIX + " §c此指令仅玩家可使用.");
            return true;
        }

        Axiom.getPlugin().getGuiManager().getMainGui().openGui((Player) sender);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}