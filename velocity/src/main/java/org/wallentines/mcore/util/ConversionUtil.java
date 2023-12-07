package org.wallentines.mcore.util;

import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.wallentines.mcore.*;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.midnightlib.registry.Identifier;

/**
 * A utility for converting various MidnightCore types to Velocity types and vice-versa
 */
public class ConversionUtil {

    /**
     * Converts a velocity ChannelIdentifier into an Identifier
     * @param id The ChannelIdentifier to convert
     * @return A converted Identifier
     */
    public static Identifier toIdentifier(ChannelIdentifier id) {

        if((id instanceof MinecraftChannelIdentifier mci)) {
            return new Identifier(mci.getNamespace(), mci.getName());
        }

        return Identifier.parseOrDefault(id.getId(), MidnightCoreAPI.MOD_ID);
    }

    /**
     * Converts an Identifier into a velocity ChannelIdentifier
     * @param id The Identifier to convert
     * @return A converted ChannelIdentifier
     */
    public static ChannelIdentifier toChannelIdentifier(Identifier id) {

        return MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());
    }


    public static net.kyori.adventure.text.Component toAdventure(Component component) {

        JsonObject obj = ModernSerializer.INSTANCE.serialize(GsonContext.INSTANCE, component).getOrThrow().getAsJsonObject();
        return GsonComponentSerializer.builder().build().deserializeFromTree(obj);
    }

    /**
     * Ensures the given ProxyPlayer is actually a velocity Player
     * @param player The ProxyPlayer to validate
     * @return The ProxyPlayer casted to a Player
     */
    public static Player validate(ProxyPlayer player) {

        if(!(player instanceof VelocityPlayer vpl)) {
            throw new IllegalArgumentException("Attempt to access a non-Velocity player!");
        }

        return vpl.getInternal();
    }


    /**
     * Ensures the given ProxyServer is actually a velocity RegisteredServer
     * @param server The ProxyServer to validate
     * @return The ProxyServer casted to a RegisteredServer
     */
    public static RegisteredServer validate(ProxyServer server) {

        if(!(server instanceof VelocityServer vs)) {
            throw new IllegalArgumentException("Attempt to access a non-Velocity server!");
        }

        return vs.getInternal();
    }

}
