package org.wallentines.mcore.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.event.SingletonHandlerList;
import org.wallentines.midnightlib.module.ModuleManager;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

@Mixin(MinecraftServer.class)
@Implements(@Interface(iface=Server.class, prefix = "mcore$"))
public abstract class MixinMinecraftServer implements Server {

    @Unique
    private final ModuleManager<Server, ServerModule> mcore$moduleManager = new ModuleManager<>(ServerModule.REGISTRY, this);
    @Unique
    private final HandlerList<Server> mcore$tickEvent = new HandlerList<>();
    @Unique
    private final HandlerList<Server> mcore$stopEvent = new SingletonHandlerList<>();
    @Unique
    private final HandlerList<Player> mcore$joinEvent = new HandlerList<>();
    @Unique
    private final HandlerList<Player> mcore$leaveEvent = new HandlerList<>();

    @Shadow private PlayerList playerList;

    @Shadow public abstract Commands getCommands();
    @Shadow public abstract CommandSourceStack createCommandSourceStack();
    @Shadow public abstract boolean isDedicatedServer();
    @Shadow public abstract Path getWorldPath(LevelResource levelResource);


    public Player mcore$getPlayer(UUID uuid) {
        return playerList.getPlayer(uuid);
    }

    public Player mcore$findPlayer(String name) {
        return playerList.getPlayerByName(name);
    }

    public Stream<Player> mcore$getPlayers() {
        if(playerList == null) return Stream.empty();
        return playerList.getPlayers().stream().map(pl -> (Player) pl);
    }

    public int mcore$getPlayerCount() {
        return playerList.getPlayerCount();
    }

    public void mcore$runCommand(String command, boolean quiet) {
        CommandSourceStack stack = createCommandSourceStack();
        if(quiet) stack.withSuppressedOutput();
        getCommands().performPrefixedCommand(stack, command);
    }

    @Intrinsic(displace = true)
    public boolean mcore$isDedicatedServer() {
        return isDedicatedServer();
    }

    public ModuleManager<Server, ServerModule> mcore$getModuleManager() {
        return mcore$moduleManager;
    }

    public Path mcore$getConfigDirectory() {
        if(isDedicatedServer()) {
            return Path.of("config");
        } else {
            return getWorldPath(LevelResource.ROOT).resolve("config");
        }
    }

    public HandlerList<Server> mcore$tickEvent() {
        return mcore$tickEvent;
    }

    public HandlerList<Server> mcore$shutdownEvent() {
        return mcore$stopEvent;
    }

    public HandlerList<Player> mcore$joinEvent() {
        return mcore$joinEvent;
    }

    public HandlerList<Player> mcore$leaveEvent() {
        return mcore$leaveEvent;
    }

    @Intrinsic(displace = true)
    public void mcore$submit(Runnable runnable) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        server.submit(runnable);
    }

    public GameVersion mcore$getVersion() {
        return new GameVersion(SharedConstants.getCurrentVersion().getId(), SharedConstants.getProtocolVersion());
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        mcore$tickEvent.invoke((Server)this);
    }

    @Inject(method = "runServer", at=@At(value = "INVOKE", target="Lnet/minecraft/server/MinecraftServer;initServer()Z", shift = At.Shift.AFTER))
    private void afterInit(CallbackInfo ci) {
        Server.RUNNING_SERVER.reset();
        loadModules();
        Server.RUNNING_SERVER.set(this);
    }

    @Inject(method="stopServer", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;saveAll()V"))
    private void onSavePlayers(CallbackInfo ci) {
        mcore$stopEvent.invoke((Server)this);
    }

}
