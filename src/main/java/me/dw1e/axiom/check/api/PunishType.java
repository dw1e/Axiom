package me.dw1e.axiom.check.api;

public enum PunishType {
    NONE("§f无"),
    KICK("§e踢出"),
    BAN("§c封禁");

    private final String description;

    PunishType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
