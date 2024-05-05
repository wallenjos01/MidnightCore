package org.wallentines.mcore.adapter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.CustomScoreboard;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.types.Singleton;

import java.util.stream.Stream;

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
     * Creates a new ItemStack using the given ID, count, and data
     * @param id The item's ID
     * @param count The item's count
     * @param data The item's data value
     * @return A new ItemStack
     */
    ItemStack buildItem(Identifier id, int count, byte data);

    /**
     * Gets the type ID of the given item stack
     * @param stack The item to look up
     * @return The item's ID
     */
    Identifier getItemId(ItemStack stack);

    /**
     * Changes the NBT tag on an Item Stack
     * @param itemStack The item stack to update
     * @param tag The tag to set
     */
    default void setTag(ItemStack itemStack, ConfigSection tag) { }

    /**
     * Retrieves the NBT tag of an Item Stack
     * @param itemStack The item stack to lookup
     * @return The item stack's tag
     */
    default @Nullable ConfigSection getTag(ItemStack itemStack) {
        return null;
    }

    /**
     * Saves the structured component on the given ItemStack with the given ID
     * @param is The ItemStack to lookup
     * @param component The component to save
     * @return A ConfigObject, or null if the ItemStack does not have a component with that ID
     */
    default @Nullable ConfigObject saveComponent(ItemStack is, Identifier component) {
        return null;
    }

    /**
     * Loads a structured component with the given ID and configuration onto the ItemStack
     * @param is The ItemStack to load a component for
     * @param component The component ID
     * @param value The component configuration
     */
    default void loadComponent(ItemStack is, Identifier component, ConfigObject value) { }

    /**
     * Removes the structured component with the given ID from the ItemStack
     * @param is The ItemStack to load
     * @param component The ID of the component to remove
     */
    default void removeComponent(ItemStack is, Identifier component) { }

    /**
     * Gets identifiers of all the components on the given ItemStack. On pre-1.20.5 servers, this will contain only
     * the custom data component id
     * @param is The ItemStack to lookup
     * @return A stream of component ids
     */
    default Stream<Identifier> getComponentIds(ItemStack is) {
        if(getTag(is) != null) {
            return Stream.of(org.wallentines.mcore.ItemStack.CUSTOM_DATA_COMPONENT);
        }
        return Stream.empty();
    }

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
     * Gets the translation key for the given ItemStack's item type
     * @param itemStack The item to lookup
     * @return A translation key (i.e. "item.minecraft.apple")
     */
    default String getTranslationKey(ItemStack itemStack) {

        return itemStack.getTranslationKey();
    }

    /**
     * Gets the color associates with the given ItemStack's item rarity
     * @param itemStack The ItemStack to lookup
     * @return A Color
     */
    Color getRarityColor(ItemStack itemStack);


    default void setObjectiveName(Objective objective, Component component) {
        objective.setDisplayName(component.toLegacyText());
    }

    default void setTeamPrefix(Team team, Component component) {
        team.setPrefix(component.toLegacyText());
    }

    default void setNumberFormat(Objective objective, CustomScoreboard.NumberFormat fmt) { }
    default void setNumberFormat(Objective objective, CustomScoreboard.NumberFormat fmt, String playerName) { }


    /**
     * Contains the registered singleton for the given version. Populated as soon as the plugin is loaded
     */
    Singleton<Adapter> INSTANCE = new Singleton<>();

}
