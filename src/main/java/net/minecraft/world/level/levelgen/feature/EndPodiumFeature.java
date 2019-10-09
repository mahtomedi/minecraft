package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPodiumFeature extends Feature<NoneFeatureConfiguration> {
    public static final BlockPos END_PODIUM_LOCATION = BlockPos.ZERO;
    private final boolean active;

    public EndPodiumFeature(boolean param0) {
        super(NoneFeatureConfiguration::deserialize);
        this.active = param0;
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        for(BlockPos var0 : BlockPos.betweenClosed(
            new BlockPos(param3.getX() - 4, param3.getY() - 1, param3.getZ() - 4), new BlockPos(param3.getX() + 4, param3.getY() + 32, param3.getZ() + 4)
        )) {
            boolean var1 = var0.closerThan(param3, 2.5);
            if (var1 || var0.closerThan(param3, 3.5)) {
                if (var0.getY() < param3.getY()) {
                    if (var1) {
                        this.setBlock(param0, var0, Blocks.BEDROCK.defaultBlockState());
                    } else if (var0.getY() < param3.getY()) {
                        this.setBlock(param0, var0, Blocks.END_STONE.defaultBlockState());
                    }
                } else if (var0.getY() > param3.getY()) {
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
            this.setBlock(param0, param3.above(var2), Blocks.BEDROCK.defaultBlockState());
        }

        BlockPos var3 = param3.above(2);

        for(Direction var4 : Direction.Plane.HORIZONTAL) {
            this.setBlock(param0, var3.relative(var4), Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, var4));
        }

        return true;
    }
}
