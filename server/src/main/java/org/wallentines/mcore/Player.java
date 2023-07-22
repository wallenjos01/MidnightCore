package org.wallentines.mcore;


import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.text.Component;

/**
 * An interface representing a Server-side player object
 */
public interface Player extends Entity {

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


    static void registerPlaceholders(PlaceholderManager manager) {

        manager.registerSupplier("player_uuid", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Player.class, pl -> pl.getUUID().toString(), "")));
        manager.registerSupplier("player_username", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Player.class, Player::getUsername, "")));
        manager.registerSupplier("player_name", PlaceholderSupplier.of(ctx -> ctx.onValueOr(Player.class, Player::getDisplayName, Component.empty())));

    }

}
