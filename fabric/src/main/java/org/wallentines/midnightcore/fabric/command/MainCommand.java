package org.wallentines.midnightcore.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.Registries;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;

public class MainCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mcore")
            .requires(Permissions.require(Constants.DEFAULT_NAMESPACE + ".command", 4))
            .executes(MainCommand::emptyCommand)
            .then(Commands.literal("modules")
                .executes(MainCommand::modulesCommand)
            )
        );

    }


    private static int emptyCommand(CommandContext<CommandSourceStack> context) {

        try {
            LangProvider prov = MidnightCore.getInstance().getLangProvider();

            context.getSource().sendSuccess(ConversionUtil.toComponent(prov.getMessage("mcore.command.main", "en_us")), false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;

    }

    private static int modulesCommand(CommandContext<CommandSourceStack> context) {

        try {
            LangProvider prov = MidnightCore.getInstance().getLangProvider();

            context.getSource().sendSuccess(ConversionUtil.toComponent(prov.getMessage("mcore.command.modules.header", "en_us")), false);

            ModuleManager<MidnightCoreAPI> manager = MidnightCoreAPI.getInstance().getModuleManager();
            for(ModuleInfo<MidnightCoreAPI> info : Registries.MODULE_REGISTRY) {

                CustomPlaceholderInline cp = CustomPlaceholderInline.create("module_id", info.getId().toString());

                if(manager.isModuleLoaded(info.getId())) {
                    context.getSource().sendSuccess(ConversionUtil.toComponent(prov.getMessage("mcore.command.modules.enabled", "en_us", cp)), false);
                } else {
                    context.getSource().sendSuccess(ConversionUtil.toComponent(prov.getMessage("mcore.command.modules.disabled", "en_us", cp)), false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;

    }

}
