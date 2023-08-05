package org.wallentines.mcore.messaging;

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
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class VelocityMessagingModule extends ProxyMessagingModule {

    private VelocityProxy proxy;

    @Override
    public boolean initialize(ConfigSection section, Proxy data) {

        VelocityProxy proxy = (VelocityProxy) data;
        proxy.getInternal().getEventManager().register(proxy.getPlugin(), this);

        this.proxy = proxy;

        return true;
    }

    @Override
    protected void sendPlayerMessage(ProxyPlayer player, Identifier id, ByteBuf out) {
        ConversionUtil.validate(player).sendPluginMessage(ConversionUtil.toChannelIdentifier(id), out.array());
    }

    @Override
    protected void sendServerMessage(ProxyServer server, Identifier id, ByteBuf out) {
        ConversionUtil.validate(server).sendPluginMessage(ConversionUtil.toChannelIdentifier(id), out.array());
    }

    @Subscribe
    private void onMessage(PluginMessageEvent event) {

        if(event.getSource() instanceof Player pl) {
            if(handle(
                    proxy.getPlayer(pl.getUniqueId()),
                    ConversionUtil.toIdentifier(event.getIdentifier()),
                    Unpooled.wrappedBuffer(event.getData()))) {

                event.setResult(PluginMessageEvent.ForwardResult.handled());
            }
        } else if(event.getSource() instanceof ServerConnection sc) {
            if(handleServer(
                    proxy.getPlayer(sc.getPlayer().getUniqueId()),
                    ConversionUtil.toIdentifier(event.getIdentifier()),
                    Unpooled.wrappedBuffer(event.getData()))) {

                event.setResult(PluginMessageEvent.ForwardResult.handled());
            }
        }
    }

    @Subscribe
    private void onPreLogin(PreLoginEvent event) {

        onLogin.invoke(new VelocityLoginNegotiator(event.getUsername(), (LoginPhaseConnection) event.getConnection()));
    }

    @Subscribe
    private void onLoginMessage(ServerLoginPluginMessageEvent event) {

        handleLogin(
                proxy.getPlayer(event.getConnection().getPlayer().getUniqueId()),
                ConversionUtil.toIdentifier(event.getIdentifier()),
                Unpooled.wrappedBuffer(event.getContents()));
    }

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<>(VelocityMessagingModule::new, ProxyMessagingModule.ID, new ConfigSection());

}
