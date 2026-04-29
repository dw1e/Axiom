package me.dw1e.axiom.check.api;

public enum Category {

    AIM("Aim", "自瞄检查"),
    AUTO_CLICKER("AutoClicker", "自动点击器检查"),
    BAD_PACKET("BadPacket", "非法数据包修改检查"),
    FLY("Fly", "飞行检查"),
    HITBOX("Hitbox", "碰撞箱修改检查"),
    INTERACT("Interact", "交互检查"),
    INVENTORY("Inventory", "背包操作检查"),
    KILL_AURA("KillAura", "杀戮光环检查"),
    NET_ANALYSIS("NetAnalysis", "网络分析检查"),
    PHASE("Phase", "穿墙检查"),
    SCAFFOLD("Scaffold", "自动搭路检查"),
    SPEED("Speed", "移速修改检查"),
    TIMER("Timer", "加速时间检查"),
    VELOCITY("Velocity", "击退修改检查");

    private final String name;
    private final String description;

    Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public final String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
