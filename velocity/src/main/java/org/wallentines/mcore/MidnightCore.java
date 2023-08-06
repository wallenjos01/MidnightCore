package org.wallentines.mcore;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.wallentines.mcore.extension.VelocityExtensionModule;
import org.wallentines.mcore.messaging.VelocityMessagingModule;

@Plugin(id=MidnightCoreAPI.MOD_ID, name="MidnightCore", version="0.1.0-SNAPSHOT")
public class MidnightCore {

    @Inject
    public MidnightCore(ProxyServer server) {

        VelocityProxy proxy = new VelocityProxy(this, server);
        Proxy.RUNNING_PROXY.set(proxy);

        // Register default modules.
        ProxyModule.tryRegister(VelocityMessagingModule.ID, VelocityMessagingModule.MODULE_INFO);
        ProxyModule.tryRegister(VelocityExtensionModule.ID, VelocityExtensionModule.MODULE_INFO);

    }

    @Subscribe
    private void onInit(ProxyInitializeEvent ev) {

        Proxy.RUNNING_PROXY.get().loadModules(ProxyModule.REGISTRY);
    }

}
