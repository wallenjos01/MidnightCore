package org.wallentines.midnightcore.fabric.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.module.extension.Extension;
import org.wallentines.midnightcore.fabric.module.extension.ExtensionModule;
import org.wallentines.midnightlib.config.ConfigSection;
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

        negotiator = new ClientNegotiator(this);
        negotiator.registerHandler(ExtensionModule.SUPPORTED_EXTENSION_PACKET, buf -> {

            // Read server extensions
            int serverExtensions = buf.readVarInt();

            List<String> ids = new ArrayList<>();

            for(int i = 0 ; i < serverExtensions ; i++) {

                String id = buf.readUtf();
                if(getLoadedExtensionIds().contains(Identifier.parse(id))) {

                    ids.add(id);
                }
            }

            // Respond with supported server extensions
            FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
            out.writeVarInt(ids.size());

            for (String s : ids) {
                out.writeUtf(s);
            }

            return out;
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
    public boolean isClient() {
        return true;
    }

    public ClientNegotiator getNegotiator() {
        return negotiator;
    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(ClientExtensionModule::new, ID, new ConfigSection().with("extensions", new ConfigSection()));
}
