package me.dw1e.axiom.gui.impl;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.gui.Gui;
import me.dw1e.axiom.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public final class MainGui extends Gui {

    public MainGui() {
        super(27, "Axiom Anti-Cheat");

        inventory.setItem(10, new ItemBuilder(Material.PAPER, "§f重载配置")
                .setLore(Collections.singletonList("§7点击重新加载配置文件"))
                .build()
        );

        inventory.setItem(13, new ItemBuilder(Material.BOOK, "§f检查管理")
                .setLore(Collections.singletonList("§7点击打开检查管理页"))
                .build()
        );

        inventory.setItem(16, new ItemBuilder(Material.WATER_BUCKET, "§f重置违规次数")
                .setLore(Collections.singletonList("§7点击重置所有玩家的违规次数"))
                .build()
        );
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

        if (value == 10) {
            Axiom.getPlugin().reload();

            clicker.sendMessage(ConfigValue.PREFIX + " §a已重新加载配置文件.");
            clicker.closeInventory();

        } else if (value == 13) {
            Axiom.getPlugin().getGuiManager().getCheckGui().openGui(clicker);

        } else if (value == 16) {
            for (PlayerData data : Axiom.getPlugin().getDataManager().getAllData()) {
                data.resetVL();
            }

            clicker.sendMessage(ConfigValue.PREFIX + " §a已重置所有玩家的违规次数.");
            clicker.closeInventory();
        }
    }
}