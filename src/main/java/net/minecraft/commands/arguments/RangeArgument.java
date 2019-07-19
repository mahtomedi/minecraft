package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public interface RangeArgument<T extends MinMaxBounds<?>> extends ArgumentType<T> {
    static RangeArgument.Ints intRange() {
        return new RangeArgument.Ints();
    }

    public static class Floats implements RangeArgument<MinMaxBounds.Floats> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5.2", "0", "-5.4", "-100.76..", "..100");

        public MinMaxBounds.Floats parse(StringReader param0) throws CommandSyntaxException {
            return MinMaxBounds.Floats.fromReader(param0);
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static class Serializer extends RangeArgument.Serializer<RangeArgument.Floats> {
            public RangeArgument.Floats deserializeFromNetwork(FriendlyByteBuf param0) {
                return new RangeArgument.Floats();
            }
        }
    }

    public static class Ints implements RangeArgument<MinMaxBounds.Ints> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5", "0", "-5", "-100..", "..100");

        public static MinMaxBounds.Ints getRange(CommandContext<CommandSourceStack> param0, String param1) {
            return param0.getArgument(param1, MinMaxBounds.Ints.class);
        }

        public MinMaxBounds.Ints parse(StringReader param0) throws CommandSyntaxException {
            return MinMaxBounds.Ints.fromReader(param0);
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static class Serializer extends RangeArgument.Serializer<RangeArgument.Ints> {
            public RangeArgument.Ints deserializeFromNetwork(FriendlyByteBuf param0) {
                return new RangeArgument.Ints();
            }
        }
    }

    public abstract static class Serializer<T extends RangeArgument<?>> implements ArgumentSerializer<T> {
        public void serializeToNetwork(T param0, FriendlyByteBuf param1) {
        }

        public void serializeToJson(T param0, JsonObject param1) {
        }
    }
}
