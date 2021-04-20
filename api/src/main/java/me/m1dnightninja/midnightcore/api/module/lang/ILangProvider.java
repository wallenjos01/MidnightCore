package me.m1dnightninja.midnightcore.api.module.lang;

import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.UUID;

public interface ILangProvider {

    MComponent getMessage(String key, String language, Object... args);

    MComponent getUnformattedMessage(String key, String language);

    String getRawMessage(String key, String language);

    MComponent getMessage(String key, UUID player, Object... args);

}
