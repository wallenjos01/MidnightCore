package org.wallentines.midnightcore.fabric.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.event.player.AdvancementEvent;
import org.wallentines.midnightcore.fabric.module.savepoint.AdvancementExtension;
import org.wallentines.midnightlib.event.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements implements AdvancementExtension {

    @Shadow private ServerPlayer player;

    @Shadow public abstract void stopListening();

    @Shadow @Final private Map<Advancement, AdvancementProgress> advancements;

    @Shadow @Final private Set<Advancement> visible;

    @Shadow @Final private Set<Advancement> visibilityChanged;

    @Shadow @Final private Set<Advancement> progressChanged;

    @Shadow private boolean isFirstPacket;

    @Shadow @Nullable private Advancement lastSelectedTab;

    @Shadow protected abstract void checkForAutomaticTriggers(ServerAdvancementManager serverAdvancementManager);

    @Shadow protected abstract void ensureAllVisible();

    @Shadow protected abstract void registerListeners(ServerAdvancementManager serverAdvancementManager);

    @Shadow protected abstract void startProgress(Advancement advancement, AdvancementProgress advancementProgress);

    @Shadow public abstract void flushDirty(ServerPlayer serverPlayer);


    @Inject(method="award", at=@At("RETURN"))
    private void onAward(Advancement advancement, String string, CallbackInfoReturnable<Boolean> cir) {

        AdvancementEvent event = new AdvancementEvent(player, advancement);
        Event.invoke(event);

    }

    @Override
    public void loadFromMap(Map<ResourceLocation, AdvancementProgress> map, ServerAdvancementManager serverAdvancementManager) {

        stopListening();
        advancements.clear();
        visible.clear();
        visibilityChanged.clear();
        progressChanged.clear();
        isFirstPacket = true;
        lastSelectedTab = null;

        map.forEach((loc, prog) -> {
            Advancement adv = serverAdvancementManager.getAdvancement(loc);
            startProgress(adv, prog);
        });

        checkForAutomaticTriggers(serverAdvancementManager);
        ensureAllVisible();
        registerListeners(serverAdvancementManager);
        flushDirty(player);
    }

    @Override
    public Map<ResourceLocation, AdvancementProgress> saveToMap() {

        Map<ResourceLocation, AdvancementProgress> map = new HashMap<>();
        advancements.forEach((adv, prog) -> {
            FriendlyByteBuf copyBuf = new FriendlyByteBuf(Unpooled.buffer(256)); // Save and load for easy copying
            prog.serializeToNetwork(copyBuf);
            map.put(adv.getId(), AdvancementProgress.fromNetwork(copyBuf));
        });
        return map;
    }

    @Override
    public void revokeAll(ServerAdvancementManager serverAdvancementManager) {

        stopListening();
        advancements.clear();
        visible.clear();
        visibilityChanged.clear();
        progressChanged.clear();
        isFirstPacket = true;
        lastSelectedTab = null;

        checkForAutomaticTriggers(serverAdvancementManager);
        ensureAllVisible();
        registerListeners(serverAdvancementManager);
        flushDirty(player);
    }
}
