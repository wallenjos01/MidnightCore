package org.wallentines.midnightcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.MidnightCoreImpl;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.velocity.event.MidnightCoreInitializeEvent;
import org.wallentines.midnightcore.velocity.item.DummyItem;
import org.wallentines.midnightcore.velocity.module.extension.VelocityExtensionModule;
import org.wallentines.midnightcore.velocity.module.globaljoin.GlobalJoinModule;
import org.wallentines.midnightcore.velocity.module.lastserver.LastServerModule;
import org.wallentines.midnightcore.velocity.module.messaging.VelocityMessagingModule;
import org.wallentines.midnightcore.velocity.player.VelocityPlayerManager;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;

import java.nio.file.Path;

@Plugin(id = "midnightcore", name = "MidnightCore", version = "1.0.0", authors = {"M1dnight_Ninja"})
public class MidnightCore {

    private static MidnightCore INSTANCE;

    private final ProxyServer server;
    private final Path dataFolder;

    @Inject
    public MidnightCore(ProxyServer server, @DataDirectory Path dataFolder) {

        INSTANCE = this;

        this.server = server;
        this.dataFolder = dataFolder;

    }

    @Subscribe(order= PostOrder.FIRST)
    public void onInitialize(ProxyInitializeEvent event) {

        Constants.registerDefaults(JsonConfigProvider.INSTANCE);

        VelocityPlayerManager playerManager = new VelocityPlayerManager();
        MidnightCoreImpl api = new MidnightCoreImpl(
                dataFolder,
                Version.SERIALIZER.deserialize("1.19.2"),
                DummyItem::new,
                playerManager,
                title -> null,
                (id,title) -> null,
                (str, b) -> server.getCommandManager().executeAsync(server.getConsoleCommandSource(), str),
                run -> server.getScheduler().buildTask(this, run).schedule());

        Registries.MODULE_REGISTRY.register(VelocityMessagingModule.ID, VelocityMessagingModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(LastServerModule.ID, LastServerModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(GlobalJoinModule.ID, GlobalJoinModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(VelocityExtensionModule.ID, VelocityExtensionModule.MODULE_INFO);

        playerManager.register();
        api.loadModules();

        Event.invoke(new MidnightCoreInitializeEvent(this));
    }

    public static MidnightCore getInstance() {
        return INSTANCE;
    }

    public ProxyServer getServer() {
        return server;
    }
}
