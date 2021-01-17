package me.m1dnightninja.midnightcore.fabric.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.lang.AbstractLangProvider;
import me.m1dnightninja.midnightcore.common.module.AbstractLangModule;
import me.m1dnightninja.midnightcore.fabric.api.LangProvider;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.*;

public class LangModule extends AbstractLangModule<Component> {


    @Override
    public String getLanguage(UUID player) {
        return getServerLanguage();
    }

    @Override
    public String getServerLanguage() {
        return "en_us";
    }

    @Override
    public AbstractLangProvider createProvider(String name, File folder) {

        try {
            LangProvider prov = new LangProvider(folder, this);
            providers.put(name, prov);

            return prov;

        } catch(IllegalArgumentException ex) {
            MidnightCoreAPI.getLogger().warn("An error occurred while trying to create a lang provider!");
            ex.printStackTrace();

            return null;
        }

    }

    @Override
    public AbstractLangProvider getProvider(String name) {
        return null;
    }

    @Override
    public boolean initialize() {
        return true;
    }
}
