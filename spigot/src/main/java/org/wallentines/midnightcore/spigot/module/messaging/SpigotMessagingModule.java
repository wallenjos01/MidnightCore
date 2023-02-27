package org.wallentines.midnightcore.spigot.module.messaging;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.messaging.LoginNegotiator;
import org.wallentines.midnightcore.api.module.messaging.MessageHandler;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.spigot.MidnightCore;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SpigotMessagingModule extends AbstractMessagingModule implements PluginMessageListener {

    private final Set<Identifier> registeredOutChannels = new HashSet<>();
    private final Set<Identifier> registeredInChannels = new HashSet<>();

    @Override
    public void disable() {
        super.disable();
        Plugin pl = MidnightCore.getInstance();
        for(Identifier id : registeredInChannels) {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(pl, id.toString(), this);
        }
        for(Identifier id : registeredOutChannels) {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(pl, id.toString());
        }
    }

    @Override
    public void registerHandler(Identifier id, MessageHandler handler) {
        super.registerHandler(id, handler);
        if(registeredInChannels.contains(id)) {
            registeredInChannels.add(id);
            Bukkit.getMessenger().registerIncomingPluginChannel(MidnightCore.getInstance(), id.toString(), this);
        }
    }

    @Override
    public void sendRawMessage(MPlayer player, Identifier id, byte[] data) {

        if(!registeredOutChannels.contains(id)) {
            registeredOutChannels.add(id);
            Bukkit.getMessenger().registerOutgoingPluginChannel(MidnightCore.getInstance(), id.toString());
        }

        ((SpigotPlayer) player).getInternal().sendPluginMessage(MidnightCore.getInstance(), id.toString(), data);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String s, @NotNull Player player, byte @NotNull [] bytes) {

        Identifier id = Identifier.parseOrDefault(s, MidnightCoreAPI.DEFAULT_NAMESPACE);
        handle(SpigotPlayer.wrap(player), id, bytes);
    }

    @Override
    public void addLoginListener(Consumer<LoginNegotiator> onLogin) {
        throw new UnsupportedOperationException("Login message handling is not supported on Spigot!");
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<>(SpigotMessagingModule::new, AbstractMessagingModule.ID, new ConfigSection());
}
