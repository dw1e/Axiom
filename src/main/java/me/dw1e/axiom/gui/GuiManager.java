package me.dw1e.axiom.gui;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.gui.impl.CheckGui;
import me.dw1e.axiom.gui.impl.MainGui;
import me.dw1e.axiom.gui.impl.TypeGui;
import org.bukkit.entity.HumanEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class GuiManager {

    private final Set<Gui> guis = new HashSet<>();
    private final Map<Category, Gui> typeGuis = new ConcurrentHashMap<>();

    private Gui mainGui;
    private Gui checkGui;

    public void enable() {
        List<Check> checks = Axiom.getPlugin().getCheckManager().loadChecks(null);

        guis.add(mainGui = new MainGui());
        guis.add(checkGui = new CheckGui(checks));

        for (Category category : Category.values()) {
            List<Check> categoryChecks = new ArrayList<>();

            for (Check check : checks) {
                if (check.getCheckInfo().getCategory() == category) {
                    categoryChecks.add(check);
                }
            }

            typeGuis.put(category, new TypeGui(category.getName(), categoryChecks));
        }

        guis.addAll(typeGuis.values());
    }

    public void disable() {
        for (Gui gui : guis) {
            for (HumanEntity viewer : new ArrayList<>(gui.inventory.getViewers())) {
                viewer.closeInventory();
            }
        }

        guis.clear();
        typeGuis.clear();

        mainGui = checkGui = null;
    }

    public Gui getMainGui() {
        return mainGui;
    }

    public Gui getCheckGui() {
        return checkGui;
    }

    public Gui getTypeGui(Category category) {
        return typeGuis.get(category);
    }
}