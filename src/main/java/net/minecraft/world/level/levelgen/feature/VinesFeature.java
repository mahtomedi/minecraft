package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public VinesFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4) {
        BlockPos.MutableBlockPos var0 = param3.mutable();

        for(int var1 = param3.getY(); var1 < 256; ++var1) {
            var0.set(param3);
            var0.move(param2.nextInt(4) - param2.nextInt(4), 0, param2.nextInt(4) - param2.nextInt(4));
            var0.setY(var1);
            if (param0.isEmptyBlock(var0)) {
                for(Direction var2 : DIRECTIONS) {
                    if (var2 != Direction.DOWN && VineBlock.isAcceptableNeighbour(param0, var0, var2)) {
                        param0.setBlock(var0, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(var2), Boolean.valueOf(true)), 2);
                        break;
                    }
                }
            }
        }

        return true;
    }
}
