package org.wallentines.mcore.requirement;

import org.wallentines.mcore.Player;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.requirement.Check;
import org.wallentines.midnightlib.requirement.CheckType;

import java.util.function.BiPredicate;

public class PlayerCheck<T> implements Check<Player> {

    private final T value;
    private final BiPredicate<Player, T> checker;
    private final Serializer<T> serializer;

    public PlayerCheck(T value, BiPredicate<Player, T> checker, Serializer<T> serializer) {
        this.value = value;
        this.checker = checker;
        this.serializer = serializer;
    }

    @Override
    public boolean check(Player player) {
        return checker.test(player, value);
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> ctx) {
        return serializer.serialize(ctx, value);
    }

    public static <T> CheckType<Player> create(Serializer<T> serializer, BiPredicate<Player, T> checker) {
        return new CheckType<Player>() {
            @Override
            public <O> SerializeResult<Check<Player>> deserialize(SerializeContext<O> ctx, O o) {

                return serializer.deserialize(ctx, o).flatMap(t -> new PlayerCheck<>(t, checker, serializer));
            }
        };
    }

    public static <T> CheckType<Player> create(Serializer<T> serializer, String value, BiPredicate<Player, T> checker) {
        return create(serializer.fieldOf(value), checker);
    }
}
