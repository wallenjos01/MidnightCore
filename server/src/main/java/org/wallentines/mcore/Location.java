package org.wallentines.mcore;

import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

/**
 * A data type representing an entity's dimension, position, and rotation
 */
public class Location {

    /**
     * The location's dimension
     */
    public final Identifier dimension;

    /**
     * The location's position
     */
    public final Vec3d position;

    /**
     * The location's rotation yaw
     */
    public final float yaw;

    /**
     * The location's rotation pitch
     */
    public final float pitch;

    /**
     * Constructs a new Location with the given parameters
     * @param dimension The dimension
     * @param position The position
     * @param yaw The rotation yaw
     * @param pitch The rotation pitch
     */
    public Location(Identifier dimension, Vec3d position, float yaw, float pitch) {
        this.dimension = dimension;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Constructs a new Location with the given parameters
     * @param dimension The dimension
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @param yaw The rotation yaw
     * @param pitch The rotation pitch
     */
    public Location(Identifier dimension, double x, double y, double z, float yaw, float pitch) {
        this(dimension, new Vec3d(x,y,z), yaw, pitch);
    }

    public static Location parse(String s) {
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
    public String toString() {
        return String.format("%s;%f,%f,%f;%f,%f", dimension.toString(), position.getX(), position.getY(), position.getZ(), yaw, pitch);
    }

    public static final Serializer<Location> SERIALIZER = org.wallentines.mdcfg.serializer.InlineSerializer.of(Location::toString, Location::parse)
            .or(ObjectSerializer.create(
                    Identifier.serializer("minecraft").<Location>entry("world", loc -> loc.dimension).orElse(new Identifier("minecraft", "overworld")),
                    Vec3d.SERIALIZER.entry("coordinates", loc -> loc.position),
                    Serializer.FLOAT.<Location>entry("yaw", loc -> loc.yaw).orElse(0.0f),
                    Serializer.FLOAT.<Location>entry("pitch", loc -> loc.pitch).orElse(0.0f),
                    Location::new
            ));

}
