package me.m1dnightninja.midnightcore.api.math;

public class Vec3i {

    private final int[] data;

    public Vec3i(int x, int y, int z) {
        this.data = new int[] { x, y, z };
    }

    public int getX() {
        return data[0];
    }

    public int getY() {
        return data[1];
    }

    public int getZ() {
        return data[2];
    }

    public double distance(Vec3i vec2) {

        return Math.sqrt((getX()- vec2.getX())^2 + (getY()- vec2.getY())^2 + (getZ()- vec2.getZ())^2);
    }
}
