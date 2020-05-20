package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LeaveVineDecorator extends TreeDecorator {
    public static final Codec<LeaveVineDecorator> CODEC = Codec.unit(() -> LeaveVineDecorator.INSTANCE);
    public static final LeaveVineDecorator INSTANCE = new LeaveVineDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.LEAVE_VINE;
    }

    @Override
    public void place(LevelAccessor param0, Random param1, List<BlockPos> param2, List<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5) {
        param3.forEach(param4x -> {
            if (param1.nextInt(4) == 0) {
                BlockPos var3x = param4x.west();
                if (Feature.isAir(param0, var3x)) {
                    this.addHangingVine(param0, var3x, VineBlock.EAST, param4, param5);
                }
            }

            if (param1.nextInt(4) == 0) {
                BlockPos var1 = param4x.east();
                if (Feature.isAir(param0, var1)) {
                    this.addHangingVine(param0, var1, VineBlock.WEST, param4, param5);
                }
            }

            if (param1.nextInt(4) == 0) {
                BlockPos var2 = param4x.north();
                if (Feature.isAir(param0, var2)) {
                    this.addHangingVine(param0, var2, VineBlock.SOUTH, param4, param5);
                }
            }

            if (param1.nextInt(4) == 0) {
                BlockPos var3 = param4x.south();
                if (Feature.isAir(param0, var3)) {
                    this.addHangingVine(param0, var3, VineBlock.NORTH, param4, param5);
                }
            }

        });
    }

    private void addHangingVine(LevelSimulatedRW param0, BlockPos param1, BooleanProperty param2, Set<BlockPos> param3, BoundingBox param4) {
        this.placeVine(param0, param1, param2, param3, param4);
        int var0 = 4;

        for(BlockPos var7 = param1.below(); Feature.isAir(param0, var7) && var0 > 0; --var0) {
            this.placeVine(param0, var7, param2, param3, param4);
            var7 = var7.below();
        }

    }
}
