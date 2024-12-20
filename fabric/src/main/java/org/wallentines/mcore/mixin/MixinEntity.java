package org.wallentines.mcore.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.wallentines.mcore.*;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.RegistryUtil;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Set;
import java.util.UUID;

@Mixin(net.minecraft.world.entity.Entity.class)
@Implements(@Interface(iface=Entity.class, prefix = "mcore$"))
public abstract class MixinEntity implements Entity {

    @Shadow private Level level;

    @Shadow public abstract void setDeltaMovement(Vec3 vec3);

    @Shadow public abstract Vec3 getDeltaMovement();

    @Shadow public abstract void setOnGround(boolean bl);

    @Shadow private Vec3 position;

    @Shadow private float xRot;

    @Shadow private float yRot;

    @Shadow @Nullable public abstract net.minecraft.network.chat.Component getCustomName();

    @Shadow public abstract String getStringUUID();


    @Shadow public abstract boolean teleportTo(ServerLevel par1, double par2, double par3, double par4, Set<Relative> par5, float par6, float par7, boolean par8);

    @Shadow public abstract CommandSourceStack createCommandSourceStackForNameResolution(ServerLevel par1);

    @Intrinsic(displace = true)
    public UUID mcore$getUUID() {
        return ((net.minecraft.world.entity.Entity) (Object) this).getUUID();
    }

    @Intrinsic(displace = true)
    public Identifier mcore$getType() {

        net.minecraft.world.entity.Entity ent = (net.minecraft.world.entity.Entity) (Object) this;

        return RegistryUtil.registry(Registries.ENTITY_TYPE)
                .map(reg -> reg.getKey(ent.getType()))
                .map(ConversionUtil::toIdentifier)
                .orElseGet(() -> new Identifier("minecraft", "pig"));
    }

    @Intrinsic(displace = true)
    public Server mcore$getServer() {
        return (Server) ((net.minecraft.world.entity.Entity) (Object) this).getServer();
    }

    @Intrinsic(displace = true)
    public Component mcore$getDisplayName() {

        net.minecraft.network.chat.Component comp = getCustomName();
        if(comp == null) return Component.text(getStringUUID());

        return ConversionUtil.toComponent(comp);
    }

    public Identifier mcore$getDimensionId() {
        return ConversionUtil.toIdentifier(level.dimension().location());
    }

    public Vec3d mcore$getPosition() {
        return new Vec3d(position.x, position.y, position.z);
    }

    public float mcore$getYaw() {
        return yRot;
    }

    public float mcore$getPitch() {
        return xRot;
    }

    @Intrinsic(displace = true)
    public boolean mcore$isRemoved() {
        return ((net.minecraft.world.entity.Entity) (Object) this).isRemoved();
    }

    public void mcore$teleport(Location location) {

        if(level.isClientSide) {
            throw new IllegalStateException("Attempt to teleport a client-side entity!");
        }

        net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) (Object) this;

        MinecraftServer server = self.getServer();
        if(server == null) {
            throw new IllegalStateException("Attempt to teleport a non-server entity!");
        }

        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ConversionUtil.toResourceLocation(location.dimension)));
        AccessorTeleportCommand.callPerformTeleport(null, self, level, location.position.getX(), location.position.getY(), location.position.getZ(), Set.of(), location.yaw, location.pitch, null);
    }

    public ItemStack mcore$getItem(EquipmentSlot slot) {

        net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) (Object) this;
        if(self instanceof LivingEntity le) {
            return le.getItemBySlot(ConversionUtil.toMCEquipmentSlot(slot));
        }

        return null;
    }

    public void mcore$setItem(EquipmentSlot slot, ItemStack item) {

        net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) (Object) this;
        if(self instanceof LivingEntity le) {
            net.minecraft.world.item.ItemStack is = ConversionUtil.validate(item);
            le.setItemSlot(ConversionUtil.toMCEquipmentSlot(slot), is);
        }

    }

    public void mcore$runCommand(String command) {

        net.minecraft.world.entity.Entity ent = (net.minecraft.world.entity.Entity) (Object) this;

        if(ent.level().isClientSide) return;

        CommandSourceStack css = createCommandSourceStackForNameResolution((ServerLevel) ent.level());
        ent.getServer().getCommands().performPrefixedCommand(css, command);
    }
}
