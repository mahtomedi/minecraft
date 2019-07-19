package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.TranslatableComponent;

public class WorldCoordinate {
    public static final SimpleCommandExceptionType ERROR_EXPECTED_DOUBLE = new SimpleCommandExceptionType(
        new TranslatableComponent("argument.pos.missing.double")
    );
    public static final SimpleCommandExceptionType ERROR_EXPECTED_INT = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos.missing.int"));
    private final boolean relative;
    private final double value;

    public WorldCoordinate(boolean param0, double param1) {
        this.relative = param0;
        this.value = param1;
    }

    public double get(double param0) {
        return this.relative ? this.value + param0 : this.value;
    }

    public static WorldCoordinate parseDouble(StringReader param0, boolean param1) throws CommandSyntaxException {
        if (param0.canRead() && param0.peek() == '^') {
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(param0);
        } else if (!param0.canRead()) {
            throw ERROR_EXPECTED_DOUBLE.createWithContext(param0);
        } else {
            boolean var0 = isRelative(param0);
            int var1 = param0.getCursor();
            double var2 = param0.canRead() && param0.peek() != ' ' ? param0.readDouble() : 0.0;
            String var3 = param0.getString().substring(var1, param0.getCursor());
            if (var0 && var3.isEmpty()) {
                return new WorldCoordinate(true, 0.0);
            } else {
                if (!var3.contains(".") && !var0 && param1) {
                    var2 += 0.5;
                }

                return new WorldCoordinate(var0, var2);
            }
        }
    }

    public static WorldCoordinate parseInt(StringReader param0) throws CommandSyntaxException {
        if (param0.canRead() && param0.peek() == '^') {
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(param0);
        } else if (!param0.canRead()) {
            throw ERROR_EXPECTED_INT.createWithContext(param0);
        } else {
            boolean var0 = isRelative(param0);
            double var1;
            if (param0.canRead() && param0.peek() != ' ') {
                var1 = var0 ? param0.readDouble() : (double)param0.readInt();
            } else {
                var1 = 0.0;
            }

            return new WorldCoordinate(var0, var1);
        }
    }

    private static boolean isRelative(StringReader param0) {
        boolean var0;
        if (param0.peek() == '~') {
            var0 = true;
            param0.skip();
        } else {
            var0 = false;
        }

        return var0;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof WorldCoordinate)) {
            return false;
        } else {
            WorldCoordinate var0 = (WorldCoordinate)param0;
            if (this.relative != var0.relative) {
                return false;
            } else {
                return Double.compare(var0.value, this.value) == 0;
            }
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.relative ? 1 : 0;
        long var1 = Double.doubleToLongBits(this.value);
        return 31 * var0 + (int)(var1 ^ var1 >>> 32);
    }

    public boolean isRelative() {
        return this.relative;
    }
}
