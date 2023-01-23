package org.wallentines.midnightcore.api.server;

import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

@SuppressWarnings("unused")
public interface MServer {

    /**
     * Submits a function to be run synchronously on the server thread on the next tick
     * @param runnable The function to run
     */
    void submit(Runnable runnable);

    /**
     * Determines whether this server is a local (integrated) server or not
     * @return Whether this server is a local server
     */
    boolean isLocalServer();

    /**
     * Determines whether this server is a proxy server (Bungee, Velocity)
     * Proxy servers do not have access to some functions in this class.
     * @return Whether this server is a proxy server
     */
    boolean isProxy();

    /**
     * Executes a command as the server console
     * @param command The command to run
     * @param quiet Whether output should be logged to the console
     */
    void executeCommand(String command, boolean quiet);

    /**
     * Retrieves the player manager for this server
     * @return The player manager
     */
    PlayerManager getPlayerManager();

    /**
     * Gets the player with the given UUID
     * @param uuid The UUID of the player to lookup
     * @return The player with the given UUID
     */
    default MPlayer getPlayer(UUID uuid) {
        return getPlayerManager().getPlayer(uuid);
    }

    /**
     * Creates a new Inventory GUI with the given title
     * @param title The title of the new Inventory GUI
     * @return A new inventory GUI
     */
    InventoryGUI createInventoryGUI(MComponent title);

    /**
     * Creates a custom scoreboard object which can be manipulated freely
     * @param id The internal ID of the scoreboard.
     * @param title The title which appears at the top at the scoreboard
     * @return A new custom scoreboard
     */
    CustomScoreboard createScoreboard(String id, MComponent title);

    /**
     * Creates a new ItemStack with the given type, amount, and NBT tag
     * @param typeId The type of item
     * @param count The size of the item stack
     * @param tag The item NBT
     * @return A new ItemStack
     */
    MItemStack createItemStack(Identifier typeId, int count, ConfigSection tag);


    /**
     * Retrieves an active server module with the given class
     * @param clazz The clazz of the module to look up
     * @return A module with the given class
     * @param <T> The type of module
     */
    <T extends ServerModule> T getModule(Class<T> clazz);

    /**
     * Retrieves the server's module manager
     * @return The server's module manager
     */
    ModuleManager<MServer, ServerModule> getModuleManager();

}
