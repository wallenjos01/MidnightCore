package org.wallentines.midnightcore.common;

import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.item.ItemConverter;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.io.File;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class MidnightCoreImpl extends MidnightCoreAPI {

    private final FileConfig config;
    private final File dataFolder;
    private final Version gameVersion;
    private final ItemConverter itemConverter;
    private final PlayerManager playerManager;
    private final Function<MComponent, InventoryGUI> guiFunction;
    private final BiFunction<String, MComponent, CustomScoreboard> scoreboardFunction;
    private final BiConsumer<String, Boolean> console;

    private final Consumer<Runnable> serverSubmitter;
    private final Random random = new Random();

    private final ModuleManager<MidnightCoreAPI> moduleManager;

    public MidnightCoreImpl(Path dataFolder, Version gameVersion, ItemConverter itemConverter, PlayerManager playerManager, Function<MComponent, InventoryGUI> guiFunction, BiFunction<String, MComponent, CustomScoreboard> scoreboardFunction, BiConsumer<String, Boolean> console, Consumer<Runnable> serverSubmitter) {

        super();

        this.dataFolder = FileUtil.tryCreateDirectory(dataFolder);
        if(this.dataFolder == null) {
            throw new IllegalStateException("Unable to create data folder!");
        }

        this.config = FileConfig.findOrCreate("config", this.dataFolder, Constants.CONFIG_DEFAULTS);
        if(this.config == null) {
            throw new IllegalStateException("Unable to create config!");
        }
        this.config.getRoot().fill(Constants.CONFIG_DEFAULTS);
        this.config.save();

        this.moduleManager = new ModuleManager<>(Constants.DEFAULT_NAMESPACE);
        this.gameVersion = gameVersion;
        this.itemConverter = itemConverter;
        this.playerManager = playerManager;
        this.guiFunction = guiFunction;
        this.scoreboardFunction = scoreboardFunction;
        this.console = console;
        this.serverSubmitter = serverSubmitter;
    }

    public void loadModules() {
        ConfigSection sec = getConfig().getOrCreateSection("modules");
        moduleManager.loadAll(sec, this, Registries.MODULE_REGISTRY);
        config.save();
    }

    @Override
    public ConfigSection getConfig() {

        return config.getRoot();
    }

    @Override
    public void saveConfig() {

        config.getRoot().fill(Constants.CONFIG_DEFAULTS);
        config.save();
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public Version getGameVersion() {
        return gameVersion;
    }

    @Override
    public ModuleManager<MidnightCoreAPI> getModuleManager() {

        return moduleManager;
    }

    @Override
    public Registry<RequirementType<MPlayer>> getRequirementRegistry() {

        return Registries.REQUIREMENT_REGISTRY;
    }

    @Override
    public PlayerManager getPlayerManager() {

        return playerManager;
    }

    @Override
    public MItemStack createItem(Identifier id, int count, ConfigSection nbt) {

        return itemConverter.create(id, count, nbt);
    }

    @Override
    public InventoryGUI createGUI(MComponent title) {
        return guiFunction.apply(title);
    }

    @Override
    public CustomScoreboard createScoreboard(String id, MComponent title) {
        return scoreboardFunction.apply(id, title);
    }

    @Override
    public void executeConsoleCommand(String command, boolean log) {
        console.accept(command, log);
    }


    @Override
    public void executeOnServer(Runnable runnable) {
        serverSubmitter.accept(runnable);
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public String getServerLocale() {
        return config.getRoot().getString("locale");
    }

    @Override
    public void reload() {
        config.reload();
        ConfigSection sec = config.getRoot().getOrCreateSection("modules");
        moduleManager.reloadAll(sec, this, Registries.MODULE_REGISTRY);
    }

    @Override
    public void shutdown() {
        moduleManager.unloadAll();
    }
}
