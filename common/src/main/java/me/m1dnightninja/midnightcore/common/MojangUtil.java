package me.m1dnightninja.midnightcore.common;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public final class MojangUtil {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    private static final Gson GSON = new GsonBuilder().create();

    public static UUID getUUID(String playerName) {

        try {
            URL url = new URL(String.format(UUID_URL, playerName));

            String response = makeHttpRequest(url);
            if(response == null) return null;

            JsonElement ele = GSON.fromJson(response, JsonElement.class);
            if(!ele.isJsonObject()) return null;

            JsonObject obj = ele.getAsJsonObject();
            if(!obj.has("id")) return null;

            String id = obj.get("id").getAsString();
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

            JsonElement ele = GSON.fromJson(response, JsonElement.class);
            if(!ele.isJsonObject()) return null;

            JsonObject obj = ele.getAsJsonObject();
            if(!obj.has("properties") || !obj.get("properties").isJsonArray()) return null;

            JsonArray properties = obj.getAsJsonArray("properties");
            for(JsonElement property : properties) {
                if(!property.isJsonObject()) continue;

                JsonObject prop = property.getAsJsonObject();
                if(!prop.has("name") || !prop.get("name").getAsString().equals("textures")) continue;

                String value = prop.get("value").getAsString();
                String signature = prop.get("signature").getAsString();

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
