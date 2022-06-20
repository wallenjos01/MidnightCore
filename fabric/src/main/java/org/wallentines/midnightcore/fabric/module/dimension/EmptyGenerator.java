package org.wallentines.midnightcore.fabric.module.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EmptyGenerator extends ChunkGenerator {

    private static final Logger LOGGER = LogManager.getLogger("EmptyGenerator");

    public static final Codec<EmptyGenerator> CODEC = RecordCodecBuilder.create(instance ->
        commonCodec(instance).and(
            instance.group(
                RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter((gen) -> gen.biomes),
                Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(gen -> Optional.of(gen.biome))
            )
        ).apply(instance, EmptyGenerator::new)
    );

    private final Registry<Biome> biomes;
    private final Holder<Biome> biome;

    public EmptyGenerator(Registry<StructureSet> structureReg, Registry<Biome> biomes, Optional<Holder<Biome>> biome) {

        super(structureReg, Optional.empty(), new FixedBiomeSource(biome.orElseGet(() -> {
            LOGGER.error("Unknown biome, defaulting to plains");
            return biomes.getOrCreateHolderOrThrow(Biomes.PLAINS);
        })),
            b -> new BiomeGenerationSettings.Builder().build()
        );

        this.biomes = biomes;
        this.biome = biome.orElseGet(() -> biomes.getOrCreateHolderOrThrow(Biomes.PLAINS));
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) { }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) { }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) { }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {

        return CompletableFuture.completedFuture(chunkAccess);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
        return 64;
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return levelHeightAccessor.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {

        BlockState[] states = new BlockState[levelHeightAccessor.getHeight()];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());

        return new NoiseColumn(levelHeightAccessor.getMinBuildHeight(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) { }

    public static final EmptyGenerator FOREST = new EmptyGenerator(BuiltinRegistries.STRUCTURE_SETS, BuiltinRegistries.BIOME, BuiltinRegistries.BIOME.getHolder(Biomes.FOREST));

}
