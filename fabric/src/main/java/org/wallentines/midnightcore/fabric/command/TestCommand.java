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
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.savepoint.SavepointModule;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.module.skin.Skinnable;
import org.wallentines.midnightcore.api.module.vanish.VanishModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.util.UUID;

public class TestCommand {

    private static final Identifier SAVEPOINT_ID = new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "test");
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
            .then(Commands.literal("item")
                .executes(TestCommand::itemTestCommand)
            )
            .then(Commands.literal("gui")
                .executes(TestCommand::guiTestCommand)
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

    private static int itemTestCommand(CommandContext<CommandSourceStack> context) {

        try {
            ServerPlayer spl = extractPlayer(context);
            FabricPlayer fpl = FabricPlayer.wrap(spl);

            fpl.giveItem(MItemStack.Builder
                    .headWithSkin(new Skin(UUID.fromString("bc4ea7fc-63c3-415a-b5b9-204e5acadd5c"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRkODQ0ZmVlMjRkNWYyN2RkYjY2OTQzODUyOGQ4M2I2ODRkOTAxYjc1YTY4ODlmZTc0ODhkZmM0Y2Y3YTFjIn19fQ==", ""))
                    .withName(fpl.getName()).build());

        } catch (Throwable th) {
            th.printStackTrace();
        }

        return 1;
    }

    private static int guiTestCommand(CommandContext<CommandSourceStack> context) {

        try {
            ServerPlayer spl = extractPlayer(context);
            FabricPlayer fpl = FabricPlayer.wrap(spl);

            InventoryGUI gui = fpl.getServer().getMidnightCore().createGUI(new MTextComponent("Test GUI").withStyle(new MStyle().withColor(TextColor.RED)));
            gui.setItem(4, MItemStack.Builder.paneWithColor(TextColor.BLUE).withName(new MTextComponent("Hello").withStyle(new MStyle().withColor(TextColor.GOLD))).build(), (click, mpl) -> {
                mpl.sendMessage(new MTextComponent("Hello"));
            });

            gui.open(fpl, 0);

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
