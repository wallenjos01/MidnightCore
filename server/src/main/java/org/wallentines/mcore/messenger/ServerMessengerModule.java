package org.wallentines.mcore.messenger;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.pluginmsg.ServerPluginMessageModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.smi.MessengerType;

import java.nio.file.Path;

public class ServerMessengerModule extends MessengerModule implements ServerModule {

    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("messengers", new ConfigSection()
                    .with("default", new ConfigSection()
                            .with("type", "mcore:plugin_message")
                    )
            )
            .with("plugin_message_broker", new ConfigSection()
                    .with("enabled", true)
                    .with("encrypt", false)
            );

    private PluginMessageBroker broker;

    @Override
    public boolean initialize(ConfigSection config, Server data) {

        Registry<Identifier, MessengerType<?>> registry = Registry.create("smi");
        for(Identifier id : MessengerModule.REGISTRY.getIds()) {
            registry.register(id, MessengerModule.REGISTRY.get(id));
        }

        ConfigSection pm = config.getSection("plugin_message_broker");
        if(pm.getBoolean("enabled")) {

            ServerPluginMessageModule mod = data.getModuleManager().getModule(ServerPluginMessageModule.class);
            if(mod == null) {
                MidnightCoreAPI.LOGGER.error("Unable to enable plugin message messenger! Plugin message module is unloaded!");
            } else {
                Path keyPath;
                if(pm.getOrDefault("encrypt", false)) {
                    keyPath = data.getConfigDirectory().resolve("MidnightCore").resolve("messenger.key");
                } else {
                    keyPath = null;
                }
                broker = new ServerPluginMessageBroker(data, keyPath, mod);

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

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(ServerMessengerModule::new, ID, DEFAULT_CONFIG);

}
