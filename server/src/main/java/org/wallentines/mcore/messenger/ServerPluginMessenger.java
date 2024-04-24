package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.pluginmsg.ServerPluginMessageModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.EventHandler;

import javax.crypto.SecretKey;
import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

public class ServerPluginMessenger extends PluginMessenger {

    private final Server server;
    private final ServerPluginMessageModule module;
    private final EventHandler<Player> queueHandler;

    public ServerPluginMessenger(ServerMessengerModule parent, ServerPluginMessageModule module, SecretKey encryptionKey, String namespace) {
        super(encryptionKey, namespace);
        this.module = module;
        this.server = parent.getServer();
        this.queueHandler = pl -> {
            while(!queue.isEmpty()) {
                module.sendPacket(pl, queue.remove());
            }
            server.joinEvent().unregisterAll(this);
        };

        queue(REGISTER_CHANNEL, Unpooled.buffer());
    }

    @Override
    public void publish(String channel, ByteBuf message) {
        publish(new Packet(channel, namespace, message));
    }

    private void publish(Packet packet) {
        Collection<Player> players = server.getPlayers();
        if(players.isEmpty()) {
            MidnightCoreAPI.LOGGER.error("Unable to publish message! No player is online!");
            return;
        }
        module.sendPacket(players.iterator().next(), packet);
    }

    @Override
    public void queue(String channel, ByteBuf message) {

        Packet pck = new Packet(channel, namespace, message).queued();

        if(server.getPlayers().isEmpty()) {
            if(queue.isEmpty()) {
                server.joinEvent().register(this, queueHandler);
            }
            queue.add(pck);
            return;
        }
        publish(pck);
    }

    @Override
    protected void registerHandler(Consumer<Packet> handler) {

        module.registerPacketHandler(MESSAGE_ID, (data, buf) -> {
            handler.accept(readPacket(buf));
        });
    }

    public static class Type implements MessengerType {

        public static final Type INSTANCE = new Type();

        @Override
        public Messenger create(MessengerModule module, ConfigSection params) {

            if(!(module instanceof ServerMessengerModule)) {
                MidnightCoreAPI.LOGGER.error("Attempt to create a plugin messenger from an invalid module!");
                return null;
            }

            ServerMessengerModule mod = (ServerMessengerModule) module;
            Server server = mod.getServer();

            ServerPluginMessageModule parent = server.getModuleManager().getModule(ServerPluginMessageModule.class);

            SecretKey key = null;
            String namespace = params.getOrDefault("namespace", (String) null);

            if(params.getOrDefault("encrypt", false)) {
                // Read secret key
                String secretKeyPath = params.getOrDefault("key_path", "secret.key");
                File file = server.getConfigDirectory().resolve(secretKeyPath).toFile();
                key = readKey(file);

                if(key == null) return null;
            }

            return new ServerPluginMessenger(mod, parent, key, namespace);
        }
    }

}
