package me.dw1e.axiom.listener;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.Inventory;

public final class EventStateListener implements Listener {

    private final Axiom plugin;

    public EventStateListener(Axiom plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getDataManager().create(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDataManager().delete(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        PlayerData data = plugin.getDataManager().getData(event.getPlayer().getUniqueId());

        if (data != null) data.getActionProcessor().reset();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        PlayerData data = plugin.getDataManager().getData(event.getPlayer().getUniqueId());

        if (data != null) data.getActionProcessor().reset();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        Entity entered = event.getEntered();
        if (!(entered instanceof Player)) return;

        PlayerData data = plugin.getDataManager().getData(entered.getUniqueId());

        if (data != null) data.getPingProcessor().confirm(() -> data.getActionProcessor().setInsideVehicle(true));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent event) {
        LivingEntity exited = event.getExited();
        if (!(exited instanceof Player)) return;

        PlayerData data = plugin.getDataManager().getData(exited.getUniqueId());

        if (data != null) data.getPingProcessor().confirm(() -> data.getActionProcessor().setInsideVehicle(false));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Entity passenger = event.getVehicle().getPassenger();
        if (!(passenger instanceof Player)) return;

        PlayerData data = plugin.getDataManager().getData(passenger.getUniqueId());

        if (data != null) data.getPingProcessor().confirm(() -> data.getActionProcessor().setInsideVehicle(false));
    }

    @EventHandler // GUI 中的点击事件
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory top = event.getView().getTopInventory();

        // if title == "Axiom AntiCheat": open_gui()
        if (top.getHolder() instanceof Gui) {
            event.setCancelled(true);

            Inventory clicked = event.getClickedInventory();
            if (clicked != null && clicked.equals(top)) {

                ((Gui) clicked.getHolder()).onClick(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/axiom")) {
            event.getPlayer().sendMessage(ConfigValue.PREFIX + " §fAxiom §7v" + plugin.getDescription().getVersion());
        }
    }
}
