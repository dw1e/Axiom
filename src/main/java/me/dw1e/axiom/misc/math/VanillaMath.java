package me.dw1e.axiom.misc.math;

public final class VanillaMath {

    private static final float[] SIN_TABLE = new float[65536];

    static {
        for (int j = 0; j < SIN_TABLE.length; ++j) {
            SIN_TABLE[j] = (float) Math.sin((double) j * Math.PI * 2.0 / 65536.0);
        }
    }

    public static float sin(float var0) {
        return SIN_TABLE[(int) (var0 * 10430.378F) & '\uffff'];
    }

    public static float cos(float var0) {
        return SIN_TABLE[(int) (var0 * 10430.378F + 16384.0F) & '\uffff'];
    }

    public static float sqrt(double var0) {
        return (float) Math.sqrt(var0);
    }
}
