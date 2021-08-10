package me.m1dnightninja.midnightcore.fabric.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.event.PlayerJoinedEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.util.UUID;

public final class PermissionUtil {

    private static Boolean isApiPresent = null;

    public static boolean check(SharedSuggestionProvider prov, String perm) {
        return check(prov, perm, false);
    }

    public static boolean check(SharedSuggestionProvider prov, String perm, boolean def) {

        if(isApiPresent == null) {
            isApiPresent = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        }

        if(isApiPresent) {
            return checkApi(prov, perm);
        }

        return def;
    }

    public static boolean checkOrOp(SharedSuggestionProvider prov, String perm, int opLevel) {

        if(prov.hasPermission(opLevel)) return true;
        return check(prov, perm);
    }

    public static boolean check(UUID u, String perm) {
        return check(u, perm, false);
    }

    public static boolean check(UUID u, String perm, boolean def) {

        ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(u);
        if(pl == null) return def;

        if(isApiPresent == null) {
            isApiPresent = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        }

        if(isApiPresent) {
            return checkApi(pl, perm);
        }

        return def;

    }

    private static boolean checkApi(SharedSuggestionProvider prov, String perm) {
        return me.lucko.fabric.api.permissions.v0.Permissions.check(prov, perm);
    }

    private static boolean checkApi(Entity prov, String perm) {
        return me.lucko.fabric.api.permissions.v0.Permissions.check(prov, perm);
    }

    public static void registerVanillaPermissions(CommandDispatcher<CommandSourceStack> dispatcher) {

        try {
            Field req = CommandNode.class.getDeclaredField("requirement");

            for(CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChildren()) {
                if(!(node instanceof LiteralCommandNode<CommandSourceStack> lit)) continue;

                for(String s : vanilla) {
                    if(s.equals(lit.getLiteral())) {

                        req.setAccessible(true);
                        try {
                            req.set(node, lit.getRequirement().or(stack -> check(stack, "minecraft.command." + lit.getLiteral())));
                        } catch (IllegalAccessException ex) {
                            MidnightCoreAPI.getLogger().warn("Unable to apply vanilla command permission for " + lit.getLiteral() + "!");
                            ex.printStackTrace();
                        }

                    }
                }
            }

        } catch(NoSuchFieldException ex) {
            MidnightCoreAPI.getLogger().warn("Unable to enable vanilla command permissions!");
            ex.printStackTrace();
        }

        Event.register(PlayerJoinedEvent.class, MidnightCore.getInstance(), event -> {

            if(event.getPlayer().hasPermissions(2)) return;

            if(check(event.getPlayer().getUUID(), "minecraft.command.gamemode")) {

                event.getPlayer().connection.send(new ClientboundEntityEventPacket(event.getPlayer(), (byte) 26));
            }
        });

    }

    private static final String[] vanilla = {
            "advancement","attribute","ban","ban-ip","banlist","bossbar","clear","clone","data","datapack","debug","defaultgamemode","deop","difficulty","effect","enchant","execute","experience","fill","forceload","function","gamemode","gamerule","give","help","kick","kill","list","locate","locatebiome","loot","me","msg","op","pardon","pardon-ip","particle","playsound","recipe","reload","replaceitem","save-all","save-off","save-on","say","schedule","scoreboard","seed","setblock","setidletimeout","setworldspawn","spawnpoint","spectate","spreadplayers","stop","stopsound","summon","tag","team","teammsg","teleport","tell","tellraw","time","title","tm","tp","trigger","w","weather","whitelist","worldborder","xp"
    };

}
