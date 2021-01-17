package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.api.event.EntityDamageEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.EntityDeathEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    private final HashMap<LivingEntity, EntityDamageEvent> events = new HashMap<>();

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void damageDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {

        LivingEntity le = (LivingEntity) (Object) this;
        Event.invoke(new EntityDeathEvent(le, events.get(le), source));

    }

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at=@At(value = "HEAD"), cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {

        LivingEntity le = (LivingEntity) (Object) this;

        EntityDamageEvent ev = new EntityDamageEvent(le, source, amount);
        Event.invoke(ev);

        if(ev.isCancelled()) {
            info.setReturnValue(false);
            info.cancel();
        } else {
            events.put(le, ev);
        }
    }

}
