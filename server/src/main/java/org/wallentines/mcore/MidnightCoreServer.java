package org.wallentines.mcore;

import org.wallentines.mcore.lang.LangContent;
import org.wallentines.mcore.lang.LangManager;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.midnightlib.types.ResettableSingleton;
import org.wallentines.midnightlib.types.Singleton;

public class MidnightCoreServer {

    private final LangManager langManager;

    public MidnightCoreServer(Server server, LangRegistry langDefaults) {
        langManager = new LangManager(langDefaults, server.getConfigDirectory().resolve("MidnightCore").resolve("lang").toFile());
        langManager.saveLanguageDefaults("en_us", langDefaults);
    }


    public static void registerPlaceholders(PlaceholderManager manager) {

        Player.registerPlaceholders(manager);
        Server.registerPlaceholders(manager);
        LangContent.registerPlaceholders(manager);

    }

    public LangManager getLangManager() {
        return langManager;
    }

    public static final Singleton<MidnightCoreServer> INSTANCE = new ResettableSingleton<>();

}
