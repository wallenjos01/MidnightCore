package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.api.lang.AbstractLangProvider;

import java.io.File;
import java.util.UUID;

public interface ILangModule<T> extends IModule {


    /**
     * Gets the preferred language for a given player
     *
     * @param player  The UUID of the player to look up
     * @return        The player's preferred language in the format
     */

    String getLanguage(UUID player);


    /**
     * Gets the main language of the server
     *
     * @return  The server's primary language
     */

    String getServerLanguage();

    /**
     * Registers a string placeholder
     *
     * @param key      The name of the placeholder
     * @param supplier The string to replace the key with
     */

    void registerStringPlaceholder(String key, PlaceholderSupplier<String> supplier);


    /**
     * Registers a raw placeholder
     *
     * @param key      The name of the placeholder
     * @param supplier The raw value to replace the key with
     */

    void registerRawPlaceholder(String key, PlaceholderSupplier<T> supplier);


    /**
     * Returns the value associated with a placeholder
     *
     * @param key  The name of the placeholder to query
     * @param args The arguments to use when generating the result
     * @return     The result of the placeholder query as a string
     */

    String getStringPlaceholderValue(String key, Object... args);


    /**
     * Returns the raw value associated with a placeholder
     *
     * @param key  The name of the placeholder to query
     * @param args The arguments to use when generating the result
     * @return     The result of the placeholder query as a raw value
     */

    T getRawPlaceholderValue(String key, Object... args);


    /**
     * Create a new Lang Provider, and register it
     *
     * @param name   The name of the provider
     * @param folder The folder to scan in for lang files
     * @return       The created lang provider
     */

    AbstractLangProvider createProvider(String name, File folder);


    /**
     * Get a registered provider
     *
     * @param name The name to lookup
     * @return     The registered lang provider
     */

    AbstractLangProvider getProvider(String name);


    interface PlaceholderSupplier<P> {
        P get(Object... args);
    }

}
