package org.wallentines.mcore.lang;


import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.Functions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A data type representing the language entries for a particular language
 */
public class LangRegistry {

    private final HashMap<String, UnresolvedComponent> entries = new HashMap<>();

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

        return comp.resolve(ctx);
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

        return comp.resolve(ctx);
    }

    public void register(String entry, UnresolvedComponent component) {
        entries.put(entry, component);
    }

    /**
     * Saves the registry to a ConfigSection
     * @return A ConfigSection representing the registry
     */
    public ConfigSection save() {

        ConfigSection out = new ConfigSection();
        for(Map.Entry<String, UnresolvedComponent> ent : entries.entrySet()) {
            out.set(ent.getKey(), ent.getValue().toRaw());
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

        LangRegistry out = new LangRegistry();
        addAll(section, out, "", manager, tryParseJSON);

        return out;
    }

    private static void addAll(ConfigSection section, LangRegistry registry, String prefix, PlaceholderManager manager, boolean tryParseJSON) {

        for(String key : section.getKeys()) {
            ConfigObject obj = section.get(key);
            if(obj.isString()) {
                registry.entries.put(prefix + key, UnresolvedComponent.parse(obj.asString(), manager, tryParseJSON).getOrThrow());
            } else if(obj.isSection()) {
                addAll(obj.asSection(), registry, prefix + key + ".", manager, tryParseJSON);
            }
        }
    }

}