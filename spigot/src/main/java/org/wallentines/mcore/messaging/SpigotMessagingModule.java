package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.*;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class SpigotMessagingModule extends ServerMessagingModule implements PluginMessageListener {


    @Override
    public boolean initialize(ConfigSection section, Server data) {
        return true;
    }

    @Override
    public void disable() {
        super.disable();

        Plugin plugin = MidnightCore.getPlugin(MidnightCore.class);
        for(String s : Bukkit.getMessenger().getOutgoingChannels(plugin)) {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, s);
        }

    }

    @Override
    public void sendPacket(Player player, Identifier packetId, ByteBuf data) {

        Plugin plugin = MidnightCore.getPlugin(MidnightCore.class);
        String channelId = packetId.toString();

        if(!Bukkit.getMessenger().isOutgoingChannelRegistered(plugin, channelId)) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channelId);
        }

        SpigotPlayer spl = ConversionUtil.validate(player);
        spl.getInternal().sendPluginMessage(plugin, channelId, data.array());
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

        Bukkit.getMessenger().registerIncomingPluginChannel(MidnightCore.getPlugin(MidnightCore.class), packetId.toString(), this);
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
        Bukkit.getMessenger().unregisterIncomingPluginChannel(MidnightCore.getPlugin(MidnightCore.class), packetId.toString());
    }

    @Override
    protected void doUnregisterLogin(Identifier packetId) {

    }

    @Override
    protected void doUnregisterConfig(Identifier packetId) {

    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull org.bukkit.entity.Player player, @NotNull byte[] data) {

        SpigotPlayer spl = new SpigotPlayer(Server.RUNNING_SERVER.get(), player);
        handlePacket(spl, Identifier.parseOrDefault(channel, MidnightCoreAPI.MOD_ID), Unpooled.wrappedBuffer(data));

    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(SpigotMessagingModule::new, ID, new ConfigSection());


}
