package me.m1dnightninja.midnightcore.api.config;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

import java.io.File;

public class FileConfig {

    private final File file;
    private final ConfigProvider provider;

    private ConfigSection root;

    public FileConfig(File f) {

        this(f, MidnightCoreAPI.getInstance().getConfigRegistry().getProviderForFile(f));
    }

    public FileConfig(File f, ConfigProvider prov) {

        this.file = f;
        this.provider = prov;

        root = prov.loadFromFile(f);

    }

    public ConfigSection getRoot() {
        return root;
    }

    public void setRoot(ConfigSection sec) { this.root = sec; }

    public void reload() {
        root = provider.loadFromFile(file);
    }

    public void save() {
        provider.saveToFile(root, file);
    }

    public static FileConfig findFile(File[] list, String prefix) {

        if(list == null) return null;

        for(File f : list) {

            if(!f.getName().startsWith(prefix) || f.getName().equals(prefix)) continue;
            String suffix = f.getName().substring(prefix.length());

            ConfigProvider prov = MidnightCoreAPI.getInstance().getConfigRegistry().getProviderForFileType(suffix);

            if(prov != null) {
                return new FileConfig(f, prov);
            }
        }

        return null;
    }

    public static FileConfig findOrCreate(String prefix, File directory) {

        if(!directory.isDirectory()) return null;

        FileConfig out = findFile(directory.listFiles(), prefix);
        if(out != null) return out;

        ConfigProvider provider = MidnightCoreAPI.getInstance().getDefaultConfigProvider();
        return new FileConfig(new File(directory, prefix + provider.getFileExtension()), provider);

    }

}
