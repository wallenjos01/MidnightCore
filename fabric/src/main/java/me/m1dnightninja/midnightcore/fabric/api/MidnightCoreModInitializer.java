package me.m1dnightninja.midnightcore.fabric.api;

import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;

import java.util.Collection;
import java.util.Map;

public interface MidnightCoreModInitializer {

    void onInitialize();
    void onAPICreated(MidnightCore core, MidnightCoreAPI api);

    default Collection<IModule> getModules() { return null; }
    default Map<ResourceLocation, ChunkGenerator> getGenerators() { return null; }

}
