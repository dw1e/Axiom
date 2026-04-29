package me.dw1e.axiom.packet.wrapper;

public abstract class WrappedPacket {

    private boolean cancel = false;

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
