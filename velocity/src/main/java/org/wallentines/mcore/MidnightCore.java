package org.wallentines.mcore;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.wallentines.mcore.extension.VelocityExtensionModule;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.pluginmsg.VelocityPluginMessageModule;
import org.wallentines.mcore.sql.VelocitySQLModule;
import org.wallentines.mdcfg.codec.BinaryCodec;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.nio.file.Path;

@Plugin(id=MidnightCoreAPI.MOD_ID, name="MidnightCore", version="2.0.0-SNAPSHOT")
public class MidnightCore {

    private final ProxyServer server;

    @Inject
    public MidnightCore(ProxyServer server) {

        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(JSONCodec.fileCodec());
        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(BinaryCodec.fileCodec());

        MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.set(Path.of("plugins"));

        this.server = server;

        // Register default modules.
        ProxyModule.tryRegister(VelocityPluginMessageModule.ID, VelocityPluginMessageModule.MODULE_INFO);
        ProxyModule.tryRegister(VelocityExtensionModule.ID, VelocityExtensionModule.MODULE_INFO);
        ProxyModule.tryRegister(VelocitySQLModule.ID, VelocitySQLModule.MODULE_INFO);

        ProxyPlayer.registerPlaceholders(PlaceholderManager.INSTANCE);
    }

    @Subscribe
    private void onInit(ProxyInitializeEvent ev) {

        VelocityProxy proxy = new VelocityProxy(this, server);
        proxy.loadModules(ProxyModule.REGISTRY);
        Proxy.RUNNING_PROXY.set(proxy);

    }

}
