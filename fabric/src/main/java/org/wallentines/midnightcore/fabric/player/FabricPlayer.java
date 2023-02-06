package org.wallentines.midnightcore.fabric.player;

import com.mojang.authlib.GameProfile;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.common.player.AbstractPlayer;
import org.wallentines.midnightcore.fabric.event.player.ResourcePackStatusEvent;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.server.FabricServer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightcore.fabric.util.LocationUtil;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class FabricPlayer extends AbstractPlayer<ServerPlayer> {


    private static final HashMap<ServerPlayer, Consumer<ResourcePackStatus>> AWAITING_RESOURCE_PACK = new HashMap<>();

    private String locale = LangProvider.getServerLocale();

    protected FabricPlayer(UUID uuid, MServer server) {
        super(uuid, server);
    }

    @Override
    public String getUsername() {
        return run(player -> player.getGameProfile().getName(), () -> {

            MServer server = MidnightCoreAPI.getRunningServer();
            if(server == null) return getUUID().toString();

            Optional<GameProfile> prof = ((FabricServer) server).getInternal().getProfileCache().get(getUUID());
            return prof.map(GameProfile::getName).orElseGet(() -> getUUID().toString());
        });
    }

    @Override
    public MComponent getName() {
        return run(player -> ConversionUtil.toMComponent(player.getDisplayName()),
            () -> {
                MServer server = MidnightCoreAPI.getRunningServer();
                if(server == null) return new MTextComponent(getUUID().toString());

                Optional<GameProfile> prof = ((FabricServer) server).getInternal().getProfileCache().get(getUUID());
                return prof.map(gameProfile -> new MTextComponent(gameProfile.getName())).orElseGet(() -> new MTextComponent(getUUID().toString()));
            }
        );
    }

    @Override
    public Location getLocation() {
        return run(LocationUtil::getEntityLocation, () -> null);
    }

    @Override
    public MItemStack getItemInMainHand() {
        return run(player -> new FabricItem(player.getItemInHand(InteractionHand.MAIN_HAND)), () -> null);
    }

    @Override
    public MItemStack getItemInOffhand() {
        return run(player -> new FabricItem(player.getItemInHand(InteractionHand.OFF_HAND)), () -> null);
    }

    @Override
    public String getLocale() {
        return locale;
    }

    @Override
    public boolean hasPermission(String permission) {
        return run(player -> Permissions.check(player, permission), () -> false);
    }

    @Override
    public boolean hasPermission(String permission, int permissionLevel) {
        return run(player -> Permissions.check(player, permission, permissionLevel), () -> false);
    }

    @Override
    public void sendMessage(MComponent component) {

        if(component == null) return;
        run(player -> player.sendSystemMessage(ConversionUtil.toComponent(component), false), () -> {});
    }

    @Override
    public void sendActionBar(MComponent component) {

        if(component == null) return;

        run(player -> player.sendSystemMessage(ConversionUtil.toComponent(component), true), () -> { });
    }

    @Override
    public void sendTitle(MComponent component, int fadeIn, int stay, int fadeOut) {

        if(component == null) return;
        run(player -> {

            ClientboundSetTitleTextPacket title = new ClientboundSetTitleTextPacket(ConversionUtil.toComponent(component));
            ClientboundSetTitlesAnimationPacket anim = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);

            player.connection.send(title);
            player.connection.send(anim);
        }, () -> { });
    }

    @Override
    public void sendSubtitle(MComponent component, int fadeIn, int stay, int fadeOut) {

        if(component == null) return;
        run(player -> {

            ClientboundSetSubtitleTextPacket subtitle = new ClientboundSetSubtitleTextPacket(ConversionUtil.toComponent(component));
            ClientboundSetTitlesAnimationPacket anim = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);

            player.connection.send(subtitle);
            player.connection.send(anim);
        }, () -> { });
    }

    @Override
    public void clearTitles() {

        run(player -> player.connection.send(new ClientboundClearTitlesPacket(true)), () -> { });
    }

    @Override
    public void playSound(Identifier soundId, String category, float volume, float pitch) {

        run(player -> {

            SoundSource src = SoundSource.valueOf(category.toUpperCase(Locale.ROOT));
            SoundEvent ev = BuiltInRegistries.SOUND_EVENT.get(ConversionUtil.toResourceLocation(soundId));
            if(ev == null) return;

            Holder<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ev);

            long seed = player.getLevel().getRandom().nextLong();
            player.connection.send(new ClientboundSoundPacket(
                    holder, src, player.position().x, player.position().y, player.position().z, volume, pitch, seed
            ));

        }, () -> { });

    }

    @Override
    public void closeContainer() {

        run(ServerPlayer::closeContainer, () -> { });
    }

    @Override
    public void executeCommand(String cmd) {

        run(player -> {
            MServer server = MidnightCoreAPI.getRunningServer();
            if(server == null) return;

            ((FabricServer) server).getInternal().getCommands().performPrefixedCommand(player.createCommandSourceStack(), cmd);
        }, () -> { });
    }

    @Override
    public void sendChatMessage(String message) {

        byte[] data = new byte[0];

        // Forged messages cannot be signed
        run(player -> player.connection.send(
                new ServerboundChatPacket(message, Instant.now(), System.currentTimeMillis(), null,
                        new LastSeenMessages.Update(0, BitSet.valueOf(data))
                )), () -> { });
    }

    @Override
    public void giveItem(MItemStack item) {

        run(player -> {
            FabricItem it = (FabricItem) item;
            player.getInventory().add(it.getInternal());
        }, () -> { });
    }

    @Override
    public void giveItem(MItemStack item, int slot) {

        run(player -> {
            FabricItem it = (FabricItem) item;
            player.getInventory().add(slot, it.getInternal());
        }, () -> { });
    }

    @Override
    public void teleport(Location newLoc) {

        run(player -> LocationUtil.teleport(player, newLoc), () -> { });
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        run(player -> {
            GameType mc = switch (gameMode) {
                case SURVIVAL -> GameType.SURVIVAL;
                case CREATIVE -> GameType.CREATIVE;
                case ADVENTURE -> GameType.ADVENTURE;
                case SPECTATOR -> GameType.SPECTATOR;
            };
            player.setGameMode(mc);
        }, () -> { });
    }

    @Override
    public GameMode getGameMode() {
        return run(player -> switch (player.gameMode.getGameModeForPlayer()) {
            case SURVIVAL -> GameMode.SURVIVAL;
            case CREATIVE -> GameMode.CREATIVE;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
        }, () -> GameMode.SURVIVAL);
    }

    @Override
    public float getHealth() {
        return run(LivingEntity::getHealth, () -> 0.0f);
    }

    @Override
    public void applyResourcePack(String url, String hash, boolean force, MComponent promptMessage, Consumer<ResourcePackStatus> onResponse) {

        if(url == null) return;

        run(player -> {

            String outHash = hash == null ? "" : hash;
            player.connection.send(new ClientboundResourcePackPacket(url, outHash, force, ConversionUtil.toComponent(promptMessage)));
            AWAITING_RESOURCE_PACK.put(player, onResponse);
        }, () -> { });

    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public static FabricPlayer wrap(ServerPlayer player) {

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) throw new IllegalStateException("MidnightCoreAPI has not been created!");

        MServer server = api.getServer();
        if(server == null) throw new IllegalStateException("The server has not been created!");

        return (FabricPlayer) server.getPlayer(player.getUUID());
    }

    public static ServerPlayer getInternal(MPlayer player) {
        return ((FabricPlayer) player).getInternal();
    }

    static {

        Event.register(ResourcePackStatusEvent.class, FabricPlayer.class, ev ->
            AWAITING_RESOURCE_PACK.computeIfPresent(ev.getPlayer(), (k, v) -> {
                v.accept(ev.getStatus());
                return null;
            }
        ));

    }

}
