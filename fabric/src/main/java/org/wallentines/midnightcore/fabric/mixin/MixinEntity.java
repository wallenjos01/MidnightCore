package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.entity.EntityDismountVehicleEvent;
import org.wallentines.midnightcore.fabric.event.entity.EntityLoadDataEvent;
import org.wallentines.midnightcore.fabric.event.entity.EntitySaveDataEvent;
import org.wallentines.midnightcore.fabric.module.dynamiclevel.DynamicLevelContext;
import org.wallentines.midnightlib.event.Event;

@Mixin(Entity.class)
public class MixinEntity {

    @Shadow private Entity vehicle;
    @Shadow public Level level;
    private Entity midnightcore_previous_vehicle;

    @Inject(method="removeVehicle", at=@At(value = "HEAD"))
    private void onDismount(CallbackInfo ci) {

        midnightcore_previous_vehicle = vehicle;
    }

    @Inject(method="removeVehicle", at=@At(value = "INVOKE", target="Lnet/minecraft/world/entity/Entity;removePassenger(Lnet/minecraft/world/entity/Entity;)V", shift = At.Shift.AFTER))
    private void afterRemove(CallbackInfo ci) {

        if(midnightcore_previous_vehicle != null) {

            Entity ent = Entity.class.cast(this);
            EntityDismountVehicleEvent ev = new EntityDismountVehicleEvent(midnightcore_previous_vehicle, ent);
            Event.invoke(ev);

            if(ev.isCancelled() && !ev.getVehicle().isRemoved()) {
                ent.startRiding(midnightcore_previous_vehicle, true);
            }

        }
        midnightcore_previous_vehicle = null;
    }

    @ModifyVariable(method="saveWithoutId", at=@At("HEAD"), ordinal=0, argsOnly = true)
    private CompoundTag onSave(CompoundTag tag) {

        EntitySaveDataEvent event = new EntitySaveDataEvent((Entity) (Object) this, tag);
        Event.invoke(event);

        return event.getTag();
    }

    @ModifyVariable(method="load", at=@At("HEAD"), ordinal=0, argsOnly = true)
    private CompoundTag onLoad(CompoundTag tag) {

        EntityLoadDataEvent event = new EntityLoadDataEvent((Entity) (Object) this, tag);
        Event.invoke(event);

        return event.getTag();
    }

    @ModifyVariable(method="handleNetherPortal", at=@At(value="STORE"), ordinal = 0)
    private ResourceKey<Level> onNetherPortal(ResourceKey<Level> value) {

        if(level instanceof DynamicLevelContext.DynamicLevel lvl) {
            ResourceKey<Level> nether = lvl.getNether();
            return nether == level.dimension() ? lvl.getRoot() : nether;
        }
        return value;
    }

}