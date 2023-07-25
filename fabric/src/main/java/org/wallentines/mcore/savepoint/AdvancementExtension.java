package org.wallentines.mcore.savepoint;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;

import java.util.Map;

public interface AdvancementExtension {

    void loadFromMap(Map<ResourceLocation, AdvancementProgress> map, ServerAdvancementManager serverAdvancementManager);

    Map<ResourceLocation, AdvancementProgress> saveToMap();

    void revokeAll(ServerAdvancementManager serverAdvancementManager);

}
