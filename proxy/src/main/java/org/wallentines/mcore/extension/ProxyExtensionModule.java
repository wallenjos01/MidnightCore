package org.wallentines.mcore.extension;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mcore.ProxyPlayer;
import org.wallentines.mcore.messaging.ProxyMessagingModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ProxyExtensionModule implements ProxyModule {

    private final HashMap<String, Map<Identifier, Version>> unmappedPlayerExtensions = new HashMap<>();
    private final HashMap<UUID, Map<Identifier, Version>> playerExtensions = new HashMap<>();

    private ClientboundExtensionPacket cachedPacket;

    @Override
    public boolean initialize(ConfigSection section, Proxy data) {

        ProxyMessagingModule mod = data.getModuleManager().getModule(ProxyMessagingModule.class);

        if(mod == null) {
            MidnightCoreAPI.LOGGER.warn("Unable to enable Extension module! The messaging module is required!");
            return false;
        }

        List<Identifier> ids = section.getListFiltered("query_extensions", Identifier.serializer(MidnightCoreAPI.MOD_ID));
        if(ids.isEmpty()) {
            MidnightCoreAPI.LOGGER.warn("Extension module disabled. No query extensions");
            return false;
        }

        cachedPacket = new ClientboundExtensionPacket(ids);

        mod.onLogin.register(this, ev -> {
            ev.sendMessage(cachedPacket, (negotiator, buf) -> {

                ServerboundExtensionPacket pck = ServerboundExtensionPacket.read(buf);
                unmappedPlayerExtensions.put(negotiator.getUsername(), pck.getExtensions());
            });
        });

        mod.registerLoginHandler(ServerboundExtensionPacket.ID, (player, buf) -> {
            if(playerExtensions.containsKey(player.getUUID())) {
                mod.sendServerMessage(player.getServer(), new ServerboundExtensionPacket(playerExtensions.get(player.getUUID())));
            }
        });

        return true;
    }

    /**
     * Checks if a player has the given extension
     * @param player The player to query
     * @param id The extension ID
     * @return Whether the player has that extension
     */
    public boolean hasExtension(ProxyPlayer player, Identifier id) {
        return playerExtensions.containsKey(player.getUUID()) && playerExtensions.get(player.getUUID()).containsKey(id);
    }

    /**
     * Gets the version of an extension the player has
     * @param player The player to query
     * @param id The extension ID
     * @return The version of the specified extension, or null
     */
    public Version getExtensionVersion(ProxyPlayer player, Identifier id) {
        if(!hasExtension(player, id)) return null;
        return playerExtensions.get(player.getUUID()).get(id);
    }

    /**
     * Should be called by the proxy as soon as a player object is available.
     * @param player The player who just logged in
     */
    protected void onFinishLogin(ProxyPlayer player) {
        String username = player.getUsername();
        if(unmappedPlayerExtensions.containsKey(username)) {
            playerExtensions.put(player.getUUID(), unmappedPlayerExtensions.get(username));
            unmappedPlayerExtensions.remove(username);
        }
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extension");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("query_extensions", new ConfigSection());

}
