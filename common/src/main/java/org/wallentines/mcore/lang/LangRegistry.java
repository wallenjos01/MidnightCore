package org.wallentines.mcore.lang;


import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Functions;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.registry.Registry;

import java.util.function.Function;

/**
 * A data type representing the language entries for a particular language
 */
public class LangRegistry {

    private final Registry<String, UnresolvedComponent> entries = Registry.createStringRegistry(false, false, true);
    private final PlaceholderManager manager;


    /**
     * Creates a new LangRegistry with the global PlaceholderManager instance
     */
    public LangRegistry() {
        this.manager = PlaceholderManager.INSTANCE;
    }

    /**
     * Creates a new LangRegistry with the given PlaceholderManager instance
     */
    public LangRegistry(PlaceholderManager manager) {
        this.manager = manager;
    }

    /**
     * Gets the entry with the given name, or null
     * @param entry The entry to look up
     * @return An unresolved entry
     */
    @Nullable
    public UnresolvedComponent get(String entry) {
        return entries.get(entry);
    }


    /**
     * Gets the entry with the given name, or gets another entry
     * @param entry The entry to look up
     * @return An unresolved entry
     */
    public UnresolvedComponent getOr(String entry, Function<String, UnresolvedComponent> other) {

        UnresolvedComponent comp = get(entry);
        if(comp == null) return other.apply(entry);
        return comp;
    }

    /**
     * Resolves the entry with the given name, according to the given context
     * @param entry The entry to look up
     * @param ctx The context by which to resolve the entry
     * @return A resolved entry
     */
    public Component resolve(String entry, PlaceholderContext ctx) {
        UnresolvedComponent comp = get(entry);
        if(comp == null) return Component.text(entry);

        return comp.resolve(manager, ctx);
    }

    /**
     * Gets the entry with the given name, or gets another entry, and resolves it according to the given context
     * @param entry The entry to look up
     * @param ctx The context by which to resolve the entry
     * @return A resolved entry
     */
    public Component resolveOr(String entry, PlaceholderContext ctx, Functions.F2<String, PlaceholderContext, Component> other) {
        UnresolvedComponent comp = get(entry);
        if(comp == null) return other.apply(entry, ctx);

        return comp.resolve(manager, ctx);
    }

    public void register(String entry, UnresolvedComponent component) {
        entries.register(entry, component);
    }

    /**
     * Saves the registry to a ConfigSection
     * @return A ConfigSection representing the registry
     */
    public ConfigSection save() {

        ConfigSection out = new ConfigSection();
        for(String key : entries.getIds()) {
            UnresolvedComponent cmp = entries.get(key);
            if(cmp != null) {
                out.set(key, cmp.toRaw());
            }
        }

        return out;
    }

    /**
     * Creates a LangRegistry from the given ConfigSection
     * @param section The ConfigSection to read
     * @param manager The PlaceholderManager by which to resolve placeholders
     * @return A new LangRegistry
     */
    public static LangRegistry fromConfig(ConfigSection section, PlaceholderManager manager) {

        return fromConfig(section, manager, false);
    }

    /**
     * Creates a LangRegistry from the given ConfigSection, optionally parsing JSON strings
     * @param section The ConfigSection to read
     * @param manager The PlaceholderManager by which to resolve placeholders
     * @param tryParseJSON Whether parsing of JSON strings should be attempted when resolving entries
     * @return A new LangRegistry
     */
    public static LangRegistry fromConfig(ConfigSection section, PlaceholderManager manager, boolean tryParseJSON) {

        LangRegistry out = new LangRegistry(manager);

        for(String key : section.getKeys()) {
            ConfigObject obj = section.get(key);

            if(obj == null || !obj.isString()) {
                MidnightCoreAPI.LOGGER.warn("Found non-string key in lang file: " + key);
                continue;
            }

            SerializeResult<UnresolvedComponent> result = UnresolvedComponent.parse(obj.asString(), tryParseJSON);
            if(result.isComplete()) {
                out.entries.register(key, result.getOrThrow());
            } else {
                MidnightCoreAPI.LOGGER.warn("An error occurred while parsing a language entry! (" + key + ") " + result.getError());
            }
        }


        return out;
    }

}
