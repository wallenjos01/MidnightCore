package org.wallentines.mcore.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.ProxyPlayer;
import org.wallentines.mcore.ProxyServer;
import org.wallentines.midnightlib.registry.Identifier;

public class ConversionUtil {

    public static Identifier toIdentifier(ChannelIdentifier id) {

        if((id instanceof MinecraftChannelIdentifier mci)) {
            return new Identifier(mci.getNamespace(), mci.getId());
        }

        return Identifier.parseOrDefault(id.getId(), MidnightCoreAPI.MOD_ID);
    }

    public static ChannelIdentifier toChannelIdentifier(Identifier id) {

        return MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());
    }


    public static Player validate(ProxyPlayer player) {

        if(!(player instanceof Player)) {
            throw new IllegalArgumentException("Attempt to access a non-Velocity player!");
        }

        return (Player) player;
    }


    public static RegisteredServer validate(ProxyServer server) {

        if(!(server instanceof RegisteredServer)) {
            throw new IllegalArgumentException("Attempt to access a non-Velocity server!");
        }

        return (RegisteredServer) server;
    }

}
