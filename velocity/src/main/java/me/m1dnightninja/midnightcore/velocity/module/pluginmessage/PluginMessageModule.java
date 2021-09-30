package me.m1dnightninja.midnightcore.velocity.module.pluginmessage;

import com.google.common.io.ByteArrayDataOutput;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.pluginmessage.IPluginMessageHandler;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.common.module.pluginmessage.AbstractPluginMessageModule;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;
import me.m1dnightninja.midnightcore.velocity.player.VelocityPlayer;

import java.util.Optional;

public class PluginMessageModule extends AbstractPluginMessageModule {

    @Override
    public boolean initialize(ConfigSection configuration) {

        if(!super.initialize(configuration)) return false;

        MidnightCore.getInstance().getServer().getEventManager().register(MidnightCore.getInstance(), this);
        registerDefaults();

        return true;
    }

    @Override
    public void registerProvider(MIdentifier id, IPluginMessageHandler handler) {
        super.registerProvider(id, handler);

        ChannelIdentifier cid = MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());
        ProxyServer server = MidnightCore.getInstance().getServer();

        server.getChannelRegistrar().register(cid);
    }

    @Override
    protected void send(MPlayer player, MIdentifier id, ByteArrayDataOutput data) {

        Player pl = ((VelocityPlayer) player).getVelocityPlayer();
        Optional<ServerConnection> conn = pl.getCurrentServer();

        if(conn.isEmpty()) return;

        ChannelIdentifier cid = MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());

        conn.get().sendPluginMessage(cid, data.toByteArray());
    }

    @Subscribe
    private void onMessage(PluginMessageEvent event) {

        MIdentifier id = MIdentifier.parse(event.getIdentifier().getId());

        MidnightCoreAPI.getLogger().warn("Received message with id " + id);

        IPluginMessageHandler handler = handlers.get(id);
        if(handler == null) return;

        boolean toClient = event.getTarget() instanceof Player;
        Player player = toClient ? (Player) event.getTarget() : (Player) event.getSource();

        handle(VelocityPlayer.wrap(player), id, event.dataAsDataStream());

        if(!handler.visibleToPlayers()) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }


    private void registerDefaults() {

        registerProvider(MIdentifier.create("midnightcore", "send"), (player, data) -> {

            if(!data.has("server")) {

                MidnightCoreAPI.getLogger().warn("Received send request with invalid data!");
                return;
            }
            String server = data.getString("server");

            Optional<RegisteredServer> svr = MidnightCore.getInstance().getServer().getServer(server);
            if(svr.isEmpty()) return;

            ((VelocityPlayer) player).getVelocityPlayer().createConnectionRequest(svr.get()).fireAndForget();
        });

    }

}
