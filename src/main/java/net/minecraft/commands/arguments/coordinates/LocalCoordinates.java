package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LocalCoordinates implements Coordinates {
    public static final char PREFIX_LOCAL_COORDINATE = '^';
    private final double left;
    private final double up;
    private final double forwards;

    public LocalCoordinates(double param0, double param1, double param2) {
        this.left = param0;
        this.up = param1;
        this.forwards = param2;
    }

    @Override
    public Vec3 getPosition(CommandSourceStack param0) {
        Vec2 var0 = param0.getRotation();
        Vec3 var1 = param0.getAnchor().apply(param0);
        float var2 = Mth.cos((var0.y + 90.0F) * (float) (Math.PI / 180.0));
        float var3 = Mth.sin((var0.y + 90.0F) * (float) (Math.PI / 180.0));
        float var4 = Mth.cos(-var0.x * (float) (Math.PI / 180.0));
        float var5 = Mth.sin(-var0.x * (float) (Math.PI / 180.0));
        float var6 = Mth.cos((-var0.x + 90.0F) * (float) (Math.PI / 180.0));
        float var7 = Mth.sin((-var0.x + 90.0F) * (float) (Math.PI / 180.0));
        Vec3 var8 = new Vec3((double)(var2 * var4), (double)var5, (double)(var3 * var4));
        Vec3 var9 = new Vec3((double)(var2 * var6), (double)var7, (double)(var3 * var6));
        Vec3 var10 = var8.cross(var9).scale(-1.0);
        double var11 = var8.x * this.forwards + var9.x * this.up + var10.x * this.left;
        double var12 = var8.y * this.forwards + var9.y * this.up + var10.y * this.left;
        double var13 = var8.z * this.forwards + var9.z * this.up + var10.z * this.left;
        return new Vec3(var1.x + var11, var1.y + var12, var1.z + var13);
    }

    @Override
    public Vec2 getRotation(CommandSourceStack param0) {
        return Vec2.ZERO;
    }

    @Override
    public boolean isXRelative() {
        return true;
    }

    @Override
    public boolean isYRelative() {
        return true;
    }

    @Override
    public boolean isZRelative() {
        return true;
    }

    public static LocalCoordinates parse(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();
        double var1 = readDouble(param0, var0);
        if (param0.canRead() && param0.peek() == ' ') {
            param0.skip();
            double var2 = readDouble(param0, var0);
            if (param0.canRead() && param0.peek() == ' ') {
                param0.skip();
                double var3 = readDouble(param0, var0);
                return new LocalCoordinates(var1, var2, var3);
            } else {
                param0.setCursor(var0);
                throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(param0);
            }
        } else {
            param0.setCursor(var0);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(param0);
        }
    }

    private static double readDouble(StringReader param0, int param1) throws CommandSyntaxException {
        if (!param0.canRead()) {
            throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext(param0);
        } else if (param0.peek() != '^') {
            param0.setCursor(param1);
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(param0);
        } else {
            param0.skip();
            return param0.canRead() && param0.peek() != ' ' ? param0.readDouble() : 0.0;
        }
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof LocalCoordinates)) {
            return false;
        } else {
            LocalCoordinates var0 = (LocalCoordinates)param0;
            return this.left == var0.left && this.up == var0.up && this.forwards == var0.forwards;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.left, this.up, this.forwards);
    }
}
