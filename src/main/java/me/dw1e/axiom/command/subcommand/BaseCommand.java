package me.dw1e.axiom.command.subcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class BaseCommand {

    private final String name;
    private final String description;

    public BaseCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

    public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);
}
