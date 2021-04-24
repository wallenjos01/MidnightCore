package me.m1dnightninja.midnightcore.spigot;

import me.m1dnightninja.midnightcore.api.text.AbstractTimer;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractCustomScoreboard;
import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.JavaLogger;
import me.m1dnightninja.midnightcore.spigot.inventory.InventoryGUI;
import me.m1dnightninja.midnightcore.spigot.text.ActionBar;
import me.m1dnightninja.midnightcore.spigot.text.CustomScoreboard;
import me.m1dnightninja.midnightcore.spigot.text.Timer;
import me.m1dnightninja.midnightcore.spigot.api.event.MidnightCoreLoadModulesEvent;
import me.m1dnightninja.midnightcore.spigot.config.YamlConfigProvider;
import me.m1dnightninja.midnightcore.spigot.module.lang.LangModule;
import me.m1dnightninja.midnightcore.spigot.module.PlayerDataModule;
import me.m1dnightninja.midnightcore.spigot.module.skin.SkinModule;
import me.m1dnightninja.midnightcore.spigot.text.Title;
import me.m1dnightninja.midnightcore.spigot.util.ConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MidnightCore extends JavaPlugin {

    private static MidnightCore INSTANCE;

    @Override
    public void onEnable() {

        INSTANCE = this;

        ImplDelegate delegate = new ImplDelegate() {
            @Override
            public Timer createTimer(MComponent text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb) {
                return new Timer(text, seconds, countUp, cb);
            }

            @Override
            public InventoryGUI createInventoryGUI(MComponent title) {
                return new InventoryGUI(title);
            }

            @Override
            public AbstractTitle createTitle(MComponent comp, AbstractTitle.TitleOptions opts) {
                return new Title(comp, opts);
            }

            @Override
            public AbstractActionBar createActionBar(MComponent comp, AbstractActionBar.ActionBarOptions opts) {
                return new ActionBar(comp, opts);
            }

            @Override
            public AbstractCustomScoreboard createCustomScoreboard(String id, MComponent title) {
                return new CustomScoreboard(id, title);
            }

            @Override
            public boolean hasPermission(UUID u, String permission) {

                Player p = Bukkit.getPlayer(u);
                if(p == null) return false;

                return p.hasPermission(permission);
            }

            @Override
            public void sendMessage(UUID u, MComponent comp) {

                Player p = Bukkit.getPlayer(u);
                if(p == null) return;

                p.spigot().sendMessage(ConversionUtil.toSpigotComponent(comp));
            }
        };

        List<IModule> modules = new ArrayList<>(2);
        modules.add(new SkinModule());
        modules.add(new LangModule());
        modules.add(new PlayerDataModule());

        getServer().getPluginManager().callEvent(new MidnightCoreLoadModulesEvent(this, modules));

        YamlConfigProvider prov = new YamlConfigProvider();
        new MidnightCoreAPI(new JavaLogger(this.getLogger()), delegate, prov, getDataFolder(), modules.toArray(new IModule[0]));
    }

    @Override
    public void onDisable() {

        PlayerDataModule mod = MidnightCoreAPI.getInstance().getModule(PlayerDataModule.class);
        if(mod != null) mod.onDisable();

    }

    public static MidnightCore getInstance() {
        return INSTANCE;
    }
}
