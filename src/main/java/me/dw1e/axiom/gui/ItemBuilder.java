package me.dw1e.axiom.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ItemBuilder {

    private final Material type;
    private final String name;

    private List<String> lore;
    private int amount = 1;
    private short damage = 0;

    public ItemBuilder(Material type, String name) {
        this.type = type;
        this.name = name;
    }

    public ItemBuilder setDamage(int damage) {
        this.damage = (short) damage;
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemStack build() {
        ItemStack itemStack = new ItemStack(type);

        itemStack.setAmount(amount);
        itemStack.setDurability(damage);

        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(name);
        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}