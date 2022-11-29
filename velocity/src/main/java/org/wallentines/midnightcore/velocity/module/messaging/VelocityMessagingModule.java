package org.wallentines.midnightcore.velocity.module.messaging;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.LoginNegotiator;
import org.wallentines.midnightcore.api.module.messaging.MessageHandler;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.common.module.messaging.PacketBufferUtils;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightcore.velocity.player.VelocityPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Optional;

public class VelocityMessagingModule extends AbstractMessagingModule {

    public VelocityMessagingModule() {

        MidnightCore.getInstance().getServer().getEventManager().register(MidnightCore.getInstance(), this);
        registerDefaults();
    }

    @Override
    public void registerHandler(Identifier id, MessageHandler handler) {
        super.registerHandler(id, handler);

        ChannelIdentifier cid = MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());
        ProxyServer server = MidnightCore.getInstance().getServer();

        server.getChannelRegistrar().register(cid);
    }

    @Subscribe
    private void onPreLogin(PreLoginEvent event) {

        LoginNegotiator ln = new VelocityLoginNegotiator(event);
        loginHandlers.forEach(l -> l.accept(ln));

    }

    @Override
    public void sendRawMessage(MPlayer player, Identifier id, byte[] data) {

        Player pl = ((VelocityPlayer) player).getInternal();
        Optional<ServerConnection> conn = pl.getCurrentServer();

        if(conn.isEmpty()) return;

        ChannelIdentifier cid = MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());

        conn.get().sendPluginMessage(cid, data);
    }

    @Subscribe
    private void onMessage(PluginMessageEvent event) {

        Identifier id = Identifier.parse(event.getIdentifier().getId());

        MessageHandler handler = handlers.get(id);
        if(handler == null) {
            return;
        }

        boolean toClient = event.getTarget() instanceof Player;
        Player player = toClient ? (Player) event.getTarget() : (Player) event.getSource();

        ByteBuf buf = Unpooled.wrappedBuffer(event.getData());
        handle(VelocityPlayer.wrap(player), id, buf);

        if(!handler.visibleToPlayers()) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }


    private void registerDefaults() {

        registerHandler(new Identifier(Constants.DEFAULT_NAMESPACE, "send"), (player, data) -> {

            ConfigSection sec = JsonConfigProvider.INSTANCE.loadFromString(PacketBufferUtils.readUtf(data));

            if(sec == null || !sec.has("server")) {
                MidnightCoreAPI.getLogger().warn("Received send request with invalid data!");
                return;
            }
            String server = sec.getString("server");

            Optional<RegisteredServer> svr = MidnightCore.getInstance().getServer().getServer(server);
            if(svr.isEmpty()) return;

            ((VelocityPlayer) player).getInternal().createConnectionRequest(svr.get()).fireAndForget();
        });

    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(VelocityMessagingModule::new, ID, new ConfigSection());

}
