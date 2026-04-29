package me.dw1e.axiom.data.processor.impl;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.impl.autoclicker.AutoClickerA;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.data.processor.Processor;
import me.dw1e.axiom.misc.util.MaterialUtil;
import me.dw1e.axiom.misc.util.TaskUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.*;
import me.dw1e.axiom.packet.wrapper.server.SPacketCloseWindow;
import me.dw1e.axiom.packet.wrapper.server.SPacketOpenWindow;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

public final class ActionProcessor extends Processor {

    private boolean digging, abortedDigging, stoppedDigging;
    private boolean placing;
    private boolean wasSneak, sneaking, wasSprint, sprinting;
    private boolean insideVehicle;
    private boolean blocking, eating, drawingBow;
    private boolean inventoryOpen;

    private int itemSlot, usingItemSlot;

    private int swings, flyingTicks, lastCps;

    private int ticksSinceAttack;
    private int ticksSinceHitSlowdown;
    private int ticksSinceSneak;
    private int ticksSinceInsideVehicle;

    private int inventoryOpenTicks;

    public ActionProcessor(PlayerData data) {
        super(data);
        Player player = data.getPlayer();

        sneaking = player.isSneaking();
        sprinting = player.isSprinting();
        insideVehicle = player.isInsideVehicle();
    }

