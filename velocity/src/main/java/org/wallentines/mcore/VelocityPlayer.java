package org.wallentines.mcore;

import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mcore.util.GsonContext;

import java.util.UUID;

public class VelocityPlayer implements ProxyPlayer {

    private final Player player;

    public VelocityPlayer(Player player) {
        this.player = player;
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
    public void sendMessage(Component message) {
        // TODO: Proper conversion of components to adventure components

        JsonObject obj = ModernSerializer.INSTANCE.serialize(GsonContext.INSTANCE, message).getOrThrow().getAsJsonObject();
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
        return null;
    }
}
