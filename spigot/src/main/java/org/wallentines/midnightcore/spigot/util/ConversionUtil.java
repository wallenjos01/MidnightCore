package org.wallentines.midnightcore.spigot.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.spigot.adapter.AdapterManager;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

public final class ConversionUtil {

    public static Identifier toIdentifier(NamespacedKey key) {

        return new Identifier(key.getKey(), key.getNamespace());
    }

    public static Location toBukkitLocation(org.wallentines.midnightcore.api.player.Location location) {

        World w = Bukkit.getWorld(location.getWorldId().toString());
        return new Location(w, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public static org.wallentines.midnightcore.api.player.Location toLocation(Location location) {

        World w = location.getWorld();
        if(w == null) return null;

        Identifier id = Identifier.parseOrDefault(w.getName(), "minecraft");
        return new org.wallentines.midnightcore.api.player.Location(id, new Vec3d(location.getX(), location.getY(), location.getZ()), location.getYaw(), location.getPitch());
    }


}
