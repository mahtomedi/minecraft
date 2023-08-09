package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.phys.AABB;

public class UnderwaterMagmaFeature extends Feature<UnderwaterMagmaConfiguration> {
    public UnderwaterMagmaFeature(Codec<UnderwaterMagmaConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<UnderwaterMagmaConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        UnderwaterMagmaConfiguration var2 = param0.config();
        RandomSource var3 = param0.random();
        OptionalInt var4 = getFloorY(var0, var1, var2);
        if (var4.isEmpty()) {
            return false;
        } else {
            BlockPos var5 = var1.atY(var4.getAsInt());
            Vec3i var6 = new Vec3i(var2.placementRadiusAroundFloor, var2.placementRadiusAroundFloor, var2.placementRadiusAroundFloor);
            AABB var7 = new AABB(var5.subtract(var6), var5.offset(var6));
            return BlockPos.betweenClosedStream(var7)
                    .filter(param2 -> var3.nextFloat() < var2.placementProbabilityPerValidPosition)
                    .filter(param1 -> this.isValidPlacement(var0, param1))
                    .mapToInt(param1 -> {
                        var0.setBlock(param1, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                        return 1;
                    })
                    .sum()
                > 0;
        }
    }

    private static OptionalInt getFloorY(WorldGenLevel param0, BlockPos param1, UnderwaterMagmaConfiguration param2) {
        Predicate<BlockState> var0 = param0x -> param0x.is(Blocks.WATER);
        Predicate<BlockState> var1 = param0x -> !param0x.is(Blocks.WATER);
        Optional<Column> var2 = Column.scan(param0, param1, param2.floorSearchRange, var0, var1);
        return var2.map(Column::getFloor).orElseGet(OptionalInt::empty);
    }

    private boolean isValidPlacement(WorldGenLevel param0, BlockPos param1) {
        if (!this.isWaterOrAir(param0, param1) && !this.isWaterOrAir(param0, param1.below())) {
            for(Direction var0 : Direction.Plane.HORIZONTAL) {
                if (this.isWaterOrAir(param0, param1.relative(var0))) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean isWaterOrAir(LevelAccessor param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return var0.is(Blocks.WATER) || var0.isAir();
    }
}
