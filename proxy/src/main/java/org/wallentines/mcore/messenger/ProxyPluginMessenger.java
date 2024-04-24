package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyPlayer;
import org.wallentines.mcore.ProxyServer;
import org.wallentines.mcore.pluginmsg.ProxyPluginMessageModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.EventHandler;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Consumer;

public class ProxyPluginMessenger extends PluginMessenger {

    private final Map<ProxyServer, ForwardInfo> servers = new HashMap<>();
    private final ProxyPluginMessageModule module;

    public ProxyPluginMessenger(ProxyPluginMessageModule module, SecretKey encryptionKey, String namespace) {
        super(encryptionKey, namespace);
        this.module = module;
    }

    @Override
    public void publish(String channel, ByteBuf message) {
        publish(channel, message, false);
    }

    @Override
    public void queue(String channel, ByteBuf message) {
        publish(channel, message, true);
    }

    private void publish(String channel, ByteBuf message, boolean queue) {

        // Forward
        for(ForwardInfo inf : servers.values()) {

            if(inf.namespace == null || inf.namespace.equals(namespace)) {
                Packet out = new Packet(channel, namespace, inf.flags, message);
                inf.forward(out, queue);
            }
        }
    }

    @Override
    protected void registerHandler(Consumer<Packet> handler) {

        module.registerServerHandler(MESSAGE_ID, (pl, payload) -> {

            Packet pck = readPacket(payload);
            if(pck.channel.equals(REGISTER_CHANNEL)) {

                // Handle
                servers.put(pl.getServer(), new ForwardInfo(pl.getServer(), pck.namespace, pck.flags));
                return;
            }

            if(namespace == null || namespace.equals(pck.namespace)) {
                handler.accept(pck);
            }

            publish(pck.channel, pck.payload, pck.flags.contains(Flag.QUEUE));

        });

    }

    private class ForwardInfo {

        public final ProxyServer server;
        public final String namespace;
        public final EnumSet<Flag> flags;

        private final ArrayDeque<Packet> queue = new ArrayDeque<>();
        private final EventHandler<ProxyPlayer> handler;

        public ForwardInfo(ProxyServer server, String namespace, EnumSet<Flag> flags) {
            this.server = server;
            this.namespace = namespace;
            this.flags = flags;

            this.handler = pl -> {
                while(!queue.isEmpty()) {
                    module.sendPlayerMessage(pl, queue.remove());
                }
                server.connectEvent().unregisterAll(this);
            };
        }

        public void forward(Packet pck, boolean queue) {

            if(server.getPlayers().isEmpty()) {
                if(!queue) {
                    return;
                }
                if(this.queue.isEmpty()) {
                    server.connectEvent().register(this, handler);
                }
                this.queue.add(pck);
            } else {

                module.sendPlayerMessage(server.getPlayers().iterator().next(), pck);
            }

        }

    }

    public static class Type implements MessengerType {

        public static final Type INSTANCE = new Type();

        @Override
        public Messenger create(MessengerModule module, ConfigSection params) {

            if(!(module instanceof ProxyMessengerModule)) {
                MidnightCoreAPI.LOGGER.error("Attempt to create a plugin messenger from an invalid module!");
                return null;
            }

            ProxyMessengerModule mod = (ProxyMessengerModule) module;
            Proxy proxy = mod.getProxy();

            ProxyPluginMessageModule parent = proxy.getModuleManager().getModule(ProxyPluginMessageModule.class);

            SecretKey key = null;
            String namespace = params.getOrDefault("namespace", (String) null);

            if(params.getOrDefault("encrypt", false)) {
                // Read secret key
                String secretKeyPath = params.getOrDefault("key_path", "secret.key");
                File file = proxy.getConfigDirectory().resolve(secretKeyPath).toFile();
                if(file.exists()) {
                    try(FileInputStream fis = new FileInputStream(file);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                        byte[] buffer = new byte[1024];
                        int read;
                        while((read = fis.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    } catch (IOException ex) {
                        MidnightCoreAPI.LOGGER.error("Unable to read encryption key for messenger!");
                        return null;
                    }
                } else {
                    try {
                        KeyGenerator gen = KeyGenerator.getInstance("AES");
                        gen.init(256);
                        key = gen.generateKey();
                    } catch (GeneralSecurityException ex) {
                        MidnightCoreAPI.LOGGER.error("Unable to generate encryption key for messenger!");
                        return null;
                    }
                }
            }

            return new ProxyPluginMessenger(parent, key, namespace);
        }
    }


}
