package me.m1dnightninja.midnightcore.api.player;

import me.m1dnightninja.midnightcore.api.config.InlineSerializer;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

public class Location {

    private MIdentifier world;
    private Vec3d xyz;
    private float yaw;
    private float pitch;

    public Location(MIdentifier world, Vec3d xyz, float yaw, float pitch) {
        this.world = world;
        this.xyz = xyz;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location(MIdentifier world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.xyz = new Vec3d(x,y,z);
        this.yaw = yaw;
        this.pitch = pitch;
    }



    public MIdentifier getWorld() {
        return world;
    }

    public void setWorld(MIdentifier world) {
        this.world = world;
    }

    public Vec3d getCoordinates() {
        return xyz;
    }

    public void setCoordinates(Vec3d xyz) {
        this.xyz = xyz;
    }

    public double getX() {
        return xyz.getX();
    }

    public double getY() {
        return xyz.getY();
    }

    public double getZ() {
        return xyz.getZ();
    }

    public void setX(double x) {
        xyz = new Vec3d(x, xyz.getY(), xyz.getZ());
    }

    public void setY(double y) {
        xyz = new Vec3d(xyz.getX(), y, xyz.getZ());
    }

    public void setZ(double z) {
        xyz = new Vec3d(xyz.getX(), xyz.getY(), z);
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public String toString() {
        return String.format("%s;%f,%f,%f;%f,%f", world.toString(), getX(), getY(), getZ(), yaw, pitch);
    }

    public static final InlineSerializer<Location> SERIALIZER = new InlineSerializer<Location>() {
        @Override
        public Location deserialize(String s) {

            String[] parts = s.split(";");
            if(parts.length == 0) return null;

            MIdentifier id = MIdentifier.create("minecraft", "overworld");
            Vec3d xyz = Vec3d.parse(parts.length > 2 ? parts[1] : parts[0]);
            float yaw = 0.0f, pitch = 0.0f;

            if(parts.length > 2) {
                id = MIdentifier.parseOrDefault(parts[0], "minecraft");
            }

            if(parts.length > 1) {
                String rot = parts.length > 2 ? parts[2] : parts[1];
                String[] vals = rot.split(",");

                yaw = Float.parseFloat(vals[0]);
                pitch = Float.parseFloat(vals[1]);
            }

            return new Location(id, xyz, yaw, pitch);
        }

        @Override
        public String serialize(Location object) {
            return object.toString();
        }
    };
}
