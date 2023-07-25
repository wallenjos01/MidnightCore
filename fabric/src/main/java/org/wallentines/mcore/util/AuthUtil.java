package org.wallentines.mcore.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.mcore.Skin;

public class AuthUtil {

    /**
     * Retrieves a player's skin from their GameProfile
     * @param profile The profile to look into
     * @return The skin in that game profile
     */
    public static Skin getProfileSkin(GameProfile profile) {

        PropertyMap map = profile.getProperties();
        if(map == null || !map.containsKey("textures") || map.get("textures").size() == 0) return null;

        Property skin = map.get("textures").iterator().next();
        return new Skin(profile.getId(), skin.getValue(), skin.getSignature());

    }

    /**
     * Changes a skin in a GameProfile
     * @param profile The profile to change
     * @param skin The skin to put into the profile
     */
    public static GameProfile setProfileSkin(GameProfile profile, Skin skin) {

        profile.getProperties().get("textures").clear();
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        return profile;
    }

    /**
     * Makes an exact copy of an existing GameProfile
     * @param other The GameProfile to copy
     * @return A copy of the given profile
     */
    public static GameProfile copyProfile(GameProfile other) {

        GameProfile profile = new GameProfile(other.getId(), other.getName());

        for(String key : other.getProperties().keys()) {
            profile.getProperties().putAll(key, other.getProperties().get(key));
        }

        return profile;
    }

    /**
     * Creates a new game profile with the given player's name and UUID, and the given skin's texture
     * @param spl The player to generate a profile for
     * @param skin The skin texture to use
     * @return A new game profile
     */
    public static GameProfile forPlayer(ServerPlayer spl, Skin skin) {

        return setProfileSkin(copyProfile(spl.getGameProfile()), skin);
    }

}
