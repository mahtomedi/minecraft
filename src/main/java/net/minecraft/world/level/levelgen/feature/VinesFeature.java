package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public VinesFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, NoneFeatureConfiguration param5
    ) {
        BlockPos.MutableBlockPos var0 = param4.mutable();

        for(int var1 = param4.getY(); var1 < 256; ++var1) {
            var0.set(param4);
            var0.move(param3.nextInt(4) - param3.nextInt(4), 0, param3.nextInt(4) - param3.nextInt(4));
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
