package org.wallentines.mcore.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.module.ModuleManager;

import java.util.Collection;
import java.util.UUID;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server {


    @Unique
    private final ModuleManager<Server, ServerModule> midnightcore$moduleManager = new ModuleManager<>();

    @Unique
    private final HandlerList<Server> midnightcore$tickEvent = new HandlerList<>();

    @Shadow private PlayerList playerList;

    @Shadow public abstract Commands getCommands();

    @Shadow public abstract CommandSourceStack createCommandSourceStack();

    @Shadow public abstract boolean isDedicatedServer();


    @Unique
    @Override
    public Player getPlayer(UUID uuid) {
        return playerList.getPlayer(uuid);
    }

    @Unique
    @Override
    public Player findPlayer(String name) {
        return playerList.getPlayerByName(name);
    }

    @Unique
    @Override
    public Collection<Player> getPlayers() {
        return playerList.getPlayers().stream().map(pl -> (Player) pl).toList();
    }

    @Unique
    @Override
    public void runCommand(String command, boolean quiet) {
        CommandSourceStack stack = createCommandSourceStack();
        if(quiet) stack.withSuppressedOutput();
        getCommands().performPrefixedCommand(stack, command);
    }

    @Unique
    @Override
    public ModuleManager<Server, ServerModule> getModuleManager() {
        return midnightcore$moduleManager;
    }

    @Unique
    @Override
    public HandlerList<Server> tickEvent() {
        return midnightcore$tickEvent;
    }

    @Unique
    @Override
    public void submit(Runnable runnable) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        server.submit(runnable);
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        midnightcore$tickEvent.invoke(this);
    }
}