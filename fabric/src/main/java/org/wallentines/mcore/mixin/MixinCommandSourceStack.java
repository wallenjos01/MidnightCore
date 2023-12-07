package org.wallentines.mcore.mixin;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.wallentines.mcore.PermissionHolder;

@Mixin(CommandSourceStack.class)
@Implements({@Interface(iface = PermissionHolder.class, prefix = "mcore$")})
public abstract class MixinCommandSourceStack implements PermissionHolder {

    public boolean mcore$hasPermission(String permission) {
        return Permissions.check((CommandSourceStack) (Object) this, permission);
    }

    public boolean mcore$hasPermission(String permission, int defaultOpLevel) {
        return Permissions.check((CommandSourceStack) (Object) this, permission, defaultOpLevel);
    }
}
