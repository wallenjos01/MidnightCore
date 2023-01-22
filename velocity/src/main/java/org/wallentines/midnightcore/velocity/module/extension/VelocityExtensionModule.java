package org.wallentines.midnightcore.velocity.module.extension;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerLoginPluginMessageEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.api.module.extension.Extension;
import org.wallentines.midnightcore.common.module.extension.AbstractExtensionModule;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightcore.velocity.module.messaging.VelocityMessagingModule;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public class VelocityExtensionModule extends AbstractExtensionModule {

    private final HashMap<String, HashMap<Identifier, Version>> enabledExtensions = new HashMap<>();
    private final List<Identifier> supportedExtensions = new ArrayList<>();

    @Override
    public <T extends Extension> T getExtension(Class<T> clazz) {
        throw new IllegalStateException("The Velocity Extension module does not register its own extensions! It merely forwards extensions from client to servers!");
    }

    @Override
    public Collection<Identifier> getLoadedExtensionIds() {
        return supportedExtensions;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        MessagingModule mod = MidnightCoreAPI.getModule(MessagingModule.class);
        if(mod == null) return false;

        supportedExtensions.clear();
        supportedExtensions.addAll(section.getListFiltered("query_extensions", String.class).stream().map(str -> Identifier.parseOrDefault(str, Constants.DEFAULT_NAMESPACE)).toList());

        ByteBuf supportedData = createPacket(supportedExtensions);

        mod.addLoginListener(ln -> {
            ln.sendRawMessage(SUPPORTED_EXTENSION_PACKET, supportedData, res -> {
                enabledExtensions.put(ln.getPlayerUsername(), handleResponse(ln.getPlayerUsername(), res));
            });
        });

        MidnightCore.getInstance().getServer().getEventManager().register(MidnightCore.getInstance(), this);
        return true;
    }

    @Subscribe
    private void onMessage(ServerLoginPluginMessageEvent event) {

        if(event.getIdentifier().getId().equals(SUPPORTED_EXTENSION_PACKET.toString())) {

            ByteBuf data = Unpooled.wrappedBuffer(event.getContents());
            String username = event.getConnection().getPlayer().getUsername();

            if(enabledExtensions.get(username) == null) {

                event.setResult(ServerLoginPluginMessageEvent.ResponseResult.unknown());
                return;
            }

            event.setResult(ServerLoginPluginMessageEvent.ResponseResult.reply(createResponse(data, enabledExtensions.get(username).keySet(), id -> enabledExtensions.get(username).get(id)).array()));
        }
    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO =
            new ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>(VelocityExtensionModule::new, ID, DEFAULT_CONFIG.copy().with("query_extensions", new ArrayList<>()))
                    .dependsOn(VelocityMessagingModule.ID);
}
