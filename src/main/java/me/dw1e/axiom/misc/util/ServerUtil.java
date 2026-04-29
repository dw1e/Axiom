package me.dw1e.axiom.misc.util;

import me.dw1e.axiom.config.ConfigValue;
import org.bukkit.Bukkit;

public final class ServerUtil {

    public static void consoleLog(String msg) {
        Bukkit.getConsoleSender().sendMessage(ConfigValue.PREFIX + " " + msg);
    }
}
