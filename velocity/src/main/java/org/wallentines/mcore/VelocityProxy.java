package org.wallentines.mcore;

import com.google.common.eventbus.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.event.SingletonHandlerList;
import org.wallentines.midnightlib.module.ModuleManager;

import java.nio.file.Path;
import java.util.*;

public class VelocityProxy implements Proxy {

    private final ProxyServer server;
    private final MidnightCore plugin;
    private final Map<UUID, VelocityPlayer> playerCache = new HashMap<>();
    private final Map<String, VelocityServer> serverCache = new HashMap<>();

    private final ModuleManager<Proxy, ProxyModule> modules = new ModuleManager<>();
    private final HandlerList<Proxy> onShutdown = new SingletonHandlerList<>();
    private final HandlerList<ProxyPlayer> onJoin = new HandlerList<>();
    private final HandlerList<ProxyPlayer> onLeave = new HandlerList<>();
    private final HandlerList<Transfer> onTransfer = new HandlerList<>();

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

    private VelocityPlayer getPlayer(Player player) {
        return playerCache.computeIfAbsent(player.getUniqueId(), k -> new VelocityPlayer(player, this));
    }

    @Override
    public Collection<ProxyPlayer> getPlayers() {

        List<ProxyPlayer> players = new ArrayList<>(server.getPlayerCount());
        for(Player player : server.getAllPlayers()) {
            getPlayer(player.getUniqueId());
        }

        return players;
    }

    @Override
    public VelocityServer getServer(String name) {
        return serverCache.computeIfAbsent(name, (k) -> server.getServer(name).map(server -> new VelocityServer(server, this)).orElse(null));
    }

    private VelocityServer getServer(RegisteredServer server) {
        return serverCache.computeIfAbsent(server.getServerInfo().getName(), k -> new VelocityServer(server, this));
    }

    @Override
    public HandlerList<ProxyPlayer> joinEvent() {
        return onJoin;
    }

    @Override
    public HandlerList<ProxyPlayer> leaveEvent() {
        return onLeave;
    }

    @Override
    public HandlerList<Transfer> transferEvent() {
        return onTransfer;
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
    private void onShutdown(ProxyShutdownEvent ignoredEvent) {
        onShutdown.invoke(this);
    }

    @Subscribe
    private void onLeave(DisconnectEvent event) {
        onLeave.invoke(getPlayer(event.getPlayer().getUniqueId()));
    }

    @Subscribe
    private void onConnected(ServerConnectedEvent event) {
        ProxyPlayer pl = getPlayer(event.getPlayer());
        event.getPreviousServer().ifPresentOrElse(srv -> {
            onTransfer.invoke(new Transfer(
                    pl,
                    getServer(srv)));
        }, () -> {
            onJoin.invoke(pl);
        });

        getServer(event.getServer()).connectEvent().invoke(pl);
    }

}
