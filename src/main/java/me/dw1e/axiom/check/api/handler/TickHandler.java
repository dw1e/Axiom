package me.dw1e.axiom.check.api.handler;

public interface TickHandler {

    // 每服务器 Tick 调用一次
    default void onTick() {
    }
}
