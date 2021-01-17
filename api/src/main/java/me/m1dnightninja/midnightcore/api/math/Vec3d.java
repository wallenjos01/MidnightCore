package me.m1dnightninja.midnightcore.api.math;

public class Vec3d {

    private final double[] data;

    public Vec3d(double x, double y, double z) {
        this.data = new double[] { x, y, z };
    }

    public double getX() {
        return data[0];
    }

    public double getY() {
        return data[1];
    }

    public double getZ() {
        return data[2];
    }

    public double distance(Vec3d vec2) {

        double ax = getX() - vec2.getX();
        double ay = getY() - vec2.getY();
        double az = getZ() - vec2.getZ();

        return Math.sqrt((ax*ax) + (ay*ay) + (az*az));
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Vec3d)) return false;

        Vec3d other = (Vec3d) obj;

        return other.getX() == getX() && other.getY() == getY() && other.getZ() == getZ();
    }
}
