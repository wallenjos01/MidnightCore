package me.m1dnightninja.midnightcore.spigot;

import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.spigot.api.InventoryGUI;
import me.m1dnightninja.midnightcore.spigot.api.Timer;
import me.m1dnightninja.midnightcore.spigot.config.YamlConfigProvider;
import me.m1dnightninja.midnightcore.spigot.module.LangModule;
import me.m1dnightninja.midnightcore.spigot.module.SkinModule;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MidnightCore extends JavaPlugin {

    @Override
    public void onEnable() {

        ImplDelegate delegate = new ImplDelegate() {
            @Override
            public Timer createTimer(String text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb) {
                return new Timer(new TextComponent(TextComponent.fromLegacyText(text)), seconds, countUp, cb);
            }

            @Override
            public InventoryGUI createInventoryGUI(String title) {
                return new InventoryGUI(title);
            }

        };

        YamlConfigProvider prov = new YamlConfigProvider();
        ConfigSection main = prov.fromYaml(getConfig());

        new MidnightCoreAPI(new Logger(this.getLogger()), delegate, prov, new File(getDataFolder(), "config.yml"), main, new SkinModule(), new LangModule());
    }

}
