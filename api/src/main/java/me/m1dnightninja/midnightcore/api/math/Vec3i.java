package me.m1dnightninja.midnightcore.api.math;

public class Vec3i {
    private final int[] data;

    public Vec3i(int x, int y, int z) {
        this.data = new int[]{x, y, z};
    }

    public int getX() {
        return this.data[0];
    }

    public int getY() {
        return this.data[1];
    }

    public int getZ() {
        return this.data[2];
    }

    public double distance(Vec3i vec2) {
        return Math.sqrt(this.getX() - vec2.getX() ^ 2 + (this.getY() - vec2.getY()) ^ 2 + (this.getZ() - vec2.getZ()) ^ 2);
    }

    public Vec3i add(int i) {
        return new Vec3i(data[0] + i, data[1] + i, data[2] + i);
    }

    public Vec3i multiply(int i) {
        return new Vec3i(data[0] * i, data[1] * i, data[2] * i);
    }

    public Vec3i add(Vec3i i) {
        return new Vec3i(data[0] + i.data[0], data[1] + i.data[1], data[2] + i.data[2]);
    }

    public Vec3i multiply(Vec3i i) {
        return new Vec3i(data[0] * i.data[0], data[1] * i.data[1], data[2] * i.data[2]);
    }

    public Vec3i subtract(int i) {
        return new Vec3i(data[0] - i, data[1] - i, data[2] - i);
    }

    public Vec3i subtract(Vec3i i) {
        return new Vec3i(data[0] - i.data[0], data[1] - i.data[1], data[2] - i.data[2]);
    }
}

