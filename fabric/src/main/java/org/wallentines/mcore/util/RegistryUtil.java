package org.wallentines.mcore.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import org.wallentines.mcore.Server;

import java.util.Optional;

public class RegistryUtil {

    private static final RegistryAccess.Frozen BUILTIN = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    public static <T> Optional<Registry<T>> registry(ResourceKey<? extends Registry<T>> key) {
        return access().registry(key);
    }

    public static <T> Registry<T> registryOrThrow(ResourceKey<? extends Registry<T>> key) {
        return access().registryOrThrow(key);
    }

    public static RegistryAccess access() {
        Server srv = Server.RUNNING_SERVER.getOrNull();
        if(srv == null) {
            return BUILTIN;
        }

        return ((MinecraftServer) srv).registryAccess();
    }

}
