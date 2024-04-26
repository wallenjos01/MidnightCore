package org.wallentines.mcore.messenger;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyServer;
import org.wallentines.mcore.pluginmsg.ProxyPluginMessageModule;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ProxyPluginMessageBroker extends PluginMessageBroker {

    private final Proxy proxy;
    private final ProxyPluginMessageModule module;
    private final Map<String, ForwardInfo> infosByServer = new HashMap<>();
    private final Set<String> rootNamespace = new HashSet<>();
    private final Map<String, Set<String>> serversByNamespace = new HashMap<>();

    public ProxyPluginMessageBroker(Proxy proxy, ProxyPluginMessageModule module) {
        this.proxy = proxy;
        this.module = module;


        module.registerServerHandler(MESSAGE_ID, (player, payload) -> {

            Packet packet = readPacket(payload);
            if(packet.channel.equals(REGISTER_CHANNEL)) {

                byte id = packet.payload.readByte();
                if(id == REGISTER) {

                    String serverName = player.getServer().getName();
                    infosByServer.compute(serverName, (k,v) -> {
                        if(v == null) v = new ForwardInfo(player.getServer(), packet.flags);
                        return v;
                    });

                    if(packet.namespace == null) {
                        rootNamespace.add(serverName);
                    } else {
                        serversByNamespace.compute(packet.namespace, (k,v) -> {
                            if(v == null) v = new HashSet<>();
                            v.add(serverName);
                            return v;
                        });
                    }

                } else if(id == UNREGISTER) {
                    infosByServer.remove(player.getServer().getName());
                }
                return;
            }

            // Handle
            packetHandler.accept(packet);

            // Forward
            send(packet);

        });

    }

    @Override
    protected void send(Packet packet) {

        Set<String> servers;
        if(packet.namespace == null) {
            servers = rootNamespace;
        } else {
            servers = serversByNamespace.get(packet.namespace);
        }

        for(String s : servers) {
            ForwardInfo fi = infosByServer.get(s);
            module.sendServerMessage(fi.server, packet.forFlags(fi.flags));
        }
    }

    @Override
    public void shutdown() {
        infosByServer.clear();
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
        final EnumSet<Flag> flags;

        public ForwardInfo(ProxyServer server, EnumSet<Flag> flags) {
            this.server = server;
            this.flags = flags;
        }
    }

    public static final Factory FACTORY = (msg) -> {

        if(!(msg instanceof ProxyMessengerModule)) {
            throw new IllegalStateException("Unable to create plugin message broker! Plugin message module is unloaded!");
        }

        Proxy prx = ((ProxyMessengerModule) msg).getProxy();
        ProxyPluginMessageModule pm = prx.getModuleManager().getModule(ProxyPluginMessageModule.class);

        if(pm == null) {
            throw new IllegalStateException("Unable to create plugin message broker! Plugin message module is unloaded!");
        }

        return new ProxyPluginMessageBroker(prx, pm);
    };

}
