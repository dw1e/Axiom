package me.dw1e.axiom.nms;

import me.dw1e.axiom.nms.version.NMS_v1_8_R3;
import org.bukkit.Bukkit;

public final class NMSManager {

    private final NMSVisitor visitor;

    public NMSManager() {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        if (version.equals("v1_8_R3")) {
            visitor = new NMS_v1_8_R3();

        } else {
            throw new RuntimeException("不支持的服务器版本: " + version);
        }
    }

    public NMSVisitor getVisitor() {
        return visitor;
    }
}
