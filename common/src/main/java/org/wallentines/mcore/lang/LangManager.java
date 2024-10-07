package org.wallentines.mcore.lang;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.FileCodec;
import org.wallentines.mdcfg.codec.FileCodecRegistry;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads language entries for all files in a given folder
 */
public class LangManager {
    private final HashMap<String, LangRegistry> languages = new HashMap<>();
    private final LangRegistry defaults;
    private final java.nio.file.Path searchDirectory;
    private final FileCodecRegistry fileCodecRegistry;
    private final PlaceholderManager manager;
    private final boolean tryParseJSON;

    private final HashMap<String, String> languageMappings = new HashMap<>();

    /**
     * Creates a lang manager with the given defaults and search directory, using the global codec registry and placeholder manager
     * @param defaults The defaults entries to use
     * @param searchDirectory The directory to search for language files in
     */
    public LangManager(LangRegistry defaults, Path searchDirectory) {
        this(defaults, searchDirectory, MidnightCoreAPI.FILE_CODEC_REGISTRY, PlaceholderManager.INSTANCE);
    }

    /**
     * Creates a lang manager with the given defaults, search directory, codec registry, and placeholder manager
     * @param defaults The defaults entries to use
     * @param searchDirectory The directory to search for language files in
     * @param fileCodecRegistry The codec registry to use for decoding files
     * @param manager The placeholder manager to use for resolving placeholders
     */
    public LangManager(LangRegistry defaults, Path searchDirectory, FileCodecRegistry fileCodecRegistry, PlaceholderManager manager) {
        this(defaults, searchDirectory, fileCodecRegistry, manager, false);
    }

    /**
     * Creates a lang manager with the given defaults, search directory, codec registry, and placeholder manager, with an option to try parsing JSON entries
     * @param defaults The defaults entries to use
     * @param searchDirectory The directory to search for language files in
     * @param fileCodecRegistry The codec registry to use for decoding files
     * @param manager The placeholder manager to use for resolving placeholders
     * @param tryParseJSON Whether JSON strings should be decoded during resolution
     */
    public LangManager(LangRegistry defaults, Path searchDirectory, FileCodecRegistry fileCodecRegistry, PlaceholderManager manager, boolean tryParseJSON) {
        this.defaults = defaults;
        this.searchDirectory = searchDirectory;
        this.fileCodecRegistry = fileCodecRegistry;
        this.manager = manager;
        this.tryParseJSON = tryParseJSON;

        scanDirectory();
    }

    /**
     * Gets a message with the given key
     * @param key The key to lookup
     * @return A resolved message component
     */
    public Component getMessage(String key) {
        return defaults.resolve(key, new PlaceholderContext());
    }

    /**
     * Gets a message with the given key, resolved using the given context
     * @param key The key to lookup
     * @param ctx The context by which to resolve placeholders
     * @return A resolved message component
     */
    public Component getMessage(String key, PlaceholderContext ctx) {

        LangRegistry reg = ctx.onValueOr(
                LocaleHolder.class,
                lh -> languages.getOrDefault(lh.getLanguage(), defaults),
                defaults);

        return reg.resolveOr(key, ctx, defaults::resolve);
    }


    /**
     * Gets a message with the given key and language, resolved using the given context
     * @param key The key to lookup
     * @param language The language to search in
     * @param ctx The context by which to resolve placeholders
     * @return A resolved message component
     */
    public Component getMessage(String key, String language, PlaceholderContext ctx) {

        LangRegistry reg = languages.getOrDefault(findClosestLanguage(language), defaults);
        return reg.resolveOr(key, ctx, defaults::resolve);
    }

    /**
     * Gets a message with the given key and language, resolved using the given context
     * @param key The key to lookup
     * @param language The language to search in
     * @param args The context by which to resolve placeholders
     * @return A resolved message component
     */
    public Component getMessage(String key, String language, Object... args) {

        LangRegistry reg = languages.getOrDefault(findClosestLanguage(language), defaults);
        return reg.resolveOr(key, new PlaceholderContext(List.of(args)), defaults::resolve);
    }

    /**
     * Sets the entries for the given language
     * @param language The language to override
     * @param registry The registry to set
     */
    public void setLanguageEntries(String language, LangRegistry registry) {

        languages.put(language, registry);
    }

