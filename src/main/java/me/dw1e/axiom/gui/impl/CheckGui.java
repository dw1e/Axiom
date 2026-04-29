package me.dw1e.axiom.gui.impl;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.gui.Gui;
import me.dw1e.axiom.gui.GuiManager;
import me.dw1e.axiom.gui.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CheckGui extends Gui {

    private final Map<Integer, Category> categoryById = new HashMap<>();

    public CheckGui(List<Check> checks) {
        super(45, "检查管理");

        for (int i = 0; i < 9; ++i) inventory.setItem(i, BACK_BUTTON);

        int id = 9;

        for (Category category : Category.values()) {
            int total = 0;

            for (Check check : checks) {
                if (check.getCheckInfo().getCategory() == category) {
                    total++;
                }
            }

            inventory.setItem(id, new ItemBuilder(Material.BOOK, "§f" + category.getName())
                    .setLore(Collections.singletonList("§7" + category.getDescription()))
                    .setAmount(total)
                    .build()
            );

            categoryById.put(id++, category);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player clicker = (Player) event.getWhoClicked();

        if (clickedItem.getType() != Material.AIR) {
            clicker.playSound(clicker.getLocation(), Sound.CLICK, 1.0F, 1.0F);
        }

        GuiManager guiManager = Axiom.getPlugin().getGuiManager();

        int value = event.getSlot();

        if (value >= 0 && value < 9) {
            guiManager.getMainGui().openGui(clicker);
            return;
        }

        if (clickedItem.getItemMeta() == null) return;

        Category category = categoryById.get(value);
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (category == null || !category.getName().equals(displayName)) return;

        guiManager.getTypeGui(category).openGui(clicker);
    }
}