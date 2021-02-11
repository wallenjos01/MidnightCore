package me.m1dnightninja.midnightcore.api.math;

public class Vec3d {
    private final double[] data;

    public Vec3d(double x, double y, double z) {
        this.data = new double[]{x, y, z};
    }

    public double getX() {
        return this.data[0];
    }

    public double getY() {
        return this.data[1];
    }

    public double getZ() {
        return this.data[2];
    }

    public double distance(Vec3d vec2) {
        double ax = this.getX() - vec2.getX();
        double ay = this.getY() - vec2.getY();
        double az = this.getZ() - vec2.getZ();
        return Math.sqrt(ax * ax + ay * ay + az * az);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3d)) {
            return false;
        }
        Vec3d other = (Vec3d)obj;
        return other.getX() == this.getX() && other.getY() == this.getY() && other.getZ() == this.getZ();
    }

    @Override
    public String toString() {
        return getX() + "," + getY() + "," + getZ();
    }

    public static Vec3d parse(String str) {

        if(str == null || str.length() == 0 || !str.contains(",")) return null;
        String[] xyz = str.split(",");

        try {
            double x = Double.parseDouble(xyz[0]);
            double y = Double.parseDouble(xyz[1]);
            double z = Double.parseDouble(xyz[2]);

            return new Vec3d(x,y,z);
        } catch (NumberFormatException ex) {
            return null;
        }

    }
}
