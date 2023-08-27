package org.wallentines.mcore.adapter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.types.Singleton;

public interface Adapter {

    /**
     * Attempts to initialize the adapter for the current server
     * @return Whether initialization was successful
     */
    boolean initialize();

    /**
     * Runs a task on the server thread
     * @param runnable The code to run
     */
    void runOnServer(Runnable runnable);

    /**
     * Registers the specified function to be called each server tick
     * @param runnable The function to call
     */
    void addTickListener(Runnable runnable);

    /**
     * Gets the active skin of the given player
     * @param player The player to look up
     * @return The player's skin, or null if they don't have one
     */
    @Nullable
    Skin getPlayerSkin(Player player);

    /**
     * Gets a skin updater for the current Spigot version
     * @return A skin updater
     */
    SkinUpdater getSkinUpdater();

    /**
     * Sends a component to the player as a system chat message
     * @param player The player to send to
     * @param component The component to send
     */
    void sendMessage(Player player, Component component);

    /**
     * Sends a component to the player as a "game info" action bar message
     * @param player The player to send to
     * @param component The component to send
     */
    void sendActionBar(Player player, Component component);

    /**
     * Sends a component to the player as a title
     * @param player The player to send to
     * @param component The component to send
     */
    void sendTitle(Player player, Component component);

    /**
     * Sends a component to the player as a subtitle
     * @param player The player to send to
     * @param component The component to send
     */
    void sendSubtitle(Player player, Component component);

    /**
     * Changes the player's title animation timing
     * @param player The player to reset for
     * @param fadeIn The number of ticks which titles should take to fade in
     * @param stay The number of ticks which titles should stay on screen
     * @param fadeOut The number of ticks which titles should take to fade out
     */
    void setTitleAnimation(Player player, int fadeIn, int stay, int fadeOut);

    /**
     * Clears the title text for the player
     * @param player The player to clear titles for
     */
    void clearTitles(Player player);

    /**
     * Clears the title text for the player, and resets the animation timing
     * @param player The player to reset
     */
    void resetTitles(Player player);

    /**
     * Determines whether the player has the given operator level or higher
     * @param player The player to lookup
     * @param level The operator level
     * @return Whether the player has the given op level
     */
    boolean hasOpLevel(Player player, int level);

    /**
     * Reads the NBT Tag for the given player
     * @param player The player to read from
     */
    ConfigSection getTag(Player player);

    /**
     * Loads the given ConfigSection as an NBT tag for the given player
     * @param player The player to load
     * @param tag The tag to load
     */
    void loadTag(Player player, ConfigSection tag);

    /**
     * Changes the NBT tag on an Item Stack
     * @param itemStack The item stack to update
     * @param tag The tag to set
     */
    void setTag(ItemStack itemStack, ConfigSection tag);

    /**
     * Retrieves the NBT tag of an Item Stack
     * @param itemStack The item stack to lookup
     * @return The item stack's tag
     */
    ConfigSection getTag(ItemStack itemStack);

    /**
     * Ensures the given ItemStack is backed by a real Minecraft item
     * @param item The item to set up
     * @return An internally-backed item
     */
    ItemStack setupInternal(ItemStack item);


    /**
     * Retrieves the current version name and protocol version from the internal Minecraft server
     * @return The server's Minecraft version
     */
    GameVersion getGameVersion();


    /**
     * Kicks the given player from the server with the given message
     * @param player The player to kick
     * @param message The kick message to send
     */
    void kickPlayer(Player player, Component message);


    /**
     * Contains the registered singleton for the given version. Populated as soon as the plugin is loaded
     */
    Singleton<Adapter> INSTANCE = new Singleton<>();

}
