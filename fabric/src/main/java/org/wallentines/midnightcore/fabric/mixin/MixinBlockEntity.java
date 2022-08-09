package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.wallentines.midnightcore.fabric.event.entity.BlockEntityLoadDataEvent;
import org.wallentines.midnightcore.fabric.event.entity.BlockEntitySaveDataEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(BlockEntity.class)
public class MixinBlockEntity {

    @ModifyVariable(method="saveAdditional", at=@At("HEAD"), ordinal=0, argsOnly = true)
    private CompoundTag onSave(CompoundTag tag) {

        BlockEntitySaveDataEvent event = new BlockEntitySaveDataEvent((BlockEntity) (Object) this, tag);
        Event.invoke(event);

        return event.getTag();
    }

    @ModifyVariable(method="load", at=@At("HEAD"), ordinal=0, argsOnly = true)
    private CompoundTag onLoad(CompoundTag tag) {

        BlockEntityLoadDataEvent event = new BlockEntityLoadDataEvent((BlockEntity) (Object) this, tag);
        Event.invoke(event);

        return event.getTag();
    }

}
