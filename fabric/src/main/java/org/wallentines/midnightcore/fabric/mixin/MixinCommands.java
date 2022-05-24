package org.wallentines.midnightcore.fabric.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
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

import java.lang.reflect.Field;

@Mixin(Commands.class)
public abstract class MixinCommands {

    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method="<init>", at=@At(value="INVOKE", target="Lcom/mojang/brigadier/CommandDispatcher;findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V"))
    private void onInit(Commands.CommandSelection commandSelection, CallbackInfo ci) {

        Event.invoke(new CommandLoadEvent(dispatcher, commandSelection));

    }

    @Inject(method = "<init>", at=@At(value = "FIELD", target="Lnet/minecraft/commands/Commands$CommandSelection;includeIntegrated:Z", opcode = Opcodes.GETFIELD))
    private void beforeIntegrated(Commands.CommandSelection commandSelection, CallbackInfo ci) {

        if(MidnightCoreAPI.getInstance().getConfig().getBoolean("vanilla_permissions")) {

            try {
                Field field = CommandNode.class.getDeclaredField("requirement");
                field.setAccessible(true);

                for(CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChildren()) {
                    if(!(node instanceof LiteralCommandNode<CommandSourceStack> lit)) continue;

                    try {
                        field.set(node, lit.getRequirement().or(Permissions.require("minecraft.command." + lit.getLiteral())));
                    } catch (IllegalAccessException ex) {
                        MidnightCoreAPI.getLogger().warn("Unable to apply vanilla command permission for " + lit.getLiteral() + "!");
                        ex.printStackTrace();
                    }
                }

            } catch (NoSuchFieldException ex) {
                MidnightCoreAPI.getLogger().warn("Unable to register vanilla command permissions!");
            }
        }
    }

    @Inject(method="performCommand", at=@At(value="INVOKE", target="Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)I"), cancellable = true)
    private void onCommand(CommandSourceStack commandSourceStack, String string, CallbackInfoReturnable<Integer> cir) {

        CommandSendEvent event = new CommandSendEvent(commandSourceStack, string, dispatcher);
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }

}
