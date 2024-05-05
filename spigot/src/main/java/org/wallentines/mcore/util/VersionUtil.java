package org.wallentines.mcore.util;

import org.bukkit.Bukkit;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.adapter.Adapters;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.io.IOException;
import java.io.InputStream;

public class VersionUtil {



    public static GameVersion findVersion() {

        // Read from version.json
        InputStream is = VersionUtil.class.getResourceAsStream("/version.json");
        if (is != null) {
            try {
                ConfigSection sec = JSONCodec.loadConfig(is).asSection();
                return new GameVersion(sec.getString("id"), sec.getInt("protocol_version"));

            } catch (IOException ex) {
                MidnightCoreAPI.LOGGER.warn("Unable to read version info from version.json!", ex);
            }
        }

        // No version.json, try api version
        String[] packageParts = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
        if(packageParts.length < 4) {
            MidnightCoreAPI.LOGGER.warn("Unable to find game version!");
            return null;
        }

        String apiVersion = packageParts[3];
        return Adapters.estimateVersion(apiVersion);

    }



}
