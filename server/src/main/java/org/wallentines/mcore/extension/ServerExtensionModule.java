package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.wallentines.mcore.*;
import org.wallentines.mcore.lang.CustomPlaceholder;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.pluginmsg.ServerPluginMessageModule;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A module for loading optional extensions and declaring them to clients who join the server
 */
public class ServerExtensionModule implements ServerModule {

    private final ModuleManager<ServerExtensionModule, ServerExtension> manager = new ModuleManager<>(ServerExtension.REGISTRY, this);
    private final HashMap<UUID, Map<Identifier, Version>> enabledExtensions = new HashMap<>();
    private Set<Identifier> requiredExtensions = Set.of();
    private ServerPluginMessageModule smm;
    private ClientboundExtensionPacket cachedPacket;
    private Server server;


    @Override
    public boolean initialize(ConfigSection section, Server data) {

        this.server = data;

        ServerPluginMessageModule mod = data.getModuleManager().getModule(ServerPluginMessageModule.class);
        if(mod == null) {
            MidnightCoreAPI.LOGGER.warn("Unable to initialize extension module! No valid messaging module was found!");
            return false;
        }

        this.smm = mod;
        requiredExtensions = Set.copyOf(section.getListFiltered("required_extensions", Identifier.serializer(MidnightCoreAPI.MOD_ID)));

        manager.loadAll(section.getSection("extensions"));
        this.cachedPacket = new ClientboundExtensionPacket(manager.getLoadedModuleIds());

        mod.registerPacketHandler(ServerboundExtensionPacket.ID, (player, buffer) -> {
            UnresolvedComponent kick = handleResponse(player.getUUID(), player.getUsername(), buffer);
            if(kick != null) {
                player.kick(kick);
            }
        });

        // If the server does not support login query or the delay option is enabled, packets will be sent during the
        // play state, right when a player joins the game
        if (!mod.supportsLoginQuery() || section.getBoolean("delay_send")) {

            server.joinEvent().register(this, pl -> smm.sendPacket(pl, cachedPacket));
        } else {
            mod.registerLoginPacketHandler(ServerboundExtensionPacket.ID, (negotiator, buffer) -> {
                UnresolvedComponent kick = handleResponse(negotiator.getPlayerUUID(), negotiator.getPlayerName(), buffer);
                if(kick != null) {
                    negotiator.kick(kick.resolve());
                }
            });
            mod.onLogin.register(this, ln -> ln.sendPacket(cachedPacket));
        }

        // Since modules can be loaded after startup, all online players (if any) should be queried as soon as the module loads
        server.getPlayers().forEach(pl -> smm.sendPacket(pl, cachedPacket));

        return true;
    }

    /**
     * Checks if a player has the given extension
     * @param player The player to query
     * @param id The extension ID
     * @return Whether the player has that extension
     */
    public boolean hasExtension(Player player, Identifier id) {
        return enabledExtensions.containsKey(player.getUUID()) && enabledExtensions.get(player.getUUID()).containsKey(id);
    }

    /**
     * Gets the version of an extension the player has
     * @param player The player to query
     * @param id The extension ID
     * @return The version of the specified extension, or null
     */
    public Version getExtensionVersion(Player player, Identifier id) {
        if(!hasExtension(player, id)) return null;
        return enabledExtensions.get(player.getUUID()).get(id);
    }


    private UnresolvedComponent getKickMessage() {
        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        return mcore.getLangManager().component("kick.missing_extensions", CustomPlaceholder.of("entries", (ctx) -> {
            List<UnresolvedComponent> entries = requiredExtensions
                    .stream()
                    .map(id -> mcore.getLangManager().component("kick.missing_extensions.entry", CustomPlaceholder.inline("extension_id", id.toString())))
                    .toList();

            if(entries.isEmpty()) return Component.empty();
            Component out = entries.get(0).resolve(ctx);
            out.addChildren(entries.stream().skip(1).map(uc -> uc.resolve(ctx)).toList());
            return out;
        }));
    }

    private UnresolvedComponent handleResponse(UUID playerId, String username, ByteBuf response) {

        if(response == null || response.readableBytes() == 0) {
            MidnightCoreAPI.LOGGER.info("Player " + username + " ignored extension packet");
            return null;
        }

        try {

            ServerboundExtensionPacket packet = ServerboundExtensionPacket.read(response);

            Map<Identifier, Version> versions = packet.getExtensions().entrySet().stream().filter(e -> manager.isModuleLoaded(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if(!versions.keySet().containsAll(requiredExtensions)) {
                return getKickMessage();
            }

            enabledExtensions.put(playerId, versions);

            MidnightCoreAPI.LOGGER.info("Player " + username + " logged in with " + versions.size() + " enabled extensions");
            return null;

        } catch (DecoderException ex) {

            MidnightCoreAPI.LOGGER.warn("Player " + username + " sent invalid extension packet! " + ex.getMessage());

            MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
            return mcore.getLangManager().component("kick.invalid_extensions");
        }
    }

    /**
     * Returns the server which loaded this extension
     * @return The running server
     */
    public Server getServer() {
        return server;
    }


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extension");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("delay_send", false)
            .with("extensions", new ConfigSection())
            .with("required_extensions", new ConfigList());


    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(ServerExtensionModule::new, ID, DEFAULT_CONFIG).dependsOn(ServerPluginMessageModule.ID);




}
