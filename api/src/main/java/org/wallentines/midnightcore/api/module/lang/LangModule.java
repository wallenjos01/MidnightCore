package org.wallentines.midnightcore.api.module.lang;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;

import java.nio.file.Path;

public interface LangModule extends Module<MidnightCoreAPI> {

    LangProvider createProvider(Path folderPath, ConfigSection defaults);

    void registerInlinePlaceholder(String key, PlaceholderSupplier<String> supplier);

    void registerPlaceholder(String key, PlaceholderSupplier<MComponent> supplier);

    MComponent applyPlaceholders(MComponent input, Object... data);

    String applyInlinePlaceholders(String input, Object... args);

    MComponent parseText(String text, Object... data);


    MComponent getPlaceholderValue(String key, Object... data);

    String getInlinePlaceholderValue(String key, Object... data);

    String getServerLanguage();

}
