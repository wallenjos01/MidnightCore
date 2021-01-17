package me.m1dnightninja.midnightcore.fabric.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;

import java.util.Collections;
import java.util.Optional;

public class EmptyGenerator extends ChunkGenerator {

    private static final Codec<EmptyGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(ResourceLocation.CODEC.fieldOf("biome").forGetter(generator -> generator.biome)).apply(instance, instance.stable(EmptyGenerator::new)));

    private final ResourceLocation biome;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public EmptyGenerator(ResourceLocation biome) {
        super(createBiomeSource(biome), new StructureSettings(Optional.empty(), Collections.emptyMap()));
        this.biome = biome;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion region, ChunkAccess chunk) { }

    @Override
    public void fillFromNoise(LevelAccessor world, StructureFeatureManager accessor, ChunkAccess chunk) {
        for(int x = 0 ; x < 16 ; x++) {
            for(int y = 0 ; y < getGenDepth() ; y++) {
                for(int z = 0 ; z < 16 ; z++) {
                    chunk.setBlockState(new BlockPos(x,y,z), AIR, false);
                }
            }
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType) {
        return 256;
    }

    @Override
    public BlockGetter getBaseColumn(int x, int z) {
        BlockState[] st = new BlockState[getGenDepth()];

        for(int i = 0 ; i < getGenDepth() ; i++) {
            st[i] = AIR;
        }

        return new NoiseColumn(st);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    private static BiomeSource createBiomeSource(ResourceLocation biome) {
        return new FixedBiomeSource(MidnightCore.getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).get(biome));
    }
}
