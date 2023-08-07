package org.wallentines.mcore.lang;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.registry.StringRegistry;

public class PlaceholderManager {

    public static final PlaceholderManager INSTANCE = new PlaceholderManager();

    private final StringRegistry<PlaceholderSupplier> registeredPlaceholders = new StringRegistry<>();

    @Nullable
    public PlaceholderSupplier getPlaceholderSupplier(String name) {
        return registeredPlaceholders.get(name);
    }

    public void registerSupplier(String name, PlaceholderSupplier supplier) {

        if(registeredPlaceholders.hasKey(name)) {
            throw new IllegalArgumentException("Attempt to overwrite existing PlaceholderSupplier " + name + "!");
        }

        registeredPlaceholders.register(name, supplier);
    }

    public UnresolvedComponent parse(String str) {
        return UnresolvedComponent.parse(str, this).getOrThrow();
    }

    public Component parseAndResolve(String str, PlaceholderContext ctx) {
        return parseAndResolve(str, ctx, false);
    }

    public Component parseAndResolve(String str, PlaceholderContext ctx, boolean tryParseJSON) {
        return UnresolvedComponent.parse(str, this, tryParseJSON).getOrThrow().resolve(ctx);
    }

}
