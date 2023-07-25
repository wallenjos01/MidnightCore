package org.wallentines.mcore.mixin;

import com.google.gson.Gson;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerAdvancements.class)
public interface AccessorPlayerAdvancements {

    @Accessor("GSON")
    static Gson getGson() {
        throw new RuntimeException();
    }

}
