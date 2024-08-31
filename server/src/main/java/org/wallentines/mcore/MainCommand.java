package org.wallentines.mcore;

import org.wallentines.mcore.lang.CustomPlaceholder;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class MainCommand {

    public static int executeMain(Server server, CommandSender sender) {

        sender.sendSuccess(message("command.main", sender, server), false);
        return 1;
    }

    public static int executeLoadModule(Server server, Identifier moduleId, CommandSender sender) {

        if(server.getModuleManager().isModuleLoaded(moduleId)) {

            sender.sendSuccess(message("command.module.already_loaded", CustomPlaceholder.inline("module_id", moduleId)), false);
            return 1;
        }

        ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(moduleId);
        if(info == null) {
            sender.sendSuccess(message("error.module_not_found", CustomPlaceholder.inline("module_id", moduleId)), false);
            return 0;
        }

        if(ModuleUtil.loadModule(server.getModuleManager(), info, server, server.getModuleConfig())) {
            sender.sendSuccess(message("command.module.loaded", CustomPlaceholder.inline("module_id", moduleId)), false);
            return 2;
        }

        sender.sendSuccess(message("error.module_load_failed", CustomPlaceholder.inline("module_id", moduleId)), false);
        return 0;
    }

    public static int executeUnloadModule(Server server, Identifier moduleId, CommandSender sender) {

        if(!server.getModuleManager().isModuleLoaded(moduleId)) {
            sender.sendSuccess(message("command.module.already_unloaded", CustomPlaceholder.inline("module_id", moduleId)), false);
            return 1;
        }

        server.getModuleManager().unloadModule(moduleId);
        sender.sendSuccess(message("command.module.unloaded", CustomPlaceholder.inline("module_id", moduleId)), false);
        return 2;

    }

    public static int executeReloadModule(Server server, Identifier moduleId, CommandSender sender) {

        if(moduleId == null) {

            server.getModuleManager().reloadAll(server.getModuleConfig().getRoot().asSection());
            int loaded = server.getModuleManager().getCount();

            sender.sendSuccess(message("command.module.reload.all", CustomPlaceholder.inline("count", loaded)), false);
            return loaded;

        } else {

            ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(moduleId);
            if(info == null) {
                sender.sendSuccess(message("error.module_not_found", CustomPlaceholder.inline("module_id", moduleId)), false);
                return 0;
            }

            if(ModuleUtil.reloadModule(server.getModuleManager(), info, server, server.getModuleConfig())) {
                sender.sendSuccess(message("command.module.reload", CustomPlaceholder.inline("module_id", moduleId)), false);
                return 1;
            }

            sender.sendSuccess(message("error.module_load_failed", CustomPlaceholder.inline("module_id", moduleId)), false);
            return 0;
        }
    }

    public static int executeEnableModule(Server server, Identifier moduleId, CommandSender sender) {

        if(setModuleEnabled(server, moduleId, true, sender)) {
            sender.sendSuccess(message("command.module.enabled", CustomPlaceholder.inline("module_id", moduleId)), false);
            return 1;
        }
        return 0;
    }

    public static int executeDisableModule(Server server, Identifier moduleId, CommandSender sender) {

        if(setModuleEnabled(server, moduleId, false, sender)) {
            sender.sendSuccess(message("command.module.disabled", CustomPlaceholder.inline("module_id", moduleId)), false);
            return 1;
        }
        return 0;
    }

    public static int executeListModules(Server server, CommandSender sender) {

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        sender.sendSuccess(message("command.module.list.header", server), false);

        for(Identifier id : ServerModule.REGISTRY.getIds()) {

            String state = server.getModuleManager().isModuleLoaded(id) ? "loaded" : "unloaded";
            sender.sendSuccess(message(
                    "command.module.list.entry",
                    server,
                    CustomPlaceholder.inline("module_id", id),
                    CustomPlaceholder.of("state", (ctx) -> mcore
                            .getLangManager()
                            .component("module.state." + state)
                            .resolve(ctx))
            ), false);
        }

        return 1;
    }

    public static int executeReload(CommandSender sender) {

        long start = System.currentTimeMillis();

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        mcore.getLangManager().reload();

        long elapsed = System.currentTimeMillis() - start;
        sender.sendSuccess(message("command.reload", CustomPlaceholder.inline("time", elapsed)), false);

        return 1;
    }


    private static boolean setModuleEnabled(Server server, Identifier moduleId, boolean enabled, CommandSender sender) {
        ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(moduleId);
        if(info == null) {
            sender.sendSuccess(message("error.module_not_found", CustomPlaceholder.inline("module_id", moduleId)), false);
            return false;
        }

        FileWrapper<ConfigObject> obj = server.getModuleConfig();
        obj.getRoot().asSection().getOrCreateSection(moduleId.toString()).set("enabled", enabled);
        obj.save();
        return true;
    }

    private static Component message(String key, Object... args) {

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        return mcore.getLangManager().component(key, args).resolve();
    }

}
