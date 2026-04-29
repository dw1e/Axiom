package me.dw1e.axiom;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.dw1e.axiom.check.manager.CheckManager;
import me.dw1e.axiom.command.AxiomCommand;
import me.dw1e.axiom.config.ConfigManager;
import me.dw1e.axiom.data.DataManager;
import me.dw1e.axiom.gui.GuiManager;
import me.dw1e.axiom.listener.EventCheckListener;
import me.dw1e.axiom.listener.EventStateListener;
import me.dw1e.axiom.misc.ServerTickTask;
import me.dw1e.axiom.misc.util.ServerUtil;
import me.dw1e.axiom.nms.NMSManager;
import me.dw1e.axiom.packet.PacketHandler;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Axiom extends JavaPlugin {

    private static Axiom plugin;

    private ProtocolManager protocolManager;
    private ConfigManager configManager;
    private NMSManager nmsManager;
    private ServerTickTask serverTickTask;
    private CheckManager checkManager;
    private DataManager dataManager;
    private GuiManager guiManager;

    public static Axiom getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        try {
            protocolManager = ProtocolLibrary.getProtocolManager();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE,
                    "Failed to initialize ProtocolLib. Please ensure it is installed and compatible.", e);

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);

        nmsManager = new NMSManager();

        serverTickTask = new ServerTickTask();
        serverTickTask.enable();

        checkManager = new CheckManager();
        checkManager.enable();

        dataManager = new DataManager();
        dataManager.enable();

        guiManager = new GuiManager();
        guiManager.enable();

        new PacketHandler(this).register();

        new EventStateListener(this).register();
        new EventCheckListener(this).register();

        PluginCommand pluginCommand = getCommand("axiom");
        if (pluginCommand != null) {
            AxiomCommand axiomCommand = new AxiomCommand();

            pluginCommand.setExecutor(axiomCommand);
            pluginCommand.setTabCompleter(axiomCommand);
        }

        ServerUtil.consoleLog("§aAxiom has been enabled.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        if (protocolManager != null) {
            protocolManager.removePacketListeners(this);
            protocolManager = null;
        }

        if (guiManager != null) {
            guiManager.disable();
            guiManager = null;
        }

        if (dataManager != null) {
            dataManager.disable();
            dataManager = null;
        }

        if (checkManager != null) {
            checkManager.disable();
            checkManager = null;
        }

        if (serverTickTask != null) {
            serverTickTask.disable();
            serverTickTask = null;
        }

        nmsManager = null;

        configManager = null;

        plugin = null;

        ServerUtil.consoleLog("§aAxiom has been disabled.");
    }

    public void reload() {
        configManager = new ConfigManager(this);

        checkManager.disable();
        checkManager.enable();

        guiManager.disable();
        guiManager.enable();

        ServerUtil.consoleLog("§aAxiom has been reloaded.");
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public NMSManager getNmsManager() {
        return nmsManager;
    }

    public ServerTickTask getServerTickTask() {
        return serverTickTask;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
