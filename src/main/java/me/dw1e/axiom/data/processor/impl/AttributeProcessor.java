package me.dw1e.axiom.data.processor.impl;

import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.data.processor.Processor;
import me.dw1e.axiom.misc.util.PlayerUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketAbilities;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.server.SPacketAbilities;
import me.dw1e.axiom.packet.wrapper.server.SPacketEntityEffect;
import me.dw1e.axiom.packet.wrapper.server.SPacketRemoveEntityEffect;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class AttributeProcessor extends Processor {

    private int ticksSinceAbilityChange = 100;

    private boolean allowedFly, isFlying, instantlyBuild;
    private float flySpeed, walkSpeed;

    private int jumpEffect, speedEffect, slowEffect;

    private float attributeSpeed;

    public AttributeProcessor(PlayerData data) {
        super(data);
        Player player = data.getPlayer();

        allowedFly = player.getAllowFlight();
        isFlying = player.isFlying();
        instantlyBuild = player.getGameMode() == GameMode.CREATIVE;

        flySpeed = player.getFlySpeed() / 2.0F;
        walkSpeed = player.getWalkSpeed() / 2.0F;

        jumpEffect = PlayerUtil.getAmplifier(player, PotionEffectType.JUMP);
        speedEffect = PlayerUtil.getAmplifier(player, PotionEffectType.SPEED);
        slowEffect = PlayerUtil.getAmplifier(player, PotionEffectType.SLOW);
    }

    @Override
    public void preProcess(WrappedPacket packet) {
        if (packet instanceof SPacketEntityEffect) {
            SPacketEntityEffect wrapper = (SPacketEntityEffect) packet;

            if (wrapper.getEntityId() == data.getPlayer().getEntityId()) {
                int effectId = wrapper.getEffectId();
                int amplifier = wrapper.getAmplifier() + 1;

                data.getPingProcessor().confirm(() -> {
                    if (effectId == 1) {
                        speedEffect = amplifier;
                    } else if (effectId == 2) {
                        slowEffect = amplifier;
                    } else if (effectId == 8) {
                        jumpEffect = amplifier;
                    }
                });
            }

        } else if (packet instanceof SPacketRemoveEntityEffect) {
            SPacketRemoveEntityEffect wrapper = (SPacketRemoveEntityEffect) packet;

            if (wrapper.getEntityId() == data.getPlayer().getEntityId()) {
                int effectId = wrapper.getEffectId();

                data.getPingProcessor().confirm(() -> {
                    if (effectId == 1) {
                        speedEffect = 0;
                    } else if (effectId == 2) {
                        slowEffect = 0;
                    } else if (effectId == 8) {
                        jumpEffect = 0;
                    }
                });
            }

        } else if (packet instanceof SPacketAbilities) {
            SPacketAbilities wrapper = (SPacketAbilities) packet;

            data.getPingProcessor().confirm(() -> {
                if (allowedFly != wrapper.isAllowedFly() || isFlying != wrapper.isFlying()) {
                    ticksSinceAbilityChange = 0;
                }

                allowedFly = wrapper.isAllowedFly();
                isFlying = wrapper.isFlying();
                instantlyBuild = wrapper.isInstantlyBuild();

                flySpeed = wrapper.getFlySpeed();
                walkSpeed = wrapper.getWalkSpeed();
            });

        } else if (packet instanceof CPacketAbilities) {
            CPacketAbilities wrapper = (CPacketAbilities) packet;

            if (allowedFly) {
                if (isFlying != wrapper.isFlying()) ticksSinceAbilityChange = 0;

                isFlying = wrapper.isFlying();
            }
        }
    }

    @Override
    public void postProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            ++ticksSinceAbilityChange;

            attributeSpeed = computeAttributeSpeed();
        }
    }

    public float computeAttributeSpeed() {
        float attribute = walkSpeed;

        if (data.getActionProcessor().isSprinting()) attribute *= 1.3F;

        attribute *= 1.0F + (speedEffect * 0.2F);
        attribute *= Math.max(0.0F, 1.0F + (slowEffect * -0.15F));

        return attribute;
    }

    public float getAttributeSpeed() {
        return attributeSpeed;
    }

    public float getAttributeJump() {
        return 0.42F + (jumpEffect * 0.1F);
    }

    public int getTicksSinceAbilityChange() {
        return ticksSinceAbilityChange;
    }

    public boolean isAllowedFly() {
        return allowedFly;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public boolean isInstantlyBuild() {
        return instantlyBuild;
    }

    public int getJumpEffect() {
        return jumpEffect;
    }

    public int getSpeedEffect() {
        return speedEffect;
    }

    public int getSlowEffect() {
        return slowEffect;
    }

    public float getFlySpeed() {
        return flySpeed;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }
}
