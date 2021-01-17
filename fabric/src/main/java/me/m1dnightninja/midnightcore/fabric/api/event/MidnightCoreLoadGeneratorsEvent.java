package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;

import java.util.HashMap;

public class MidnightCoreLoadGeneratorsEvent extends Event {

    private final HashMap<ResourceLocation, ChunkGenerator> generators;

    public MidnightCoreLoadGeneratorsEvent(HashMap<ResourceLocation, ChunkGenerator> generators) {
        this.generators = generators;
    }

    public HashMap<ResourceLocation, ChunkGenerator> getGenerators() {
        return generators;
    }
}
