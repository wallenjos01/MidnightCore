package me.m1dnightninja.midnightcore.common.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.module.lang.PlaceholderSupplier;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.module.lang.LangProvider;

import java.io.File;
import java.util.HashMap;

public abstract class AbstractLangModule implements ILangModule {

    private static final MIdentifier ID = MIdentifier.create("midnightcore", "lang");

    private final HashMap<String, PlaceholderSupplier<String>> inlineSuppliers = new HashMap<>();
    private final HashMap<String, PlaceholderSupplier<MComponent>> suppliers = new HashMap<>();


    @Override
    public MIdentifier getId() {
        return ID;
    }


    @Override
    public String getServerLanguage() {
        return MidnightCoreAPI.getInstance().getMainConfig().has("language") ? MidnightCoreAPI.getInstance().getMainConfig().getString("language") : "en_us";
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return new ConfigSection();
    }

    @Override
    public void registerInlinePlaceholderSupplier(String search, PlaceholderSupplier<String> supplier) {
        inlineSuppliers.put(search, supplier);
    }

    @Override
    public void registerPlaceholderSupplier(String search, PlaceholderSupplier<MComponent> supplier) {
        suppliers.put(search, supplier);
    }

    @Override
    public MComponent getPlaceholderValue(String key, Object... args) {

        PlaceholderSupplier<MComponent> supplier = suppliers.get(key);
        if(supplier == null) return null;

        return supplier.get(args);
    }

    @Override
    public String getInlinePlaceholderValue(String key, Object... args) {

        PlaceholderSupplier<String> supplier = inlineSuppliers.get(key);
        if(supplier == null) return null;

        return supplier.get(args);

    }

    @Override
    public ILangProvider createLangProvider(File langFolder, ConfigProvider provider, ConfigSection defaults) {
        return new LangProvider(langFolder, this, provider, defaults);
    }
}
