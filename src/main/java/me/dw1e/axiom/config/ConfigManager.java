package me.dw1e.axiom.config;

import me.dw1e.axiom.Axiom;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class ConfigManager {

    private final File configFile, checksFile;
    private final FileConfiguration config, checks;

    public ConfigManager(Axiom plugin) {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        checksFile = new File(plugin.getDataFolder(), "checks.yml");

        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        if (!checksFile.exists()) plugin.saveResource("checks.yml", false);

        config = YamlConfiguration.loadConfiguration(configFile);
        checks = YamlConfiguration.loadConfiguration(checksFile);

        ConfigValue.updateConfig(config);
    }

    // 不建议保存 config.yml 这个文件, 这会导致你的注释全部消失!
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void saveChecks() {
        try {
            checks.save(checksFile);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getChecks() {
        return checks;
    }
}
