package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import com.mojang.math.OctahedralGroup;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.Direction;

public enum Rotation {
    NONE(OctahedralGroup.IDENTITY),
    CLOCKWISE_90(OctahedralGroup.ROT_90_Y_NEG),
    CLOCKWISE_180(OctahedralGroup.ROT_180_FACE_XZ),
    COUNTERCLOCKWISE_90(OctahedralGroup.ROT_90_Y_POS);

    private final OctahedralGroup rotation;

    private Rotation(OctahedralGroup param0) {
        this.rotation = param0;
    }

    public Rotation getRotated(Rotation param0) {
        switch(param0) {
            case CLOCKWISE_180:
                switch(this) {
                    case NONE:
                        return CLOCKWISE_180;
                    case CLOCKWISE_90:
                        return COUNTERCLOCKWISE_90;
                    case CLOCKWISE_180:
                        return NONE;
                    case COUNTERCLOCKWISE_90:
                        return CLOCKWISE_90;
                }
            case COUNTERCLOCKWISE_90:
                switch(this) {
                    case NONE:
                        return COUNTERCLOCKWISE_90;
                    case CLOCKWISE_90:
                        return NONE;
                    case CLOCKWISE_180:
                        return CLOCKWISE_90;
                    case COUNTERCLOCKWISE_90:
                        return CLOCKWISE_180;
                }
            case CLOCKWISE_90:
                switch(this) {
                    case NONE:
                        return CLOCKWISE_90;
                    case CLOCKWISE_90:
                        return CLOCKWISE_180;
                    case CLOCKWISE_180:
                        return COUNTERCLOCKWISE_90;
                    case COUNTERCLOCKWISE_90:
                        return NONE;
                }
            default:
                return this;
        }
    }

    public OctahedralGroup rotation() {
        return this.rotation;
    }

    public Direction rotate(Direction param0) {
        if (param0.getAxis() == Direction.Axis.Y) {
            return param0;
        } else {
            switch(this) {
                case CLOCKWISE_90:
                    return param0.getClockWise();
                case CLOCKWISE_180:
                    return param0.getOpposite();
                case COUNTERCLOCKWISE_90:
                    return param0.getCounterClockWise();
                default:
                    return param0;
            }
        }
    }

    public int rotate(int param0, int param1) {
        switch(this) {
            case CLOCKWISE_90:
                return (param0 + param1 / 4) % param1;
            case CLOCKWISE_180:
                return (param0 + param1 / 2) % param1;
            case COUNTERCLOCKWISE_90:
                return (param0 + param1 * 3 / 4) % param1;
            default:
                return param0;
        }
    }

    public static Rotation getRandom(Random param0) {
        return Util.getRandom(values(), param0);
    }

    public static List<Rotation> getShuffled(Random param0) {
        List<Rotation> var0 = Lists.newArrayList(values());
        Collections.shuffle(var0, param0);
        return var0;
    }
}