    @Override
    public void preProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            ticksSinceInsideVehicle = insideVehicle ? 0 : ++ticksSinceInsideVehicle;

        } else if (packet instanceof CPacketArmAnimation) {
            if (!digging && !placing) ++swings;

        } else if (packet instanceof CPacketBlockDig) {
            CPacketBlockDig wrapper = (CPacketBlockDig) packet;

            switch (wrapper.getPlayerDigType()) {
                case START_DESTROY_BLOCK:
                    digging = true;
                    abortedDigging = false;
                    stoppedDigging = false;
                    break;

                case ABORT_DESTROY_BLOCK:
                case STOP_DESTROY_BLOCK:
                    digging = false;
                    break;
                case RELEASE_USE_ITEM:
                    blocking = eating = drawingBow = false;
                    break;
            }

            if (data.getAttributeProcessor().isInstantlyBuild()) digging = false;

        } else if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;

            placing = true;

            if (wrapper.isUseItem()) {
                Material material = wrapper.getItemStack().getType();

                if (canBlock(material)) blocking = true;
                if (canEat(wrapper.getItemStack())) eating = true;
                if (canDrawBow(material)) drawingBow = true;

                if (blocking || eating || drawingBow) usingItemSlot = itemSlot;
            }

        } else if (packet instanceof CPacketClientCommand) {
            CPacketClientCommand wrapper = (CPacketClientCommand) packet;

            if (wrapper.getClientCommand() == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                inventoryOpen = true;
            }

        } else if (packet instanceof CPacketCloseWindow) {
            inventoryOpen = false;

        } else if (packet instanceof CPacketEntityAction) {
            CPacketEntityAction wrapper = (CPacketEntityAction) packet;

            switch (wrapper.getAction()) {
                case START_SNEAKING:
                    sneaking = true;
                    break;
                case STOP_SNEAKING:
                    sneaking = false;
                    break;
                case START_SPRINTING:
                    sprinting = true;
                    break;
                case STOP_SPRINTING:
                    sprinting = false;
                    break;
            }

        } else if (packet instanceof CPacketHeldItemSlot) {
            CPacketHeldItemSlot wrapper = (CPacketHeldItemSlot) packet;

            itemSlot = wrapper.getSlot();

            if (usingItemSlot != itemSlot) blocking = eating = drawingBow = false;

        } else if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = (CPacketUseEntity) packet;

            if (wrapper.getAction() == EnumWrappers.EntityUseAction.ATTACK) {
                ticksSinceAttack = 0;

                Entity target = VISITOR.getEntityById(data.getPlayer().getWorld(), wrapper.getEntityId());

                if (target instanceof Player) {
                    boolean kbEnchant = data.getPlayer().getItemInHand().getEnchantmentLevel(Enchantment.KNOCKBACK) > 0;

                    if (sprinting || kbEnchant) ticksSinceHitSlowdown = 0;
                }
            }

        } else if (packet instanceof SPacketCloseWindow) {
            data.getPingProcessor().confirm(() -> inventoryOpen = false);

        } else if (packet instanceof SPacketOpenWindow) {
            data.getPingProcessor().confirm(() -> {
                inventoryOpen = true;

                digging = false;

                blocking = eating = drawingBow = false;
            });
        }
    }

    @Override
    public void postProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            wasSneak = sneaking;
            wasSprint = sprinting;

            placing = false;

            if (abortedDigging) {
                abortedDigging = false;
                digging = false;
            }

            if (stoppedDigging) {
                stoppedDigging = false;
                digging = false;
            }

            ItemStack slotItem = data.getPlayer().getInventory().getItem(itemSlot);

            if (slotItem != null) {
                Material material = slotItem.getType();

                if (blocking && !canBlock(material)) blocking = false;
                if (eating && !canEat(slotItem)) eating = false;
                if (drawingBow && !canDrawBow(Material.BOW)) drawingBow = false;
            }

            if (flyingTicks++ >= 19) {
                AutoClickerA autoClickerA = data.getCheck(AutoClickerA.class);

                if (swings > 20) {
                    autoClickerA.flag("cps=" + swings, (swings - 20) / 2.0);
                } else {
                    autoClickerA.decreaseVL(0.1);
                }

                lastCps = swings;
                swings = flyingTicks = 0;
            }

            ++ticksSinceAttack;
            ++ticksSinceHitSlowdown;

            ticksSinceSneak = sneaking ? 0 : ++ticksSinceSneak;
            inventoryOpenTicks = inventoryOpen ? ++inventoryOpenTicks : 0;
        }
    }

    public void reset() {
        sprinting = sneaking = inventoryOpen = false;

        resetUseItem();
    }

    public void resetUseItem() {
        blocking = eating = drawingBow = false;

        TaskUtil.runTask(() -> VISITOR.releaseUseItem(data.getPlayer()));
    }

    public void closeInventory() {
        inventoryOpen = false;

        TaskUtil.runTask(() -> data.getPlayer().closeInventory());
    }

    private boolean canBlock(Material material) {
        return MaterialUtil.SWORDS.contains(material);
    }

    private boolean canEat(ItemStack itemStack) {
        Material material = itemStack.getType();

        boolean hungry = data.getPlayer().getFoodLevel() < 20;
        boolean gApple = material.equals(Material.GOLDEN_APPLE);
        boolean potion = material.equals(Material.POTION);

        boolean creative = data.getAttributeProcessor().isInstantlyBuild();

        boolean eatNormalFood = material.isEdible() && (hungry || gApple) && !creative;
        boolean drinkMilk = material.equals(Material.MILK_BUCKET);
        boolean drinkPotion = (potion && itemStack.getDurability() == 0)
                || (potion && !Potion.fromItemStack(itemStack).isSplash());

        return eatNormalFood || drinkMilk || drinkPotion;
    }

    private boolean canDrawBow(Material material) {
        return material.equals(Material.BOW) && data.getPlayer().getInventory().contains(Material.ARROW);
    }

    public boolean isDigging() {
        return digging;
    }

    public boolean isPlacing() {
        return placing;
    }

    public boolean wasSneak() {
        return wasSneak;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public boolean wasSprint() {
        return wasSprint;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public boolean isUsingItem() {
        return blocking || eating || drawingBow;
    }

    public boolean isInventoryOpen() {
        return inventoryOpen;
    }

    public int getTicksSinceAttack() {
        return ticksSinceAttack;
    }

    public int getTicksSinceHitSlowdown() {
        return ticksSinceHitSlowdown;
    }

    public int getTicksSinceSneak() {
        return ticksSinceSneak;
    }

    public int getTicksSinceInsideVehicle() {
        return ticksSinceInsideVehicle;
    }

    public int getInventoryOpenTicks() {
        return inventoryOpenTicks;
    }

    public boolean isInsideVehicle() {
        return insideVehicle;
    }

    public void setInsideVehicle(boolean insideVehicle) {
        this.insideVehicle = insideVehicle;
    }

    public int getLastCps() {
        return lastCps;
    }
}
