package org.wallentines.mcore.messenger;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mcore.pluginmsg.ProxyPluginMessageModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.smi.MessengerType;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ProxyMessengerModule extends MessengerModule implements ProxyModule {


    private Proxy proxy;
    private PluginMessageBroker broker;

    @Override
    public boolean initialize(ConfigSection config, Proxy data) {

        this.proxy = data;

        Registry<Identifier, MessengerType<?>> registry = Registry.create("smi");
        for(Identifier id : MessengerModule.REGISTRY.getIds()) {
            registry.register(id, MessengerModule.REGISTRY.get(id));
        }

        ConfigSection pm = config.getSection("plugin_message_broker");
        if(pm.getBoolean("enable")) {

            ProxyPluginMessageModule mod = data.getModuleManager().getModule(ProxyPluginMessageModule.class);
            if(mod == null) {
                MidnightCoreAPI.LOGGER.error("Unable to enable plugin message messenger! Plugin message module is unloaded!");
            } else {
                Path keyPath;
                if(pm.getBoolean("encrypt")) {
                    keyPath = data.getConfigDirectory().resolve("MidnightCore").resolve("messenger.key");
                    if(!Files.exists(keyPath)) {
                        genKey(keyPath);
                    }
                } else {
                    keyPath = null;
                }
                broker = new ProxyPluginMessageBroker(data, keyPath, mod, pm.getBoolean("persistent_registration"));

                registry.tryRegister("mcore:plugin_message", PluginMessenger.createType(broker));
            }
        }

        ConfigSection messengers = config.getSection("messengers");
        init(messengers, registry);

        return true;
    }

    @Override
    public void disable() {
        shutdown();

        if(broker != null) {
            broker.shutdown();
        }
    }

    public Proxy getProxy() {
        return proxy;
    }

    private void genKey(Path keyPath) {
        if(!Files.exists(keyPath)) {

            SecureRandom random = new SecureRandom();

            try(OutputStream fos = Files.newOutputStream(keyPath)) {
                KeyGenerator gen = KeyGenerator.getInstance("AES");
                gen.init(128, random);
                SecretKey key = gen.generateKey();
                fos.write(key.getEncoded());

            } catch (NoSuchAlgorithmException | IOException ex) {
                MidnightCoreAPI.LOGGER.warn("Failed to generate messenger key!", ex);
            }
        }
    }

    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("messengers", new ConfigSection()
                    .with("default", new ConfigSection()
                            .with("type", "mcore:plugin_message")
                            .with("encrypt", false)
                    )
            )
            .with("plugin_message_broker", new ConfigSection()
                    .with("enable", true)
                    .with("encrypt", false)
                    .with("persistent_registration", true)
            );

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<Proxy, ProxyModule>(ProxyMessengerModule::new, ID, DEFAULT_CONFIG);
}
