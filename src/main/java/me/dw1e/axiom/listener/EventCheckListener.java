package me.dw1e.axiom.listener;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.impl.killaura.KillAuraC;
import me.dw1e.axiom.check.impl.scaffold.ScaffoldA;
import me.dw1e.axiom.check.impl.scaffold.ScaffoldD;
import me.dw1e.axiom.check.impl.scaffold.ScaffoldE;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.nms.NMSVisitor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class EventCheckListener implements Listener {

    // 有些检测用需要用到 Bukkit 事件

    private final Axiom plugin;
    private final NMSVisitor visitor;

    public EventCheckListener(Axiom plugin) {
        this.plugin = plugin;
        visitor = plugin.getNmsManager().getVisitor();
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            PlayerData data = plugin.getDataManager().getData(player.getUniqueId());

            if (visitor.isUsingItem(player)) {
                if (data.getCheck(KillAuraC.class).flag()) {
                    event.setCancelled(true);
                    visitor.releaseUseItem(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getData(player.getUniqueId());

        data.getCheck(ScaffoldA.class).confirmPlace();
        data.getCheck(ScaffoldD.class).check(event.getBlock());
        data.getCheck(ScaffoldE.class).confirmPlace();
    }

}