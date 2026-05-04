package me.dw1e.axiom.data.processor.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.impl.badpacket.BadPacketK;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.data.processor.Processor;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketTransaction;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;
import me.dw1e.axiom.packet.wrapper.server.SPacketTransaction;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class PingProcessor extends Processor {

    private final Deque<Short> sentTransactions = new ConcurrentLinkedDeque<>();
    private final Map<Short, TransactionData> transactionMap = new ConcurrentHashMap<>();
    private final Deque<PacketContainer> lagCachePackets = new ConcurrentLinkedDeque<>();

    private TransactionData currentTickTransaction;

    private short transactionID = Short.MIN_VALUE;

    private volatile long lastPing, ping;
    private long lastFlyingTime;

    private int lastPingTick;
    private int currentTick = -1;

    public PingProcessor(PlayerData data) {
        super(data);

        lastFlyingTime = System.currentTimeMillis();
    }

    @Override
    public void preProcess(WrappedPacket packet) {
        long now = System.currentTimeMillis();

        if (packet instanceof CPacketTransaction) {
            CPacketTransaction wrapper = (CPacketTransaction) packet;

            // 判断是不是我们自己发的确认包
            short id = wrapper.getActionId();
            if (!transactionMap.containsKey(id)) return;

            // 计算跳过了多少确认包
            int skipped = 0;
            for (short transaction : sentTransactions) {
                if (transaction == id) break;

                skipped++;
            }

            // 跳过确认包检测. 刚进服会误判, 延迟 5 秒再检查
            if (skipped > 0 && data.getTick() > 100) {
                data.getCheck(BadPacketK.class).flag("skipped=" + skipped);
            }

            // 如果他跳过了确认包, 那么我们就默认他也接收到了在此之前的包
            // PS: 这个跳包检测有小概率会因为其它发确认包的插件由于撞 transID 而误判, 并且会连锁误判下面的 "默认收到" 机制
            Short current;
            while ((current = sentTransactions.poll()) != null) {
                TransactionData transData = transactionMap.remove(current);

                if (transData != null) {
                    lastPing = ping;
                    ping = now - transData.time;

                    transData.runAll();
                }

                if (current == id) break;
            }

        } else if (packet instanceof SPacketTransaction) {
            SPacketTransaction wrapper = (SPacketTransaction) packet;

            short id = wrapper.getActionId();
            TransactionData transData = transactionMap.get(id);

            if (transData != null && transData.time == 0L) {
                transData.time = now;
                sentTransactions.add(id);
            }

        } else if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = (CPacketUseEntity) packet;

            // 吞掉从卡顿恢复后造成的攻击, 限制 FakeLag 优势. 正常玩家卡了本来就没什么游戏体验, 无所谓
            if (wrapper.getAction() == EnumWrappers.EntityUseAction.ATTACK && hasFast() && !data.isBypass()) {
                packet.setCancel(true);
            }
        }
    }

    @Override
    public void postProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            lastFlyingTime = System.currentTimeMillis();

            PacketContainer lagPacket;

            while ((lagPacket = lagCachePackets.poll()) != null) {
                Axiom.getPlugin().getProtocolManager().sendServerPacket(data.getPlayer(), lagPacket);
            }
        }
    }

    /*
     * 在当前 tick 上注册一个 confirm 回调
     * <p>
     * 特性：
     * - 同一个 tick 内所有 confirm 共享一个 transaction
     * - 只会发送一次 transaction 包
     * - 回包后执行所有 action
     */
    @SuppressWarnings("deprecation")
    public void confirm(Runnable runnable) {
        int tick = data.getTick();

        // 新 tick -> 创建新的 transaction
        if (tick != currentTick) {
            currentTick = tick;

            short id = transactionID++;
            if (transactionID >= 0) transactionID = Short.MIN_VALUE;

            currentTickTransaction = new TransactionData();
            transactionMap.put(id, currentTickTransaction);

            // 每 tick 只发送一次 transaction
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.TRANSACTION);
            packet.getIntegers().write(0, 0);
            packet.getShorts().write(0, id);
            packet.getBooleans().write(0, false);

            Axiom.getPlugin().getProtocolManager().sendServerPacket(data.getPlayer(), packet);
        }

        // 同 tick 的 confirm 全部挂到这个 transaction 上
        currentTickTransaction.add(runnable);
    }

    public void testConnect() {
        int tick = Axiom.getPlugin().getServerTickTask().getTick();

        if (tick - lastPingTick >= 20) {
            lastPingTick = tick;

            confirm(() -> {/* test ping */});
        }
    }

    public long getLastPing() {
        return lastPing;
    }

    public long getPing() {
        return ping;
    }

    public int getPingTicks() {
        return (int) Math.ceil(ping / 50.0);
    }

    public boolean hasPingJitter() {
        return hasPingJitter(50L, 0.25);
    }

    public boolean hasPingJitter(long minDiff, double ratio) {
        if (ping <= 0L || lastPing <= 0L) return false;

        long diff = Math.abs(ping - lastPing);
        return diff >= minDiff && diff >= lastPing * ratio;
    }

    public boolean hasLag() {
        return System.currentTimeMillis() - lastFlyingTime > 110L;
    }

    public boolean hasFast() {
        return System.currentTimeMillis() - lastFlyingTime < 15L;
    }

    public Deque<PacketContainer> getLagCachePackets() {
        return lagCachePackets;
    }

    private static final class TransactionData {

        private final Deque<Runnable> actions = new ConcurrentLinkedDeque<>();
        private volatile long time;

        public void add(Runnable action) {
            if (action != null) actions.add(action);
        }

        public void runAll() {
            Runnable action;

            while ((action = actions.poll()) != null) {
                action.run();
            }
        }
    }
}