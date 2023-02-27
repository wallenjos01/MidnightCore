package org.wallentines.midnightcore.common.module.extension;

import io.netty.buffer.ByteBuf;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.api.module.extension.ServerExtension;
import org.wallentines.midnightcore.api.module.extension.ServerExtensionModule;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractServerExtensionModule implements ServerExtensionModule {


    protected final ModuleManager<ServerExtensionModule, ServerExtension> manager = new ModuleManager<>();
    protected final HashMap<UUID, HashMap<Identifier, Version>> enabledExtensions = new HashMap<>();

    private MessagingModule messagingModule;
    private ByteBuf supportedPacket;
    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        // Load Extensions
        manager.loadAll(section.getSection("extensions"), this, ServerExtension.REGISTRY);

        supportedPacket = ExtensionHelper.createPacket(manager.getLoadedModuleIds());

        // Events
        messagingModule = data.getModule(MessagingModule.class);
        if(messagingModule == null) {
            return false;
        }

        messagingModule.registerHandler(ExtensionHelper.SUPPORTED_EXTENSION_PACKET, (mpl, res) ->
                enabledExtensions.put(mpl.getUUID(), ExtensionHelper.handleResponse(mpl.getUsername(), res)));

        registerEvents();

        return true;
    }

    @Override
    public <T extends ServerExtension> T getExtension(Class<T> clazz) {

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

    public boolean playerHasExtension(MPlayer player, Identifier id) {
        Map<Identifier, Version> extensions = enabledExtensions.get(player.getUUID());
        return extensions != null && extensions.containsKey(id);
    }

    public Version getExtensionVersion(MPlayer player, Identifier id) {
        return playerHasExtension(player, id) ? enabledExtensions.get(player.getUUID()).get(id) : null;
    }

    protected abstract void registerEvents();

    protected ByteBuf getSupportedExtensionsPacket() {
        return supportedPacket;
    }

    protected MessagingModule getMessagingModule() {
        return messagingModule;
    }

}
