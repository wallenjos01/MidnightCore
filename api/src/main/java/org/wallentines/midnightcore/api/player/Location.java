package org.wallentines.midnightcore.api.player;

import org.wallentines.midnightlib.config.serialization.InlineSerializer;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

public class Location {

    private final Identifier worldId;
    private final Vec3d coords;
    private final float yaw;
    private final float pitch;

    public Location(Identifier worldId, Vec3d coords, float yaw, float pitch) {
        this.worldId = worldId;
        this.coords = coords;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Identifier getWorldId() {
        return worldId;
    }

    public Vec3d getCoordinates() {
        return coords;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public double getX() {

        return coords.getX();
    }

    public double getY() {

        return coords.getY();
    }

    public double getZ() {

        return coords.getZ();
    }

    public static final InlineSerializer<Location> SERIALIZER = new InlineSerializer<Location>() {
        @Override
        public Location deserialize(String s) {

            String[] parts = s.split(";");
            if (parts.length == 0) return null;

            Identifier id = new Identifier("minecraft", "overworld");
            Vec3d xyz = Vec3d.parse(parts.length > 2 ? parts[1] : parts[0]);
            float yaw = 0.0f, pitch = 0.0f;

            if (parts.length > 2) {
                id = Identifier.parseOrDefault(parts[0], "minecraft");
            }

            if (parts.length > 1) {
                String rot = parts.length > 2 ? parts[2] : parts[1];
                String[] values = rot.split(",");

                yaw = Float.parseFloat(values[0]);
                pitch = Float.parseFloat(values[1]);
            }

            return new Location(id, xyz, yaw, pitch);
        }

        @Override
        public String serialize(Location object) {
            return String.format("%s;%f,%f,%f;%f,%f", object.worldId.toString(), object.getX(), object.getY(), object.getZ(), object.getYaw(), object.getPitch());
        }

        @Override
        public boolean canDeserialize(String s) {
            return deserialize(s) != null;
        }
    };
}
