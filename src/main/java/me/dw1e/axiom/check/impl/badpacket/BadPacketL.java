package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockDig;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BadPacketL extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "L", "检查快速挖掘")
                    .setThresholds(8);

    private boolean digging;
    private Block targetBlock;
    private float accumulatedDamage;
    private int startTick;

    public BadPacketL(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (attributeProcessor.isInstantlyBuild()) {
            reset();
            return;
        }

        Player player = data.getPlayer();

        if (packet instanceof CPacketFlying) {
            if (digging && targetBlock != null) {
                float damage = VISITOR.getBlockDigSpeed(player, targetBlock);

                if (damage > 0F) accumulatedDamage += damage;
            }

        } else if (packet instanceof CPacketBlockDig) {
            CPacketBlockDig wrapper = (CPacketBlockDig) packet;

            switch (wrapper.getPlayerDigType()) {
                case START_DESTROY_BLOCK:
                    targetBlock = wrapper.getBlockPosition().toLocation(player.getWorld()).getBlock();

                    digging = true;
                    accumulatedDamage = 0F;
                    startTick = data.getTick();

                    float damage = VISITOR.getBlockDigSpeed(player, targetBlock);
                    if (damage > 0F) accumulatedDamage += damage;

                    break;

                case ABORT_DESTROY_BLOCK:
                    reset();
                    break;

                case STOP_DESTROY_BLOCK:
                    if (!digging || targetBlock == null) {
                        reset();
                        return;
                    }

                    int ticks = data.getTick() - startTick;

                    if (accumulatedDamage < 1F || ticks == 0) {
                        double speedFactor = accumulatedDamage <= 0.0F ? 20.0 : 1.0 / accumulatedDamage;

                        if (flag(String.format("block=%s, speed=%.2fx", targetBlock.getType(), speedFactor))) {
                            packet.setCancel(true);
                            BlockUtil.resyncBlockAt(player, targetBlock);
                        }
                    }

                    reset();
                    break;
            }
        }
    }

    private void reset() {
        digging = false;
        targetBlock = null;
        accumulatedDamage = 0F;
        startTick = 0;
    }
}