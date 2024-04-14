package org.wallentines.mcore;

import org.wallentines.mcore.lang.*;
import org.wallentines.mcore.requirement.CooldownRequirement;
import org.wallentines.mcore.requirement.PlayerCheck;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.math.Region;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.requirement.CheckType;
import org.wallentines.midnightlib.requirement.StringCheck;
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
    private final File dataDirectory;

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

        dataDirectory = directory.toFile();
        if(!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
            throw new IllegalStateException("Unable to create config directory!");
        }

        langManager = new LangManager(langDefaults, langDirectory.toFile());
        langManager.saveLanguageDefaults("en_us", langDefaults);

        this.config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", dataDirectory, defaultConfig);
        testCommand = config.getRoot().asSection().getBoolean("register_test_command");

        config.save();
    }


    public static void registerPlaceholders(PlaceholderManager manager) {

        Player.registerPlaceholders(manager);
        Server.registerPlaceholders(manager);
        Entity.registerPlaceholders(manager);
        LangContent.registerPlaceholders(manager);

        manager.registerSupplier("mcore_config_dir", PlaceholderSupplier.inline(ctx -> INSTANCE.get().dataDirectory.toString()));

    }

    static void registerRequirements(Registry<CheckType<Player>> registry) {

        registry.register("cooldown", CooldownRequirement.type());
        registry.register("permission", PlayerCheck.create(Serializer.STRING, "value", Player::hasPermission));
        registry.register("world", PlayerCheck.create(Identifier.serializer("minecraft"), "value", (pl, id) -> pl.getDimensionId().equals(id)));
        registry.register("region", PlayerCheck.create(Region.SERIALIZER, "value", (pl, reg) -> reg.isWithin(pl.getPosition())));
        registry.register("locale", PlayerCheck.create(Serializer.STRING, "value", (pl, str) -> str.contains("_") ? pl.getLanguage().equals(str) : pl.getLanguage().startsWith(str)));
        registry.register("username", StringCheck.type(Player::getUsername));
        registry.register("uuid", StringCheck.type(pl -> pl.getUUID().toString()));
        registry.register("game_mode", StringCheck.type(pl -> pl.getGameMode().getId()));

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
