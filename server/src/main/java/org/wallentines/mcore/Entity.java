package org.wallentines.mcore;

import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
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
     * Gets the entity's type ID
     * @return the entity's type
     */
    Identifier getType();

    /**
     * Gets the server which created this entity
     * @return The entity's server
     */
    Server getServer();

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

    /**
     * Gets the item in the given equipment slot.
     * @param slot The slot to look up
     * @return The item in that slot
     */
    ItemStack getItem(EquipmentSlot slot);

    /**
     * Changes the item in the given equipment slot. For entities that cannot hold items, this will do nothing.
     * @param slot The slot to update
     * @param item The item to put there
     */
    void setItem(EquipmentSlot slot, ItemStack item);
    
    /**
     * Runs a command on the server as this entity
     * @param command The command text to run
     */
    void runCommand(String command);

    enum EquipmentSlot {

        MAINHAND("mainhand"),
        OFFHAND("offhand"),
        FEET("feet"),
        LEGS("legs"),
        CHEST("chest"),
        HEAD("head");

        private final String id;

        EquipmentSlot(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static EquipmentSlot byId(String id) {
            for(EquipmentSlot slot : values()) {
                if(slot.id.equals(id)) {
                    return slot;
                }
            }
            return null;
        }

        public static final Serializer<EquipmentSlot> SERIALIZER = InlineSerializer.of(EquipmentSlot::getId, EquipmentSlot::byId);
    }

    static void registerPlaceholders(PlaceholderManager manager) {

        manager.registerSupplier("entity_uuid", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Entity.class, en -> en.getUUID().toString(), "")));
        manager.registerSupplier("entity_type", PlaceholderSupplier.of(ctx -> ctx.onValueOr(Entity.class, en -> Component.translate("entity." + en.getType().getNamespace() + "." + en.getType().getPath()), Component.empty())));
        manager.registerSupplier("entity_name", PlaceholderSupplier.of(ctx -> ctx.onValueOr(Entity.class, Entity::getDisplayName, Component.empty())));

    }

}
