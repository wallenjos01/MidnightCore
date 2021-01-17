package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.api.skin.Skin;
import me.m1dnightninja.midnightcore.api.skin.SkinCallback;

import java.util.UUID;

public interface ISkinModule extends IModule {

    /**
     * Retrieves the skin of a player from either the server's memory
     * or Mojang's servers. It is recommended to use the asynchronous
     * method if retrieving a skin of a player who is not logged in.
     *
     * @param uid  The UUID of the player whose skin will be retrieved
     * @return     The skin of the player, or null if invalid
     */
    Skin getSkin(UUID uid);


    /**
     * Calls getSkin() asynchronously
     *
     * @param uid       The UUID of the player whose skin will be retrieved
     * @param callback  The callback containing the retrieved skin, or null if invalid
     */
    void getSkinAsync(UUID uid, SkinCallback callback);


    /**
     * Retrieves a player's login skin from the server's memory, or
     * Mojang's servers if they are not logged in.
     *
     * @param uid  The UUID of the player whose login skin will be retrieved
     * @return     The original skin of the player, or null if invalid
     */
    Skin getOriginalSkin(UUID uid);


    /**
     * Calls getOriginalSkin() asynchronously
     *
     * @param uid       The UUID of the player whose login skin will be retrieved
     * @param callback  The callback containing the retrieved skin, or null if invalid
     */
    void getOriginalSkinAsync(UUID uid, SkinCallback callback);


    /**
     * Retrieves a player's skin from Mojang's servers
     *
     * @param uid  The UUID of the player whose skin will be retrieved
     * @return     The skin of the player, or null if invalid
     */
    Skin getOnlineSkin(UUID uid);


    /**
     * Calls getOnlineSkin() asynchronously
     *
     * @param uid       The UUID of the player whose skin will be retrieved
     * @param callback  The callback containing the retrieved skin, or null if invalid
     */
    void getOnlineSkinAsync(UUID uid, SkinCallback callback);


    /**
     * Changes a Player's skin
     *
     * @param uid   The UUID of the player whose skin will be changed
     * @param skin  The Skin to apply
     */
    void setSkin(UUID uid, Skin skin);


    /**
     * Resets a Player's skin to the one they logged in with
     *
     * @param uid  The UUID of the player who will be reset
     */
    void resetSkin(UUID uid);


    /**
     * Updates a Player's skin changes so they can be seen by themselves and others
     *
     * @param uid  The UUID of the player who will be updated
     */
    void updateSkin(UUID uid);

}
