package org.wallentines.mcore.util;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.TextColor;

import java.util.UUID;

public class ItemUtil {

    /**
     * Splits a UUID into four 32-bit integers
     * @param uuid The UUID to split
     * @return An integer array of length 4
     */
    public static int[] splitUUID(UUID uuid) {
        long u1 = uuid.getMostSignificantBits();
        long u2 = uuid.getLeastSignificantBits();
        return new int[] { (int) (u1 >> 32), (int) u1, (int) (u2 >> 32), (int) u2 };
    }

    /**
     * Creates a UUID from four 32-bit integers
     * @param ints The data to create a UUID from
     * @return A UUID
     */
    public static UUID joinUUID(int[] ints) {

        if(ints.length != 4) {
            MidnightCoreAPI.LOGGER.warn("Attempt to form UUID from array of length " + ints.length + "! Expected length 4!");
            return null;
        }

        return new UUID(
                (long) ints[0] << 32 | (long) ints[1] & 0xFFFFFFFFL,
                (long) ints[2] << 32 | (long) ints[3] & 0xFFFFFFFFL
        );
    }

    /**
     * Set italic to false on the component if unset, so components appear as intended on ItemStacks
     * @param component The component to modify
     * @return A modified component
     */
    public static Component applyItemNameBaseStyle(Component component) {
        Component out = component;
        if(out.italic == null) {
            out = out.withItalic(false);
        }
        return out;
    }

    /**
     * Set italic to false and color to white on the component if unset, so components appear as intended on ItemStacks
     * @param component The component to modify
     * @return A modified component
     */
    public static Component applyItemLoreBaseStyle(Component component) {
        Component out = component;
        if(out.italic == null) {
            out = out.withItalic(false);
        }
        if(out.color == null) {
            out = out.withColor(TextColor.WHITE);
        }
        return out;
    }
}
