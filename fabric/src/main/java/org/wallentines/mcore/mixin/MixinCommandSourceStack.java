package org.wallentines.mcore.mixin;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.wallentines.mcore.CommandSender;
import org.wallentines.mcore.Location;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.function.Supplier;

@Mixin(CommandSourceStack.class)
@Implements({@Interface(iface = CommandSender.class, prefix = "mcore$")})
public abstract class MixinCommandSourceStack implements CommandSender {

    @Shadow public abstract void sendSuccess(Supplier<net.minecraft.network.chat.Component> supplier, boolean bl);

    @Shadow public abstract void sendFailure(net.minecraft.network.chat.Component component);

    @Shadow @Nullable public abstract ServerPlayer getPlayer();

    @Shadow public abstract Vec3 getPosition();

    @Shadow public abstract ServerLevel getLevel();

    @Shadow public abstract Vec2 getRotation();

    public boolean mcore$hasPermission(String permission) {
        return Permissions.check((CommandSourceStack) (Object) this, permission);
    }

    public boolean mcore$hasPermission(String permission, int defaultOpLevel) {
        return Permissions.check((CommandSourceStack) (Object) this, permission, defaultOpLevel);
    }

    public void mcore$sendSuccess(Component message, boolean log) {
        sendSuccess(() -> new WrappedComponent(ComponentResolver.resolveComponent(message, this, getPlayer())), log);
    }

    public void mcore$sendFailure(Component message) {
        sendFailure(new WrappedComponent(message));
    }

    public String mcore$getLanguage() {
        ServerPlayer pl = getPlayer();
        if(pl == null) return "en_us";
        return ((Player) pl).getLanguage();
    }

    public Location mcore$getLocation() {

        Vec3 vec = getPosition();
        Identifier world = ConversionUtil.toIdentifier(getLevel().dimension().location());
        Vec2 rot = getRotation();

        return new Location(world, new Vec3d(vec.x, vec.y, vec.z), rot.x, rot.y);
    }
}

