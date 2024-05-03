package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.pluginmsg.ServerPluginMessageModule;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.mdcfg.serializer.InlineSerializer;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ServerPluginMessageBroker extends PluginMessageBroker {

    private final Server server;
    private final ServerPluginMessageModule module;
    private final Queue<Packet> queue = new ArrayDeque<>();
    private boolean firstSend = false;

    private ServerPluginMessageBroker(Server server, ServerPluginMessageModule module, boolean encrypt, RegisterTime registerTime) {
        super(encrypt);
        this.server = server;
        this.module = module;

        server.joinEvent().register(this, pl -> {

            if(pl.getServer().getPlayerCount() == 1) {

                // Server was empty
                switch (registerTime) {
                    case STARTUP: {
                        if(firstSend) {
                            module.sendPacket(pl, new Packet(REGISTER, writeRegistration()));
                            firstSend = true;
                        } else {
                            module.sendPacket(pl, new Packet(ONLINE, null));
                        }
                        break;
                    }
                    case REQUESTED: {
                        module.sendPacket(pl, new Packet(ONLINE, null));
                        break;
                    }
                    case ALWAYS: {
                        module.sendPacket(pl, new Packet(REGISTER, writeRegistration()));
                        break;
                    }

                }

                while(!queue.isEmpty()) {
                    module.sendPacket(pl, queue.remove());
                }
            }

        });

        module.registerPacketHandler(MESSAGE_ID, (pl, buf) -> {

            Packet pck = readPacket(buf);
            if(pck.isSystemMessage()) {
                if(pck.systemChannel == REQUEST) {
                    send(new Packet(REGISTER, writeRegistration()));
                }
                return;
            }
            if(pck.isExpired()) return;

            handle(pck);
        });
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

        payload.writeBoolean(key != null);
        payload.writeBoolean(rootNamespace);

        PacketBufferUtil.writeVarInt(payload, messengers.size());
        for(String s : namespaces) {
            PacketBufferUtil.writeUtf(payload, s);
        }

        return payload;
    }

    @Override
    public void register(PluginMessenger messenger) {
        super.register(messenger);
    }

    @Override
    protected void send(Packet packet) {

        if(server.getPlayerCount() == 0) {
            queue.removeIf(Packet::isExpired);
            if(packet.ttl > 0) {
                queue.add(packet);
            }
            return;
        }

        module.sendPacket(server.getPlayers().iterator().next(), packet);
    }

    @Override
    public void onShutdown() {

        send(new Packet(UNREGISTER, null));

        module.unregisterPacketHandler(MESSAGE_ID);
        server.joinEvent().unregisterAll(this);

    }

    @Override
    protected File getKeyFile() {
        return server.getConfigDirectory().resolve("MidnightCore").resolve("messenger.key").toFile();
    }


    public static final Factory FACTORY = (msg, cfg) -> {

        if(!(msg instanceof ServerMessengerModule)) {
            throw new IllegalStateException("Unable to create plugin message broker! Server messenger required!");
        }

        Server srv = ((ServerMessengerModule) msg).getServer();
        ServerPluginMessageModule pm = srv.getModuleManager().getModule(ServerPluginMessageModule.class);

        RegisterTime time = cfg.getOptional("register", RegisterTime.SERIALIZER).orElse(RegisterTime.STARTUP);

        if(pm == null) {
            throw new IllegalStateException("Unable to create plugin message broker! Plugin message module is unloaded!");
        }

        return new ServerPluginMessageBroker(srv, pm, cfg.getOrDefault("encrypt", false), time);
    };

    private enum RegisterTime {
        STARTUP("startup"),
        ALWAYS("always"),
        REQUESTED("requested");
        final String id;

        RegisterTime(String id) {
            this.id = id;
        }

        static RegisterTime byId(String id) {
            for(RegisterTime rt : values()) {
                if(rt.id.equals(id)) return rt;
            }
            return null;
        }

        static final InlineSerializer<RegisterTime> SERIALIZER = InlineSerializer.of(rt -> rt.id, RegisterTime::byId);
    }

}
