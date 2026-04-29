package me.dw1e.axiom.check.impl.autoclicker;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class AutoClickerA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.AUTO_CLICKER, "A", "检查 CPS 长时间高于 20")
                    .setVLToAlert(3)
                    .setThresholds(15)
                    .setPunishType(PunishType.KICK)
                    .setPunishReason("Double butterfly or drag clicking is not allowed on this server!");

    public AutoClickerA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
    }
}
