package org.wallentines.midnightcore.common.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

public final class MojangUtil {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    public static UUID getUUID(String playerName) {

        try {
            URL url = new URL(String.format(UUID_URL, playerName));

            String response = makeHttpRequest(url);
            if(response == null) return null;

            ConfigSection sec = JsonConfigProvider.INSTANCE.loadFromString(response);

            String id = sec.getString("id");
            id = id.substring(0,8) + "-" + id.substring(8,12) + "-" + id.substring(12,16) + "-" + id.substring(16,20) + "-" + id.substring(20,32);

            return UUID.fromString(id);

        } catch(IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Skin getSkin(UUID playerId) {

        if(playerId == null) return null;

        try {

            URL url = new URL(String.format(SKIN_URL, playerId.toString().replace("-", "")));

            String response = makeHttpRequest(url);
            if(response == null) return null;

            ConfigSection sec = JsonConfigProvider.INSTANCE.loadFromString(response);

            if(!sec.has("properties", Collection.class)) return null;

            for(ConfigSection property : sec.getListFiltered("properties", ConfigSection.class)) {

                if(!property.has("name") || !property.getString("name").equals("textures")) continue;

                String value = property.getString("value");
                String signature = property.getString("signature");

                return new Skin(playerId, value, signature);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }


    public static Skin getSkinFromProfile(GameProfile profile) {

        PropertyMap map = profile.getProperties();

        if(map == null || !map.containsKey("textures") || map.get("textures").size() == 0) return null;

        Property skin = map.get("textures").iterator().next();

        return new Skin(profile.getId(), skin.getValue(), skin.getSignature());
    }

    public static void setSkinOfProfile(GameProfile profile, Skin skin) {

        profile.getProperties().get("textures").clear();
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
    }


    private static String makeHttpRequest(URL url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        if(conn.getResponseCode() != 200) return null;
        InputStream responseStream = conn.getInputStream();

        StringBuilder response = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));

        String line;
        while((line = reader.readLine()) != null) {
            response.append(line);
        }

        return response.toString();
    }

}
