package org.wallentines.mcore;

import com.google.common.eventbus.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.event.SingletonHandlerList;
import org.wallentines.midnightlib.module.ModuleManager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VelocityProxy implements Proxy {

    private final ProxyServer server;
    private final MidnightCore plugin;
    private final Map<UUID, VelocityPlayer> playerCache = new HashMap<>();
    private final Map<String, VelocityServer> serverCache = new HashMap<>();

    private final ModuleManager<Proxy, ProxyModule> modules = new ModuleManager<>();
    private final HandlerList<Proxy> onShutdown = new SingletonHandlerList<>();

    public VelocityProxy(MidnightCore plugin, ProxyServer server) {
        this.server = server;
        this.plugin = plugin;

        server.getEventManager().register(plugin, this);
    }

    @Override
    public Path getConfigDirectory() {
        return Path.of("plugins");
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
        return playerCache.compute(uuid, (k,v) -> server.getPlayer(uuid).map(player -> v == null ? new VelocityPlayer(player) : v).orElse(null));
    }

    @Override
    public VelocityServer getServer(String name) {
        return serverCache.compute(name, (k,v) -> server.getServer(name).map(server -> v == null ? new VelocityServer(server) : v).orElse(null));
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

}
