package me.m1dnightninja.midnightcore.spigot;

import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.text.MTimer;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.MScoreboard;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.MidnightCoreImpl;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.spigot.event.MidnightCoreAPIInitializedEvent;
import me.m1dnightninja.midnightcore.spigot.inventory.SpigotInventoryGUI;
import me.m1dnightninja.midnightcore.spigot.inventory.SpigotItem;
import me.m1dnightninja.midnightcore.spigot.module.savepoint.SavePointModule;
import me.m1dnightninja.midnightcore.spigot.module.vanish.VanishModule;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayerManager;
import me.m1dnightninja.midnightcore.spigot.text.SpigotScoreboard;
import me.m1dnightninja.midnightcore.spigot.text.SpigotTimer;
import me.m1dnightninja.midnightcore.spigot.event.MidnightCoreLoadModulesEvent;
import me.m1dnightninja.midnightcore.spigot.config.YamlConfigProvider;
import me.m1dnightninja.midnightcore.spigot.module.lang.LangModule;
import me.m1dnightninja.midnightcore.spigot.module.playerdata.PlayerDataModule;
import me.m1dnightninja.midnightcore.spigot.module.skin.SkinModule;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MidnightCore extends JavaPlugin {

    private static MidnightCore INSTANCE;

    @Override
    public void onEnable() {

        INSTANCE = this;

        ConfigRegistry.INSTANCE.setDefaultProvider(YamlConfigProvider.INSTANCE);
        ConfigRegistry.INSTANCE.registerProvider(JsonConfigProvider.INSTANCE);

        List<IModule> modules = new ArrayList<>(2);
        modules.add(new SkinModule());
        modules.add(new LangModule());
        modules.add(new PlayerDataModule());
        modules.add(new SavePointModule());
        modules.add(new VanishModule());

        getServer().getPluginManager().callEvent(new MidnightCoreLoadModulesEvent(this, modules));

        MidnightCoreAPI api = new MidnightCoreImpl(new SpigotPlayerManager(), SpigotItem::new, getDataFolder(), modules.toArray(new IModule[0])) {
            @Override
            public SpigotTimer createTimer(MComponent text, int seconds, boolean countUp, MTimer.TimerCallback cb) {
                return new SpigotTimer(text, seconds, countUp, cb);
            }

            @Override
            public SpigotInventoryGUI createInventoryGUI(MComponent title) {
                return new SpigotInventoryGUI(title);
            }

            @Override
            public MScoreboard createScoreboard(String id, MComponent title) {
                return new SpigotScoreboard(id, title);
            }

            @Override
            public void executeConsoleCommand(String cmd) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }

            @Override
            public String getGameVersion() {

                String ver = getServer().getVersion();
                if(ver.contains("(MC: ")) {

                    ver = ver.substring(ver.indexOf("(MC: ") + 5, ver.length() - 1);

                }
                return ver;
            }

            @Override
            public boolean isProxy() {
                return false;
            }
        };
        api.getConfigRegistry().registerProvider(JsonConfigProvider.INSTANCE);

        getServer().getPluginManager().callEvent(new MidnightCoreAPIInitializedEvent(this, api));
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
