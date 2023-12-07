package org.wallentines.mcore;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.mcore.util.ConversionUtil;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.UUID;

public class VelocityPlayer implements ProxyPlayer {

    private final Player player;
    private final Proxy proxy;

    public VelocityPlayer(Player player, Proxy proxy) {
        this.player = player;
        this.proxy = proxy;
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public String getUsername() {
        return player.getUsername();
    }

    @Override
    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public String getLanguage() {
        Locale loc = player.getEffectiveLocale();
        if(loc == null) {
            return null;
        }
        return loc.toString().toLowerCase();
    }

    @Override
    public void sendMessage(Component message) {
        player.sendMessage(ConversionUtil.toAdventure(ComponentResolver.resolveComponent(message, this)));
    }

    @Override
    public void sendToServer(ProxyServer server) {
        RegisteredServer rs = ((VelocityServer) server).getInternal();
        player.createConnectionRequest(rs).fireAndForget();
    }

    @Override
    public ProxyServer getServer() {
        return player.getCurrentServer().map(srv -> proxy.getServer(srv.getServerInfo().getName())).orElse(null);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(String permission, int defaultOpLevel) {
        return player.hasPermission(permission);
    }

    @Override
    public String getHostname() {
        return player.getVirtualHost().map(InetSocketAddress::getHostString).orElse("");
    }

    public Player getInternal() {
        return player;
    }
}
