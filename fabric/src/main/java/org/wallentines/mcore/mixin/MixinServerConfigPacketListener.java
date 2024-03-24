package org.wallentines.mcore.mixin;

import com.mojang.authlib.GameProfile;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.*;
import org.wallentines.mcore.Server;

import java.util.concurrent.ExecutionException;

@Mixin(ServerConfigurationPacketListenerImpl.class)
@Implements({@Interface(iface=org.wallentines.mcore.ConfiguringPlayer.class, prefix = "mcore$")})
public class MixinServerConfigPacketListener  {

    @Shadow
    @Final private GameProfile gameProfile;

    @Shadow private ClientInformation clientInformation;

    public boolean mcore$hasPermission(String permission) {
        try {
            return Permissions.check(gameProfile.getId(), permission).get();
        } catch (InterruptedException | ExecutionException ex) {
            return false;
        }
    }

    public boolean mcore$hasPermission(String permission, int defaultOpLevel) {
        return ((AccessorPacketListener) this).getMinecraftServer().getProfilePermissions(gameProfile) >= defaultOpLevel || mcore$hasPermission(permission);
    }

    public String mcore$getLanguage() {
        return clientInformation.language();
    }

    public Server mcore$getServer() {
        return ((AccessorPacketListener) this).getMinecraftServer();
    }
}
