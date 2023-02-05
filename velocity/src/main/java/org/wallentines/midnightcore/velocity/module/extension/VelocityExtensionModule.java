package org.wallentines.midnightcore.velocity.module.extension;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerLoginPluginMessageEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.extension.ServerExtensionModule;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.module.extension.ServerExtension;
import org.wallentines.midnightcore.common.module.extension.ExtensionHelper;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightcore.velocity.module.messaging.VelocityMessagingModule;
import org.wallentines.midnightlib.Version;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public class VelocityExtensionModule implements ServerExtensionModule {

    private final HashMap<String, HashMap<Identifier, Version>> enabledExtensions = new HashMap<>();
    private final List<Identifier> supportedExtensions = new ArrayList<>();

    @Override
    public <T extends ServerExtension> T getExtension(Class<T> clazz) {
        throw new IllegalStateException("The Velocity Extension module does not register its own extensions! It merely forwards extensions from client to servers!");
    }

    @Override
    public Collection<Identifier> getLoadedExtensionIds() {
        return supportedExtensions;
    }

    @Override
    public boolean playerHasExtension(MPlayer player, Identifier id) {

        String username = player.getUsername();
        return enabledExtensions.containsKey(username) && enabledExtensions.get(username).containsKey(id);
    }

    @Override
    public Version getExtensionVersion(MPlayer player, Identifier id) {

        String username = player.getUsername();
        return enabledExtensions.containsKey(username) ? enabledExtensions.get(username).get(id) : null;
    }

    @Override
    public boolean initialize(ConfigSection section, MServer server) {

        MessagingModule mod = server.getModule(MessagingModule.class);
        if(mod == null) return false;

        supportedExtensions.clear();
        supportedExtensions.addAll(section.getListFiltered("query_extensions", Serializer.STRING).stream().map(str ->
                Identifier.parseOrDefault(str, MidnightCoreAPI.DEFAULT_NAMESPACE)).toList());

        ByteBuf supportedData = ExtensionHelper.createPacket(supportedExtensions);

        mod.addLoginListener(ln ->
            ln.sendRawMessage(ExtensionHelper.SUPPORTED_EXTENSION_PACKET, supportedData, res ->
                enabledExtensions.put(
                        ln.getPlayerUsername(),
                        ExtensionHelper.handleResponse(ln.getPlayerUsername(), res)
                )
        ));

        MidnightCore.getInstance().getServer().getEventManager().register(MidnightCore.getInstance(), this);
        return true;
    }

    @Subscribe
    private void onMessage(ServerLoginPluginMessageEvent event) {

        if(event.getIdentifier().getId().equals(ExtensionHelper.SUPPORTED_EXTENSION_PACKET.toString())) {

            ByteBuf data = Unpooled.wrappedBuffer(event.getContents());
            String username = event.getConnection().getPlayer().getUsername();

            if(enabledExtensions.get(username) == null) {

                event.setResult(ServerLoginPluginMessageEvent.ResponseResult.unknown());
                return;
            }

            event.setResult(ServerLoginPluginMessageEvent.ResponseResult.reply(
                    ExtensionHelper.createResponse(
                            data,
                            enabledExtensions.get(username).keySet(),
                            id -> enabledExtensions.get(username).get(id)
                    ).array()));
        }
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO =
            new ModuleInfo<MServer, ServerModule>(
                    VelocityExtensionModule::new,
                    ExtensionHelper.ID,
                    DEFAULT_CONFIG.copy().with("query_extensions", new ConfigList())
            ).dependsOn(VelocityMessagingModule.ID);
}
