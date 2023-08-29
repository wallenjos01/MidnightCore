package org.wallentines.mcore;

import org.wallentines.mcore.lang.LangContent;
import org.wallentines.mcore.lang.LangManager;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.types.ResettableSingleton;
import org.wallentines.midnightlib.types.Singleton;

public class MidnightCoreServer {

    private final LangManager langManager;
    private final boolean testCommand;

    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("register_test_command", false);

    public MidnightCoreServer(Server server, LangRegistry langDefaults) {
        langManager = new LangManager(langDefaults, server.getConfigDirectory().resolve("MidnightCore").resolve("lang").toFile());
        langManager.saveLanguageDefaults("en_us", langDefaults);

        FileWrapper<ConfigObject> config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", server.getConfigDirectory().resolve("MidnightCore").toFile(), DEFAULT_CONFIG);
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
