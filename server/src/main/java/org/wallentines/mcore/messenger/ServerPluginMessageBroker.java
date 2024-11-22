package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.pluginmsg.ServerPluginMessageModule;
import org.wallentines.mcore.util.PacketBufferUtil;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ServerPluginMessageBroker extends PluginMessageBroker {

    private final ServerPluginMessageModule module;
    private final Server server;
    private boolean firstSend = false;

    private final Queue<MessengerPacket> queue = new ArrayDeque<>();

    public ServerPluginMessageBroker(Server server, Path keyFile, ServerPluginMessageModule module) {
        super(keyFile);
        this.module = module;
        this.server = server;

        server.joinEvent().register(this, pl -> {
            if(pl.getServer().getPlayerCount() != 1) return;
            if(firstSend) {
                module.sendPacket(pl, new MessengerPacket(REGISTER, writeRegistration(), cipher));
                firstSend = true;
            } else {
                module.sendPacket(pl, new MessengerPacket(ONLINE, null, cipher));
            }

            while(!queue.isEmpty()) {
                module.sendPacket(pl, queue.remove());
            }
        });


        module.registerPacketHandler(MessengerPacket.MESSAGE_ID, (pl, buf) -> {

            MessengerPacket pck = MessengerPacket.readPacket(buf, cipher);
            if(pck.isSystemMessage()) {
                if(pck.systemChannel == REQUEST) {
                    sendPacket(new MessengerPacket(REGISTER, writeRegistration(), cipher));
                }
                return;
            }
            if(pck.isExpired()) return;

            handle(pck);
        });
    }

    @Override
    protected void sendPacket(MessengerPacket packet) {

        if(server.getPlayerCount() == 0) {
            queue.removeIf(MessengerPacket::isExpired);
            if(packet.ttl > 0) {
                queue.add(packet);
            }
            return;
        }

        module.sendPacket(server.getPlayers().iterator().next(), packet);

    }

    @Override
    protected void shutdown() {
        sendPacket(new MessengerPacket(UNREGISTER, null, cipher));
        module.unregisterPacketHandler(MessengerPacket.MESSAGE_ID);
        server.joinEvent().unregisterAll(this);
    }

    private ByteBuf writeRegistration() {

        ByteBuf payload = Unpooled.buffer();

        boolean rootNamespace = false;
        List<String> namespaces = new ArrayList<>();

        for(PluginMessenger mess : messengers) {
            if(mess.namespace == null) {
                rootNamespace = true;
            } else {
                namespaces.add(mess.namespace);
            }
        }

        payload.writeBoolean(cipher != null);
        payload.writeBoolean(rootNamespace);

        PacketBufferUtil.writeVarInt(payload, namespaces.size());
        for(String s : namespaces) {
            PacketBufferUtil.writeUtf(payload, s);
        }

        return payload;
    }
}
