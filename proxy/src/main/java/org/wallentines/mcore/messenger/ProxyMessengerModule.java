package org.wallentines.mcore.messenger;

import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class ProxyMessengerModule extends MessengerModule implements ProxyModule {


    private Proxy proxy;

    @Override
    public boolean initialize(ConfigSection config, Proxy data) {

        this.proxy = data;
        return super.init(config);
    }

    @Override
    public void disable() {
        shutdown();
    }

    public Proxy getProxy() {
        return proxy;
    }

    static {
        MessengerType.REGISTRY.register("plugin_message", new PluginMessenger.Type(ProxyPluginMessageBroker.FACTORY));
    }

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<>(ProxyMessengerModule::new, MessengerModule.ID, DEFAULT_CONFIG.with("enabled", true));
}
