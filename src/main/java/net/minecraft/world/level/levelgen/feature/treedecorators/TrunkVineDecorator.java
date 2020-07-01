package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TrunkVineDecorator extends TreeDecorator {
    public static final Codec<TrunkVineDecorator> CODEC = Codec.unit(() -> TrunkVineDecorator.INSTANCE);
    public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.TRUNK_VINE;
    }

    @Override
    public void place(WorldGenLevel param0, Random param1, List<BlockPos> param2, List<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5) {
        param2.forEach(param4x -> {
            if (param1.nextInt(3) > 0) {
                BlockPos var3x = param4x.west();
                if (Feature.isAir(param0, var3x)) {
                    this.placeVine(param0, var3x, VineBlock.EAST, param4, param5);
                }
            }

            if (param1.nextInt(3) > 0) {
                BlockPos var1 = param4x.east();
                if (Feature.isAir(param0, var1)) {
                    this.placeVine(param0, var1, VineBlock.WEST, param4, param5);
                }
            }

            if (param1.nextInt(3) > 0) {
                BlockPos var2 = param4x.north();
                if (Feature.isAir(param0, var2)) {
                    this.placeVine(param0, var2, VineBlock.SOUTH, param4, param5);
                }
            }

            if (param1.nextInt(3) > 0) {
                BlockPos var3 = param4x.south();
                if (Feature.isAir(param0, var3)) {
                    this.placeVine(param0, var3, VineBlock.NORTH, param4, param5);
                }
            }

        });
    }
}
