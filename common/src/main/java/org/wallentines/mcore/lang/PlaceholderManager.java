package org.wallentines.mcore.lang;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.ComponentUtil;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class for registering and applying placeholders to strings and components
 */
public class PlaceholderManager {

    /**
     * The global placeholder manager instance. Use this rather than creating your own unless you need to isolate your
     * placeholders from other programs.
     */
    public static final PlaceholderManager INSTANCE = new PlaceholderManager();

    public static final Pattern VALID_PLACEHOLDER_ID = Pattern.compile("[A-Za-z0-9-_]+");

    private final StringRegistry<PlaceholderSupplier> registeredPlaceholders = new StringRegistry<>();

    /**
     * Gets the placeholder supplier for the placeholder with the given name
     * @param name The name to lookup
     * @return The placeholder supplier, or null if none is registered with that name
     */
    @Nullable
    public PlaceholderSupplier getPlaceholderSupplier(String name) {
        return registeredPlaceholders.get(name);
    }

    /**
     * Registers a new placeholder supplier with the given name
     * @param name The name of the new placeholder supplier
     * @param supplier The new placeholder supplier
     * @throws IllegalArgumentException If a placeholder supplier with the given name is already registered, or the name
     * does not match the pattern {@link PlaceholderManager#VALID_PLACEHOLDER_ID here}
     */
    public void registerSupplier(String name, PlaceholderSupplier supplier) {

        if(!VALID_PLACEHOLDER_ID.matcher(name).matches()) {
            throw new IllegalArgumentException("Placeholder name " + name + " does not match the pattern " + VALID_PLACEHOLDER_ID.pattern());
        }

        if(registeredPlaceholders.hasKey(name)) {
            throw new IllegalArgumentException("Attempt to overwrite existing PlaceholderSupplier " + name + "!");
        }

        registeredPlaceholders.register(name, supplier);
    }

    /**
     * Parses a string, locating all placeholders, but does not resolve the placeholders yet.
     * @param str The string to parse
     * @return An unresolved component representing the string and all its placeholders
     */
    public UnresolvedComponent parse(String str) {
        return UnresolvedComponent.parse(str).getOrThrow();
    }

    /**
     * Parses a string, locating all placeholders, then resolves the placeholders according to the given context.
     * Will not attempt to parse strings as JSON
     * @param str The string to parse
     * @return A new component
     */
    public Component parseAndResolve(String str, PlaceholderContext ctx) {
        return parseAndResolve(str, ctx, false);
    }

    /**
     * Parses a string, locating all placeholders, then resolves the placeholders according to the given context.
     * Optionally, attempts to parse strings as JSON components.
     * @param str The string to parse
     * @return A new component
     */
    public Component parseAndResolve(String str, PlaceholderContext ctx, boolean tryParseJSON) {
        return UnresolvedComponent.parse(str, tryParseJSON).getOrThrow().resolve(this, ctx);
    }

    public void registerDefaults() {

        registerSupplier("toUpperCase", PlaceholderSupplier.of(ctx ->
                ctx.getParameter() == null ?
                        Component.empty() :
                        ComponentUtil.editText(ctx.getParameter(), String::toUpperCase)));

        registerSupplier("toLowerCase", PlaceholderSupplier.of(ctx ->
                ctx.getParameter() == null ?
                        Component.empty() :
                        ComponentUtil.editText(ctx.getParameter(), String::toLowerCase)));

        registerSupplier("toTitleCase", PlaceholderSupplier.of(ctx ->
                ctx.getParameter() == null ?
                        Component.empty() :
                        ComponentUtil.editText(ctx.getParameter(), str ->
                                str.isEmpty() ?
                                        str :
                                        Arrays.stream(str.split(" "))
                                                .map(word -> Character.toTitleCase(word.charAt(0)) + word.substring(1).toLowerCase())
                                                .collect(Collectors.joining(" ")))));

        registerSupplier("first", PlaceholderSupplier.of(ctx ->
                ctx.getParameter() == null ?
                        Component.empty() :
                        ComponentUtil.editText(ctx.getParameter(), str ->
                                str.isEmpty() ?
                                        str :
                                        str.charAt(0) + "")));

        registerSupplier("eachFirst", PlaceholderSupplier.of(ctx ->
                ctx.getParameter() == null ?
                        Component.empty() :
                        ComponentUtil.editText(ctx.getParameter(), str ->
                                str.isEmpty() ?
                                        str :
                                        Arrays.stream(str.split(" "))
                                                .map(word -> word.charAt(0) + "")
                                                .collect(Collectors.joining()))));

    }

    static {
        INSTANCE.registerDefaults();
    }

}
