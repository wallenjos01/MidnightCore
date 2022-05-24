package org.wallentines.midnightcore.velocity.module.lastserver;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Optional;

public class LastServerModule implements Module<MidnightCoreAPI> {

    private ProxyServer server;

    private FileConfig locations;

    @Override
    public boolean initialize(ConfigSection configuration, MidnightCoreAPI api) {

        locations = FileConfig.findOrCreate("locations", MidnightCoreAPI.getInstance().getDataFolder());

        server = MidnightCore.getInstance().getServer();
        server.getEventManager().register(MidnightCore.getInstance(), this);

        return true;
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {

        String uid = event.getPlayer().getUniqueId().toString();
        if(locations.getRoot().has(uid)) {

            String serverId = locations.getRoot().getString(uid);
            Optional<RegisteredServer> srv = server.getServer(serverId);

            srv.ifPresent(event::setInitialServer);
        }

    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerLeave(DisconnectEvent event) {

        Optional<ServerConnection> conn = event.getPlayer().getCurrentServer();
        conn.ifPresent(serverConnection -> locations.getRoot().set(event.getPlayer().getUniqueId().toString(), serverConnection.getServerInfo().getName()));

    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onShutdown(ProxyShutdownEvent event) {

        locations.save();
    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "last_server");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(LastServerModule::new, ID, new ConfigSection());

}
