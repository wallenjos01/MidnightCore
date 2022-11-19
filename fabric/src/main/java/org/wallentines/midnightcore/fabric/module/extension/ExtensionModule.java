package org.wallentines.midnightcore.fabric.module.extension;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.fabric.event.server.ServerBeginQueryEvent;
import org.wallentines.midnightcore.fabric.event.world.BlockBreakEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.util.*;

public class ExtensionModule implements Module<MidnightCoreAPI> {

    public static final Registry<ModuleInfo<ExtensionModule>> SUPPORTED_EXTENSIONS = new Registry<>();
    public static final ResourceLocation SUPPORTED_EXTENSION_PACKET = new ResourceLocation(Constants.DEFAULT_NAMESPACE, "supported_extensions");

    private final ModuleManager<ExtensionModule> manager = new ModuleManager<>();
    private final HashMap<UUID, Set<Identifier>> enabledExtensions = new HashMap<>();

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

                    Set<Identifier> ids = new HashSet<>();

                    int count = res.readVarInt();
                    for(int i = 0 ; i < count ; i++) {
                        ids.add(Identifier.parseOrDefault(res.readUtf(), Constants.DEFAULT_NAMESPACE));
                    }

                    enabledExtensions.put(ev.getProfile().getId(), ids);

                    StringBuilder bld = new StringBuilder("Enabled Extensions for ").append(ev.getProfile().getId()).append(": ");
                    for(Identifier id : enabledExtensions.get(ev.getProfile().getId())) {
                        bld.append(id).append(", ");
                    }

                    MidnightCoreAPI.getLogger().info(bld.toString());
                }
            });
        });

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientNegotiator.register(this);
        }

        return true;
    }

    public boolean isExtensionSupported(MPlayer player, Identifier id) {
        return enabledExtensions.containsKey(player.getUUID()) && enabledExtensions.get(player.getUUID()).contains(id);
    }

    public <T extends Module<ExtensionModule>> boolean isExtensionSupported(MPlayer player, Class<T> ext) {

        Identifier id = manager.getModuleId(ext);
        if(id == null) return false;

        return isExtensionSupported(player, id);
    }

    public <T extends Module<ExtensionModule>> T getExtension(Class<T> clazz) {

        return manager.getModule(clazz);
    }

    public Collection<Identifier> getLoadedExtensionIds() {

        return manager.getLoadedModuleIds();
    }

    @Override
    public void disable() {
        manager.unloadAll();
    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "extension");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(ExtensionModule::new, ID, new ConfigSection().with("extensions", new ConfigSection()));

    private static class DummyExtension implements Module<ExtensionModule> {
        @Override
        public boolean initialize(ConfigSection section, ExtensionModule data) {

            Event.register(BlockBreakEvent.class, this, ev -> {

                MPlayer mpl = FabricPlayer.wrap(ev.getPlayer());
                if(data.isExtensionSupported(mpl, getClass())) {
                    mpl.sendMessage(new MTextComponent("dummy component enabled"));
                }

            });

            return true;
        }
    }

    static {
        final Identifier DUMMY_ID = new Identifier(Constants.DEFAULT_NAMESPACE, "dummy");
        SUPPORTED_EXTENSIONS.register(DUMMY_ID, new ModuleInfo<>(DummyExtension::new, DUMMY_ID, new ConfigSection()));
    }

}
