package me.m1dnightninja.midnightcore.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;

import java.util.Optional;

public class CustomPayloadListener {

    private static final ChannelIdentifier sendId = MinecraftChannelIdentifier.create("midnightcore", "send");

    public CustomPayloadListener() {
        MidnightCore.getInstance().getServer().getChannelRegistrar().register(sendId);
    }

    @Subscribe
    public void onMessage(PluginMessageEvent event) {

        if(!event.getIdentifier().equals(sendId) || !(event.getTarget() instanceof Player)) {
            return;
        }

        String server = event.dataAsDataStream().readUTF();
        Optional<RegisteredServer> optionalServer = MidnightCore.getInstance().getServer().getServer(server);

        Player pl = (Player) event.getTarget();
        optionalServer.ifPresent(svr -> pl.createConnectionRequest(svr).connect());

        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

}
