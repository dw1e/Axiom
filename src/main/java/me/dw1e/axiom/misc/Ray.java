package me.dw1e.axiom.misc;

import org.bukkit.util.Vector;

public final class Ray implements Cloneable {

    private Vector origin, direction;

    public Ray(Vector origin, Vector direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector getPointAtDistance(double distance) {
        return origin.clone().add(direction.clone().multiply(distance));
    }

    public Ray clone() {
        try {
            Ray clone = (Ray) super.clone();
            clone.origin = origin.clone();
            clone.direction = direction.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Vector getOrigin() {
        return origin;
    }

    public Vector getDirection() {
        return direction;
    }
}