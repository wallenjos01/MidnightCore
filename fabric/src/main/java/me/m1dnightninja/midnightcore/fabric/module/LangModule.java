package me.m1dnightninja.midnightcore.fabric.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.module.AbstractLangModule;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerChangeSettingsEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

public class LangModule extends AbstractLangModule {

    private final HashMap<UUID, String> languages = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection configuration) {

        Event.register(PlayerChangeSettingsEvent.class, this, event -> {
            languages.put(event.getPlayer().getUUID(), event.getLocale());
        });

        registerPlaceholderSupplier("player_name", (objs) -> {
            for(Object o : objs) {
                if(o instanceof ServerPlayer) {
                    return MComponent.Serializer.fromJson(Component.Serializer.toJson(((ServerPlayer) o).getName()));
                }
            } return null;
        });
        registerPlaceholderSupplier("player_display_name", (objs) -> {
            for(Object o : objs) {
                if(o instanceof ServerPlayer) {
                    return MComponent.Serializer.fromJson(Component.Serializer.toJson(((ServerPlayer) o).getDisplayName()));
                }
            } return null;
        });

        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return new ConfigSection();
    }

    @Override
    public String getPlayerLocale(UUID u) {
        return languages.getOrDefault(u, getServerLanguage());
    }

    @Override
    public String getServerLanguage() {
        return MidnightCoreAPI.getInstance().getMainConfig().has("language") ? MidnightCoreAPI.getInstance().getMainConfig().getString("language") : "en_us";
    }
}
