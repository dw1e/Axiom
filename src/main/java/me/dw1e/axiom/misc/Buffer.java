package me.dw1e.axiom.misc;

public final class Buffer {

    private final double limit;
    private double value;

    public Buffer(double limit) {
        this.limit = limit;
    }

    public double add(double amount) {
        return value = Math.min(limit, value + amount);
    }

    public void reduce(double amount) {
        value = Math.max(0.0, value - amount);
    }

    public double add() {
        return add(1.0);
    }

    public void reduce() {
        reduce(1.0);
    }

    public double multiply(double factor) {
        return value = Math.max(0.0, Math.min(limit, value * factor));
    }

    public void reset() {
        value = 0.0;
    }

    public double get() {
        return value;
    }
}
