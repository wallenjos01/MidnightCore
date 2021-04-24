package me.m1dnightninja.midnightcore.fabric.module.lang;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.PlaceholderSupplier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.module.lang.AbstractLangModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerChangeSettingsEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public class LangModule extends AbstractLangModule {

    private final HashMap<UUID, String> languages = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection configuration) {

        Event.register(PlayerChangeSettingsEvent.class, this, event -> languages.put(event.getPlayer().getUUID(), event.getLocale()));

        registerPlaceholderSupplier("player_name", playerOrUUID(player -> MComponent.Serializer.fromJson(Component.Serializer.toJson(player.getName()))));
        registerPlaceholderSupplier("player_display_name", playerOrUUID(player -> MComponent.Serializer.fromJson(Component.Serializer.toJson(player.getDisplayName()))));

        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return new ConfigSection();
    }

    @Override
    public String getPlayerLocale(UUID u) {

        if(u == null) return getServerLanguage();
        return languages.getOrDefault(u, getServerLanguage());
    }

    @Override
    public String getServerLanguage() {
        return MidnightCoreAPI.getInstance().getMainConfig().has("language") ? MidnightCoreAPI.getInstance().getMainConfig().getString("language") : "en_us";
    }

    @Override
    public LangProvider createLangProvider(File langFolder, ConfigProvider provider, ConfigSection defaults) {
        return new LangProvider(langFolder, this, provider, defaults);
    }

    public static <T> PlaceholderSupplier<T> playerOrUUID(Function<ServerPlayer, T> run) {
        return args -> {
            for(Object o : args) {
                if(o instanceof ServerPlayer) {
                    return run.apply((ServerPlayer) o);
                }
                if(o instanceof UUID) {

                    ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer((UUID) o);
                    if(pl == null) return null;

                    return run.apply(pl);
                }
            }
            return null;
        };
    }

}
