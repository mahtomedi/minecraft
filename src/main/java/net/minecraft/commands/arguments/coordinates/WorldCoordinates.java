package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class WorldCoordinates implements Coordinates {
    private final WorldCoordinate x;
    private final WorldCoordinate y;
    private final WorldCoordinate z;

    public WorldCoordinates(WorldCoordinate param0, WorldCoordinate param1, WorldCoordinate param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    @Override
    public Vec3 getPosition(CommandSourceStack param0) {
        Vec3 var0 = param0.getPosition();
        return new Vec3(this.x.get(var0.x), this.y.get(var0.y), this.z.get(var0.z));
    }

    @Override
    public Vec2 getRotation(CommandSourceStack param0) {
        Vec2 var0 = param0.getRotation();
        return new Vec2((float)this.x.get((double)var0.x), (float)this.y.get((double)var0.y));
    }

    @Override
    public boolean isXRelative() {
        return this.x.isRelative();
    }

    @Override
    public boolean isYRelative() {
        return this.y.isRelative();
    }

    @Override
    public boolean isZRelative() {
        return this.z.isRelative();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof WorldCoordinates)) {
            return false;
        } else {
            WorldCoordinates var0 = (WorldCoordinates)param0;
            if (!this.x.equals(var0.x)) {
                return false;
            } else {
                return !this.y.equals(var0.y) ? false : this.z.equals(var0.z);
            }
        }
    }

    public static WorldCoordinates parseInt(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();
        WorldCoordinate var1 = WorldCoordinate.parseInt(param0);
        if (param0.canRead() && param0.peek() == ' ') {
            param0.skip();
            WorldCoordinate var2 = WorldCoordinate.parseInt(param0);
            if (param0.canRead() && param0.peek() == ' ') {
                param0.skip();
                WorldCoordinate var3 = WorldCoordinate.parseInt(param0);
                return new WorldCoordinates(var1, var2, var3);
            } else {
                param0.setCursor(var0);
                throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(param0);
            }
        } else {
            param0.setCursor(var0);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(param0);
        }
    }

    public static WorldCoordinates parseDouble(StringReader param0, boolean param1) throws CommandSyntaxException {
        int var0 = param0.getCursor();
        WorldCoordinate var1 = WorldCoordinate.parseDouble(param0, param1);
        if (param0.canRead() && param0.peek() == ' ') {
            param0.skip();
            WorldCoordinate var2 = WorldCoordinate.parseDouble(param0, false);
            if (param0.canRead() && param0.peek() == ' ') {
                param0.skip();
                WorldCoordinate var3 = WorldCoordinate.parseDouble(param0, param1);
                return new WorldCoordinates(var1, var2, var3);
            } else {
                param0.setCursor(var0);
                throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(param0);
            }
        } else {
            param0.setCursor(var0);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(param0);
        }
    }

    public static WorldCoordinates absolute(double param0, double param1, double param2) {
        return new WorldCoordinates(new WorldCoordinate(false, param0), new WorldCoordinate(false, param1), new WorldCoordinate(false, param2));
    }

    public static WorldCoordinates absolute(Vec2 param0) {
        return new WorldCoordinates(new WorldCoordinate(false, (double)param0.x), new WorldCoordinate(false, (double)param0.y), new WorldCoordinate(true, 0.0));
    }

    public static WorldCoordinates current() {
        return new WorldCoordinates(new WorldCoordinate(true, 0.0), new WorldCoordinate(true, 0.0), new WorldCoordinate(true, 0.0));
    }

    @Override
    public int hashCode() {
        int var0 = this.x.hashCode();
        var0 = 31 * var0 + this.y.hashCode();
        return 31 * var0 + this.z.hashCode();
    }
}
