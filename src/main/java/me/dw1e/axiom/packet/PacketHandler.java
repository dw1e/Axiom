package me.dw1e.axiom.packet;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class PacketHandler extends PacketAdapter {

    private final Axiom plugin;

    public PacketHandler(Axiom plugin) {
        super(plugin, ListenerPriority.HIGH, ClassWrapper.getProcessedPackets());
        this.plugin = plugin;
    }

    public void register() {
        plugin.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.isCancelled() || event.isPlayerTemporary()) return;

        PlayerData data = plugin.getDataManager().getData(event.getPlayer().getUniqueId());
        WrappedPacket wrappedPacket = ClassWrapper.wrapPacket(event.getPacketType(), event.getPacket());

        if (data != null) data.process(wrappedPacket);
        if (wrappedPacket.isCancel()) event.setCancelled(true);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled() || event.isPlayerTemporary()) return;

        PacketContainer packet = event.getPacket();

        PlayerData data = plugin.getDataManager().getData(event.getPlayer().getUniqueId());
        WrappedPacket wrappedPacket = ClassWrapper.wrapPacket(event.getPacketType(), packet);

        if (data != null) {
            // 玩家卡顿的同时服务器停止向其发送数据包, 对于真正卡顿的玩家几乎无影响
            // 不要停止发送所有数据包, 目前只停发实体位置更新包 (PS: 会在恢复后发回去, 不是拦截就完了的!)
            if (data.getPingProcessor().hasLag() && ClassWrapper.CACHE_PACKETS.contains(packet.getType())) {
                data.getPingProcessor().getLagCachePackets().add(packet);
                event.setCancelled(true);

            } else {
                data.process(wrappedPacket);
            }
        }

        if (wrappedPacket.isCancel()) event.setCancelled(true);
    }
}
