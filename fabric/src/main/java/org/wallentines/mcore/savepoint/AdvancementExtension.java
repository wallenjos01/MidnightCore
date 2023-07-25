package org.wallentines.mcore.savepoint;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;

import java.util.Map;

/**
 * Extends the default PlayerAdvancements object with new functionality
 */
public interface AdvancementExtension {

    /**
     * Loads data from an existing map
     * @param map The data to load
     * @param serverAdvancementManager The server advancement manager to read advancements from
     */
    void loadFromMap(Map<ResourceLocation, AdvancementProgress> map, ServerAdvancementManager serverAdvancementManager);

    /**
     * Saves advancement data to a map
     * @return Saved advancement data
     */
    Map<ResourceLocation, AdvancementProgress> saveToMap();

    /**
     * Revokes all advancements granted to the player
     */
    void revokeAll();

}
