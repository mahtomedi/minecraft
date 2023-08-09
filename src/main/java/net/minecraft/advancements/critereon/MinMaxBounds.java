package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public interface MinMaxBounds<T extends Number> {
    SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
    SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));

    Optional<T> min();

    Optional<T> max();

    default boolean isAny() {
        return this.min().isEmpty() && this.max().isEmpty();
    }

    default Optional<T> unwrapPoint() {
        Optional<T> var0 = this.min();
        Optional<T> var1 = this.max();
        return var0.equals(var1) ? var0 : Optional.empty();
    }

    static <T extends Number, R extends MinMaxBounds<T>> Codec<R> createCodec(Codec<T> param0, MinMaxBounds.BoundsFactory<T, R> param1) {
        Codec<R> var0 = RecordCodecBuilder.create(
            param2 -> param2.group(
                        ExtraCodecs.strictOptionalField(param0, "min").forGetter(MinMaxBounds::min),
                        ExtraCodecs.strictOptionalField(param0, "max").forGetter(MinMaxBounds::max)
                    )
                    .apply(param2, param1::create)
        );
        return Codec.either(var0, param0)
            .xmap(
                param1x -> param1x.map(
                        (Function<? super R, ? extends R>)(param0x -> param0x), param1xx -> param1.create(Optional.of(param1xx), Optional.of(param1xx))
                    ),
                param0x -> {
                    Optional<T> var0x = param0x.unwrapPoint();
                    return var0x.isPresent() ? Either.right(var0x.get()) : Either.left(param0x);
                }
            );
    }

    static <T extends Number, R extends MinMaxBounds<T>> R fromReader(
        StringReader param0,
        MinMaxBounds.BoundsFromReaderFactory<T, R> param1,
        Function<String, T> param2,
        Supplier<DynamicCommandExceptionType> param3,
        Function<T, T> param4
    ) throws CommandSyntaxException {
        if (!param0.canRead()) {
            throw ERROR_EMPTY.createWithContext(param0);
        } else {
            int var0 = param0.getCursor();

            try {
                Optional<T> var1 = readNumber(param0, param2, param3).map(param4);
                Optional<T> var2;
                if (param0.canRead(2) && param0.peek() == '.' && param0.peek(1) == '.') {
                    param0.skip();
                    param0.skip();
                    var2 = readNumber(param0, param2, param3).map(param4);
                    if (var1.isEmpty() && var2.isEmpty()) {
                        throw ERROR_EMPTY.createWithContext(param0);
                    }
                } else {
                    var2 = var1;
                }

                if (var1.isEmpty() && var2.isEmpty()) {
                    throw ERROR_EMPTY.createWithContext(param0);
                } else {
                    return param1.create(param0, var1, var2);
                }
            } catch (CommandSyntaxException var8) {
                param0.setCursor(var0);
                throw new CommandSyntaxException(var8.getType(), var8.getRawMessage(), var8.getInput(), var0);
            }
        }
    }

    private static <T extends Number> Optional<T> readNumber(StringReader param0, Function<String, T> param1, Supplier<DynamicCommandExceptionType> param2) throws CommandSyntaxException {
        int var0 = param0.getCursor();

        while(param0.canRead() && isAllowedInputChat(param0)) {
            param0.skip();
        }

        String var1 = param0.getString().substring(var0, param0.getCursor());
        if (var1.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(param1.apply(var1));
            } catch (NumberFormatException var6) {
                throw param2.get().createWithContext(param0, var1);
            }
        }
    }

    private static boolean isAllowedInputChat(StringReader param0) {
        char var0 = param0.peek();
        if ((var0 < '0' || var0 > '9') && var0 != '-') {
            if (var0 != '.') {
                return false;
            } else {
                return !param0.canRead(2) || param0.peek(1) != '.';
            }
        } else {
            return true;
        }
    }

    @FunctionalInterface
    public interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
        R create(Optional<T> var1, Optional<T> var2);
    }

    @FunctionalInterface
    public interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
        R create(StringReader var1, Optional<T> var2, Optional<T> var3) throws CommandSyntaxException;
    }

    public static record Doubles(Optional<Double> min, Optional<Double> max, Optional<Double> minSq, Optional<Double> maxSq) implements MinMaxBounds<Double> {
        public static final MinMaxBounds.Doubles ANY = new MinMaxBounds.Doubles(Optional.empty(), Optional.empty());
        public static final Codec<MinMaxBounds.Doubles> CODEC = MinMaxBounds.createCodec(Codec.DOUBLE, MinMaxBounds.Doubles::new);

        private Doubles(Optional<Double> param0, Optional<Double> param1) {
            this(param0, param1, squareOpt(param0), squareOpt(param1));
        }

        private static MinMaxBounds.Doubles create(StringReader param0, Optional<Double> param1, Optional<Double> param2) throws CommandSyntaxException {
            if (param1.isPresent() && param2.isPresent() && param1.get() > param2.get()) {
                throw ERROR_SWAPPED.createWithContext(param0);
            } else {
                return new MinMaxBounds.Doubles(param1, param2);
            }
        }

        private static Optional<Double> squareOpt(Optional<Double> param0) {
            return param0.map(param0x -> param0x * param0x);
        }

        public static MinMaxBounds.Doubles exactly(double param0) {
            return new MinMaxBounds.Doubles(Optional.of(param0), Optional.of(param0));
        }

        public static MinMaxBounds.Doubles between(double param0, double param1) {
            return new MinMaxBounds.Doubles(Optional.of(param0), Optional.of(param1));
        }

        public static MinMaxBounds.Doubles atLeast(double param0) {
            return new MinMaxBounds.Doubles(Optional.of(param0), Optional.empty());
        }

        public static MinMaxBounds.Doubles atMost(double param0) {
            return new MinMaxBounds.Doubles(Optional.empty(), Optional.of(param0));
        }

        public boolean matches(double param0) {
            if (this.min.isPresent() && this.min.get() > param0) {
                return false;
            } else {
                return this.max.isEmpty() || !(this.max.get() < param0);
            }
        }

        public boolean matchesSqr(double param0) {
            if (this.minSq.isPresent() && this.minSq.get() > param0) {
                return false;
            } else {
                return this.maxSq.isEmpty() || !(this.maxSq.get() < param0);
            }
        }

        public static MinMaxBounds.Doubles fromJson(@Nullable JsonElement param0) {
            return param0 != null && !param0.isJsonNull() ? Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, param0), JsonParseException::new) : ANY;
        }

        public JsonElement serializeToJson() {
            return (JsonElement)(this.isAny() ? JsonNull.INSTANCE : Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new));
        }

        public static MinMaxBounds.Doubles fromReader(StringReader param0) throws CommandSyntaxException {
            return fromReader(param0, param0x -> param0x);
        }

        public static MinMaxBounds.Doubles fromReader(StringReader param0, Function<Double, Double> param1) throws CommandSyntaxException {
            return MinMaxBounds.fromReader(
                param0, MinMaxBounds.Doubles::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, param1
            );
        }
    }

    public static record Ints(Optional<Integer> min, Optional<Integer> max, Optional<Long> minSq, Optional<Long> maxSq) implements MinMaxBounds<Integer> {
        public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints(Optional.empty(), Optional.empty());
        public static final Codec<MinMaxBounds.Ints> CODEC = MinMaxBounds.createCodec(Codec.INT, MinMaxBounds.Ints::new);

        private Ints(Optional<Integer> param0, Optional<Integer> param1) {
            this(param0, param1, param0.map(param0x -> param0x.longValue() * param0x.longValue()), squareOpt(param1));
        }

        private static MinMaxBounds.Ints create(StringReader param0, Optional<Integer> param1, Optional<Integer> param2) throws CommandSyntaxException {
            if (param1.isPresent() && param2.isPresent() && param1.get() > param2.get()) {
                throw ERROR_SWAPPED.createWithContext(param0);
            } else {
                return new MinMaxBounds.Ints(param1, param2);
            }
        }

        private static Optional<Long> squareOpt(Optional<Integer> param0) {
            return param0.map(param0x -> param0x.longValue() * param0x.longValue());
        }

        public static MinMaxBounds.Ints exactly(int param0) {
            return new MinMaxBounds.Ints(Optional.of(param0), Optional.of(param0));
        }

        public static MinMaxBounds.Ints between(int param0, int param1) {
            return new MinMaxBounds.Ints(Optional.of(param0), Optional.of(param1));
        }

        public static MinMaxBounds.Ints atLeast(int param0) {
            return new MinMaxBounds.Ints(Optional.of(param0), Optional.empty());
        }

        public static MinMaxBounds.Ints atMost(int param0) {
            return new MinMaxBounds.Ints(Optional.empty(), Optional.of(param0));
        }

        public boolean matches(int param0) {
            if (this.min.isPresent() && this.min.get() > param0) {
                return false;
            } else {
                return this.max.isEmpty() || this.max.get() >= param0;
            }
        }

        public boolean matchesSqr(long param0) {
            if (this.minSq.isPresent() && this.minSq.get() > param0) {
                return false;
            } else {
                return this.maxSq.isEmpty() || this.maxSq.get() >= param0;
            }
        }

        public static MinMaxBounds.Ints fromJson(@Nullable JsonElement param0) {
            return param0 != null && !param0.isJsonNull() ? Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, param0), JsonParseException::new) : ANY;
        }

        public JsonElement serializeToJson() {
            return (JsonElement)(this.isAny() ? JsonNull.INSTANCE : Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new));
        }

        public static MinMaxBounds.Ints fromReader(StringReader param0) throws CommandSyntaxException {
            return fromReader(param0, param0x -> param0x);
        }

        public static MinMaxBounds.Ints fromReader(StringReader param0, Function<Integer, Integer> param1) throws CommandSyntaxException {
            return MinMaxBounds.fromReader(
                param0, MinMaxBounds.Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, param1
            );
        }
    }
}
