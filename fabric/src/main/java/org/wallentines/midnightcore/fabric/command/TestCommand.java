package org.wallentines.midnightcore.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.LevelStem;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.SavepointModule;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.module.skin.Skinnable;
import org.wallentines.midnightcore.api.module.vanish.VanishModule;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.common.util.Util;
import org.wallentines.midnightcore.fabric.level.EmptyGenerator;
import org.wallentines.midnightcore.fabric.level.DynamicLevelContext;
import org.wallentines.midnightcore.fabric.level.DynamicLevelStorage;
import org.wallentines.midnightcore.fabric.level.WorldConfig;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.nio.file.Path;
import java.util.UUID;

public class TestCommand {

    private static final Identifier SAVEPOINT_ID = new Identifier(Constants.DEFAULT_NAMESPACE, "test");
    private static final UUID SKIN_UUID = UUID.fromString("ce784258-10ca-45fb-b787-8dde07375f2b");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mcoretest")
            .requires(Permissions.require(Constants.makeNode("command.test"), 4))
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
            .then(Commands.literal("placeholder")
                .then(Commands.argument("value", StringArgumentType.greedyString())
                    .executes(TestCommand::placeholderTestCommand)
                )
            )
            .then(Commands.literal("scoreboard")
                .executes(TestCommand::scoreboardTestCommand)
            )
        );
    }

    private static int skinTestCommand(CommandContext<CommandSourceStack> context) {

        ServerPlayer spl = extractPlayer(context);
        if(spl == null) return 0;

        SkinModule mod = MidnightCoreAPI.getModule(SkinModule.class);
        if(mod == null) throw new CommandRuntimeException(Component.literal("Skin Module is not loaded!"));

        mod.getOnlineSkinAsync(SKIN_UUID, skin -> {
            try {

                ((Skinnable) spl).setSkin(skin);

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
            SavepointModule mod = MidnightCoreAPI.getModule(SavepointModule.class);
            if(mod == null) throw new CommandRuntimeException(Component.literal("Savepoint Module is not loaded!"));

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
            SavepointModule mod = MidnightCoreAPI.getModule(SavepointModule.class);
            if(mod == null) throw new CommandRuntimeException(Component.literal("Savepoint Module is not loaded!"));

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

            VanishModule mod = MidnightCoreAPI.getModule(VanishModule.class);
            if(mod == null) throw new CommandRuntimeException(Component.literal("Vanish Module is not loaded!"));

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

            WorldConfig conf = new WorldConfig(new Identifier(Constants.DEFAULT_NAMESPACE, "test"))
                .generator(EmptyGenerator.create(Biomes.FOREST))
                .rootDimensionType(LevelStem.NETHER);

            Path dataFolder = Util.getOr(MidnightCoreAPI.getInstance(), inst -> inst.getDataFolder().toPath(), () -> Path.of(""));

            DynamicLevelStorage storage = DynamicLevelStorage.create(dataFolder, dataFolder.resolve("backups"));
            DynamicLevelContext ctx = storage.createWorldContext("test", conf);

            ctx.loadDimension(conf.getRootDimensionId(), w -> mpl.teleport(new Location(ConversionUtil.toIdentifier(w.dimension().location()), new Vec3d(0.0d, 100.0d, 0.0d), 0.0f, 0.0f)));
            mpl.sendMessage(new MTextComponent("Loading dimension..."));

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

    private static int placeholderTestCommand(CommandContext<CommandSourceStack> context) {

        try {

            String arg = context.getArgument("value", String.class);
            ServerPlayer spl = extractPlayer(context);

            if(spl == null) return 0;

            FabricPlayer mpl = FabricPlayer.wrap(spl);
            MidnightCoreAPI api = MidnightCoreAPI.getInstance();

            MComponent txt = PlaceholderManager.INSTANCE.parseText(arg, mpl, api == null ? null : api.getLangProvider());
            mpl.sendMessage(txt);

        } catch (Throwable th) {
            th.printStackTrace();
        }
        return 1;
    }

    private static int scoreboardTestCommand(CommandContext<CommandSourceStack> context) {

        try {
            ServerPlayer spl = extractPlayer(context);
            FabricPlayer fpl = FabricPlayer.wrap(spl);
            MServer srv = fpl.getServer();

            CustomScoreboard sb = srv.getMidnightCore().createScoreboard(CustomScoreboard.generateRandomId(), MComponent.parse("&aScoreboard Test"));
            sb.setLine(9, MComponent.parse("Line 9"));
            sb.setLine(6, PlaceholderManager.INSTANCE.parseText("Hello, %player_name%", fpl));

            sb.addViewer(fpl);

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
