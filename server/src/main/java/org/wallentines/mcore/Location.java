package org.wallentines.mcore;

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
}
