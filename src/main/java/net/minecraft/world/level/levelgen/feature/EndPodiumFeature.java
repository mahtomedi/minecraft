package net.minecraft.world.level.levelgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPodiumFeature extends Feature<NoneFeatureConfiguration> {
    public static final int PODIUM_RADIUS = 4;
    public static final int PODIUM_PILLAR_HEIGHT = 4;
    public static final int RIM_RADIUS = 1;
    public static final float CORNER_ROUNDING = 0.5F;
    private static final BlockPos END_PODIUM_LOCATION = BlockPos.ZERO;
    private final boolean active;

    public static BlockPos getLocation(BlockPos param0) {
        return END_PODIUM_LOCATION.offset(param0);
    }

    public EndPodiumFeature(boolean param0) {
        super(NoneFeatureConfiguration.CODEC);
        this.active = param0;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();

        for(BlockPos var2 : BlockPos.betweenClosed(
            new BlockPos(var0.getX() - 4, var0.getY() - 1, var0.getZ() - 4), new BlockPos(var0.getX() + 4, var0.getY() + 32, var0.getZ() + 4)
        )) {
            boolean var3 = var2.closerThan(var0, 2.5);
            if (var3 || var2.closerThan(var0, 3.5)) {
                if (var2.getY() < var0.getY()) {
                    if (var3) {
                        this.setBlock(var1, var2, Blocks.BEDROCK.defaultBlockState());
                    } else if (var2.getY() < var0.getY()) {
                        this.setBlock(var1, var2, Blocks.END_STONE.defaultBlockState());
                    }
                } else if (var2.getY() > var0.getY()) {
                    this.setBlock(var1, var2, Blocks.AIR.defaultBlockState());
                } else if (!var3) {
                    this.setBlock(var1, var2, Blocks.BEDROCK.defaultBlockState());
                } else if (this.active) {
                    this.setBlock(var1, new BlockPos(var2), Blocks.END_PORTAL.defaultBlockState());
                } else {
                    this.setBlock(var1, new BlockPos(var2), Blocks.AIR.defaultBlockState());
                }
            }
        }

        for(int var4 = 0; var4 < 4; ++var4) {
            this.setBlock(var1, var0.above(var4), Blocks.BEDROCK.defaultBlockState());
        }

        BlockPos var5 = var0.above(2);

        for(Direction var6 : Direction.Plane.HORIZONTAL) {
            this.setBlock(var1, var5.relative(var6), Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, var6));
        }

        return true;
    }
}
