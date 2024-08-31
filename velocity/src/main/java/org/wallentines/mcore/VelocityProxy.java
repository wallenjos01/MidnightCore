package org.wallentines.mcore;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.event.SingletonHandlerList;
import org.wallentines.midnightlib.module.ModuleManager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class VelocityProxy implements Proxy {

    private final ProxyServer server;
    private final MidnightCore plugin;
    private final Map<UUID, VelocityPlayer> playerCache = new HashMap<>();
    private final Map<String, VelocityServer> serverCache = new HashMap<>();

    private final ModuleManager<Proxy, ProxyModule> modules = new ModuleManager<>(ProxyModule.REGISTRY, this);
    private final HandlerList<Proxy> onShutdown = new SingletonHandlerList<>();
    private final HandlerList<ProxyPlayer> onJoin = new HandlerList<>();
    private final HandlerList<Connection> onConnect = new HandlerList<>();
    private final HandlerList<ProxyPlayer> onLeave = new HandlerList<>();

    public VelocityProxy(MidnightCore plugin, ProxyServer server) {
        this.server = server;
        this.plugin = plugin;

        server.getEventManager().register(plugin, this);
    }

    @Override
    public Path getConfigDirectory() {
        return MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get();
    }

    @Override
    public ModuleManager<Proxy, ProxyModule> getModuleManager() {
        return modules;
    }

    @Override
    public HandlerList<Proxy> shutdownEvent() {
        return onShutdown;
    }

    @Override
    public VelocityPlayer getPlayer(UUID uuid) {
        return playerCache.computeIfAbsent(uuid, (k) -> server.getPlayer(uuid).map(player -> new VelocityPlayer(player, this)).orElse(null));
    }

    public VelocityPlayer getPlayer(Player player) {
        return playerCache.computeIfAbsent(player.getUniqueId(), k -> new VelocityPlayer(player, this));
    }

    @Override
    public Stream<ProxyPlayer> getPlayers() {
        return server.getAllPlayers().stream().map(this::getPlayer);
    }

    @Override
    public int getPlayerCount() {
        return server.getPlayerCount();
    }

    @Override
    public VelocityServer getServer(String name) {
        return serverCache.computeIfAbsent(name, (k) -> server.getServer(name).map(server -> new VelocityServer(server, this)).orElse(null));
    }

    public VelocityServer getServer(RegisteredServer server) {
        return serverCache.computeIfAbsent(server.getServerInfo().getName(), k -> new VelocityServer(server, this));
    }

    @Override
    public HandlerList<ProxyPlayer> joinEvent() {
        return onJoin;
    }

    @Override
    public HandlerList<Connection> connectEvent() {
        return onConnect;
    }

    @Override
    public HandlerList<ProxyPlayer> leaveEvent() {
        return onLeave;
    }

    public ProxyServer getInternal() {
        return server;
    }

    public MidnightCore getPlugin() {
        return plugin;
    }

    /**
     * Fired when the proxy shuts down. Propagates the event to the Proxy interface
     * @param ignoredEvent The fired event.
     */
    @Subscribe
    public void onShutdown(ProxyShutdownEvent ignoredEvent) {
        onShutdown.invoke(this);
    }

    @Subscribe
    public void onLeave(DisconnectEvent event) {
        onLeave.invoke(getPlayer(event.getPlayer().getUniqueId()));
    }

    @Subscribe
    public void onConnected(ServerConnectedEvent event) {

        ProxyPlayer pl = getPlayer(event.getPlayer());
        if(event.getPreviousServer().isEmpty()) {
            onJoin.invoke(pl);
        }

        org.wallentines.mcore.ProxyServer srv = getServer(event.getServer());
        Connection conn = new Connection(
                pl,
                srv,
                event.getPreviousServer().map(this::getServer).orElse(null));

        onConnect.invoke(conn);
        srv.connectEvent().invoke(pl);

    }

}
