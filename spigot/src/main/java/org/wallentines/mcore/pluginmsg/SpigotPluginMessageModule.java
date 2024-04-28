package org.wallentines.mcore.pluginmsg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.*;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class SpigotPluginMessageModule extends ServerPluginMessageModule implements PluginMessageListener {


    private Plugin plugin;
    private final ChannelReflector reflector = new ChannelReflector();

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        plugin = MidnightCore.getPlugin(MidnightCore.class);

        return true;
    }

    @Override
    public void disable() {
        super.disable();
        if(!plugin.isEnabled()) return;

        for(String s : Bukkit.getMessenger().getOutgoingChannels(plugin)) {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, s);
        }
    }

    @Override
    public void sendPacket(Player player, Identifier packetId, ByteBuf data) {

        if(!plugin.isEnabled()) return;
        SpigotPlayer spl = ConversionUtil.validate(player);

        reflector.init(spl.getInternal().getClass());

        String channelId = packetId.toString();

        if(!Bukkit.getMessenger().isOutgoingChannelRegistered(plugin, channelId)) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channelId);
        }

        reflector.ensureChannel(spl.getInternal(), packetId.toString());
        spl.getInternal().sendPluginMessage(plugin, channelId, PacketBufferUtil.getBytes(data));
    }

    @Override
    protected void sendPacket(ConfiguringPlayer player, Identifier packetId, ByteBuf data) {
        throw new IllegalStateException("Sending config messages is not available on Spigot!");
    }


    @Override
    public boolean supportsLoginQuery() {
        return false;
    }

    @Override
    public boolean supportsConfigMessaging() {
        return false;
    }

    @Override
    protected void doRegister(Identifier packetId) {

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, packetId.toString(), this);
        MidnightCoreAPI.LOGGER.warn("Registered channel " + packetId);
    }

    @Override
    protected void doRegisterLogin(Identifier packetId) {
        throw new IllegalStateException("Registering login messages is not available on Spigot!");
    }

    @Override
    protected void doRegisterConfig(Identifier packetId) {
        throw new IllegalStateException("Registering config messages is not available on Spigot!");
    }

    @Override
    protected void doUnregister(Identifier packetId) {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, packetId.toString());
    }

    @Override
    protected void doUnregisterLogin(Identifier packetId) {

    }

    @Override
    protected void doUnregisterConfig(Identifier packetId) {

    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull org.bukkit.entity.Player player, byte @NotNull [] data) {

        MidnightCoreAPI.LOGGER.warn("Received a plugin message in channel " + channel);
        SpigotPlayer spl = new SpigotPlayer(Server.RUNNING_SERVER.get(), player);
        handlePacket(spl, Identifier.parseOrDefault(channel, MidnightCoreAPI.MOD_ID), Unpooled.wrappedBuffer(data));

    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(SpigotPluginMessageModule::new, ID, new ConfigSection());

    private static class ChannelReflector {
        Method getChannels;
        Method addChannel;
        boolean initialized = false;

        public void init(Class<?> clazz) {
            if(initialized) return;
            initialized = true;
            try {
                this.getChannels = clazz.getMethod("getListeningPluginChannels");
                this.addChannel = clazz.getMethod("addChannel", String.class);
            } catch (NoSuchMethodException ex) {
                MidnightCoreAPI.LOGGER.warn("Failed to find plugin message channel methods!", ex);
            }
        }
        @SuppressWarnings("unchecked")
        public void ensureChannel(org.bukkit.entity.Player player, String channel) {

            if(addChannel == null) return;
            try {
                Set<String> channels = (Set<String>) getChannels.invoke(player);
                if(!channels.contains(channel)) addChannel.invoke(player, channel);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                MidnightCoreAPI.LOGGER.warn("Failed to add plugin message channel for player!", ex);
                // Ignore
            }
        }

    }


}
