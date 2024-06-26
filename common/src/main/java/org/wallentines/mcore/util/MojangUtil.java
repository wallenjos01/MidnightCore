package org.wallentines.mcore.util;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Skin;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


/**
 * A utility class for interacting with Mojang's web APIs
 */
public class MojangUtil {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    /**
     * Retrieves a player UUID by their username.
     * @param playerName The username to look up.
     * @return The UUID of the player, or null
     */
    public static UUID getUUID(String playerName) {

        try {
            URL url = new URI(String.format(UUID_URL, playerName)).toURL();

            ConfigSection sec = makeHttpRequest(url);
            if(sec == null) {
                return null;
            }

            String id = sec.getString("id");
            id = id.substring(0,8) + "-" + id.substring(8,12) + "-" + id.substring(12,16) + "-" + id.substring(16,20) + "-" + id.substring(20,32);

            return UUID.fromString(id);

        } catch(IOException | URISyntaxException | IllegalArgumentException ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while looking a player's UUID!", ex);
        }

        return null;
    }

    /**
     * Retrieves a player UUID by their username, asynchronously
     * @param playerName The username to look up.
     * @return The UUID of the player, or null
     */
    public static CompletableFuture<UUID> getUUIDAsync(String playerName) {

        return CompletableFuture.supplyAsync(() -> getUUID(playerName));
    }

    /**
     * Retrieves a player's profile data, including username and skin, by their UUID.
     * @param playerId The uuid to look up.
     * @return The current username of the player, or null
     */
    public static PlayerData getPlayerData(UUID playerId) {

        try {
            URL url = new URI(String.format(SKIN_URL, playerId.toString().replace("-", ""))).toURL();
            ConfigSection sec = makeHttpRequest(url);
            if(sec == null) {
                return new PlayerData(playerId, null, null);
            }

            String username = sec.getOrDefault("name", (String) null);
            Skin skin = null;

            if(sec.hasList("properties")) {
                for (ConfigSection property : sec.getListFiltered("properties", ConfigSection.SERIALIZER)) {

                    if (!property.has("name") || !property.getString("name").equals("textures")) continue;

                    String value = property.getString("value");
                    String signature = property.getString("signature");

                    skin = new Skin(playerId, value, signature);
                }
            }

            return new PlayerData(playerId, username, skin);

        } catch(IOException | URISyntaxException | IllegalArgumentException ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while looking a player's UUID!", ex);
        }

        return new PlayerData(playerId, null, null);
    }

    /**
     * Retrieves a player username by their UUID.
     * @param playerId The uuid to look up.
     * @return The current username of the player, or null
     */
    public static String getUsername(UUID playerId) {

        if(playerId == null) return null;
        return getPlayerData(playerId).name;
    }



    /**
     * Retrieves a player username by their UUID.
     * @param playerId The uuid to look up.
     * @return The current username of the player, or null
     */
    public static CompletableFuture<String> getUsernameAsync(UUID playerId) {

        return CompletableFuture.supplyAsync(() -> getUsername(playerId));
    }

    /**
     * Retrieves a player Skin by their UUID, asynchronously
     * @param playerId The UUID to look up.
     * @return The Skin of the player, or null
     */
    public static Skin getSkin(UUID playerId) {

        if(playerId == null) return null;
        return getPlayerData(playerId).skin;
    }

    /**
     * Retrieves a player Skin by their UUID, asynchronously
     * @param playerId The UUID to look up.
     * @return The Skin of the player, or null
     */
    public static CompletableFuture<Skin> getSkinAsync(UUID playerId) {

        return CompletableFuture.supplyAsync(() -> getSkin(playerId));
    }

    /**
     * Retrieves a player Skin by their username, asynchronously
     * @param name The username to look up.
     * @return The Skin of the player, or null
     */
    public static CompletableFuture<Skin> getSkinByNameAsync(String name) {

        return CompletableFuture.supplyAsync(() -> getSkin(getUUID(name)));
    }

    private static ConfigSection makeHttpRequest(URL url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        if(conn.getResponseCode() != 200) {
            MidnightCoreAPI.LOGGER.warn("Received invalid response from " + url);
            return null;
        }

        InputStream responseStream = conn.getInputStream();
        ConfigObject obj;
        try {
            obj = JSONCodec.minified().decode(ConfigContext.INSTANCE, responseStream);
        } catch (DecodeException ex) {

            MidnightCoreAPI.LOGGER.warn("Unable to parse response from " + url + "! " + ex.getMessage());
            return new ConfigSection();
        }
        if(!obj.isSection()) return new ConfigSection();

        ConfigSection out = obj.asSection();

        responseStream.close();
        conn.disconnect();

        return out;
    }

    public static class PlayerData {

        public final UUID uuid;
        public final String name;
        public final Skin skin;

        public PlayerData(UUID uuid, String name, Skin skin) {
            this.uuid = uuid;
            this.name = name;
            this.skin = skin;
        }

    }

}
