package org.wallentines.mcore.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.mcore.Client;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.midnightlib.module.ModuleManager;

import java.nio.file.Path;

@Mixin(Minecraft.class)
public class MixinMinecraft implements Client {

    @Unique
    private final ModuleManager<Client, ClientModule> midnightcore$modules = new ModuleManager<>();

    @Inject(method="<init>", at=@At("RETURN"))
    private void onInit(GameConfig gameConfig, CallbackInfo ci) {

        MidnightCoreAPI.LOGGER.warn("Client created!");
        Client.RUNNING_CLIENT.set(this);

        loadModules(ClientModule.REGISTRY);
    }

    @Unique
    @Override
    public ModuleManager<Client, ClientModule> getModuleManager() {
        return midnightcore$modules;
    }

    @Unique
    @Override
    public Path getConfigDirectory() {
        return Path.of("config");
    }
}
