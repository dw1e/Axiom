package me.dw1e.axiom.config;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.PunishType;
import org.bukkit.configuration.file.FileConfiguration;

public final class CheckValue {

    private final CheckMeta checkMeta;
    private final String path;

    private boolean enabled;
    private PunishType punishType;
    private double vlToAlert;
    private double thresholds;
    private String punishReason;

    public CheckValue(CheckMeta checkMeta) {
        this.checkMeta = checkMeta;
        path = checkMeta.getCategory().getName() + "." + checkMeta.getType();

        addDefault();
        updateConfig();
    }

    private static void editConfig(String path, Object value) {
        Axiom.getPlugin().getConfigManager().getChecks().set(path, value);
        Axiom.getPlugin().getConfigManager().saveChecks();
    }

    private void addDefault() {
        FileConfiguration config = Axiom.getPlugin().getConfigManager().getChecks();

        config.options().copyDefaults(true);

        config.addDefault(path + ".enabled", checkMeta.isEnabled());
        config.addDefault(path + ".punish_type", checkMeta.getPunishType().name());
        config.addDefault(path + ".vl_to_alert", checkMeta.getVLToAlert());
        config.addDefault(path + ".thresholds", checkMeta.getThresholds());
        config.addDefault(path + ".punish_reason", checkMeta.getPunishReason());
    }

    public void updateConfig() {
        FileConfiguration config = Axiom.getPlugin().getConfigManager().getChecks();

        enabled = config.getBoolean(path + ".enabled");
        punishType = PunishType.valueOf(config.getString(path + ".punish_type"));
        vlToAlert = config.getDouble(path + ".vl_to_alert");
        thresholds = config.getDouble(path + ".thresholds");
        punishReason = config.getString(path + ".punish_reason");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        editConfig(path + ".enabled", enabled);
    }

    public PunishType getPunishType() {
        return punishType;
    }

    public void setPunishType(PunishType punishType) {
        this.punishType = punishType;
        editConfig(path + ".punish_type", punishType.name());
    }

    public double getVLToAlert() {
        return vlToAlert;
    }

    public void setVLToAlert(double vlToAlert) {
        this.vlToAlert = vlToAlert;
        editConfig(path + ".vl_to_alert", vlToAlert);
    }

    public double getThresholds() {
        return thresholds;
    }

    public void setThresholds(double thresholds) {
        this.thresholds = thresholds;
        editConfig(path + ".thresholds", thresholds);
    }

    public String getPunishReason() {
        return punishReason;
    }

    public void setPunishReason(String punishReason) {
        this.punishReason = punishReason;
        editConfig(path + ".punish_reason", punishReason);
    }
}
