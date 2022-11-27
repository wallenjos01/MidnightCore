package org.wallentines.midnightcore.fabric.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.client.ClientCustomMessageEvent;
import org.wallentines.midnightcore.fabric.module.extension.Extension;
import org.wallentines.midnightcore.fabric.module.extension.ExtensionModule;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientExtensionModule implements ExtensionModule {

    private ClientNegotiator negotiator;

    private final ModuleManager<ExtensionModule, Extension> manager = new ModuleManager<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        manager.loadAll(section.getSection("extensions"), this, ExtensionModule.SUPPORTED_EXTENSIONS);

        Event.register(ClientCustomMessageEvent.class, this, 90, ev -> {

            if(ev.getId().equals(ConversionUtil.toResourceLocation(ExtensionModule.SUPPORTED_EXTENSION_PACKET))) {
                ev.setHandled(true);
                ev.respond(respond(ev.getData()));
            }
        });

        negotiator = new ClientNegotiator(this);
        negotiator.registerHandler(ConversionUtil.toResourceLocation(ExtensionModule.SUPPORTED_EXTENSION_PACKET), this::respond);

        return true;
    }

    private FriendlyByteBuf respond(FriendlyByteBuf buf) {

        // Read server extensions
        int serverExtensions = buf.readVarInt();

        List<Identifier> ids = new ArrayList<>();

        for(int i = 0 ; i < serverExtensions ; i++) {

            Identifier id = Identifier.parse(buf.readUtf());

            if(getLoadedExtensionIds().contains(id)) {

                ids.add(id);
            }
        }

        // Respond with supported server extensions
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeVarInt(ids.size());

        for (Identifier s : ids) {
            out.writeUtf(s.toString());

            Version ver = manager.getModuleById(s).getVersion();
            out.writeUtf(ver.toString());
        }

        return out;
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
    public boolean isClient() {
        return true;
    }

    public ClientNegotiator getNegotiator() {
        return negotiator;
    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(ClientExtensionModule::new, ID, new ConfigSection().with("extensions", new ConfigSection()));
}
