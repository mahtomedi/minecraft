package net.minecraft.client.renderer;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum FaceInfo {
    DOWN(
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MAX_Z)
    ),
    UP(
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MIN_Z)
    ),
    NORTH(
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MIN_Z)
    ),
    SOUTH(
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MAX_Z)
    ),
    WEST(
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MIN_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MAX_Z)
    ),
    EAST(
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MAX_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MIN_Y, FaceInfo.Constants.MIN_Z),
        new FaceInfo.VertexInfo(FaceInfo.Constants.MAX_X, FaceInfo.Constants.MAX_Y, FaceInfo.Constants.MIN_Z)
    );

    private static final FaceInfo[] BY_FACING = Util.make(new FaceInfo[6], param0 -> {
        param0[FaceInfo.Constants.MIN_Y] = DOWN;
        param0[FaceInfo.Constants.MAX_Y] = UP;
        param0[FaceInfo.Constants.MIN_Z] = NORTH;
        param0[FaceInfo.Constants.MAX_Z] = SOUTH;
        param0[FaceInfo.Constants.MIN_X] = WEST;
        param0[FaceInfo.Constants.MAX_X] = EAST;
    });
    private final FaceInfo.VertexInfo[] infos;

    public static FaceInfo fromFacing(Direction param0) {
        return BY_FACING[param0.get3DDataValue()];
    }

    private FaceInfo(FaceInfo.VertexInfo... param0) {
        this.infos = param0;
    }

    public FaceInfo.VertexInfo getVertexInfo(int param0) {
        return this.infos[param0];
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Constants {
        public static final int MAX_Z = Direction.SOUTH.get3DDataValue();
        public static final int MAX_Y = Direction.UP.get3DDataValue();
        public static final int MAX_X = Direction.EAST.get3DDataValue();
        public static final int MIN_Z = Direction.NORTH.get3DDataValue();
        public static final int MIN_Y = Direction.DOWN.get3DDataValue();
        public static final int MIN_X = Direction.WEST.get3DDataValue();
    }

    @OnlyIn(Dist.CLIENT)
    public static class VertexInfo {
        public final int xFace;
        public final int yFace;
        public final int zFace;

        private VertexInfo(int param0, int param1, int param2) {
            this.xFace = param0;
            this.yFace = param1;
            this.zFace = param2;
        }
    }
}
