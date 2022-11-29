package org.wallentines.midnightcore.fabric.level;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class WorldConfig {

    private final ResourceKey<Level> rootDimensionId;

    private boolean hardcore = false;
    private boolean generateStructures = true;
    private boolean bonusChest = false;
    private boolean noSave = false;

    private long seed = RandomSource.create().nextLong();

    private Difficulty difficulty = Difficulty.NORMAL;

    private GameType defaultGameMode = GameType.SURVIVAL;
    private ResourceKey<WorldPreset> worldType = WorldPresets.NORMAL;
    private ResourceKey<LevelStem> rootDimensionType = LevelStem.OVERWORLD;

    private ResourceKey<Level> netherOverride = null;
    private ResourceKey<Level> endOverride = null;
    private ChunkGenerator generator;
    private ConfigSection generatorSettings = new ConfigSection();

    private int functionPermissionLevel = 2;
    private boolean ignoreSessionLock = false;


    public WorldConfig(Identifier id) {
        this(ConversionUtil.toResourceLocation(id));
    }

    public WorldConfig(ResourceLocation rootDimensionId) {
        this.rootDimensionId = ResourceKey.create(Registry.DIMENSION_REGISTRY, rootDimensionId);
    }

    public WorldConfig(ResourceKey<Level> rootDimensionId) {
        this.rootDimensionId = rootDimensionId;
    }


    public ResourceKey<Level> getRootDimensionId() {
        return rootDimensionId;
    }


    public boolean isHardcore() {
        return hardcore;
    }

    public WorldConfig hardcore(boolean hardcore) {
        this.hardcore = hardcore; return this;
    }

    public boolean shouldNotSave() {
        return noSave;
    }

    public WorldConfig noSave(boolean noSave) {
        this.noSave = noSave;
        return this;
    }

    public long getSeed() {
        return seed;
    }

    public WorldConfig seed(long seed) {
        this.seed = seed;
        return this;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public WorldConfig difficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public GameType getDefaultGameMode() {
        return defaultGameMode;
    }

    public WorldConfig defaultGameMode(GameType defaultGameMode) {
        this.defaultGameMode = defaultGameMode;
        return this;
    }

    public ResourceKey<WorldPreset> getWorldType() {
        return worldType;
    }

    public WorldConfig worldType(ResourceKey<WorldPreset> worldType) {
        this.worldType = worldType; return this;
    }

    public ResourceKey<LevelStem> getRootDimensionType() {
        return rootDimensionType;
    }

    public WorldConfig rootDimensionType(ResourceKey<LevelStem> rootDimensionType) {
        this.rootDimensionType = rootDimensionType;
        return this;
    }

    public ResourceKey<Level> getNetherDimensionId() {
        return netherOverride;
    }

    public WorldConfig netherId(ResourceKey<Level> nether) {
        this.netherOverride = nether;
        return this;
    }

    public ResourceKey<Level> getEndDimensionId() {
        return endOverride;
    }

    public WorldConfig endId(ResourceKey<Level> end) {
        this.endOverride = end;
        return this;
    }

    public ChunkGenerator getGenerator() {
        return generator;
    }

    public WorldConfig generator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }

    public ConfigSection getGeneratorSettings() {
        return generatorSettings;
    }

    public WorldConfig generatorSettings(ConfigSection generatorSettings) {
        this.generatorSettings = generatorSettings;
        return this;
    }

    public int getFunctionPermissionLevel() {
        return functionPermissionLevel;
    }

    public WorldConfig functionPermissionLevel(int functionPermissionLevel) {
        this.functionPermissionLevel = functionPermissionLevel;
        return this;
    }

    public boolean shouldIgnoreSessionLock() {
        return ignoreSessionLock;
    }

    public WorldConfig ignoreSessionLock(boolean ignoreSessionLock) {
        this.ignoreSessionLock = ignoreSessionLock;
        return this;
    }

    public boolean shouldGenerateStructures() {
        return generateStructures;
    }

    public WorldConfig generateStructures(boolean generateStructures) {
        this.generateStructures = generateStructures;
        return this;
    }

    public boolean hasBonusChest() {
        return bonusChest;
    }

    public WorldConfig setBonusChest(boolean bonusChest) {
        this.bonusChest = bonusChest;
        return this;
    }
}
