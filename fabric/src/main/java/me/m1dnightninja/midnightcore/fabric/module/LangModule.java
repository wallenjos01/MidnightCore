package me.m1dnightninja.midnightcore.fabric.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.lang.AbstractLangProvider;
import me.m1dnightninja.midnightcore.common.module.AbstractLangModule;
import me.m1dnightninja.midnightcore.fabric.api.LangProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
    public AbstractLangProvider createProvider(String name, File folder, HashMap<String, String> defaults) {

        try {
            LangProvider prov = new LangProvider(folder, this, defaults);
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
    public boolean initialize(ConfigSection section) {

        registerRawPlaceholder("player_display_name", createSupplier(ServerPlayer.class, Player::getDisplayName));
        registerRawPlaceholder("player_name", createSupplier(ServerPlayer.class, Player::getName));
        registerRawPlaceholder("player_tablist_name", createSupplier(ServerPlayer.class, ServerPlayer::getTabListDisplayName));
        registerStringPlaceholder("player_profile_name", createSupplier(ServerPlayer.class, obj -> obj.getGameProfile().getName()));
        registerStringPlaceholder("player_uuid_name", createSupplier(ServerPlayer.class, Player::getStringUUID));

        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return null;
    }


}
