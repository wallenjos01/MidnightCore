package org.wallentines.mcore.messenger;

import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class ServerMessengerModule extends MessengerModule implements ServerModule {

    private Server server;
    @Override
    public boolean initialize(ConfigSection config, Server data) {

        this.server = data;
        return super.init(config, ServerPluginMessageBroker.FACTORY);
    }

    @Override
    public void disable() {
        shutdown();
    }

    public Server getServer() {
        return server;
    }

    public static final ModuleInfo<Server, ServerModule> STARTUP_REGISTER = new ModuleInfo<>(
            ServerMessengerModule::new,
            MessengerModule.ID,
            DEFAULT_CONFIG.copy().with("broker", new ConfigSection().with("register", "startup"))
    );

    public static final ModuleInfo<Server, ServerModule> ALWAYS_REGISTER = new ModuleInfo<>(
            ServerMessengerModule::new,
            MessengerModule.ID,
            DEFAULT_CONFIG.copy().with("broker", new ConfigSection().with("register", "always"))
    );
}
