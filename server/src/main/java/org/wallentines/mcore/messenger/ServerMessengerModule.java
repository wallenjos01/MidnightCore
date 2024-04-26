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
        return super.init(config);
    }

    @Override
    public void disable() {
        shutdown();
    }

    public Server getServer() {
        return server;
    }

    static {
        MessengerType.REGISTRY.register("plugin_message", new PluginMessenger.Type(ServerPluginMessageBroker.FACTORY));
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(ServerMessengerModule::new, MessengerModule.ID, new ConfigSection());
}
