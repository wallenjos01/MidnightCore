package me.m1dnightninja.midnightcore.api.module.lang;

import me.m1dnightninja.midnightcore.api.text.MComponent;

public class CustomPlaceholder {

    private final String key;
    private final MComponent replacement;

    public CustomPlaceholder(String key, MComponent replacement) {
        this.key = key;
        this.replacement = replacement;
    }

    public String getKey() {
        return key;
    }

    public MComponent getReplacement() {
        return replacement;
    }
}
