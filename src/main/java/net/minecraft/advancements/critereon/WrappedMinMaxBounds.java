package net.minecraft.advancements.critereon;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.TranslatableComponent;

public class WrappedMinMaxBounds {
    public static final WrappedMinMaxBounds ANY = new WrappedMinMaxBounds(null, null);
    public static final SimpleCommandExceptionType ERROR_INTS_ONLY = new SimpleCommandExceptionType(new TranslatableComponent("argument.range.ints"));
    private final Float min;
    private final Float max;

    public WrappedMinMaxBounds(@Nullable Float param0, @Nullable Float param1) {
        this.min = param0;
        this.max = param1;
    }

    @Nullable
    public Float getMin() {
        return this.min;
    }

    @Nullable
    public Float getMax() {
        return this.max;
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
