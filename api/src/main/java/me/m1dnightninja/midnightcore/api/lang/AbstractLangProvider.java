package me.m1dnightninja.midnightcore.api.lang;

import java.io.File;
import java.util.HashMap;

public abstract class AbstractLangProvider {

    protected File folder;
    protected final HashMap<String, File> files = new HashMap<>();
    protected final HashMap<String, HashMap<String, String>> entries = new HashMap<>();

    protected final HashMap<String, String> defaults;

    protected AbstractLangProvider(File folder, HashMap<String, String> defaults) {
        this.defaults = defaults;
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException();
        }
        this.folder = folder;
        for (File f : folder.listFiles()) {
            if (!this.verifyFile(f)) continue;
            this.files.put(f.getName(), f);
            loadEntries(f.getName());
        }
    }

    public void reloadAllEntries() {

        entries.clear();
        for(String s : files.keySet()) {
            loadEntries(s);
        }
    }

    public abstract boolean hasMessage(String language, String key);

    public boolean hasMessage(String key) {
        return hasMessage(getServerLanguage(), key);
    }

    public abstract String getRawMessage(String language, String key);

    public abstract String getMessage(String language, String key, Object ... args);

    public String getRawMessage(String key) {
        return getRawMessage(getServerLanguage(), key);
    }

    public String getMessage(String key, Object... args) {
        return getMessage(getServerLanguage(), key, args);
    }

    public abstract String getServerLanguage();

    protected abstract boolean verifyFile(File file);

    public abstract void loadEntries(String language);
}

