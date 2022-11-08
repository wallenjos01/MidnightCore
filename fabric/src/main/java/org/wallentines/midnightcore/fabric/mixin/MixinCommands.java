package org.wallentines.midnightcore.fabric.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightcore.fabric.event.server.CommandSendEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(Commands.class)
public abstract class MixinCommands {

    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at=@At(value = "FIELD", target="Lnet/minecraft/commands/Commands$CommandSelection;includeIntegrated:Z", opcode = Opcodes.GETFIELD))
    private void beforeIntegrated(Commands.CommandSelection commandSelection, CommandBuildContext ctx, CallbackInfo ci) {

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api != null && api.getConfig().getBoolean("vanilla_permissions")) {
            for(CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChildren()) {
                if(!(node instanceof LiteralCommandNode<CommandSourceStack> lit)) continue;
                ((AccessorCommandNode) lit).setRequirement(lit.getRequirement().or(Permissions.require("minecraft.command." + lit.getLiteral())));
            }
        }

        Event.invoke(new CommandLoadEvent(dispatcher, commandSelection, ctx));
    }

    @Inject(method="performCommand", at=@At(value="INVOKE", target="Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I"), cancellable = true)
    private void onCommand(ParseResults<CommandSourceStack> parseResults, String string, CallbackInfoReturnable<Integer> cir) {

        CommandSendEvent event = new CommandSendEvent(parseResults.getContext().getSource(), string, dispatcher);
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }

}
