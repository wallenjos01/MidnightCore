package me.m1dnightninja.midnightcore.fabric.module;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.common.JsonWrapper;
import me.m1dnightninja.midnightcore.common.module.AbstractPermissionModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.lang.reflect.Field;

public class PermissionModule extends AbstractPermissionModule {

    @Override
    public boolean initialize(ConfigSection sec) {

        File f = new File(MidnightCore.getInstance().getConfigDirectory(), "permissions.json");

        if(!f.exists()) {
            new JsonWrapper().save(f);
        }

        section = MidnightCore.getInstance().getDefaultConfigProvider().loadFromFile(f);

        if(sec.has("vanilla_permissions", Boolean.class) && sec.getBoolean("vanilla_permissions")) {

            try {
                Field req = CommandNode.class.getDeclaredField("requirement");

                CommandRegistrationCallback.EVENT.register((dispatcher, b) -> {

                    for(CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChildren()) {
                        if(!(node instanceof LiteralCommandNode)) continue;

                        LiteralCommandNode<CommandSourceStack> lit = (LiteralCommandNode<CommandSourceStack>) node;

                        for(String s : vanilla) {
                            if(s.equals(lit.getLiteral())) {

                                req.setAccessible(true);
                                try {
                                    req.set(node, lit.getRequirement().or(stack -> hasPermission(stack, "minecraft.command." + lit.getLiteral())));
                                } catch (IllegalAccessException ex) {
                                    MidnightCoreAPI.getLogger().warn("Unable to apply vanilla command permission for " + lit.getLiteral() + "!");
                                    ex.printStackTrace();
                                }

                            }
                        }
                    }
                });

            } catch(NoSuchFieldException ex) {
                MidnightCoreAPI.getLogger().warn("Unable to enable vanilla command permissions!");
                ex.printStackTrace();
            }
        }

        return super.initialize(sec);
    }

    public boolean hasPermission(CommandSourceStack stack, String permission) {

        if(stack.getEntity() instanceof ServerPlayer) {
            return hasPermission(stack.getEntity().getUUID(), permission);
        }

        return true;
    }

    @Override
    public void reloadAllPermissions() {
        super.reloadAllPermissions();

        for(ServerPlayer pl : MidnightCore.getServer().getPlayerList().getPlayers()) {
            MidnightCore.getServer().getPlayerList().sendPlayerPermissionLevel(pl);
        }
    }

    @Override
    public ConfigSection getDefaultConfig() {

        ConfigSection sec = new ConfigSection();
        sec.set("vanilla_permissions", true);

        return sec;
    }
    
    private static final String[] vanilla = {
        "advancement","attribute","ban","ban-ip","banlist","bossbar","clear","clone","data","datapack","debug","defaultgamemode","deop","difficulty","effect","enchant","execute","experience","fill","forceload","function","gamemode","gamerule","give","help","kick","kill","list","locate","locatebiome","loot","me","msg","op","pardon","pardon-ip","particle","playsound","recipe","reload","replaceitem","save-all","save-off","save-on","say","schedule","scoreboard","seed","setblock","setidletimeout","setworldspawn","spawnpoint","spectate","spreadplayers","stop","stopsound","summon","tag","team","teammsg","teleport","tell","tellraw","time","title","tm","tp","trigger","w","weather","whitelist","worldborder","xp"
    };
    
}
