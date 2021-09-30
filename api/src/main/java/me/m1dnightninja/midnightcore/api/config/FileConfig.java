package me.m1dnightninja.midnightcore.api.config;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

import java.io.File;

public class FileConfig {

    private final File file;
    private final ConfigProvider provider;

    private ConfigSection root;

    public FileConfig(File f) {

        this(f, ConfigRegistry.INSTANCE.getProviderForFile(f));
    }

    public FileConfig(File f, ConfigProvider prov) {

        this.file = f;
        this.provider = prov;

        root = prov.loadFromFile(f);

    }

    public ConfigSection getRoot() {
        return root;
    }

    public ConfigProvider getProvider() {
        return provider;
    }

    public File getFile() {
        return file;
    }

    public void setRoot(ConfigSection sec) { this.root = sec; }

    public void reload() {
        root = provider.loadFromFile(file);
    }

    public void save() {
        provider.saveToFile(root, file);
    }


    public static FileConfig fromFile(File f) {

        ConfigProvider prov = ConfigRegistry.INSTANCE.getProviderForFile(f);
        if(prov != null) return new FileConfig(f, prov);

        return null;
    }

    public static FileConfig findFile(File[] list, String prefix) {

        if(list == null) return null;

        for(File f : list) {

            if(!f.getName().startsWith(prefix) || f.getName().equals(prefix)) continue;
            String suffix = f.getName().substring(prefix.length());

            ConfigProvider prov = ConfigRegistry.INSTANCE.getProviderForFileType(suffix);

            if(prov != null) {
                return new FileConfig(f, prov);
            }
        }

        return null;
    }

    public static FileConfig findOrCreate(String prefix, File directory, ConfigSection defaults) {

        if(!directory.exists() && !directory.mkdirs()) {
            MidnightCoreAPI.getLogger().warn("Unable to create folder " + directory.getAbsolutePath() + "!");
            return null;
        }

        if(!directory.isDirectory()) return null;

        FileConfig out = findFile(directory.listFiles(), prefix);
        if(out != null) return out;

        ConfigProvider provider = ConfigRegistry.INSTANCE.getDefaultProvider();
        if(provider == null) return null;

        File f = new File(directory, prefix + provider.getFileExtension());
        provider.saveToFile(defaults, f);

        return new FileConfig(f, provider);

    }

    public static FileConfig findOrCreate(String prefix, File directory) {

        return findOrCreate(prefix, directory, new ConfigSection());
    }

}
