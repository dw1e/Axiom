package me.dw1e.axiom.gui.impl;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.config.CheckValue;
import me.dw1e.axiom.gui.Gui;
import me.dw1e.axiom.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TypeGui extends Gui {

    private final Map<Integer, Check> checksById = new HashMap<>();

    public TypeGui(String name, List<Check> list) {
        super(45, name);

        for (int i = 0; i < 9; ++i) inventory.setItem(i, BACK_BUTTON);

        int id = 9;

        for (Check check : list) {
            CheckValue checkValue = Axiom.getPlugin().getCheckManager().getCheckValue(check.getClass());
            CheckMeta checkMeta = check.getCheckInfo();

            inventory.setItem(id, new ItemBuilder(Material.INK_SACK, getName(checkMeta))
                    .setDamage(getDurability(checkValue.isEnabled()))
                    .setLore(getLore(checkMeta, checkValue))
                    .build()
            );

            checksById.put(id++, check);
        }
    }

    private static short getDurability(boolean enabled) {
        return (short) (enabled ? 10 : 8);
    }

    private static String getName(CheckMeta checkMeta) {
        return "§f" + checkMeta.getCategory().getName() + " §8(§7类型 §f" + checkMeta.getType() + "§8)";
    }

    private static List<String> getLore(CheckMeta checkMeta, CheckValue checkValue) {
        return Arrays.asList(
                "",
                "§f§l描述: §7" + checkMeta.getDescription(),
                "",
                "§f§l当前状态: " + (checkValue.isEnabled() ? "§a开启" : "§c关闭") + "§8 (左键单击)",
                "§f§l惩罚措施: §f" + checkValue.getPunishType().getDescription() + "§8 (右键单击)",
                "§f§l警报阈值: §f" + checkValue.getVLToAlert(),
                "§f§l处罚阈值: §f" + checkValue.getThresholds(),
                "§f§l处罚原因: §7" + shorten(checkValue.getPunishReason())
        );
    }

    private static <T extends Enum<T>> T next(T current) {
        T[] values = current.getDeclaringClass().getEnumConstants();
        return values[(current.ordinal() + 1) % values.length];
    }

    private static String shorten(String text) {
        if (text == null || text.isEmpty()) return "§7§o默认";

        if (text.length() <= 32) return text;

        return text.substring(0, 32) + "...";
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player clicker = (Player) event.getWhoClicked();

        if (clickedItem.getType() != Material.AIR) {
            clicker.playSound(clicker.getLocation(), Sound.CLICK, 1.0F, 1.0F);
        }

        int value = event.getSlot();

        if (value >= 0 && value < 9) {
            Axiom.getPlugin().getGuiManager().getCheckGui().openGui(clicker);
            return;
        }

        Check check = checksById.get(value);
        if (check == null) return;

        CheckMeta checkMeta = check.getCheckInfo();

        ItemMeta itemMeta = clickedItem.getItemMeta();
        if (itemMeta == null || !itemMeta.getDisplayName().equals(getName(checkMeta))) return;

        ClickType click = event.getClick();
        CheckValue checkValue = Axiom.getPlugin().getCheckManager().getCheckValue(check.getClass());

        if (click.isLeftClick()) {
            checkValue.setEnabled(!checkValue.isEnabled());

        } else if (click.isRightClick()) {
            checkValue.setPunishType(next(checkValue.getPunishType()));
        }

        itemMeta.setLore(getLore(checkMeta, checkValue));

        clickedItem.setItemMeta(itemMeta);
        clickedItem.setDurability(getDurability(checkValue.isEnabled()));

        inventory.setItem(value, clickedItem);
    }
}