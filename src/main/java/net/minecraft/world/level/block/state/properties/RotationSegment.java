package net.minecraft.world.level.block.state.properties;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class RotationSegment {
    private static final int MAX_SEGMENT_INDEX = 15;
    private static final int NORTH_0 = 0;
    private static final int EAST_90 = 4;
    private static final int SOUTH_180 = 8;
    private static final int WEST_270 = 12;

    public static int getMaxSegmentIndex() {
        return 15;
    }

    public static int convertToSegment(Direction param0) {
        return param0.getAxis().isVertical() ? 0 : param0.getOpposite().get2DDataValue() * 4;
    }

    public static int convertToSegment(float param0) {
        return Mth.floor((double)((180.0F + param0) * 16.0F / 360.0F) + 0.5) & 15;
    }

    public static Optional<Direction> convertToDirection(int param0) {
        Direction var0 = switch(param0) {
            case 0 -> Direction.NORTH;
            case 4 -> Direction.EAST;
            case 8 -> Direction.SOUTH;
            case 12 -> Direction.WEST;
            default -> null;
        };
        return Optional.ofNullable(var0);
    }

    public static float convertToDegrees(int param0) {
        return (float)param0 * 22.5F;
    }
}
