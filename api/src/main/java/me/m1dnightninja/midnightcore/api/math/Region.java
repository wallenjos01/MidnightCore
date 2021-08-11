package me.m1dnightninja.midnightcore.api.math;

import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.config.InlineSerializer;

public class Region {

    private final Vec3d lower;
    private final Vec3d extent;

    public Region(Vec3d lower, Vec3d extent) {
        this.lower = lower;
        this.extent = extent;
    }

    public Vec3d getLowerBound() {
        return lower;
    }

    public Vec3d getUpperBound() {
        return lower.add(extent);
    }

    public Vec3d getExtent() {
        return extent;
    }

    public boolean isWithin(Vec3d vector) {

        Vec3d upper = getUpperBound();

        return vector.getX() >= lower.getX() && vector.getX() < upper.getX() &&
               vector.getY() >= lower.getY() && vector.getY() < upper.getY() &&
               vector.getZ() >= lower.getZ() && vector.getZ() < upper.getZ();

    }

    public boolean isWithin(Vec3i vector) {

        return isWithin(new Vec3d(vector.getX(), vector.getY(), vector.getZ()));
    }

    public static final InlineSerializer<Region> SERIALIZER = new InlineSerializer<Region>() {
        @Override
        public Region deserialize(String s) {

            String[] ss = s.split(";");

            Vec3d lower = Vec3d.parse(ss[0]);
            Vec3d extent = Vec3d.parse(ss[1]);

            return new Region(lower, extent);
        }

        @Override
        public String serialize(Region object) {
            return object.lower.toString() + ";" + object.extent.toString();
        }
    };

}
