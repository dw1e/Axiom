package me.dw1e.axiom.data;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.api.handler.TickHandler;
import me.dw1e.axiom.check.manager.CheckManager;
import me.dw1e.axiom.command.subcommand.impl.AlertsCommand;
import me.dw1e.axiom.data.processor.Processor;
import me.dw1e.axiom.data.processor.impl.*;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlayerData {

    private static final CheckManager CHECK_MANAGER = Axiom.getPlugin().getCheckManager();

    private final Player player;

    private final ActionProcessor actionProcessor;
    private final AttributeProcessor attributeProcessor;
    private final EntityProcessor entityProcessor;
    private final MoveProcessor moveProcessor;
    private final CollideProcessor collideProcessor;
    private final PingProcessor pingProcessor;
    private final EmulationProcessor emulationProcessor;
    private final Processor[] processors;

    private final Map<Class<? extends Check>, Check> checkMap = new HashMap<>();
    private final Check[] checks;
    private final TickHandler[] tickChecks;

    private boolean bypass;
    private boolean toggleAlert;
    private boolean kicked, banned;
    private int tick;

    public PlayerData(Player player) {
        this.player = player;

        bypass = player.hasPermission("axiom.bypass");
        toggleAlert = player.hasPermission("axiom.command.alerts") && player.hasMetadata(AlertsCommand.META_ALERTS);

        processors = new Processor[]{
                actionProcessor = new ActionProcessor(this),
                attributeProcessor = new AttributeProcessor(this),
                moveProcessor = new MoveProcessor(this),
                entityProcessor = new EntityProcessor(this),
                collideProcessor = new CollideProcessor(this),
                pingProcessor = new PingProcessor(this),
                emulationProcessor = new EmulationProcessor(this)
        };

        List<Check> loadedChecks = CHECK_MANAGER.loadChecks(this);
        List<TickHandler> tickHandlers = new ArrayList<>();

        for (Check check : loadedChecks) {
            checkMap.put(check.getClass(), check);

            if (check instanceof TickHandler) {
                tickHandlers.add((TickHandler) check);
            }
        }

        checks = loadedChecks.toArray(new Check[0]);
        tickChecks = tickHandlers.toArray(new TickHandler[0]);
    }

    public void process(WrappedPacket packet) {
        preProcess(packet);

        if (!bypass) {
            for (Check check : checks) {
                if (!CHECK_MANAGER.getCheckValue(check.getClass()).isEnabled()) continue;

                check.handle(packet);
            }
        }

        postProcess(packet);
    }

    private void preProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) ++tick;

        for (Processor processor : processors) processor.preProcess(packet);
    }

    private void postProcess(WrappedPacket packet) {
        for (Processor processor : processors) processor.postProcess(packet);
    }

    public void resetVL() {
        for (Check check : checks) check.resetVL();
    }

    public ActionProcessor getActionProcessor() {
        return actionProcessor;
    }

    public AttributeProcessor getAttributeProcessor() {
        return attributeProcessor;
    }

    public EntityProcessor getEntityProcessor() {
        return entityProcessor;
    }

    public MoveProcessor getMoveProcessor() {
        return moveProcessor;
    }

    public CollideProcessor getCollideProcessor() {
        return collideProcessor;
    }

    public PingProcessor getPingProcessor() {
        return pingProcessor;
    }

    public EmulationProcessor getEmulationProcessor() {
        return emulationProcessor;
    }

    public Processor[] getProcessors() {
        return processors;
    }

    public <T extends Check> T getCheck(Class<T> clazz) {
        return clazz.cast(checkMap.get(clazz));
    }

    public Check[] getChecks() {
        return checks;
    }

    public TickHandler[] getTickChecks() {
        return tickChecks;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    public boolean isToggleAlert() {
        return toggleAlert;
    }

    public void setToggleAlert(boolean toggleAlert) {
        this.toggleAlert = toggleAlert;
    }

    public boolean isKicked() {
        return kicked;
    }

    public void setKicked(boolean kicked) {
        this.kicked = kicked;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public int getTick() {
        return tick;
    }
}