    /**
     * Clears all entries and reloads them from disk
     */
    public void reload() {

        languages.clear();
        scanDirectory();
    }

    public UnresolvedComponent component(String key, Object... args) {
        UnresolvedComponent comp = UnresolvedComponent.builder()
                .appendPlaceholder("lang", key)
                .tryParseJSON(tryParseJSON)
                .withContextValue(this)
                .build();

        for(Object o : args) comp.getContext().addValue(o);
        return comp;
    }

    public UnresolvedComponent componentWith(String key, List<Object> args) {
        UnresolvedComponent comp = UnresolvedComponent.builder()
                .appendPlaceholder("lang", key)
                .tryParseJSON(tryParseJSON)
                .withContextValue(this)
                .build();

        for(Object o : args) comp.getContext().addValue(o);
        return comp;
    }

    /**
     * Saves the given registry to the search directory as the given language name. Will not overwrite existing entries.
     * @param language The language to save
     * @param registry The entries to save
     */
    public void saveLanguageDefaults(String language, LangRegistry registry) {

        if(!Files.isDirectory(searchDirectory)) {
            try {
                Files.createDirectories(searchDirectory);
            } catch (IOException ex) {
                throw new RuntimeException("An error occurred while creating a lang folder!", ex);
            }
        }

        FileWrapper<ConfigObject> wrapper = fileCodecRegistry.findOrCreate(ConfigContext.INSTANCE, language, searchDirectory);
        if(wrapper.getRoot() != null && wrapper.getRoot().isSection()) {
            wrapper.getRoot().asSection().fill(registry.save());
        } else {
            wrapper.setRoot(registry.save());
        }

        wrapper.save();
    }

    private void scanDirectory() {

        if(searchDirectory == null || !Files.isDirectory(searchDirectory)) {
            return;
        }

        try(Stream<Path> stream = Files.list(searchDirectory)) {

            stream.filter(Files::isRegularFile).forEach(path -> {
                FileCodec codec = fileCodecRegistry.forFile(path);

                if(codec == null) {
                    MidnightCoreAPI.LOGGER.warn("Unable to find file codec for lang file {}!", path.toAbsolutePath());
                    return;
                }

                ConfigObject obj;
                try {
                    obj = codec.loadFromFile(ConfigContext.INSTANCE, path, StandardCharsets.UTF_8);
                } catch (DecodeException | IOException ex) {
                    MidnightCoreAPI.LOGGER.warn("An error occurred while decoding lang file {}!", path.toAbsolutePath(), ex);
                    return;
                }

                if(!obj.isSection()) {
                    MidnightCoreAPI.LOGGER.warn("Lang file {} formatted incorrectly! Expected root to be a section!", path.toAbsolutePath());
                    return;
                }

                languages.put(
                        path.getFileName().toString().substring(0, path.getFileName().toString().lastIndexOf(".")),
                        LangRegistry.fromConfig(obj.asSection(), manager, tryParseJSON)
                );
            });

        } catch (IOException ex) {
            throw new RuntimeException("An error occurred while scanning lang directory!", ex);
        }
    }

    // Attempts to find the closest loaded language to the requested string
    private String findClosestLanguage(String language) {

        if (language == null) {
            return null;
        }

        if (languages.containsKey(language) || !language.contains("_")) {
            return language;
        }

        return languageMappings.computeIfAbsent(language, l -> {

            String lang = l.split("_")[0];
            for (String key : languages.keySet()) {
                if (!key.contains("_")) {
                    continue;
                }

                String targetLang = key.split("_")[0];
                if (lang.equals(targetLang)) {
                    return key;
                }
            }
            return l;
        });
    }

    public static void registerPlaceholders(PlaceholderManager manager) {

        manager.registerSupplier("lang", PlaceholderSupplier.of(ctx -> {

            if(ctx.getParameter() == null) {
                return null;
            }

            String param = ctx.getParameter().allText();
            LangManager langManager = ctx.getValue(LangManager.class);

            if(param == null || langManager == null) {
                return null;
            }

            return langManager.getMessage(param, ctx);
        }));

    }

}
