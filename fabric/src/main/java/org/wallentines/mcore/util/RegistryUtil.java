package org.wallentines.mcore.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import org.wallentines.mcore.Server;

import java.util.Optional;

public class RegistryUtil {

    @SuppressWarnings("unchecked")
    public static <T> Optional<Registry<T>> registry(ResourceKey<? extends Registry<T>> key) {

        Server srv = Server.RUNNING_SERVER.getOrNull();
        if(srv == null) {
            return (Optional<Registry<T>>) BuiltInRegistries.REGISTRY.getOptional(key.location());
        }

        return ((MinecraftServer) srv).registryAccess().registry(key);

    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> registryOrThrow(ResourceKey<? extends Registry<T>> key) {

        Server srv = Server.RUNNING_SERVER.getOrNull();
        if(srv == null) {
            return (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
        }

        return ((MinecraftServer) srv).registryAccess().registryOrThrow(key);

    }

}
