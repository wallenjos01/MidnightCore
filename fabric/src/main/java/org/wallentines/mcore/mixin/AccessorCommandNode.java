package org.wallentines.mcore.mixin;

import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(CommandNode.class)
public interface AccessorCommandNode {

    @Mutable
    @Accessor
    <S> void setRequirement(Predicate<S> requirement);

}
