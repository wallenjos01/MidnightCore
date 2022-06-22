package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.Blocks;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class DynamicLevelModule implements Module<MidnightCoreAPI> {

    private final Registry<PortalType> portalTypeRegistry = new Registry<>();

    public final AtomicReference<DynamicOps<Tag>> registryOps = new AtomicReference<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        portalTypeRegistry.register(new Identifier("minecraft", "the_nether"), new PortalType(Blocks.NETHER_PORTAL));
        portalTypeRegistry.register(new Identifier("minecraft", "the_end"), new PortalType(Blocks.END_PORTAL));

        return true;
    }

    public DynamicLevelStorage createLevelStorage(Path path, Path backupPath) {

        return DynamicLevelStorage.create(this, path, backupPath);
    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "dynamic_level");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(DynamicLevelModule::new, ID, new ConfigSection());
}
