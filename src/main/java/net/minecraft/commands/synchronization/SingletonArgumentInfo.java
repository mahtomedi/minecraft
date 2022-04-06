package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.FriendlyByteBuf;

public class SingletonArgumentInfo<A extends ArgumentType<?>> implements ArgumentTypeInfo<A, SingletonArgumentInfo<A>.Template> {
    private final SingletonArgumentInfo<A>.Template template;

    private SingletonArgumentInfo(Function<CommandBuildContext, A> param0) {
        this.template = new SingletonArgumentInfo.Template(param0);
    }

    public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextFree(Supplier<T> param0) {
        return new SingletonArgumentInfo<>(param1 -> param0.get());
    }

    public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextAware(Function<CommandBuildContext, T> param0) {
        return new SingletonArgumentInfo<>(param0);
    }

    public void serializeToNetwork(SingletonArgumentInfo<A>.Template param0, FriendlyByteBuf param1) {
    }

    public void serializeToJson(SingletonArgumentInfo<A>.Template param0, JsonObject param1) {
    }

    public SingletonArgumentInfo<A>.Template deserializeFromNetwork(FriendlyByteBuf param0) {
        return this.template;
    }

    public SingletonArgumentInfo<A>.Template unpack(A param0) {
        return this.template;
    }

    public final class Template implements ArgumentTypeInfo.Template<A> {
        private final Function<CommandBuildContext, A> constructor;

        public Template(Function<CommandBuildContext, A> param1) {
            this.constructor = param1;
        }

        @Override
        public A instantiate(CommandBuildContext param0) {
            return this.constructor.apply(param0);
        }

        @Override
        public ArgumentTypeInfo<A, ?> type() {
            return SingletonArgumentInfo.this;
        }
    }
}
