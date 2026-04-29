package me.dw1e.axiom.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public abstract class Gui implements InventoryHolder {

    protected static final ItemStack BACK_BUTTON =
            new ItemBuilder(Material.STAINED_GLASS_PANE, "§c返回")
                    .setDamage(14)
                    .setLore(Collections.singletonList("§7点击返回上一页"))
                    .build();

    protected final Inventory inventory;

    public Gui(int size, String title) {
        inventory = Bukkit.createInventory(this, size, title);
    }

    public void openGui(HumanEntity player) {
        if (inventory != null) player.openInventory(inventory);
    }

    public abstract void onClick(InventoryClickEvent event);

    public Inventory getInventory() {
        return inventory;
    }
}
