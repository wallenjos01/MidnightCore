package org.wallentines.mcore;

import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

/**
 * An interface representing a server-side entity
 */
public interface Entity {

    /**
     * Retrieves the Entity's unique ID
     * @return The Entity's UUID
     */
    UUID getUUID();

    /**
     * Gets the entity's display name
     * @return The entity's display name
     */
    Component getDisplayName();

    /**
     * Determines the ID of the dimension the entity is currently in
     * @return The entity's dimension ID
     */
    Identifier getDimensionId();

    /**
     * Determines the entity's position in the world
     * @return The entity's position
     */
    Vec3d getPosition();

    /**
     * Gets the entity's Yaw (Y-Rotation)
     * @return The entity's yaw
     */
    float getYaw();

    /**
     * Gets the entity's Pitch (X-Rotation)
     * @return The entity's pitch
     */
    float getPitch();

    /**
     * Determines if the entity object is still valid in the current context. Will be false after it dies or despawns.
     * @return Whether the entity object is still valid
     */
    boolean isRemoved();

    /**
     * Gets the entity's Location
     * @return The entity's location
     */
    default Location getLocation() {
        return new Location(getDimensionId(), getPosition(), getYaw(), getPitch());
    }

    /**
     * Teleports the entity to the given location
     * @param location The location to teleport to
     */
    void teleport(Location location);

}
