package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public class WrappedMinMaxBounds {
    public static final WrappedMinMaxBounds ANY = new WrappedMinMaxBounds(null, null);
    public static final SimpleCommandExceptionType ERROR_INTS_ONLY = new SimpleCommandExceptionType(Component.translatable("argument.range.ints"));
    @Nullable
    private final Float min;
    @Nullable
    private final Float max;

    public WrappedMinMaxBounds(@Nullable Float param0, @Nullable Float param1) {
        this.min = param0;
        this.max = param1;
    }

    public static WrappedMinMaxBounds exactly(float param0) {
        return new WrappedMinMaxBounds(param0, param0);
    }

    public static WrappedMinMaxBounds between(float param0, float param1) {
        return new WrappedMinMaxBounds(param0, param1);
    }

    public static WrappedMinMaxBounds atLeast(float param0) {
        return new WrappedMinMaxBounds(param0, null);
    }

    public static WrappedMinMaxBounds atMost(float param0) {
        return new WrappedMinMaxBounds(null, param0);
    }

    public boolean matches(float param0) {
        if (this.min != null && this.max != null && this.min > this.max && this.min > param0 && this.max < param0) {
            return false;
        } else if (this.min != null && this.min > param0) {
            return false;
        } else {
            return this.max == null || !(this.max < param0);
        }
    }

    public boolean matchesSqr(double param0) {
        if (this.min != null && this.max != null && this.min > this.max && (double)(this.min * this.min) > param0 && (double)(this.max * this.max) < param0) {
            return false;
        } else if (this.min != null && (double)(this.min * this.min) > param0) {
            return false;
        } else {
            return this.max == null || !((double)(this.max * this.max) < param0);
        }
    }

    @Nullable
    public Float getMin() {
        return this.min;
    }

    @Nullable
    public Float getMax() {
        return this.max;
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else if (this.min != null && this.max != null && this.min.equals(this.max)) {
            return new JsonPrimitive(this.min);
        } else {
            JsonObject var0 = new JsonObject();
            if (this.min != null) {
                var0.addProperty("min", this.min);
            }

            if (this.max != null) {
                var0.addProperty("max", this.min);
            }

            return var0;
        }
    }

    public static WrappedMinMaxBounds fromJson(@Nullable JsonElement param0) {
        if (param0 == null || param0.isJsonNull()) {
            return ANY;
        } else if (GsonHelper.isNumberValue(param0)) {
            float var0 = GsonHelper.convertToFloat(param0, "value");
            return new WrappedMinMaxBounds(var0, var0);
        } else {
            JsonObject var1 = GsonHelper.convertToJsonObject(param0, "value");
            Float var2 = var1.has("min") ? GsonHelper.getAsFloat(var1, "min") : null;
            Float var3 = var1.has("max") ? GsonHelper.getAsFloat(var1, "max") : null;
            return new WrappedMinMaxBounds(var2, var3);
        }
    }

    public static WrappedMinMaxBounds fromReader(StringReader param0, boolean param1) throws CommandSyntaxException {
        return fromReader(param0, param1, param0x -> param0x);
    }

    public static WrappedMinMaxBounds fromReader(StringReader param0, boolean param1, Function<Float, Float> param2) throws CommandSyntaxException {
        if (!param0.canRead()) {
            throw MinMaxBounds.ERROR_EMPTY.createWithContext(param0);
        } else {
            int var0 = param0.getCursor();
            Float var1 = optionallyFormat(readNumber(param0, param1), param2);
            Float var2;
            if (param0.canRead(2) && param0.peek() == '.' && param0.peek(1) == '.') {
                param0.skip();
                param0.skip();
                var2 = optionallyFormat(readNumber(param0, param1), param2);
                if (var1 == null && var2 == null) {
                    param0.setCursor(var0);
                    throw MinMaxBounds.ERROR_EMPTY.createWithContext(param0);
                }
            } else {
                if (!param1 && param0.canRead() && param0.peek() == '.') {
                    param0.setCursor(var0);
                    throw ERROR_INTS_ONLY.createWithContext(param0);
                }

                var2 = var1;
            }

            if (var1 == null && var2 == null) {
                param0.setCursor(var0);
                throw MinMaxBounds.ERROR_EMPTY.createWithContext(param0);
            } else {
                return new WrappedMinMaxBounds(var1, var2);
            }
        }
    }

    @Nullable
    private static Float readNumber(StringReader param0, boolean param1) throws CommandSyntaxException {
        int var0 = param0.getCursor();

        while(param0.canRead() && isAllowedNumber(param0, param1)) {
            param0.skip();
        }

        String var1 = param0.getString().substring(var0, param0.getCursor());
        if (var1.isEmpty()) {
            return null;
        } else {
            try {
                return Float.parseFloat(var1);
            } catch (NumberFormatException var5) {
                if (param1) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext(param0, var1);
                } else {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(param0, var1);
                }
            }
        }
    }

    private static boolean isAllowedNumber(StringReader param0, boolean param1) {
        char var0 = param0.peek();
        if ((var0 < '0' || var0 > '9') && var0 != '-') {
            if (param1 && var0 == '.') {
                return !param0.canRead(2) || param0.peek(1) != '.';
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Nullable
    private static Float optionallyFormat(@Nullable Float param0, Function<Float, Float> param1) {
        return param0 == null ? null : param1.apply(param0);
    }
}
