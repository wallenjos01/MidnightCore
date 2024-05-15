package org.wallentines.mcore.extension;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mcore.VelocityProxy;
import org.wallentines.mcore.pluginmsg.ProxyPluginMessageModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class VelocityExtensionModule extends ProxyExtensionModule {

    private VelocityProxy proxy;
    @Override
    public boolean initialize(ConfigSection section, Proxy data) {

        VelocityProxy proxy = (VelocityProxy) data;
        proxy.getInternal().getEventManager().register(proxy.getPlugin(), this);

        this.proxy = proxy;

        return true;
    }

    /**
     * Fired when the player logs in. This is the earliest event when the Player object has been created, giving us
     * access to the UUID. This is propagated up to the parent to deal with finalizing player extensions
     * @param event The login event
     */
    @Subscribe
    public void onLogin(LoginEvent event) {
        onFinishLogin(proxy.getPlayer(event.getPlayer()));
    }

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<Proxy, ProxyModule>(VelocityExtensionModule::new, ID, DEFAULT_CONFIG).dependsOn(ProxyPluginMessageModule.ID);

}
