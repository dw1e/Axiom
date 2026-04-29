package me.dw1e.axiom.misc.util;

import me.dw1e.axiom.misc.math.VanillaMath;
import org.bukkit.util.Vector;

import java.util.Collection;

public final class MathUtil {

    public static final double EXPANDER = Math.pow(2.0, 24.0);

    public static double hypot(final double x, final double z) {
        return Math.sqrt(x * x + z * z);
    }

    public static double gcd(double a, double b) {
        long expansionA = (long) (Math.abs(a) * EXPANDER), expansionB = (long) (Math.abs(b) * EXPANDER);

        return getGcd(expansionA, expansionB) / EXPANDER;
    }

    public static long getGcd(long current, long previous) {
        return previous <= 16384L ? current : getGcd(previous, current % previous);
    }

    public static float wrapAngleTo180(float angle) {
        angle %= 360.0F;

        if (angle >= 180.0F) angle -= 360.0F;
        if (angle < -180.0F) angle += 360.0F;

        return angle;
    }

    public static double angle(Vector a, Vector b) {
        double dot = Math.min(Math.max(a.dot(b) / (a.length() * b.length()), -1), 1);
        return Math.acos(dot);
    }

    public static Vector getDirection(float yaw, float pitch) {
        Vector vector = new Vector();
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        vector.setY(-VanillaMath.sin(rotY));
        double xz = VanillaMath.cos(rotY);
        vector.setX(-xz * VanillaMath.sin(rotX));
        vector.setZ(xz * VanillaMath.cos(rotX));
        return vector;
    }

    public static double mean(Collection<? extends Number> samples) {
        if (samples.isEmpty()) return 0.0;

        double sum = 0.0;

        for (Number val : samples) sum += val.doubleValue();

        return sum / samples.size();
    }

    public static double variance(Collection<? extends Number> samples) {
        if (samples.size() <= 1) return 0.0;

        double sumSquaredDiff = 0.0;

        for (Number val : samples) {
            double diff = val.doubleValue() - mean(samples);

            sumSquaredDiff += diff * diff;
        }

        return sumSquaredDiff / (samples.size() - 1);
    }

    public static double deviation(Collection<? extends Number> samples) {
        return Math.sqrt(variance(samples));
    }
}
