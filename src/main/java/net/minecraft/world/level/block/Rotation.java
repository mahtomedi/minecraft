package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum Rotation implements StringRepresentable {
    NONE("none", OctahedralGroup.IDENTITY),
    CLOCKWISE_90("clockwise_90", OctahedralGroup.ROT_90_Y_NEG),
    CLOCKWISE_180("180", OctahedralGroup.ROT_180_FACE_XZ),
    COUNTERCLOCKWISE_90("counterclockwise_90", OctahedralGroup.ROT_90_Y_POS);

    public static final Codec<Rotation> CODEC = StringRepresentable.fromEnum(Rotation::values);
    private final String id;
    private final OctahedralGroup rotation;

    private Rotation(String param0, OctahedralGroup param1) {
        this.id = param0;
        this.rotation = param1;
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

    public static Rotation getRandom(RandomSource param0) {
        return Util.getRandom(values(), param0);
    }

    public static List<Rotation> getShuffled(RandomSource param0) {
        return Util.shuffledCopy(values(), param0);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
