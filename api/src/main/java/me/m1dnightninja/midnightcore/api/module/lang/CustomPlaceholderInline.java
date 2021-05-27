package me.m1dnightninja.midnightcore.api.module.lang;


public class CustomPlaceholderInline {

    private final String key;
    private final String replacement;

    public CustomPlaceholderInline(String key, String replacement) {
        this.key = key;
        this.replacement = replacement;
    }

    public String getKey() {
        return key;
    }

    public String getReplacement() {
        return replacement;
    }


}
