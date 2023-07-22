package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Content;

public class LangContent extends Content {

    private final LangManager manager;
    private final String key;
    private final PlaceholderContext context;

    public LangContent(LangManager manager, String key) {
        super("lang");
        this.manager = manager;
        this.key = key;
        this.context = new PlaceholderContext();
    }

    public LangContent(LangManager manager, String key, Object... values) {
        super("lang");
        this.manager = manager;
        this.key = key;
        this.context = new PlaceholderContext(values);
    }

    public LangContent(LangManager manager, String key, PlaceholderContext ctx) {
        super("lang");
        this.manager = manager;
        this.key = key;
        this.context = ctx;
    }

    public LangManager getLangManager() {
        return manager;
    }

    public String getKey() {
        return key;
    }

    public PlaceholderContext getContext() {
        return context;
    }

    @Override
    public boolean requiresResolution() {
        return true;
    }
}
