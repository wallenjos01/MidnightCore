package org.wallentines.mcore.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.wallentines.mcore.Skin;

public class AuthUtil {

    /**
     * Retrieves a player's skin from their GameProfile
     * @param profile The profile to look into
     * @return The skin in that game profile
     */
    public static Skin skinFromGameProfile(GameProfile profile) {

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
    public static void setGameProfileSkin(GameProfile profile, Skin skin) {

        profile.getProperties().get("textures").clear();
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
    }

}
