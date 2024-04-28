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
        return super.init(config, ProxyPluginMessageBroker.FACTORY);
    }

    @Override
    public void disable() {
        shutdown();
    }

    public Proxy getProxy() {
        return proxy;
    }

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<>(
            ProxyMessengerModule::new,
            MessengerModule.ID,
            DEFAULT_CONFIG
                    .copy()
                    .with("enabled", true)
                    .with("broker", new ConfigSection()
                            .with("persistent_registration", true)
                    )
    );
}
