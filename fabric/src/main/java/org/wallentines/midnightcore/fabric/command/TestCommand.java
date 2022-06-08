package org.wallentines.midnightcore.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.LevelStem;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.SavepointModule;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.module.skin.Skinnable;
import org.wallentines.midnightcore.api.module.vanish.VanishModule;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.Registries;
import org.wallentines.midnightcore.fabric.module.dimension.DimensionModule;
import org.wallentines.midnightcore.fabric.module.dimension.EmptyGenerator;
import org.wallentines.midnightcore.fabric.module.dimension.WorldCreator;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.io.File;
import java.util.UUID;

public class TestCommand {

    private static final Identifier SAVEPOINT_ID = new Identifier(Constants.DEFAULT_NAMESPACE, "test");
    private static final UUID SKIN_UUID = UUID.fromString("ce784258-10ca-45fb-b787-8dde07375f2b");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mcoretest")
            .requires(Permissions.require(Constants.DEFAULT_NAMESPACE + ".command.test", 4))
            .then(Commands.literal("skin")
                .executes(TestCommand::skinTestCommand)
            )
            .then(Commands.literal("savepoint")
                .then(Commands.literal("save")
                    .executes(TestCommand::saveTestCommand)
                )
                .then(Commands.literal("load")
                    .executes(TestCommand::loadTestCommand)
                )
            )
            .then(Commands.literal("vanish")
                .executes(TestCommand::vanishTestCommand)
            )
            .then(Commands.literal("dimension")
                .executes(TestCommand::dimensionTestCommand)
            )
            .then(Commands.literal("requirement")
                .then(Commands.argument("type", ResourceLocationArgument.id())
                    .then(Commands.argument("value", StringArgumentType.greedyString())
                        .executes(context -> requirementTestCommand(context, context.getArgument("type", ResourceLocation.class), context.getArgument("value", String.class)))
                    )
                )
            )
        );
    }

    private static int skinTestCommand(CommandContext<CommandSourceStack> context) {

        ServerPlayer spl = extractPlayer(context);
        if(spl == null) return 0;

        SkinModule mod = MidnightCoreAPI.getInstance().getModuleManager().getModule(SkinModule.class);

        mod.getOnlineSkinAsync(SKIN_UUID, skin -> {
            try {

                ((Skinnable) spl).setSkin(skin);

                /*FabricPlayer fp = FabricPlayer.wrap(spl);
                mod.setSkin(fp, skin);
                mod.updateSkin(fp);*/

            } catch (Throwable th) {
                th.printStackTrace();
                throw th;
            }
            context.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Skin acquired")), false);
        });

        return 0;
    }

    private static int saveTestCommand(CommandContext<CommandSourceStack> context) {

        ServerPlayer spl = extractPlayer(context);
        if(spl == null) return 0;

        try {
            SavepointModule mod = MidnightCoreAPI.getInstance().getModuleManager().getModule(SavepointModule.class);
            mod.savePlayer(FabricPlayer.wrap(spl), SAVEPOINT_ID);

            context.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Saved!")), false);

        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
        return 0;
    }

    private static int loadTestCommand(CommandContext<CommandSourceStack> context) {

        ServerPlayer spl = extractPlayer(context);
        if(spl == null) return 0;

        try {
            SavepointModule mod = MidnightCoreAPI.getInstance().getModuleManager().getModule(SavepointModule.class);
            mod.loadPlayer(FabricPlayer.wrap(spl), SAVEPOINT_ID);

            context.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Loaded!")), false);

        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
        return 0;
    }

    private static int vanishTestCommand(CommandContext<CommandSourceStack> context) {

        try {
            ServerPlayer spl = extractPlayer(context);
            if(spl == null) return 0;

            MPlayer mpl = FabricPlayer.wrap(spl);

            VanishModule mod = MidnightCoreAPI.getInstance().getModuleManager().getModule(VanishModule.class);
            if(mod.isVanished(mpl)) {

                mod.revealPlayer(mpl);
                context.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Unvanished!")), false);
            } else {

                mod.vanishPlayer(mpl);
                context.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Vanished!")), false);
            }
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
        return 0;
    }

    private static int dimensionTestCommand(CommandContext<CommandSourceStack> context) {

        try {

            ServerPlayer spl = extractPlayer(context);
            if(spl == null) return 0;

            MPlayer mpl = FabricPlayer.wrap(spl);

            DimensionModule mod = MidnightCoreAPI.getInstance().getModuleManager().getModule(DimensionModule.class);

            File dim = new File(MidnightCoreAPI.getInstance().getDataFolder(), "test_dimension");

            WorldCreator cre = new WorldCreator(new ResourceLocation(Constants.DEFAULT_NAMESPACE, "test"), LevelStem.NETHER, EmptyGenerator.FOREST);
            cre.setFolderName(dim.getName());

            context.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Loading dimension...")), false);
            mod.createWorld(cre, dim.toPath(), w -> {
                context.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Dimension loaded! Teleporting...")), false);
                mpl.teleport(new Location(ConversionUtil.toIdentifier(w.dimension().location()), new Vec3d(0,100,0), 0, 0));
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        return 0;
    }

    private static int requirementTestCommand(CommandContext<CommandSourceStack> context, ResourceLocation loc, String data) {

        ServerPlayer spl = extractPlayer(context);
        if(spl == null) return 0;

        try {
            RequirementType<MPlayer> type = Registries.REQUIREMENT_REGISTRY.get(ConversionUtil.toIdentifier(loc));

            Requirement<MPlayer> requirement = new Requirement<>(type, data);
            boolean success = requirement.check(FabricPlayer.wrap(spl));

            context.getSource().sendSuccess(MutableComponent.create(new LiteralContents(success ? "success" : "failure")), false);

        } catch (Throwable th) {
            th.printStackTrace();
        }
        return 1;
    }


    private static ServerPlayer extractPlayer(CommandContext<CommandSourceStack> context) {

        ServerPlayer spl;
        try {
            spl = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException ex) {
            context.getSource().sendFailure(MutableComponent.create(new LiteralContents("Only players can execute this command!")));
            return null;
        }
        return spl;
    }
}
