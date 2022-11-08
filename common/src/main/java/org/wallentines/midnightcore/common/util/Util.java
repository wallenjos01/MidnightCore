package org.wallentines.midnightcore.common.util;

import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.module.Module;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Util {

    public static <T, R> R getOr(@Nullable T obj, Function<T, R> run, Supplier<R> def) {
        if(obj == null) return def.get();
        return run.apply(obj);
    }

    @Nullable
    public static <T, R> R getOrNull(@Nullable T obj, Function<T, R> run) {
        return getOr(obj, run, () -> null);
    }

    public static <T> void runIfPresent(@Nullable T obj, Consumer<T> run) {
        if(obj != null) run.accept(obj);
    }

    public static <T extends Module<MidnightCoreAPI>> T getModule(Class<T> clazz) {

        return getOr(MidnightCoreAPI.getInstance(), inst -> inst.getModuleManager().getModule(clazz), () -> null);
    }

}
