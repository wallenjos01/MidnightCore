package org.wallentines.mcore.mixin;

import com.google.common.collect.Sets;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.mcore.*;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ContentConverter;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.AuthUtil;
import org.wallentines.mcore.util.ConversionUtil;

import java.util.HashSet;
import java.util.List;


@Mixin(ServerPlayer.class)
@Implements({@Interface(iface = Player.class, prefix = "mcore$"), @Interface(iface = ScoreboardHolder.class, prefix = "mcore_sb$")})
public abstract class MixinServerPlayer implements Player {

    @Unique
    private ServerScoreboard mcore$scoreboard = null;

    @Unique
    private String mcore$language = "en_us";

    @Shadow public abstract void sendSystemMessage(net.minecraft.network.chat.Component component, boolean bl);
    @Shadow @Final public MinecraftServer server;

    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Shadow public abstract boolean hasDisconnected();

    @Shadow public abstract boolean setGameMode(GameType gameType);

    @Shadow public ServerGamePacketListenerImpl connection;

    @Shadow public abstract ServerLevel serverLevel();

    public String mcore$getUsername() {
        return ((net.minecraft.world.entity.player.Player) (Object) this).getGameProfile().getName();
    }

    public Skin mcore$getSkin() {
        return AuthUtil.getProfileSkin(((net.minecraft.world.entity.player.Player) (Object) this).getGameProfile());
    }

    public Component mcore$getDisplayName() {
        ServerPlayer spl = (ServerPlayer) (Object) this;
        return ContentConverter.convertReverse(spl.getDisplayName());
    }

    public boolean mcore$isOnline() {
        return !hasDisconnected();
    }


    public boolean mcore$hasPermission(String permission) {
        return Permissions.check((ServerPlayer) (Object) this, permission);
    }

    public boolean mcore$hasPermission(String permission, int defaultOpLevel) {
        return Permissions.check((ServerPlayer) (Object) this, permission, defaultOpLevel);
    }

    public void mcore$sendMessage(Component component) {

        sendSystemMessage(WrappedComponent.resolved(component, this), false);
    }

    public void mcore$sendActionBar(Component component) {

        sendSystemMessage(WrappedComponent.resolved(component, this), true);
    }

    public void mcore$sendTitle(Component title) {
        connection.send(new ClientboundSetTitleTextPacket(WrappedComponent.resolved(title, this)));
    }

    public void mcore$sendSubtitle(Component title) {
        connection.send(new ClientboundSetSubtitleTextPacket(WrappedComponent.resolved(title, this)));
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

    public ItemStack mcore$getHandItem() {
        return ((net.minecraft.world.entity.player.Player) (Object) this).getItemInHand(InteractionHand.MAIN_HAND);
    }

    public ItemStack mcore$getOffhandItem() {
        return ((net.minecraft.world.entity.player.Player) (Object) this).getItemInHand(InteractionHand.OFF_HAND);
    }

    public void mcore$giveItem(ItemStack item) {

        ((net.minecraft.world.entity.player.Player) (Object) this).getInventory().add(ConversionUtil.validate(item));
    }

    public String mcore$getLanguage() {
        return mcore$language;
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

    @Inject(method="updateOptions", at=@At("RETURN"))
    private void onUpdateOptions(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        mcore$language = packet.language();
    }

    public Scoreboard mcore_sb$getScoreboard() {
        return mcore$scoreboard == null ? serverLevel().getScoreboard() : mcore$scoreboard;
    }

    public void mcore_sb$setScoreboard(ServerScoreboard scoreboard) {

        Scoreboard prev = mcore_sb$getScoreboard();
        mcore$scoreboard = scoreboard;

        MidnightCoreAPI.LOGGER.warn("Updated Scoreboard Override");
        ServerPlayer spl = (ServerPlayer) (Object) this;

        // Clear existing scoreboard if necessary
        if(prev instanceof ServerScoreboard psb) {
            HashSet<Objective> visited = Sets.newHashSet();
            for (PlayerTeam playerTeam : psb.getPlayerTeams()) {
                spl.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam));
            }
            for (int i = 0; i < 19; ++i) {
                Objective objective = psb.getDisplayObjective(i);
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
}
