package org.wallentines.mcore.adapter;

import org.wallentines.mcore.GameVersion;

import java.util.function.Function;
import java.util.function.Supplier;

public class UncertainGameVersion<T> extends GameVersion {

    private final Supplier<T> checker;
    private final Function<T, Integer> protocolSupplier;

    private Integer resolvedVersion;

    public UncertainGameVersion(String id, int tempProtocol, Supplier<T> checker, Function<T, Integer> protocolSupplier) {
        super(id, tempProtocol);

        this.checker = checker;
        this.protocolSupplier = protocolSupplier;
    }

    @Override
    public int getProtocolVersion() {

        if(resolvedVersion == null) {

            T val = checker.get();
            if(val == null) {
                return super.getProtocolVersion();
            }

            resolvedVersion = protocolSupplier.apply(val);
        }

        return resolvedVersion;
    }
}
