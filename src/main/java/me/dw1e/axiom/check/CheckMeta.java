package me.dw1e.axiom.check;

import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.PunishType;

public final class CheckMeta {

    private final Category category;
    private final String type;
    private final String description;

    private boolean enabled = true;
    private double vlToAlert = 1;
    private double thresholds = 20;
    private PunishType punishType = PunishType.BAN;
    private String punishReason = ""; // 留空则使用配置里的默认原因

    public CheckMeta(Category category, String type, String description) {
        this.category = category;
        this.type = type;
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CheckMeta setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public double getVLToAlert() {
        return vlToAlert;
    }

    public CheckMeta setVLToAlert(double vlToAlert) {
        this.vlToAlert = vlToAlert;
        return this;
    }

    public double getThresholds() {
        return thresholds;
    }

    public CheckMeta setThresholds(double thresholds) {
        this.thresholds = thresholds;
        return this;
    }

    public PunishType getPunishType() {
        return punishType;
    }

    public CheckMeta setPunishType(PunishType punishType) {
        this.punishType = punishType;
        return this;
    }

    public String getPunishReason() {
        return punishReason;
    }

    public CheckMeta setPunishReason(String punishReason) {
        this.punishReason = punishReason;
        return this;
    }
}
