package org.wallentines.mcore;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.mixin.command.ArgumentTypesAccessor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.GsonContext;


public class ServerSideArgumentInfo<A extends ArgumentType<?>> implements ArgumentTypeInfo<A, ServerSideArgumentInfo<A>.Template> {

    private final FriendlyByteBuf preWritten;
    private final JsonObject preJson;
    private final ArgumentTypeInfo<?,?> parent;

    public ServerSideArgumentInfo(ArgumentType<?> parent) {

        Tuples.T3<ArgumentTypeInfo<?,?>, FriendlyByteBuf, JsonObject> prepared = prepare(parent);

        this.parent = prepared.p1;
        this.preWritten = prepared.p2;
        this.preJson = prepared.p3;
    }

    public ArgumentTypeInfo<?,?> getParent() {
        return parent;
    }

    private static <GC> Tuples.T3<ArgumentTypeInfo<?,?>, FriendlyByteBuf, JsonObject> prepare(ArgumentType<GC> type) {

        ArgumentTypeInfo<ArgumentType<GC>, ?> info = ArgumentTypeInfos.byClass(type);
        return prepare(type, info);
    }

    private static <GC, GA extends ArgumentType<GC>, GT extends ArgumentTypeInfo.Template<GA>> Tuples.T3<ArgumentTypeInfo<?,?>, FriendlyByteBuf, JsonObject> prepare(GA type, ArgumentTypeInfo<GA, GT> info) {

        GT template = info.unpack(type);

        FriendlyByteBuf out1 = new FriendlyByteBuf(Unpooled.buffer());
        JsonObject out2 = new JsonObject();

        info.serializeToNetwork(template, out1);
        info.serializeToJson(template, out2);

        return new Tuples.T3<>(info, out1, out2);
    }

    @Override
    public void serializeToNetwork(ServerSideArgumentInfo.Template template, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBytes(preWritten);
    }

    @Override
    public @NotNull Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        throw new NotImplementedException("Server side argument infos cannot be deserialized!");
    }

    @Override
    public void serializeToJson(ServerSideArgumentInfo.Template template, JsonObject jsonObject) {
        GsonContext.INSTANCE.mergeMap(preJson, jsonObject);
    }

    @Override
    public @NotNull Template unpack(A argumentType) {
        return new Template();
    }

    public static <A extends ArgumentType<?>> void register(Class<A> clazz, ArgumentType<?> info) {
        ArgumentTypesAccessor.fabric_getClassMap().put(clazz, new ServerSideArgumentInfo<>(info));
    }


    public final class Template implements ArgumentTypeInfo.Template<A> {


        public @NotNull A instantiate(@NotNull CommandBuildContext ctx) {
            throw new NotImplementedException("Server side argument infos cannot be instantiated from template!");
        }

        public @NotNull ArgumentTypeInfo<A, ?> type() {
            return ServerSideArgumentInfo.this;
        }
    }

}
