package org.wallentines.midnightcore.fabric.module.extension;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.fabric.event.server.ServerBeginQueryEvent;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;
import java.util.function.Consumer;

public class ServerExtensionModule implements ExtensionModule {

    private final HashMap<Module<ExtensionModule>, Consumer<ServerNegotiator>> queries = new HashMap<>();
    private final ModuleManager<ExtensionModule, Extension> manager = new ModuleManager<>();
    private final HashMap<UUID, HashMap<Identifier, Version>> enabledExtensions = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        // Load Extensions
        manager.loadAll(section.getSection("extensions"), this, SUPPORTED_EXTENSIONS);

        FriendlyByteBuf supportedData = new FriendlyByteBuf(Unpooled.buffer());

        supportedData.writeVarInt(manager.getCount());
        for(Identifier id : manager.getLoadedModuleIds()) {
            supportedData.writeUtf(id.toString());
        }

        // Events
        Event.register(ServerBeginQueryEvent.class, this, ev -> {

            ev.getNegotiator().sendPacket(SUPPORTED_EXTENSION_PACKET, supportedData, res -> {

                if(res == null) {

                    MidnightCoreAPI.getLogger().info("Player " + ev.getProfile().getId() + " Ignored Extensions Packet");

                } else {

                    HashMap<Identifier, Version> ids = new HashMap<>();

                    int count = res.readVarInt();
                    for(int i = 0 ; i < count ; i++) {
                        ids.put(Identifier.parseOrDefault(res.readUtf(), Constants.DEFAULT_NAMESPACE), Version.SERIALIZER.deserialize(res.readUtf()));
                    }

                    enabledExtensions.put(ev.getProfile().getId(), ids);

                    StringBuilder bld = new StringBuilder("Enabled Extensions for ").append(ev.getProfile().getId()).append(": ");

                    int i = 0;
                    for(Map.Entry<Identifier, Version> id : ids.entrySet()) {
                        if(i > 0) {
                            bld.append(", ");
                        }
                        bld.append(id.getKey()).append(": ").append(id.getValue());
                        i++;
                    }

                    MidnightCoreAPI.getLogger().info(bld.toString());

                    for(Identifier id : manager.getLoadedModuleIds()) {
                        if(!ids.containsKey(id)) continue;

                        Module<ExtensionModule> mod = manager.getModuleById(id);
                        if(queries.containsKey(mod)) queries.get(mod).accept(ev.getNegotiator());
                    }
                }
            });


        });

        return true;
    }

    @Override
    public <T extends Extension> T getExtension(Class<T> clazz) {

        return manager.getModule(clazz);
    }

    @Override
    public Collection<Identifier> getLoadedExtensionIds() {

        return manager.getLoadedModuleIds();
    }

    @Override
    public void disable() {
        manager.unloadAll();
    }

    @Override
    public boolean isClient() {
        return false;
    }

    public boolean playerHasExtension(MPlayer player, Identifier id) {
        return enabledExtensions.containsKey(player.getUUID()) && enabledExtensions.get(player.getUUID()).containsKey(id);
    }

    public Version getExtensionVersion(MPlayer player, Identifier id) {
        return playerHasExtension(player, id) ? enabledExtensions.get(player.getUUID()).get(id) : null;
    }


    public void registerQuery(Module<ExtensionModule> mod, Consumer<ServerNegotiator> negotiator) {
        queries.put(mod, negotiator);
    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(ServerExtensionModule::new, ID, new ConfigSection().with("extensions", new ConfigSection()));

}
