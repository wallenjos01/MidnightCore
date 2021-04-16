package me.m1dnightninja.midnightcore.spigot;

import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.common.JavaLogger;
import me.m1dnightninja.midnightcore.spigot.api.InventoryGUI;
import me.m1dnightninja.midnightcore.spigot.api.Timer;
import me.m1dnightninja.midnightcore.spigot.api.event.MidnightCoreLoadModulesEvent;
import me.m1dnightninja.midnightcore.spigot.config.YamlConfigProvider;
import me.m1dnightninja.midnightcore.spigot.module.LangModule;
import me.m1dnightninja.midnightcore.spigot.module.SkinModule;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

            @Override
            public boolean hasPermission(UUID u, String permission) {

                Player p = Bukkit.getPlayer(u);
                if(p == null) return false;

                return p.hasPermission(permission);
            }
        };

        List<IModule> modules = new ArrayList<>(2);
        modules.set(0, new SkinModule());
        modules.set(1, new LangModule());

        getServer().getPluginManager().callEvent(new MidnightCoreLoadModulesEvent(this, modules));

        YamlConfigProvider prov = new YamlConfigProvider();
        new MidnightCoreAPI(new JavaLogger(this.getLogger()), delegate, prov, getDataFolder(), modules.toArray(new IModule[0]));
    }

}
