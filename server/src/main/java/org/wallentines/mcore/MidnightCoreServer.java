package org.wallentines.mcore;

import org.wallentines.mcore.lang.LangContent;
import org.wallentines.mcore.lang.LangManager;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.util.FileUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.types.ResettableSingleton;
import org.wallentines.midnightlib.types.Singleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class MidnightCoreServer {

    private final LangManager langManager;
    private final boolean testCommand;

    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("register_test_command", false);

    public MidnightCoreServer(Server server, LangRegistry langDefaults, Path globalConfig) {

        ConfigSection defaultConfig = DEFAULT_CONFIG;

        Path directory = server.getConfigDirectory().resolve("MidnightCore");
        Path langDirectory = directory.resolve("lang");

        if(!server.isDedicatedServer() && globalConfig != directory && !directory.toFile().exists()) {

            FileWrapper<ConfigObject> config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", globalConfig.toFile(), DEFAULT_CONFIG);
            defaultConfig.fillOverwrite(config.getRoot().asSection());

            File langFolder = globalConfig.resolve("lang").toFile();
            if(langFolder.isDirectory()) {
                try {
                    FileUtil.copyFolder(langFolder, langDirectory);
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

        FileWrapper<ConfigObject> config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", directory.toFile(), defaultConfig);
        testCommand = config.getRoot().asSection().getBoolean("register_test_command");

        config.save();
    }


    public static void registerPlaceholders(PlaceholderManager manager) {

        Player.registerPlaceholders(manager);
        Server.registerPlaceholders(manager);
        LangContent.registerPlaceholders(manager);
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public boolean registerTestCommand() {
        return testCommand;
    }

    public static final Singleton<MidnightCoreServer> INSTANCE = new ResettableSingleton<>();

}
