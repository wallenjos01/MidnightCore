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
}

