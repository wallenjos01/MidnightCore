package org.wallentines.mcore;


import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.skin.SkinModule;
import org.wallentines.mcore.skin.Skinnable;
import org.wallentines.mcore.text.Component;

/**
 * An interface representing a Server-side player object.
 * <br/>
 * Warning: You cannot take for granted that a Player will remain valid for the duration of the time they are on the
 * server. Player objects are recreated by the server whenever the player dies and respawns. When persistence is
 * required, store by UUID or look into {@link WrappedPlayer WrappedPlayer}
 */
public interface Player extends Entity, Skinnable {

    /**
     * Gets the player's username associated with the GameProfile they signed in with
     * @return The player's username
     */
    String getUsername();

    /**
     * Sends the user a system message
     * @param component The message to send
     */
    void sendMessage(Component component);

    /**
     * Sends the user an action bar message
     * @param component The message to send
     */
    void sendActionBar(Component component);

    /**
     * Sends a title to the player
     * @param title The title to send
     */
    void sendTitle(Component title);

    /**
     * Sends a subtitle to the player
     * @param title The title to send
     */
    void sendSubtitle(Component title);

    /**
     * Clears the titles for the player
     */
    void clearTitles();

    /**
     * Changes the title timings for the player
     * @param fadeIn How many ticks it should take for a title to fade in
     * @param stay How many ticks the title should stay on screen
     * @param fadeOut How many ticks it should take for a title to fade out
     */
    void setTitleTimes(int fadeIn, int stay, int fadeOut);

    /**
     * Clears the titles for the player and resets timings to default
     */
    void resetTitles();

    /**
     * Retrieves the item in the player's main hand
     * @return The item in the player's main hand
     */
    ItemStack getHandItem();

    /**
     * Retrieves the item in the player's offhand
     * @return The item in the player's offhand
     */
    ItemStack getOffhandItem();

    /**
     * Gives an item to the player
     * @param item The item to give
     */
    void giveItem(ItemStack item);

    /**
     * Gets the player's game language (Only available on 1.12+, will otherwise default to en_us)
     * @return The player's game language
     */
    String getLanguage();

    /**
     * Returns the player's game mode
     * @return The player's game mode
     */
    GameMode getGameMode();

    /**
     * Changes the player's game mode
     * @param mode The new game mode
     */
    void setGameMode(GameMode mode);

    /**
     * Determines if the player is still connected to the server
     * @return Whether the player is online
     */
    boolean isOnline();

    /**
     * Creates a new WrappedPlayer from this Player
     * @return A new WrappedPlayer
     */
    default WrappedPlayer wrap() {
        return new WrappedPlayer(this);
    }

    /**
     * Changes a player's skin. Requires the skin module to be loaded!
     * @param skin The player's new skin
     */
    @Override
    default void setSkin(Skin skin) {

        getServer().getModuleManager().getModule(SkinModule.class).setSkin(this, skin);
    }

    static void registerPlaceholders(PlaceholderManager manager) {

        manager.registerSupplier("player_uuid", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Player.class, pl -> pl.getUUID().toString(), "")));
        manager.registerSupplier("player_username", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Player.class, Player::getUsername, "")));
        manager.registerSupplier("player_name", PlaceholderSupplier.of(ctx -> ctx.onValueOr(Player.class, Entity::getDisplayName, Component.empty())));

    }

}
