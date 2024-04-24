package org.wallentines.mcore.messenger;

import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mdcfg.ConfigSection;

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
        MessengerType.REGISTRY.register("plugin_message", ProxyPluginMessenger.Type.INSTANCE);
    }
}
