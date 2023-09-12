package org.wallentines.mcore.adapter.v1_13_R2;

import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.ServerPing;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.adapter.UncertainGameVersion;

public class VersionUtil {

    public static GameVersion getGameVersion() {

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        return new UncertainGameVersion<>(server.getVersion(), 401,
                () -> ((CraftServer) Bukkit.getServer()).getServer().getServerPing().getServerData(),
                ServerPing.ServerData::getProtocolVersion
        );
    }

}
