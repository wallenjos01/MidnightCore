package me.m1dnightninja.midnightcore.velocity.module;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;

import java.io.File;
import java.util.Optional;

public class LastJoinedModule implements IModule {

    private ProxyServer server;

    private File locationFile;
    private ConfigSection locations;
    private ConfigProvider prov;

    @Override
    public boolean initialize(ConfigSection configuration) {

        prov = MidnightCoreAPI.getInstance().getDefaultConfigProvider();

        locationFile = new File(MidnightCoreAPI.getInstance().getDataFolder(), "locations" + prov.getFileExtension());

        if(!locationFile.exists()) {
            prov.saveToFile(new ConfigSection(), locationFile);
        }

        locations = prov.loadFromFile(locationFile);

        server = MidnightCore.getInstance().getServer();
        server.getEventManager().register(MidnightCore.getInstance(), this);

        return true;
    }

    @Override
    public MIdentifier getId() {
        return MIdentifier.create("midnightcore","last_server_joined");
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return new ConfigSection();
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {

        String uid = event.getPlayer().getUniqueId().toString();
        if(locations.has(uid)) {

            String serverId = locations.getString(uid);
            Optional<RegisteredServer> srv = server.getServer(serverId);

            srv.ifPresent(event::setInitialServer);
        }

    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerLeave(DisconnectEvent event) {

        Optional<ServerConnection> conn = event.getPlayer().getCurrentServer();
        conn.ifPresent(serverConnection -> locations.set(event.getPlayer().getUniqueId().toString(), serverConnection.getServerInfo().getName()));

    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onShutdown(ProxyShutdownEvent event) {

        prov.saveToFile(locations, locationFile);
    }
}
