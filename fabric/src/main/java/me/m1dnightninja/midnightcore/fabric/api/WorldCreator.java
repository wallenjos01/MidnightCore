package me.m1dnightninja.midnightcore.fabric.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;

public class WorldCreator {

    private final ResourceLocation worldId;

    private ResourceKey<LevelStem> dimension = LevelStem.OVERWORLD;
    private ChunkGenerator generator;

    private boolean upgradeWorld = false;
    private boolean safeMode = false;

    private String levelName;
    private long seed = 0L;
    private GameType defaultGameMode = GameType.SURVIVAL;
    private Difficulty difficulty = Difficulty.NORMAL;
    private GameRules gameRules = new GameRules();

    private BlockPos spawnPosition;

    public WorldCreator(ResourceLocation worldId) {
        this.worldId = worldId;
        levelName = worldId.getPath();
    }

    public WorldCreator(ResourceLocation worldId, ResourceKey<LevelStem> dimension) {
        this.worldId = worldId;
        this.dimension = dimension;
        levelName = worldId.getPath();
    }

    public WorldCreator(ResourceLocation worldId, ResourceKey<LevelStem> dimension, ChunkGenerator generator) {
        this.worldId = worldId;
        this.dimension = dimension;
        this.generator = generator;
        levelName = worldId.getPath();
    }


    public ResourceLocation getWorldId() {
        return worldId;
    }

    public ResourceKey<LevelStem> getDimension() {
        return dimension;
    }

    public void setDimension(ResourceKey<LevelStem> dimension) {
        this.dimension = dimension;
    }

    public ChunkGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(ChunkGenerator generator) {
        this.generator = generator;
    }

    public boolean isUpgradeWorld() {
        return upgradeWorld;
    }

    public void setUpgradeWorld(boolean upgradeWorld) {
        this.upgradeWorld = upgradeWorld;
    }

    public boolean isSafeMode() {
        return safeMode;
    }

    public void setSafeMode(boolean safeMode) {
        this.safeMode = safeMode;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public GameType getDefaultGameMode() {
        return defaultGameMode;
    }

    public void setDefaultGameMode(GameType defaultGameMode) {
        this.defaultGameMode = defaultGameMode;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public GameRules getGameRules() {
        return gameRules;
    }

    public void setGameRules(GameRules gameRules) {
        this.gameRules = gameRules;
    }

    public BlockPos getSpawnPosition() {
        return spawnPosition;
    }

    public void setSpawnPosition(BlockPos spawnPosition) {
        this.spawnPosition = spawnPosition;
    }

    public LevelSettings getLevelInfo() {
        return new LevelSettings(levelName, defaultGameMode, false, difficulty, false, gameRules, DataPackConfig.DEFAULT);
    }
}
