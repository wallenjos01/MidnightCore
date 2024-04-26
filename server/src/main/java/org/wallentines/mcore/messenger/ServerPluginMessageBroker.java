package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.pluginmsg.ServerPluginMessageModule;
import org.wallentines.midnightlib.event.EventHandler;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public class ServerPluginMessageBroker extends PluginMessageBroker {

    private final Server server;
    private final ServerPluginMessageModule module;
    private final EventHandler<Player> queueHandler;
    private final Queue<Packet> queue = new ArrayDeque<>();

    public ServerPluginMessageBroker(Server server, ServerPluginMessageModule module) {
        super();
        this.server = server;
        this.module = module;

        this.queueHandler = (pl) -> {
            while(!queue.isEmpty()) {
                module.sendPacket(pl, queue.remove());
            }
            server.joinEvent().unregisterAll(this);
        };

        module.registerPacketHandler(MESSAGE_ID, (pl, buf) -> {
            packetHandler.accept(readPacket(buf));
        });
    }

    @Override
    public void register(PluginMessenger messenger) {
        super.register(messenger);

        ByteBuf payload = Unpooled.buffer();
        payload.writeByte(REGISTER);
        send(new Packet(REGISTER_CHANNEL, messenger.encrypt, messenger.namespace, payload).queued());
    }

    @Override
    protected void send(Packet packet) {

        if(server.getPlayers().isEmpty()) {
            if(packet.flags.contains(Flag.QUEUE)) {
                return;
            }

            if(queue.isEmpty()) {
                server.joinEvent().register(this, queueHandler);
            }

            queue.add(packet);
            return;
        }

        module.sendPacket(server.getPlayers().iterator().next(), packet);
    }

    @Override
    public void shutdown() {

        ByteBuf payload = Unpooled.buffer();
        payload.writeByte(UNREGISTER);
        send(new Packet(REGISTER_CHANNEL, key != null, null, payload).queued());
    }

    @Override
    protected File getKeyFile() {
        return server.getConfigDirectory().resolve("MidnightCore").resolve("messenger.key").toFile();
    }


    public static final Factory FACTORY = (msg) -> {

        if(!(msg instanceof ServerMessengerModule)) {
            throw new IllegalStateException("Unable to create plugin message broker! Plugin message module is unloaded!");
        }

        Server srv = ((ServerMessengerModule) msg).getServer();
        ServerPluginMessageModule pm = srv.getModuleManager().getModule(ServerPluginMessageModule.class);

        if(pm == null) {
            throw new IllegalStateException("Unable to create plugin message broker! Plugin message module is unloaded!");
        }

        return new ServerPluginMessageBroker(srv, pm);
    };
}
