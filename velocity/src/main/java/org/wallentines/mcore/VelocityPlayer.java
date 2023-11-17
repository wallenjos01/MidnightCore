package org.wallentines.mcore;

import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mcore.util.GsonContext;

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
        // TODO: Proper conversion of components to adventure components

        JsonObject obj = ModernSerializer.INSTANCE.serialize(GsonContext.INSTANCE, ComponentResolver.resolveComponent(message, this)).getOrThrow().getAsJsonObject();
        net.kyori.adventure.text.Component cmp = GsonComponentSerializer.builder().build().deserializeFromTree(obj);
        player.sendMessage(cmp);
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

}
