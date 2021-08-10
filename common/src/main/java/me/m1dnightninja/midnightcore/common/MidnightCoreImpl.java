package me.m1dnightninja.midnightcore.common;

import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.inventory.ItemConverter;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.math.Vec3i;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.module.IModuleRegistry;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayerManager;
import me.m1dnightninja.midnightcore.api.player.Requirement;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.common.module.ModuleRegistry;

import java.io.File;
import java.util.*;

public abstract class MidnightCoreImpl extends me.m1dnightninja.midnightcore.api.MidnightCoreAPI {

    private final Random random;
    private final ModuleRegistry moduleRegistry;

    private final ConfigRegistry configRegistry;
    private final ConfigProvider defaultConfigProvider;
    private final FileConfig mainConfig;

    private final MPlayerManager playerManager;
    private final ItemConverter itemConverter;


    private final File dataFolder;

    public MidnightCoreImpl(ConfigRegistry registry, MPlayerManager playerManager, ItemConverter converter, ConfigProvider def, File dataFolder, IModule... modules) {

        // Register variables
        this.random = new Random();

        this.configRegistry = registry;
        this.defaultConfigProvider = def;

        this.playerManager = playerManager;
        this.itemConverter = converter;

        this.dataFolder = dataFolder;

        // Config Provider defaults
        configRegistry.registerProvider(defaultConfigProvider);

        configRegistry.registerSerializer(Skin.class, Skin.SERIALIZER);
        configRegistry.registerSerializer(MItemStack.class, MItemStack.SERIALIZER);
        configRegistry.registerSerializer(Requirement.class, Requirement.SERIALIZER);

        configRegistry.registerInlineSerializer(MIdentifier.class, MIdentifier.SERIALIZER);
        configRegistry.registerInlineSerializer(Vec3d.class, Vec3d.SERIALIZER);
        configRegistry.registerInlineSerializer(Vec3i.class, Vec3i.SERIALIZER);
        configRegistry.registerInlineSerializer(UUID.class, Skin.UID_SERIALIZER);

        // Load config
        File configFile = new File(dataFolder, "config" + def.getFileExtension());
        if(!configFile.exists()) {
            def.saveToFile(new ConfigSection(), configFile);
        }

        this.mainConfig = new FileConfig(configFile, defaultConfigProvider);

        // Load module configs from
        ConfigSection moduleConfig = null;
        if(mainConfig.getRoot().has("modules", ConfigSection.class)) {
            moduleConfig = mainConfig.getRoot().getSection("modules");
        }

        if(moduleConfig == null) {
            moduleConfig = new ConfigSection();
            mainConfig.getRoot().set("modules", moduleConfig);
            mainConfig.getRoot().set("language", "en_us");
        }

        this.moduleRegistry = new ModuleRegistry(moduleConfig);
        moduleRegistry.loadAll(modules);

        // Log a list of loaded modules
        Collection<MIdentifier> moduleIds = moduleRegistry.getLoadedModuleIds();

        LOGGER.info("Loaded " + moduleIds.size() + " " + (moduleIds.size() == 1 ? "Module!" : "Modules!"));
        for(MIdentifier id : moduleIds) {
            LOGGER.info(" - " + id.toString());
        }

    }


    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public <T extends IModule> T getModule(Class<T> clazz) {
        return moduleRegistry.getModule(clazz);
    }

    @Override
    public ConfigSection getMainConfig() {
        return mainConfig.getRoot();
    }

    @Override
    public ConfigProvider getDefaultConfigProvider() {
        return defaultConfigProvider;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public IModule getModuleById(MIdentifier id) {
        return moduleRegistry.getModuleById(id);
    }

    @Override
    public boolean isModuleLoaded(MIdentifier id) {
        return moduleRegistry.isModuleLoaded(id);
    }

    @Override
    public IModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    @Override
    public MPlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public ItemConverter getItemConverter() {
        return itemConverter;
    }

    @Override
    public ConfigRegistry getConfigRegistry() {
        return configRegistry;
    }

}
