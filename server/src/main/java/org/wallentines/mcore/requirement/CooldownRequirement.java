package org.wallentines.mcore.requirement;

import org.wallentines.mdcfg.TypeReference;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.requirement.Check;
import org.wallentines.midnightlib.requirement.CheckType;

import java.util.HashMap;

public class CooldownRequirement<T> implements Check<T> {

    private final Type<T> type;
    private final HashMap<T, Long> cooldowns = new HashMap<>();
    private final long cooldown;

    public CooldownRequirement(Type<T> type, long cooldown) {
        this.type = type;
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
    public CheckType<T, ?> type() {
        return type;
    }

    public long cooldown() {
        return cooldown;
    }

    public static class Type<T> implements CheckType<T, CooldownRequirement<T>> {

        private final Serializer<CooldownRequirement<T>> serializer;

        public Type() {
            this.serializer = Serializer.LONG.fieldOf("value").flatMap(CooldownRequirement<T>::cooldown, cd -> new CooldownRequirement<>(this, cd));
        }

        @Override
        public TypeReference<CooldownRequirement<T>> type() {
            return new TypeReference<CooldownRequirement<T>>() {};
        }

        @Override
        public Serializer<CooldownRequirement<T>> serializer() {
            return serializer;
        }
    }

//
//    @Override
//    public <O> SerializeResult<O> serialize(SerializeContext<O> ctx) {
//        Map<String, O> out = new HashMap<>();
//        out.put("value", ctx.toNumber(cooldown));
//        return SerializeResult.success(ctx.toMap(out));
//    }

//    public static <T> CheckType<T> type() {
//        return new CheckType<T>() {
//            @Override
//            public <O> SerializeResult<Check<T>> deserialize(SerializeContext<O> ctx, O o) {
//                return Serializer.LONG.fieldOf("value").deserialize(ctx, o).flatMap(CooldownRequirement::new);
//            }
//        };
//    }
}
