package org.wallentines.mcore.messenger;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyServer;
import org.wallentines.mcore.pluginmsg.ProxyPluginMessageModule;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.BinaryCodec;
import org.wallentines.mdcfg.serializer.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ProxyPluginMessageBroker extends PluginMessageBroker {

    private final Proxy proxy;
    private final ProxyPluginMessageModule module;
    private final Map<String, ForwardInfo> forwarders = new HashMap<>();
    private final boolean enablePersistence;

    public ProxyPluginMessageBroker(Proxy proxy, ProxyPluginMessageModule module, boolean enablePersistence) {
        this.proxy = proxy;
        this.module = module;
        this.enablePersistence = enablePersistence;

        if (enablePersistence) {
            loadRegistrations();
        }

        module.registerServerHandler(MESSAGE_ID, (msg, payload) -> {

            ProxyServer server = msg.server();
            Packet packet = readPacket(payload);

            if(packet.flags.contains(Flag.SYSTEM)) {

                switch (packet.systemChannel) {
                    case REGISTER: {
                        int messengers = PacketBufferUtil.readVarInt(packet.payload);
                        ForwardInfo info = new ForwardInfo(server);
                        for(int i = 0 ; i < messengers ; i++) {
                            EnumSet<Flag> flags = Flag.unpack(packet.payload.readByte());
                            if(flags.contains(Flag.NAMESPACED)) {
                                String namespace = PacketBufferUtil.readUtf(packet.payload);
                                info.flagsByNamespace.put(namespace, flags);
                            } else {
                                info.rootNamespace = flags;
                            }
                        }
                        forwarders.put(server.getName(), info);
                        break;
                    }
                    case UNREGISTER: {
                        String serverName = server.getName();
                        forwarders.remove(serverName);
                        break;
                    }
                    case ONLINE: {
                        flushQueue(server);
                        break;
                    }
                }
                return;
            }

            if(!forwarders.containsKey(server.getName())) {
                module.sendServerMessage(server, new Packet(REQUEST, null));
            }

            // Handle
            handle(packet);

            // Forward
            forward(packet, server);
        });

    }

    private void loadRegistrations() {

        File in = proxy.getConfigDirectory().resolve("MidnightCore").resolve("messenger.mdb").toFile();
        if(!in.exists()) return;

        ConfigList root;
        try {
            root = BinaryCodec.fileCodec().loadFromFile(ConfigContext.INSTANCE, in, StandardCharsets.UTF_8).asList();
        } catch (IOException ex) {
            MidnightCoreAPI.LOGGER.warn("Unable to load registrations!");
            return;
        }

        Serializer<ForwardInfo> ser = ForwardInfo.SERIALIZER.forContext(proxy);
        for(ConfigObject obj : root.values()) {
            ser.deserialize(ConfigContext.INSTANCE, obj).get().ifPresent(fi -> {
                forwarders.put(fi.server.getName(), fi);
            });
        }
    }

    private void flushQueue(ProxyServer server) {

        ForwardInfo info = forwarders.get(server.getName());

        if(info == null) {
            module.sendServerMessage(server, new Packet(REQUEST, null));
            return;
        }

        while(!info.queue.isEmpty()) {

            Packet packet = info.queue.remove();
            EnumSet<Flag> flags = info.getFlags(packet.namespace);
            if(flags == null) {
                continue;
            }
            module.sendServerMessage(info.server, info.queue.remove().forFlags(flags));
        }

    }

    @Override
    protected void send(Packet packet) {
        forward(packet, null);
    }

    protected void forward(Packet packet, ProxyServer sender) {

        for(ForwardInfo fi : forwarders.values()) {

            if(fi.server == sender) continue;

            EnumSet<Flag> flags = fi.getFlags(packet.namespace);
            if(flags == null) {
                continue;
            }

            if(fi.server.getPlayerCount() == 0) {
                if(packet.flags.contains(Flag.QUEUE) && flags.contains(Flag.QUEUE)) {
                    fi.queue.add(packet);
                }
            } else {
                module.sendServerMessage(fi.server, packet.forFlags(flags));
            }
        }
    }

    @Override
    public void onShutdown() {

        if(enablePersistence) {

            File out = proxy.getConfigDirectory().resolve("MidnightCore").resolve("messenger.mdb").toFile();
            ConfigList root = new ConfigList();
            Serializer<ForwardInfo> ser = ForwardInfo.SERIALIZER.forContext(proxy);
            for(ForwardInfo fi : forwarders.values()) {
                root.add(fi, ser);
            }
            try {
                BinaryCodec.fileCodec().saveToFile(ConfigContext.INSTANCE, root, out, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                MidnightCoreAPI.LOGGER.warn("Unable to save registrations!");
            }
        }
        forwarders.clear();
        module.unregisterServerHandler(MESSAGE_ID);
    }

    @Override
    protected File getKeyFile() {

        File out = proxy.getConfigDirectory().resolve("MidnightCore").resolve("messenger.key").toFile();
        if(!out.exists()) {

            try(FileOutputStream fos = new FileOutputStream(out)) {
                KeyGenerator gen = KeyGenerator.getInstance("AES");
                gen.init(256);
                SecretKey key = gen.generateKey();

                fos.write(key.getEncoded());

            } catch (NoSuchAlgorithmException | IOException ex) {
                MidnightCoreAPI.LOGGER.warn("Failed to generate messenger key!", ex);
                return out;
            }
        }

        return out;
    }

    private static class ForwardInfo {

        final ProxyServer server;
        final Queue<Packet> queue = new ArrayDeque<>();
        final Map<String, EnumSet<Flag>> flagsByNamespace = new HashMap<>();
        EnumSet<Flag> rootNamespace;

        ForwardInfo(ProxyServer server) {
            this.server = server;
        }

        EnumSet<Flag> getFlags(String namespace) {
            if(namespace == null) return rootNamespace;
            return flagsByNamespace.get(namespace);
        }


        static final ContextSerializer<ProxyServer, Proxy> SERVER = new ContextSerializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> ctx, ProxyServer value, Proxy context) {
                return SerializeResult.success(ctx.toString(value.getName()));
            }

            @Override
            public <O> SerializeResult<ProxyServer> deserialize(SerializeContext<O> ctx, O value, Proxy context) {
                if(!ctx.isString(value)) return SerializeResult.failure("Expected a string!");
                return SerializeResult.ofNullable(context.getServer(ctx.asString(value)));
            }
        };

        static final ContextSerializer<ForwardInfo, Proxy> SERIALIZER = ContextObjectSerializer.create(
                SERVER.entry("server", (fi, ctx) -> fi.server),
                Flag.SERIALIZER.mapOf().<ForwardInfo, Proxy>entry("namespaces", (fi, ctx) -> fi.flagsByNamespace).optional(),
                Flag.SERIALIZER.<ForwardInfo, Proxy>entry("root", (fi, ctx) -> fi.rootNamespace).optional(),
                (ctx, svr, ns, root) -> {
                    ForwardInfo fi = new ForwardInfo(svr);
                    fi.flagsByNamespace.putAll(ns);
                    fi.rootNamespace = root;
                    return fi;
                }
        );

    }

    public static final Factory FACTORY = (msg, cfg) -> {

        if(!(msg instanceof ProxyMessengerModule)) {
            throw new IllegalStateException("Unable to create plugin message broker! Proxy messenger required!");
        }

        Proxy prx = ((ProxyMessengerModule) msg).getProxy();
        ProxyPluginMessageModule pm = prx.getModuleManager().getModule(ProxyPluginMessageModule.class);

        if(pm == null) {
            throw new IllegalStateException("Unable to create plugin message broker! Plugin message module is unloaded!");
        }

        return new ProxyPluginMessageBroker(prx, pm, cfg.getOrDefault("persistent_registration", true));
    };

}
