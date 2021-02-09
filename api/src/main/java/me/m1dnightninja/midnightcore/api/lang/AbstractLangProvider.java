package me.m1dnightninja.midnightcore.api.lang;

import java.io.File;
import java.util.HashMap;

public abstract class AbstractLangProvider {

    protected File folder;

    protected HashMap<String, File> files;
    protected HashMap<String, HashMap<String, String>> entries;

    protected AbstractLangProvider(File folder) {
        if(!folder.isDirectory()) throw new IllegalArgumentException();

        this.folder = folder;

        for(File f : folder.listFiles()) {
            if(verifyFile(f)) {
                files.put(f.getName(), f);
            }
        }
    }

    public abstract boolean hasMessage(String language, String key);

    public boolean hasMessage(String key) {
        return hasMessage(getServerLanguage(), key);
    }

    public abstract String getRawMessage(String language, String key);

    public abstract String getMessage(String language, String key, Object... objs);

    protected abstract boolean verifyFile(File file);

    protected abstract void loadEntries(String fileName);

}
