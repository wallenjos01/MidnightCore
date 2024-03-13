package org.wallentines.mcore.requirement;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.requirement.Check;
import org.wallentines.midnightlib.requirement.CheckType;

import java.util.HashMap;
import java.util.Map;

public class CooldownRequirement<T> implements Check<T> {

    private final HashMap<T, Long> cooldowns = new HashMap<>();
    private final long cooldown;

    public CooldownRequirement(long cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public boolean check(T t) {

        if(!cooldowns.containsKey(t) || System.currentTimeMillis() - cooldowns.get(t) > cooldown) {

            cooldowns.put(t, System.currentTimeMillis());
            return true;

        } else {

            cooldowns.remove(t);
            return false;
        }
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> ctx) {
        Map<String, O> out = new HashMap<>();
        out.put("value", ctx.toNumber(cooldown));
        return SerializeResult.success(ctx.toMap(out));
    }

    public static <T> CheckType<T> type() {
        return new CheckType<T>() {
            @Override
            public <O> SerializeResult<Check<T>> deserialize(SerializeContext<O> ctx, O o) {
                return Serializer.LONG.fieldOf("value").deserialize(ctx, o).flatMap(CooldownRequirement::new);
            }
        };
    }
}
