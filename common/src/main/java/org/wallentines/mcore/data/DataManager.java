package org.wallentines.mcore.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileCodecRegistry;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

/**
 * A class for loading and saving data files from a directory on disk.
 */
public class DataManager {

    private final Path searchDirectory;
    private final FileCodecRegistry fileCodecRegistry;
    private final HashMap<String, FileWrapper<ConfigObject>> openFiles = new HashMap<>();
    private final Queue<String> opened = new ArrayDeque<>();

    /**
     * The maximum number of files that will be cached at any given time
     */
    public int cacheSize = 8;


    /**
     * Constructs a new data manager with the given search directory, using the default codec registry
     * @param searchDirectory The directory to search for and save data files in
     */
    public DataManager(Path searchDirectory) {
        this(searchDirectory, MidnightCoreAPI.FILE_CODEC_REGISTRY);
    }

    /**
     * Constructs a new data manager with the given search directory and codec registry
     * @param searchDirectory The directory to search for and save data files in
     * @param fileCodecRegistry The codecs to use to decode and encode data files
     */
    public DataManager(Path searchDirectory, FileCodecRegistry fileCodecRegistry) {
        this.searchDirectory = searchDirectory;
        this.fileCodecRegistry = fileCodecRegistry;
    }

    /**
     * Gets the data associated with a specific key
     * @param key The key to lookup
     * @return The data associated with the given key
     */
    public ConfigSection getData(String key) {

        FileWrapper<ConfigObject> out = getWrapper(key, true);
        if(out.getRoot() == null) {
            out.setRoot(new ConfigSection());
        }

        return out.getRoot().asSection();
    }


    /**
     * Gets the data associated with a specific key
     * @param key The key to lookup
     * @return The data associated with the given key
     */
    @Nullable
    public ConfigSection getDataOrNull(String key) {

        FileWrapper<ConfigObject> wrapper = getWrapper(key, false);
        if(wrapper == null) return null;

        if(wrapper.getRoot() == null) {
            wrapper.setRoot(new ConfigSection());
        }

        return wrapper.getRoot().asSection();
    }


    /**
     * Saves the cached data for the given key to disk
     * @param key The key to save
     */
    public void save(String key) {

        if(!openFiles.containsKey(key)) return;
        openFiles.remove(key).save();
        opened.remove(key);
    }


    /**
     * Saves the given data for the given key to disk
     * @param key The key to save
     * @param data The data to save
     */
    public void save(String key, ConfigSection data) {

        FileWrapper<ConfigObject> obj = getWrapper(key, true);

        obj.setRoot(data);
        obj.save();

        openFiles.remove(key);
        opened.remove(key);
    }

    /**
     * Saves all cached files to disk
     */
    public void saveAll() {
        while(!opened.isEmpty()) {
            String key = opened.remove();
            openFiles.remove(key).save();
        }
    }

    /**
     * Clears all data associated with the given key and deletes the file
     * @param key The key to clear
     * @return Whether clearing was successful
     */
    public boolean clear(String key) {

        FileWrapper<ConfigObject> obj;
        if(openFiles.containsKey(key)) {
            obj = openFiles.remove(key);
            opened.remove(key);
        } else {
            obj = getWrapper(key, false);
        }

        if(obj == null) return true;

        try {
            if (!Files.deleteIfExists(obj.getPath())) {
                MidnightCoreAPI.LOGGER.error("Unable to delete data file {}!", obj.getPath());
                return false;
            }
        } catch (IOException ex) {
            MidnightCoreAPI.LOGGER.error("An error occurred while deleting data file {}!", obj.getPath(), ex);
        }
        return true;
    }

    /**
     * Clears all references to files in the cache.
     */
    public void clearCache() {

        opened.clear();
        openFiles.clear();
    }


    @Contract("_,true -> !null")
    private FileWrapper<ConfigObject> getWrapper(String key, boolean create) {

        if(cacheSize == 0) {
            return findWrapper(key, create);
        }

        if(opened.size() == cacheSize) {
            openFiles.remove(opened.remove()).save();
        }

        FileWrapper<ConfigObject> out = openFiles.computeIfAbsent(key, k -> findWrapper(k, create));
        if(out != null) {
            opened.add(key);
        }

        return out;
    }

    private FileWrapper<ConfigObject> findWrapper(String key, boolean create) {

        FileWrapper<ConfigObject> wrapper;

        if(create) {
            wrapper = fileCodecRegistry.findOrCreate(ConfigContext.INSTANCE, key, searchDirectory, new ConfigSection());
        } else {
            wrapper = fileCodecRegistry.find(ConfigContext.INSTANCE, key, searchDirectory, new ConfigSection());
        }

        return wrapper;

    }


}
