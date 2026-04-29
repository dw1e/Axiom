package me.dw1e.axiom.data.processor;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.nms.NMSVisitor;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public abstract class Processor {

    protected static final NMSVisitor VISITOR = Axiom.getPlugin().getNmsManager().getVisitor();

    protected PlayerData data;

    public Processor(PlayerData data) {
        this.data = data;
    }

    // 预处理: 在检测执行前调用, 用于先更新玩家数据, 状态缓存等
    // 后续所有检测读取到的都是 preProcess 后的数据状态
    public abstract void preProcess(WrappedPacket packet);

    // 后处理: 在所有检测执行完后调用, 用于收尾, 提交状态, 清理临时数据等
    // 如果某些数据在 preProcess 就直接修改, 检测将无法获取处理前或处理中间状态
    public abstract void postProcess(WrappedPacket packet);
}
