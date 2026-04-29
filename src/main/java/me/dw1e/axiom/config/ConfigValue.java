package me.dw1e.axiom.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public final class ConfigValue {

    public static final String PREFIX = "§3[Axiom]§7";

    public static String ALERTS_FORMAT;
    public static boolean ALERTS_COOLDOWN_ENABLED;
    public static int ALERTS_COOLDOWN_INTERVAL;
    public static boolean ALERTS_PRINT_TO_CONSOLE;

    public static int VIOLATIONS_RESET_INTERVAL;
    public static String VIOLATIONS_RESET_MESSAGE;

    public static long LAG_PROTECTION_THRESHOLD;
    public static String LAG_PROTECTION_MESSAGE;

    public static boolean PUNISH_ENABLED;
    public static String PUNISH_MESSAGE;
    public static String PUNISH_DEFAULT_REASON;
    public static List<String> PUNISH_COMMANDS_KICK;
    public static List<String> PUNISH_COMMANDS_BAN;

    public static void updateConfig(FileConfiguration config) {
        ALERTS_FORMAT = config.getString("alerts.format");
        ALERTS_COOLDOWN_ENABLED = config.getBoolean("alerts.cooldown.enabled");
        ALERTS_COOLDOWN_INTERVAL = config.getInt("alerts.cooldown.interval");
        ALERTS_PRINT_TO_CONSOLE = config.getBoolean("alerts.print_to_console");

        VIOLATIONS_RESET_INTERVAL = config.getInt("violations_reset.interval");
        VIOLATIONS_RESET_MESSAGE = config.getString("violations_reset.message");

        LAG_PROTECTION_THRESHOLD = config.getLong("lag_protection.threshold");
        LAG_PROTECTION_MESSAGE = config.getString("lag_protection.message");

        PUNISH_ENABLED = config.getBoolean("punish.enabled");
        PUNISH_MESSAGE = config.getString("punish.message");
        PUNISH_DEFAULT_REASON = config.getString("punish.default_reason");
        PUNISH_COMMANDS_KICK = config.getStringList("punish.commands.kick");
        PUNISH_COMMANDS_BAN = config.getStringList("punish.commands.ban");
    }

}
