package org.wallentines.midnightcore.fabric.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class ServerSideArgumentInfo<A extends ArgumentType<?>> implements ArgumentTypeInfo<A, ServerSideArgumentInfo<A>.Template> {

    private final ArgumentTypeInfo<?, ?> parent;
    private final ServerSideArgumentInfo<A>.Template template;

    private final StringArgumentType.StringType stringType;

    private ServerSideArgumentInfo(ArgumentTypeInfo<?, ?> parent, StringArgumentType.StringType stringType, Function<CommandBuildContext, A> function) {
        this.parent = parent;
        this.stringType = stringType;
        this.template = new ServerSideArgumentInfo<A>.Template(function);
    }

    public ArgumentTypeInfo<?, ?> getParent() {
        return parent;
    }

    public static <T extends ArgumentType<?>> ServerSideArgumentInfo<T> contextFree(ArgumentTypeInfo<?, ?> parent, Supplier<T> supplier) {
        return new ServerSideArgumentInfo<>(parent, null, ctx -> supplier.get());
    }

    public static <T extends ArgumentType<?>> ServerSideArgumentInfo<T> contextAware(ArgumentTypeInfo<?, ?> parent, Function<CommandBuildContext, T> function) {
        return new ServerSideArgumentInfo<>(parent, null, function);
    }

    public static <T extends ArgumentType<?>> ServerSideArgumentInfo<T> string(StringArgumentType.StringType type, Supplier<T> supplier) {
        return new ServerSideArgumentInfo<>(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.get(new ResourceLocation("brigadier", "string")), type, ctx -> supplier.get());
    }

    public void serializeToNetwork(ServerSideArgumentInfo<A>.@NotNull Template template, @NotNull FriendlyByteBuf friendlyByteBuf) {

        friendlyByteBuf.writeEnum(stringType);
    }

    public void serializeToJson(ServerSideArgumentInfo<A>.@NotNull Template template, @NotNull JsonObject jsonObject) {
    }

    public ServerSideArgumentInfo<A>.Template deserializeFromNetwork(@NotNull FriendlyByteBuf friendlyByteBuf) {
        return this.template;
    }

    public ServerSideArgumentInfo<A>.Template unpack(@NotNull A argumentType) {
        return this.template;
    }

    public final class Template implements ArgumentTypeInfo.Template<A> {
        private final Function<CommandBuildContext, A> constructor;

        public Template(Function<CommandBuildContext, A> function) {
            this.constructor = function;
        }

        public A instantiate(@NotNull CommandBuildContext commandBuildContext) {
            return this.constructor.apply(commandBuildContext);
        }

        public ArgumentTypeInfo<A, ?> type() {
            return ServerSideArgumentInfo.this;
        }
    }
}