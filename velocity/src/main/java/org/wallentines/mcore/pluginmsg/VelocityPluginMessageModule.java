package org.wallentines.mcore.pluginmsg;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerLoginPluginMessageEvent;
import com.velocitypowered.api.proxy.LoginPhaseConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.*;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class VelocityPluginMessageModule extends ProxyPluginMessageModule {

    private VelocityProxy proxy;

    private Method mSetConnection = null;

    @Override
    public boolean initialize(ConfigSection section, Proxy data) {

        VelocityProxy proxy = (VelocityProxy) data;
        proxy.getInternal().getEventManager().register(proxy.getPlugin(), this);

        this.proxy = proxy;

        return true;
    }

    @Override
    protected void sendPlayerMessage(ProxyPlayer player, Identifier id, ByteBuf out) {
        ConversionUtil.validate(player).sendPluginMessage(ConversionUtil.toChannelIdentifier(id), PacketBufferUtil.getBytes(out));
    }

    @Override
    protected void sendServerMessage(ProxyServer server, Identifier id, ByteBuf out) {

        if(server == null) {
            MidnightCoreAPI.LOGGER.warn("Attempt to send plugin message to null server!");
            return;
        }

        if(!ConversionUtil.validate(server).sendPluginMessage(ConversionUtil.toChannelIdentifier(id), PacketBufferUtil.getBytes(out))) {
            MidnightCoreAPI.LOGGER.warn("Failed to send plugin message in channel " + id + " to server " + server.getName());
        }
    }

    @Override
    public void registerPlayerHandler(Identifier id, PacketHandler<ProxyPlayer> handler) {
        super.registerPlayerHandler(id, handler);
        proxy.getInternal().getChannelRegistrar().register(ConversionUtil.toChannelIdentifier(id));
    }

    @Override
    public void registerServerHandler(Identifier id, PacketHandler<ServerMessage> handler) {
        super.registerServerHandler(id, handler);
        proxy.getInternal().getChannelRegistrar().register(ConversionUtil.toChannelIdentifier(id));
    }

    @Subscribe
    public void onMessage(PluginMessageEvent event) {

        if(event.getSource() instanceof Player pl) {
            if(handle(
                    proxy.getPlayer(pl),
                    ConversionUtil.toIdentifier(event.getIdentifier()),
                    Unpooled.wrappedBuffer(event.getData()))) {

                event.setResult(PluginMessageEvent.ForwardResult.handled());
            }
        } else if(event.getSource() instanceof ServerConnection sc) {

            Player pl = sc.getPlayer();

            // Velocity might be processing this message before assigning the player's server.
            if(pl.getCurrentServer().isEmpty()) {

                if(mSetConnection == null) {
                    try {
                        mSetConnection = pl.getClass().getMethod("setConnectedServer", sc.getClass());
                    } catch (NoSuchMethodException ex) {
                        // Ignore
                    }
                }
                if(mSetConnection != null) {
                    try {
                        mSetConnection.invoke(pl, sc);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        // Ignore
                    }
                }
            }

            if(handleServer(
                    proxy.getPlayer(pl),
                    proxy.getServer(sc.getServer()),
                    ConversionUtil.toIdentifier(event.getIdentifier()),
                    Unpooled.wrappedBuffer(event.getData()))) {

                event.setResult(PluginMessageEvent.ForwardResult.handled());
            }
        }
    }

    /**
     * Fired when a player logs in. At this stage, the username must be used. 1.19+ clients send the UUID at the same
     * time as the username, but older clients do not. Additionally, this UUID is not entirely reliable as it may change
     * depending on the forwarding type the proxy uses.
     * @param event The pre login event.
     */
    @Subscribe
    public void onPreLogin(PreLoginEvent event) {

        onLogin.invoke(new VelocityLoginNegotiator(event.getUsername(), (LoginPhaseConnection) event.getConnection()));
    }

    /**
     * Fired when a server sends a login plugin message to the client. This will always be intercepted by the proxy, as
     * the player will only ever log in once. However, the proxy can still respond on the player's behalf.
     * @param event The message received
     */
    @Subscribe
    public void onLoginMessage(ServerLoginPluginMessageEvent event) {

        handleLogin(
                proxy.getPlayer(event.getConnection().getPlayer().getUniqueId()),
                ConversionUtil.toIdentifier(event.getIdentifier()),
                Unpooled.wrappedBuffer(event.getContents()));
    }

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<>(VelocityPluginMessageModule::new, ID, new ConfigSection());

}
