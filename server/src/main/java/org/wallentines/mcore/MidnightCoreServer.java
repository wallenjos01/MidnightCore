package org.wallentines.mcore;

import org.wallentines.mcore.lang.LangContent;
import org.wallentines.mcore.lang.LangManager;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.math.Region;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.requirement.RequirementType;
import org.wallentines.midnightlib.types.ResettableSingleton;
import org.wallentines.midnightlib.types.Singleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MidnightCoreServer {

    private final LangManager langManager;
    private final boolean testCommand;

    private final FileWrapper<ConfigObject> config;

    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("register_test_command", false);

    public MidnightCoreServer(Server server, LangRegistry langDefaults) {

        ConfigSection defaultConfig = DEFAULT_CONFIG;

        Path directory = server.getConfigDirectory().resolve("MidnightCore");
        Path langDirectory = directory.resolve("lang");
        Path globalConfig = MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get().resolve("MidnightCore");

        if(!server.isDedicatedServer() && globalConfig != directory && !directory.toFile().exists()) {

            FileWrapper<ConfigObject> config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", globalConfig.toFile(), DEFAULT_CONFIG);
            defaultConfig.fillOverwrite(config.getRoot().asSection());

            File langFolder = globalConfig.resolve("lang").toFile();
            if(langFolder.isDirectory()) {
                try {
                    Files.copy(langFolder.toPath(), langDirectory);
                } catch (IOException ex) {
                    MidnightCoreAPI.LOGGER.warn("Unable to copy lang defaults to world!");
                }
            }

        }

        File configDir = directory.toFile();
        if(!configDir.isDirectory() && !configDir.mkdirs()) {
            throw new IllegalStateException("Unable to create config directory!");
        }

        langManager = new LangManager(langDefaults, langDirectory.toFile());
        langManager.saveLanguageDefaults("en_us", langDefaults);

        this.config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", directory.toFile(), defaultConfig);
        testCommand = config.getRoot().asSection().getBoolean("register_test_command");

        config.save();
    }


    public static void registerPlaceholders(PlaceholderManager manager) {

        Player.registerPlaceholders(manager);
        Server.registerPlaceholders(manager);
        Entity.registerPlaceholders(manager);
        LangContent.registerPlaceholders(manager);
    }

    static void registerRequirements(Registry<RequirementType<Player>> registry) {

        registry.register(new Identifier(MidnightCoreAPI.MOD_ID, "cooldown"), new CooldownRequirement<>());
        registry.register(new Identifier(MidnightCoreAPI.MOD_ID, "permission"), (pl,obj,req) -> pl.hasPermission(obj.asString()));
        registry.register(new Identifier(MidnightCoreAPI.MOD_ID, "world"), (pl,obj,req) -> pl.getLocation().dimension.equals(Identifier.parseOrDefault(obj.asString(), "minecraft")));
        registry.register(new Identifier(MidnightCoreAPI.MOD_ID, "region"), (pl,obj,req) -> Region.parse(obj.asString()).isWithin(pl.getPosition()));
        registry.register(new Identifier(MidnightCoreAPI.MOD_ID, "locale"), (pl,obj,req) -> obj.asString().contains("_") ? pl.getLanguage().equals(obj.asString()) : pl.getLanguage().startsWith(obj.asString()));

    }

    public LangManager getLangManager() {
        return langManager;
    }

    public boolean registerTestCommand() {
        return testCommand;
    }

    public ConfigSection getConfig() {
        return config.getRoot().asSection();
    }


    public static final Singleton<MidnightCoreServer> INSTANCE = new ResettableSingleton<>();

}
