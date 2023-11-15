package org.wallentines.mcore;

import org.wallentines.mcore.lang.CustomPlaceholder;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.function.Consumer;

public class MainCommand {

    public static int executeMain(Server server, Consumer<Component> messageSender) {

        messageSender.accept(message("command.main", server));
        return 1;
    }

    public static int executeLoadModule(Server server, Identifier moduleId, Consumer<Component> messageSender) {

        if(server.getModuleManager().isModuleLoaded(moduleId)) {

            messageSender.accept(message("command.module.already_loaded", CustomPlaceholder.inline("module_id", moduleId)));
            return 1;
        }

        ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(moduleId);
        if(info == null) {
            messageSender.accept(message("error.module_not_found", CustomPlaceholder.inline("module_id", moduleId)));
            return 0;
        }

        if(ModuleUtil.loadModule(server.getModuleManager(), info, server, server.getModuleConfig())) {
            messageSender.accept(message("command.module.loaded", CustomPlaceholder.inline("module_id", moduleId)));
            return 2;
        }

        messageSender.accept(message("error.module_load_failed", CustomPlaceholder.inline("module_id", moduleId)));
        return 0;
    }

    public static int executeUnloadModule(Server server, Identifier moduleId, Consumer<Component> messageSender) {

        if(!server.getModuleManager().isModuleLoaded(moduleId)) {
            messageSender.accept(message("command.module.already_unloaded", CustomPlaceholder.inline("module_id", moduleId)));
            return 1;
        }

        server.getModuleManager().unloadModule(moduleId);
        messageSender.accept(message("command.module.unloaded", CustomPlaceholder.inline("module_id", moduleId)));
        return 2;

    }

    public static int executeReloadModule(Server server, Identifier moduleId, Consumer<Component> messageSender) {

        if(moduleId == null) {

            server.getModuleManager().reloadAll(server.getModuleConfig().getRoot().asSection(), server, ServerModule.REGISTRY);
            int loaded = server.getModuleManager().getCount();

            messageSender.accept(message("command.module.reload.all", CustomPlaceholder.inline("count", loaded)));
            return loaded;

        } else {

            ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(moduleId);
            if(info == null) {
                messageSender.accept(message("error.module_not_found", CustomPlaceholder.inline("module_id", moduleId)));
                return 0;
            }

            if(ModuleUtil.reloadModule(server.getModuleManager(), info, server, server.getModuleConfig())) {
                messageSender.accept(message("command.module.reload", CustomPlaceholder.inline("module_id", moduleId)));
                return 1;
            }

            messageSender.accept(message("error.module_load_failed", CustomPlaceholder.inline("module_id", moduleId)));
            return 0;
        }
    }

    public static int executeEnableModule(Server server, Identifier moduleId, Consumer<Component> messageSender) {

        if(setModuleEnabled(server, moduleId, true, messageSender)) {
            messageSender.accept(message("command.module.enabled", CustomPlaceholder.inline("module_id", moduleId)));
            return 1;
        }
        return 0;
    }

    public static int executeDisableModule(Server server, Identifier moduleId, Consumer<Component> messageSender) {

        if(setModuleEnabled(server, moduleId, false, messageSender)) {
            messageSender.accept(message("command.module.disabled", CustomPlaceholder.inline("module_id", moduleId)));
            return 1;
        }
        return 0;
    }

    public static int executeListModules(Server server, Consumer<Component> messageSender) {

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        messageSender.accept(message("command.module.list.header", server));

        for(Identifier id : ServerModule.REGISTRY.getIds()) {

            String state = server.getModuleManager().isModuleLoaded(id) ? "loaded" : "unloaded";
            messageSender.accept(message(
                    "command.module.list.entry",
                    server,
                    CustomPlaceholder.inline("module_id", id),
                    CustomPlaceholder.of("state", () -> mcore.getLangManager().component("module.state." + state))
            ));
        }

        return 1;
    }

    public static int executeReload(Consumer<Component> messageSender) {

        long start = System.currentTimeMillis();

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        mcore.getLangManager().reload();

        long elapsed = System.currentTimeMillis() - start;
        messageSender.accept(message("command.reload", CustomPlaceholder.inline("time", elapsed)));

        return 1;
    }


    private static boolean setModuleEnabled(Server server, Identifier moduleId, boolean enabled, Consumer<Component> messageSender) {
        ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(moduleId);
        if(info == null) {
            messageSender.accept(message("error.module_not_found", CustomPlaceholder.inline("module_id", moduleId)));
            return false;
        }

        FileWrapper<ConfigObject> obj = server.getModuleConfig();
        obj.getRoot().asSection().getOrCreateSection(moduleId.toString()).set("enabled", enabled);
        obj.save();
        return true;
    }

    private static Component message(String key, Object... args) {

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        return mcore.getLangManager().component(key, args);
    }

}
