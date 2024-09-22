package org.wallentines.mcore.mixin;

import com.google.common.collect.Sets;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.*;
import org.wallentines.mcore.*;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.AuthUtil;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Mixin(ServerPlayer.class)
@Implements({
        @Interface(iface = Player.class, prefix = "mcore$"),
        @Interface(iface = ScoreboardHolder.class, prefix = "mcore_sb$"),
        @Interface(iface = PermissionHolder.class, prefix = "mcore_pm$")
})
public abstract class MixinServerPlayer implements Player, ScoreboardHolder {

    @Unique
    private ServerScoreboard mcore$scoreboard = null;

    @Shadow public abstract void sendSystemMessage(net.minecraft.network.chat.Component component, boolean bl);
    @Shadow @Final public MinecraftServer server;

    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Shadow public abstract boolean hasDisconnected();

    @Shadow public abstract boolean setGameMode(GameType gameType);

    @Shadow public ServerGamePacketListenerImpl connection;

    @Shadow public abstract ServerLevel serverLevel();


    @Shadow private String language;

    public String mcore$getUsername() {
        return ((net.minecraft.world.entity.player.Player) (Object) this).getGameProfile().getName();
    }

    public Skin mcore$getSkin() {
        return AuthUtil.getProfileSkin(((net.minecraft.world.entity.player.Player) (Object) this).getGameProfile());
    }

    @Intrinsic(displace = true)
    public Component mcore$getDisplayName() {
        ServerPlayer spl = (ServerPlayer) (Object) this;
        return ConversionUtil.toComponent(spl.getDisplayName());
    }

    public boolean mcore$isOnline() {
        return !hasDisconnected();
    }


    public boolean mcore_pm$hasPermission(String permission) {
        return Permissions.check((ServerPlayer) (Object) this, permission);
    }

    public boolean mcore_pm$hasPermission(String permission, int defaultOpLevel) {
        return Permissions.check((ServerPlayer) (Object) this, permission, defaultOpLevel);
    }

    public void mcore$sendMessage(Component component) {

        sendSystemMessage(new WrappedComponent(component), false);
    }

    public void mcore$sendActionBar(Component component) {

        sendSystemMessage(new WrappedComponent(component), true);
    }

    public void mcore$sendTitle(Component title) {
        connection.send(new ClientboundSetTitleTextPacket(new WrappedComponent(title)));
    }

    public void mcore$sendSubtitle(Component title) {
        connection.send(new ClientboundSetSubtitleTextPacket(new WrappedComponent(title)));
    }

    public void mcore$clearTitles() {
        connection.send(new ClientboundClearTitlesPacket(false));
    }

    public void mcore$setTitleTimes(int fadeIn, int stay, int fadeOut) {
        connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
    }

    public void mcore$resetTitles() {
        connection.send(new ClientboundClearTitlesPacket(true));
    }

    public void mcore$giveItem(ItemStack item) {

        ((net.minecraft.world.entity.player.Player) (Object) this).getInventory().add(ConversionUtil.validate(item));
    }

    public String mcore$getLanguage() {
        return language;
    }

    public GameMode mcore$getGameMode() {
        return switch (gameMode.getGameModeForPlayer()) {
            case SURVIVAL -> GameMode.SURVIVAL;
            case CREATIVE -> GameMode.CREATIVE;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
        };
    }

    public void mcore$setGameMode(GameMode mode) {
        setGameMode(switch (mode) {
            case SURVIVAL -> GameType.SURVIVAL;
            case CREATIVE -> GameType.CREATIVE;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
        });
    }

    @Intrinsic(displace = true)
    public Scoreboard mcore_sb$getScoreboard() {
        return mcore$scoreboard == null ? serverLevel().getScoreboard() : mcore$scoreboard;
    }

    public void mcore_sb$setScoreboard(ServerScoreboard scoreboard) {

        Scoreboard prev = mcore_sb$getScoreboard();
        mcore$scoreboard = scoreboard;

        ServerPlayer spl = (ServerPlayer) (Object) this;

        // Clear existing scoreboard if necessary
        if(prev instanceof ServerScoreboard psb) {
            HashSet<Objective> visited = Sets.newHashSet();
            for (PlayerTeam playerTeam : psb.getPlayerTeams()) {
                spl.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam));
            }
            for (DisplaySlot ds : DisplaySlot.values()) {
                Objective objective = psb.getDisplayObjective(ds);
                if (objective == null || visited.contains(objective)) continue;
                List<Packet<?>> list = psb.getStopTrackingPackets(objective);
                for (Packet<?> packet : list) {
                    spl.connection.send(packet);
                }
                visited.add(objective);
            }
        }

        // Send new scoreboard
        if(scoreboard != null) {
            ((AccessorPlayerList) spl.server.getPlayerList()).callUpdateScoreboard(scoreboard, spl);
        }
    }

    public void mcore$kick(Component message) {

        connection.disconnect(new WrappedComponent(message));
    }


    public CompletableFuture<byte[]> mcore$getCookie(Identifier id) {

        ResourceLocation loc = ConversionUtil.toResourceLocation(id);
        connection.send(new ClientboundCookieRequestPacket(loc));

        HandlerList<CookieResponse> event = ((CookieHolder) connection).responseEvent();
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        event.register(future, cookie -> {
            if(cookie.player() == MixinServerPlayer.this && cookie.id().equals(id)) {
                future.complete(cookie.data());
                event.unregisterAll(future);
            }
        });

        return future;
    }

    public void mcore$setCookie(Identifier id, byte[] data) {
        connection.send(new ClientboundStoreCookiePacket(ConversionUtil.toResourceLocation(id), data));
    }

    public void mcore$clearCookie(Identifier id) {
        connection.send(new ClientboundStoreCookiePacket(ConversionUtil.toResourceLocation(id), null));
    }

    public void mcore$transfer(String hostname, int port) {
        connection.send(new ClientboundTransferPacket(hostname, port));
    }

    public void mcore$addResourcePack(ResourcePack pack) {

        UnresolvedComponent msg = pack.message();
        WrappedComponent cmp = msg == null ? null : new WrappedComponent(msg.resolveFor(this));
        connection.send(new ClientboundResourcePackPushPacket(pack.uuid(), pack.url(), pack.hash(), pack.forced(), Optional.ofNullable(cmp)));
    }

    public void mcore$removeResourcePack(UUID id) {
        connection.send(new ClientboundResourcePackPopPacket(Optional.of(id)));
    }

    public void mcore$clearResourcePacks() {
        connection.send(new ClientboundResourcePackPopPacket(Optional.empty()));
    }
}
