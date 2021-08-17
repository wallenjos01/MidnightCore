package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.event.EntityDamageEvent;
import me.m1dnightninja.midnightcore.fabric.event.EntityDeathEvent;
import me.m1dnightninja.midnightcore.fabric.event.EntityEatEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    private EntityDamageEvent lastEvent;

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void damageDeath(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {

        LivingEntity le = (LivingEntity) (Object) this;
        Event.invoke(new EntityDeathEvent(le, lastEvent, source));

    }

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at=@At(value = "HEAD"), cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {

        LivingEntity le = (LivingEntity) (Object) this;

        EntityDamageEvent ev = new EntityDamageEvent(le, source, amount);
        Event.invoke(ev);

        if(ev.isCancelled()) {
            info.setReturnValue(false);
            info.cancel();
            return;

        }

        lastEvent = ev;
    }

    @Inject(method = "eat", at=@At(value="HEAD"), cancellable = true)
    private void onEat(Level level, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {

        LivingEntity le = (LivingEntity) (Object) this;

        EntityEatEvent event = new EntityEatEvent(le, itemStack);
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(itemStack);
            cir.cancel();
        }

    }

}
