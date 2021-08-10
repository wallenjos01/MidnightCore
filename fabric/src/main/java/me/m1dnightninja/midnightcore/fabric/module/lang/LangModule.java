package me.m1dnightninja.midnightcore.fabric.module.lang;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.lang.AbstractLangModule;
import me.m1dnightninja.midnightcore.common.module.lang.LangProvider;
import me.m1dnightninja.midnightcore.fabric.event.PlayerChangeSettingsEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.util.HashMap;

public class LangModule extends AbstractLangModule {

    private final HashMap<MPlayer, String> languages = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection configuration) {

        super.initialize(configuration);

        Event.register(PlayerChangeSettingsEvent.class, this, event -> languages.put(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUUID()), event.getLocale()));
        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return new ConfigSection();
    }

    @Override
    public String getPlayerLocale(MPlayer u) {

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

    public static void sendCommandSuccess(CommandContext<CommandSourceStack> context, ILangProvider langProvider, boolean notify, String key, Object... args) {

        MPlayer u = null;
        try {
            ServerPlayer pl = context.getSource().getPlayerOrException();
            u = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(pl.getUUID());

        } catch(CommandSyntaxException ex) {
            // Ignore
        }

        context.getSource().sendSuccess(ConversionUtil.toMinecraftComponent(langProvider.getMessage(key, u, args)), notify);

    }

    public static void sendCommandFailure(CommandContext<CommandSourceStack> context, ILangProvider langProvider, String key, Object... args) {

        MPlayer u = null;
        try {
            ServerPlayer pl = context.getSource().getPlayerOrException();
            u = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(pl.getUUID());

        } catch(CommandSyntaxException ex) {
            // Ignore
        }

        context.getSource().sendFailure(ConversionUtil.toMinecraftComponent(langProvider.getMessage(key, u, args)));

    }

}
