package org.wallentines.mcore.util;

import org.wallentines.mcore.*;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

public class ConversionUtil {


    public static Location toLocation(org.bukkit.Location loc) {

        Identifier id = loc.getWorld() == null ? new Identifier("minecraft", "overworld") : Identifier.parseOrDefault(loc.getWorld().getName(), "minecraft");
        return new Location(id, new Vec3d(loc.getX(), loc.getY(), loc.getZ()), loc.getYaw(), loc.getPitch());
    }

    public static SpigotPlayer validate(Player player) {
        if(!(player instanceof SpigotPlayer)) {
            throw new IllegalArgumentException("Attempt to access non-Spigot Player!");
        }
        return (SpigotPlayer) player;
    }

    public static SpigotItem validate(ItemStack itemStack) {
        if(!(itemStack instanceof SpigotItem)) {
            throw new IllegalArgumentException("Attempt to access non-Spigot Player!");
        }
        return (SpigotItem) itemStack;
    }

}
