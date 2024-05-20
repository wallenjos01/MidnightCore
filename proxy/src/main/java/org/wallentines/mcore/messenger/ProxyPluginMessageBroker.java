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
import java.security.SecureRandom;
import java.util.*;

public class ProxyPluginMessageBroker extends PluginMessageBroker {

    private final Proxy proxy;
    private final ProxyPluginMessageModule module;
    private final Map<String, ForwardInfo> forwarders = new HashMap<>();
    private final boolean enablePersistence;

    public ProxyPluginMessageBroker(Proxy proxy, ProxyPluginMessageModule module, boolean encrypt, boolean enablePersistence) {
        super();

        this.proxy = proxy;
        this.module = module;
        this.enablePersistence = enablePersistence;

        init(encrypt);

        if (enablePersistence) {
            loadRegistrations();
        }

        module.registerServerHandler(MESSAGE_ID, (msg, payload) -> {

            ProxyServer server = msg.server();
            Packet packet = readPacket(payload);

            if(packet.isSystemMessage()) {

                switch (packet.systemChannel) {
                    case REGISTER: {

                        boolean enc = packet.payload.readBoolean();
                        boolean root = packet.payload.readBoolean();
                        int messengers = PacketBufferUtil.readVarInt(packet.payload);

                        List<String> namespaces = new ArrayList<>();

                        for(int i = 0 ; i < messengers ; i++) {
                            EnumSet<Flag> flags = Flag.unpack(packet.payload.readByte());
                            if(flags.contains(Flag.NAMESPACED)) {
                                String namespace = PacketBufferUtil.readUtf(packet.payload);
                                namespaces.add(namespace);
                            }
                        }

                        ForwardInfo info = new ForwardInfo(server, enc, root, namespaces);

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

            if(packet.isExpired()) return;

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
                fi.persisted = true;
                forwarders.put(fi.server.getName(), fi);
            });
        }
    }

    private void flushQueue(ProxyServer server) {

        ForwardInfo info = forwarders.get(server.getName());

        if(info == null || info.persisted) {
            module.sendServerMessage(server, new Packet(REQUEST, null));
            if(info == null) return;
        }

        while(!info.queue.isEmpty()) {

            Packet packet = info.queue.remove();
            if(packet.isExpired()) continue;

            module.sendServerMessage(info.server, packet);
        }

    }

    @Override
    protected void send(Packet packet) {
        forward(packet, null);
    }

    protected void forward(Packet packet, ProxyServer sender) {

        for(ForwardInfo fi : forwarders.values()) {

            Packet toSend = packet.encrypted(fi.encrypt);
            if(fi.server == sender || !fi.hasNamespace(toSend.namespace)) continue;

            if(fi.server.getPlayerCount() == 0) {
                if(toSend.ttl > 0 && !toSend.isExpired()) {
                    fi.queue.add(toSend);
                }
            } else {
                flushQueue(sender);
                module.sendServerMessage(fi.server, toSend);
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

            SecureRandom random = new SecureRandom();

            try(FileOutputStream fos = new FileOutputStream(out)) {
                KeyGenerator gen = KeyGenerator.getInstance("AES");
                gen.init(128, random);
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
        final boolean encrypt;
        final Set<String> namespaces;
        final boolean rootNamespace;
        boolean persisted = false;

        final Queue<Packet> queue = new ArrayDeque<>();

        ForwardInfo(ProxyServer server, boolean encrypt, boolean rootNamespace, Collection<String> namespace) {
            this.server = server;
            this.encrypt = encrypt;
            this.namespaces = Set.copyOf(namespace);
            this.rootNamespace = rootNamespace;
        }

        boolean hasNamespace(String namespace) {
            if(namespace == null) return rootNamespace;
            return namespaces.contains(namespace);
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
                Serializer.BOOLEAN.entry("encrypt", (fi, ctx) -> fi.encrypt),
                Serializer.BOOLEAN.entry("root", (fi, ctx) -> fi.rootNamespace),
                Serializer.STRING.listOf().entry("namespaces", (fi, ctx) -> fi.namespaces),
                (ctx, server, enc, root, ns) -> new ForwardInfo(server, enc, root, ns)
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

        return new ProxyPluginMessageBroker(
                prx,
                pm,
                cfg.getOrDefault("encrypt", false),
                cfg.getOrDefault("persistence", true));
    };

}
