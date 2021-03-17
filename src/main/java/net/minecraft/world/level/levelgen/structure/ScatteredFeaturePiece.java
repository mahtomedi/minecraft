package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;

public abstract class ScatteredFeaturePiece extends StructurePiece {
    protected final int width;
    protected final int height;
    protected final int depth;
    protected int heightPosition = -1;

    protected ScatteredFeaturePiece(StructurePieceType param0, Random param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        super(param0, 0);
        this.width = param5;
        this.height = param6;
        this.depth = param7;
        this.setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(param1));
        if (this.getOrientation().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new BoundingBox(param2, param3, param4, param2 + param5 - 1, param3 + param6 - 1, param4 + param7 - 1);
        } else {
            this.boundingBox = new BoundingBox(param2, param3, param4, param2 + param7 - 1, param3 + param6 - 1, param4 + param5 - 1);
        }

    }

    protected ScatteredFeaturePiece(StructurePieceType param0, CompoundTag param1) {
        super(param0, param1);
        this.width = param1.getInt("Width");
        this.height = param1.getInt("Height");
        this.depth = param1.getInt("Depth");
        this.heightPosition = param1.getInt("HPos");
    }

    @Override
    protected void addAdditionalSaveData(ServerLevel param0, CompoundTag param1) {
        param1.putInt("Width", this.width);
        param1.putInt("Height", this.height);
        param1.putInt("Depth", this.depth);
        param1.putInt("HPos", this.heightPosition);
    }

    protected boolean updateAverageGroundHeight(LevelAccessor param0, BoundingBox param1, int param2) {
        if (this.heightPosition >= 0) {
            return true;
        } else {
            int var0 = 0;
            int var1 = 0;
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

            for(int var3 = this.boundingBox.z0; var3 <= this.boundingBox.z1; ++var3) {
                for(int var4 = this.boundingBox.x0; var4 <= this.boundingBox.x1; ++var4) {
                    var2.set(var4, 64, var3);
                    if (param1.isInside(var2)) {
                        var0 += param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, var2).getY();
                        ++var1;
                    }
                }
            }

            if (var1 == 0) {
                return false;
            } else {
                this.heightPosition = var0 / var1;
                this.boundingBox.move(0, this.heightPosition - this.boundingBox.y0 + param2, 0);
                return true;
            }
        }
    }
}
