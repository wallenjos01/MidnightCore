package org.wallentines.midnightcore.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.util.Collections;

public class ExecuteAugment {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralCommandNode<CommandSourceStack> executeNode = (LiteralCommandNode<CommandSourceStack>) dispatcher.getRoot().getChild("execute");
        dispatcher.register(executeNode.createBuilder()
            .then(Commands.literal("requirement")
                .then(Commands.argument("type", ResourceLocationArgument.id())
                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(Registries.REQUIREMENT_REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder))
                    .then(Commands.argument("data", StringArgumentType.string())
                        .fork(executeNode, ctx -> {

                            ServerPlayer sp = ctx.getSource().getPlayer();
                            if(sp == null) return Collections.emptyList();

                            MPlayer mpl = FabricPlayer.wrap(sp);
                            RequirementType<MPlayer> type = Registries.REQUIREMENT_REGISTRY.get(ConversionUtil.toIdentifier(ResourceLocationArgument.getId(ctx, "type")));
                            if(type == null) return Collections.emptyList();

                            Requirement<MPlayer> req = new Requirement<>(type, ctx.getArgument("data", String.class));
                            return req.check(mpl) ? Collections.singletonList(ctx.getSource()) : Collections.emptyList();
                        })
                    )
                )
            )
        );
    }

}
