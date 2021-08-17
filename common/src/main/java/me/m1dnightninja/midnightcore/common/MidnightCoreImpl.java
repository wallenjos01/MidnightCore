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
import me.m1dnightninja.midnightcore.api.player.Location;
import me.m1dnightninja.midnightcore.api.player.MPlayerManager;
import me.m1dnightninja.midnightcore.api.player.Requirement;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.module.ModuleRegistry;

import java.io.File;
import java.util.*;

public abstract class MidnightCoreImpl extends me.m1dnightninja.midnightcore.api.MidnightCoreAPI {

    private final Random random;
    private final ModuleRegistry moduleRegistry;

    private final ConfigRegistry configRegistry;
    private final FileConfig mainConfig;

    private final MPlayerManager playerManager;
    private final ItemConverter itemConverter;

    private final File dataFolder;

    public MidnightCoreImpl(MPlayerManager playerManager, ItemConverter converter, File dataFolder, IModule... modules) {

        // Register variables
        this.random = new Random();

        this.configRegistry = ConfigRegistry.INSTANCE;

        this.playerManager = playerManager;
        this.itemConverter = converter;

        this.dataFolder = dataFolder;

        // Config Provider defaults
        configRegistry.registerSerializer(Skin.class, Skin.SERIALIZER);
        configRegistry.registerSerializer(MItemStack.class, MItemStack.SERIALIZER);
        configRegistry.registerSerializer(Requirement.class, Requirement.SERIALIZER);

        configRegistry.registerInlineSerializer(MIdentifier.class, MIdentifier.SERIALIZER);
        configRegistry.registerInlineSerializer(MComponent.class, MComponent.Serializer.SERIALIZER);
        configRegistry.registerInlineSerializer(Vec3d.class, Vec3d.SERIALIZER);
        configRegistry.registerInlineSerializer(Vec3i.class, Vec3i.SERIALIZER);
        configRegistry.registerInlineSerializer(Location.class, Location.SERIALIZER);
        configRegistry.registerInlineSerializer(UUID.class, Skin.UID_SERIALIZER);

        // Load config
        this.mainConfig = FileConfig.findOrCreate("config", dataFolder);
        if(mainConfig == null) {
            throw new IllegalStateException("Unable to initialize MidnightCore! No ConfigProviders exist!");
        }

        // Default Config
        ConfigSection defaultConfig = new ConfigSection();
        defaultConfig.set("language", "en_us");
        defaultConfig.set("disabled_modules", new ArrayList<>());
        defaultConfig.set("modules", new ConfigSection());

        mainConfig.getRoot().fill(defaultConfig);

        this.moduleRegistry = new ModuleRegistry(mainConfig.getRoot().getSection("modules"), mainConfig.getRoot().getListFiltered("disabled_modules", MIdentifier.class));
        moduleRegistry.loadAll(modules);

        mainConfig.save();

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
        return ConfigRegistry.INSTANCE.getDefaultProvider();
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
