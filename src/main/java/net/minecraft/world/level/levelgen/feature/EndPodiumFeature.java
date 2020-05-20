package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPodiumFeature extends Feature<NoneFeatureConfiguration> {
    public static final BlockPos END_PODIUM_LOCATION = BlockPos.ZERO;
    private final boolean active;

    public EndPodiumFeature(boolean param0) {
        super(NoneFeatureConfiguration.CODEC);
        this.active = param0;
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, NoneFeatureConfiguration param5
    ) {
        for(BlockPos var0 : BlockPos.betweenClosed(
            new BlockPos(param4.getX() - 4, param4.getY() - 1, param4.getZ() - 4), new BlockPos(param4.getX() + 4, param4.getY() + 32, param4.getZ() + 4)
        )) {
            boolean var1 = var0.closerThan(param4, 2.5);
            if (var1 || var0.closerThan(param4, 3.5)) {
                if (var0.getY() < param4.getY()) {
                    if (var1) {
                        this.setBlock(param0, var0, Blocks.BEDROCK.defaultBlockState());
                    } else if (var0.getY() < param4.getY()) {
                        this.setBlock(param0, var0, Blocks.END_STONE.defaultBlockState());
                    }
                } else if (var0.getY() > param4.getY()) {
                    this.setBlock(param0, var0, Blocks.AIR.defaultBlockState());
                } else if (!var1) {
                    this.setBlock(param0, var0, Blocks.BEDROCK.defaultBlockState());
                } else if (this.active) {
                    this.setBlock(param0, new BlockPos(var0), Blocks.END_PORTAL.defaultBlockState());
                } else {
                    this.setBlock(param0, new BlockPos(var0), Blocks.AIR.defaultBlockState());
                }
            }
        }

        for(int var2 = 0; var2 < 4; ++var2) {
            this.setBlock(param0, param4.above(var2), Blocks.BEDROCK.defaultBlockState());
        }

        BlockPos var3 = param4.above(2);

        for(Direction var4 : Direction.Plane.HORIZONTAL) {
            this.setBlock(param0, var3.relative(var4), Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, var4));
        }

        return true;
    }
}
