package me.m1dnightninja.midnightcore.spigot;

import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractCustomScoreboard;
import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.JavaLogger;
import me.m1dnightninja.midnightcore.spigot.api.InventoryGUI;
import me.m1dnightninja.midnightcore.spigot.api.Timer;
import me.m1dnightninja.midnightcore.spigot.api.event.MidnightCoreLoadModulesEvent;
import me.m1dnightninja.midnightcore.spigot.config.YamlConfigProvider;
import me.m1dnightninja.midnightcore.spigot.module.LangModule;
import me.m1dnightninja.midnightcore.spigot.module.SkinModule;
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
            public Timer createTimer(MComponent text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb) {
                return new Timer(text, seconds, countUp, cb);
            }

            @Override
            public InventoryGUI createInventoryGUI(MComponent title) {
                return new InventoryGUI(title);
            }

            // TODO: Implement titles and scoreboards on Spigot
            @Override
            public AbstractTitle createTitle(MComponent comp, AbstractTitle.TitleOptions opts) {
                return null;
            }

            @Override
            public AbstractActionBar createActionBar(MComponent comp, AbstractActionBar.ActionBarOptions opts) {
                return null;
            }

            @Override
            public AbstractCustomScoreboard createCustomScoreboard(String id, MComponent title) {
                return null;
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
