package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;

public abstract class MinMaxBounds<T extends Number> {
    public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(new TranslatableComponent("argument.range.empty"));
    public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(new TranslatableComponent("argument.range.swapped"));
    protected final T min;
    protected final T max;

    protected MinMaxBounds(@Nullable T param0, @Nullable T param1) {
        this.min = param0;
        this.max = param1;
    }

    @Nullable
    public T getMin() {
        return this.min;
    }

    @Nullable
    public T getMax() {
        return this.max;
    }

    public boolean isAny() {
        return this.min == null && this.max == null;
    }

    public JsonElement serializeToJson() {
        if (this.isAny()) {
            return JsonNull.INSTANCE;
        } else if (this.min != null && this.min.equals(this.max)) {
            return new JsonPrimitive(this.min);
        } else {
            JsonObject var0 = new JsonObject();
            if (this.min != null) {
                var0.addProperty("min", this.min);
            }

            if (this.max != null) {
                var0.addProperty("max", this.max);
            }

            return var0;
        }
    }

    protected static <T extends Number, R extends MinMaxBounds<T>> R fromJson(
        @Nullable JsonElement param0, R param1, BiFunction<JsonElement, String, T> param2, MinMaxBounds.BoundsFactory<T, R> param3
    ) {
        if (param0 == null || param0.isJsonNull()) {
            return param1;
        } else if (GsonHelper.isNumberValue(param0)) {
            T var0 = param2.apply(param0, "value");
            return param3.create(var0, var0);
        } else {
            JsonObject var1 = GsonHelper.convertToJsonObject(param0, "value");
            T var2 = var1.has("min") ? param2.apply(var1.get("min"), "min") : null;
            T var3 = var1.has("max") ? param2.apply(var1.get("max"), "max") : null;
            return param3.create(var2, var3);
        }
    }

    protected static <T extends Number, R extends MinMaxBounds<T>> R fromReader(
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
                T var1 = optionallyFormat(readNumber(param0, param2, param3), param4);
                T var2;
                if (param0.canRead(2) && param0.peek() == '.' && param0.peek(1) == '.') {
                    param0.skip();
                    param0.skip();
                    var2 = optionallyFormat(readNumber(param0, param2, param3), param4);
                    if (var1 == null && var2 == null) {
                        throw ERROR_EMPTY.createWithContext(param0);
                    }
                } else {
                    var2 = var1;
                }

                if (var1 == null && var2 == null) {
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

    @Nullable
    private static <T extends Number> T readNumber(StringReader param0, Function<String, T> param1, Supplier<DynamicCommandExceptionType> param2) throws CommandSyntaxException {
        int var0 = param0.getCursor();

        while(param0.canRead() && isAllowedInputChat(param0)) {
            param0.skip();
        }

        String var1 = param0.getString().substring(var0, param0.getCursor());
        if (var1.isEmpty()) {
            return null;
        } else {
            try {
                return param1.apply(var1);
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

    @Nullable
    private static <T> T optionallyFormat(@Nullable T param0, Function<T, T> param1) {
        return param0 == null ? null : param1.apply(param0);
    }

    @FunctionalInterface
    public interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
        R create(@Nullable T var1, @Nullable T var2);
    }

    @FunctionalInterface
    public interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
        R create(StringReader var1, @Nullable T var2, @Nullable T var3) throws CommandSyntaxException;
    }

    public static class Floats extends MinMaxBounds<Float> {
        public static final MinMaxBounds.Floats ANY = new MinMaxBounds.Floats(null, null);
        private final Double minSq;
        private final Double maxSq;

        private static MinMaxBounds.Floats create(StringReader param0, @Nullable Float param1, @Nullable Float param2) throws CommandSyntaxException {
            if (param1 != null && param2 != null && param1 > param2) {
                throw ERROR_SWAPPED.createWithContext(param0);
            } else {
                return new MinMaxBounds.Floats(param1, param2);
            }
        }

        @Nullable
        private static Double squareOpt(@Nullable Float param0) {
            return param0 == null ? null : param0.doubleValue() * param0.doubleValue();
        }

        private Floats(@Nullable Float param0, @Nullable Float param1) {
            super(param0, param1);
            this.minSq = squareOpt(param0);
            this.maxSq = squareOpt(param1);
        }

        public static MinMaxBounds.Floats atLeast(float param0) {
            return new MinMaxBounds.Floats(param0, null);
        }

        public boolean matches(float param0) {
            if (this.min != null && this.min > param0) {
                return false;
            } else {
                return this.max == null || !(this.max < param0);
            }
        }

        public boolean matchesSqr(double param0) {
            if (this.minSq != null && this.minSq > param0) {
                return false;
            } else {
                return this.maxSq == null || !(this.maxSq < param0);
            }
        }

        public static MinMaxBounds.Floats fromJson(@Nullable JsonElement param0) {
            return fromJson(param0, ANY, GsonHelper::convertToFloat, MinMaxBounds.Floats::new);
        }

        public static MinMaxBounds.Floats fromReader(StringReader param0) throws CommandSyntaxException {
            return fromReader(param0, param0x -> param0x);
        }

        public static MinMaxBounds.Floats fromReader(StringReader param0, Function<Float, Float> param1) throws CommandSyntaxException {
            return fromReader(param0, MinMaxBounds.Floats::create, Float::parseFloat, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidFloat, param1);
        }
    }

    public static class Ints extends MinMaxBounds<Integer> {
        public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints(null, null);
        private final Long minSq;
        private final Long maxSq;

        private static MinMaxBounds.Ints create(StringReader param0, @Nullable Integer param1, @Nullable Integer param2) throws CommandSyntaxException {
            if (param1 != null && param2 != null && param1 > param2) {
                throw ERROR_SWAPPED.createWithContext(param0);
            } else {
                return new MinMaxBounds.Ints(param1, param2);
            }
        }

        @Nullable
        private static Long squareOpt(@Nullable Integer param0) {
            return param0 == null ? null : param0.longValue() * param0.longValue();
        }

        private Ints(@Nullable Integer param0, @Nullable Integer param1) {
            super(param0, param1);
            this.minSq = squareOpt(param0);
            this.maxSq = squareOpt(param1);
        }

        public static MinMaxBounds.Ints exactly(int param0) {
            return new MinMaxBounds.Ints(param0, param0);
        }

        public static MinMaxBounds.Ints atLeast(int param0) {
            return new MinMaxBounds.Ints(param0, null);
        }

        public boolean matches(int param0) {
            if (this.min != null && this.min > param0) {
                return false;
            } else {
                return this.max == null || this.max >= param0;
            }
        }

        public static MinMaxBounds.Ints fromJson(@Nullable JsonElement param0) {
            return fromJson(param0, ANY, GsonHelper::convertToInt, MinMaxBounds.Ints::new);
        }

        public static MinMaxBounds.Ints fromReader(StringReader param0) throws CommandSyntaxException {
            return fromReader(param0, param0x -> param0x);
        }

        public static MinMaxBounds.Ints fromReader(StringReader param0, Function<Integer, Integer> param1) throws CommandSyntaxException {
            return fromReader(param0, MinMaxBounds.Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, param1);
        }
    }
}
