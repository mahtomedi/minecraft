package net.minecraft.world.level;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface BlockGetter extends LevelHeightAccessor {
    @Nullable
    BlockEntity getBlockEntity(BlockPos var1);

    BlockState getBlockState(BlockPos var1);

    FluidState getFluidState(BlockPos var1);

    default int getLightEmission(BlockPos param0) {
        return this.getBlockState(param0).getLightEmission();
    }

    default int getMaxLightLevel() {
        return 15;
    }

    default Stream<BlockState> getBlockStates(AABB param0) {
        return BlockPos.betweenClosedStream(param0).map(this::getBlockState);
    }

    default BlockHitResult isBlockInLine(ClipBlockStateContext param0) {
        return traverseBlocks(
            param0.getFrom(),
            param0.getTo(),
            param0,
            (param0x, param1) -> {
                BlockState var0 = this.getBlockState(param1);
                Vec3 var1x = param0x.getFrom().subtract(param0x.getTo());
                return param0x.isTargetBlock().test(var0)
                    ? new BlockHitResult(param0x.getTo(), Direction.getNearest(var1x.x, var1x.y, var1x.z), new BlockPos(param0x.getTo()), false)
                    : null;
            },
            param0x -> {
                Vec3 var0 = param0x.getFrom().subtract(param0x.getTo());
                return BlockHitResult.miss(param0x.getTo(), Direction.getNearest(var0.x, var0.y, var0.z), new BlockPos(param0x.getTo()));
            }
        );
    }

    default BlockHitResult clip(ClipContext param0) {
        return traverseBlocks(param0.getFrom(), param0.getTo(), param0, (param0x, param1) -> {
            BlockState var0 = this.getBlockState(param1);
            FluidState var1x = this.getFluidState(param1);
            Vec3 var2 = param0x.getFrom();
            Vec3 var3 = param0x.getTo();
            VoxelShape var4 = param0x.getBlockShape(var0, this, param1);
            BlockHitResult var5 = this.clipWithInteractionOverride(var2, var3, param1, var4, var0);
            VoxelShape var6 = param0x.getFluidShape(var1x, this, param1);
            BlockHitResult var7 = var6.clip(var2, var3, param1);
            double var8 = var5 == null ? Double.MAX_VALUE : param0x.getFrom().distanceToSqr(var5.getLocation());
            double var9 = var7 == null ? Double.MAX_VALUE : param0x.getFrom().distanceToSqr(var7.getLocation());
            return var8 <= var9 ? var5 : var7;
        }, param0x -> {
            Vec3 var0 = param0x.getFrom().subtract(param0x.getTo());
            return BlockHitResult.miss(param0x.getTo(), Direction.getNearest(var0.x, var0.y, var0.z), new BlockPos(param0x.getTo()));
        });
    }

    @Nullable
    default BlockHitResult clipWithInteractionOverride(Vec3 param0, Vec3 param1, BlockPos param2, VoxelShape param3, BlockState param4) {
        BlockHitResult var0 = param3.clip(param0, param1, param2);
        if (var0 != null) {
            BlockHitResult var1 = param4.getInteractionShape(this, param2).clip(param0, param1, param2);
            if (var1 != null && var1.getLocation().subtract(param0).lengthSqr() < var0.getLocation().subtract(param0).lengthSqr()) {
                return var0.withDirection(var1.getDirection());
            }
        }

        return var0;
    }

    default double getBlockFloorHeight(VoxelShape param0, Supplier<VoxelShape> param1) {
        if (!param0.isEmpty()) {
            return param0.max(Direction.Axis.Y);
        } else {
            double var0 = param1.get().max(Direction.Axis.Y);
            return var0 >= 1.0 ? var0 - 1.0 : Double.NEGATIVE_INFINITY;
        }
    }

    default double getBlockFloorHeight(BlockPos param0) {
        return this.getBlockFloorHeight(this.getBlockState(param0).getCollisionShape(this, param0), () -> {
            BlockPos var0 = param0.below();
            return this.getBlockState(var0).getCollisionShape(this, var0);
        });
    }

    static <T, C> T traverseBlocks(Vec3 param0, Vec3 param1, C param2, BiFunction<C, BlockPos, T> param3, Function<C, T> param4) {
        if (param0.equals(param1)) {
            return param4.apply(param2);
        } else {
            double var0 = Mth.lerp(-1.0E-7, param1.x, param0.x);
            double var1 = Mth.lerp(-1.0E-7, param1.y, param0.y);
            double var2 = Mth.lerp(-1.0E-7, param1.z, param0.z);
            double var3 = Mth.lerp(-1.0E-7, param0.x, param1.x);
            double var4 = Mth.lerp(-1.0E-7, param0.y, param1.y);
            double var5 = Mth.lerp(-1.0E-7, param0.z, param1.z);
            int var6 = Mth.floor(var3);
            int var7 = Mth.floor(var4);
            int var8 = Mth.floor(var5);
            BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos(var6, var7, var8);
            T var10 = param3.apply(param2, var9);
            if (var10 != null) {
                return var10;
            } else {
                double var11 = var0 - var3;
                double var12 = var1 - var4;
                double var13 = var2 - var5;
                int var14 = Mth.sign(var11);
                int var15 = Mth.sign(var12);
                int var16 = Mth.sign(var13);
                double var17 = var14 == 0 ? Double.MAX_VALUE : (double)var14 / var11;
                double var18 = var15 == 0 ? Double.MAX_VALUE : (double)var15 / var12;
                double var19 = var16 == 0 ? Double.MAX_VALUE : (double)var16 / var13;
                double var20 = var17 * (var14 > 0 ? 1.0 - Mth.frac(var3) : Mth.frac(var3));
                double var21 = var18 * (var15 > 0 ? 1.0 - Mth.frac(var4) : Mth.frac(var4));
                double var22 = var19 * (var16 > 0 ? 1.0 - Mth.frac(var5) : Mth.frac(var5));

                while(var20 <= 1.0 || var21 <= 1.0 || var22 <= 1.0) {
                    if (var20 < var21) {
                        if (var20 < var22) {
                            var6 += var14;
                            var20 += var17;
                        } else {
                            var8 += var16;
                            var22 += var19;
                        }
                    } else if (var21 < var22) {
                        var7 += var15;
                        var21 += var18;
                    } else {
                        var8 += var16;
                        var22 += var19;
                    }

                    T var23 = param3.apply(param2, var9.set(var6, var7, var8));
                    if (var23 != null) {
                        return var23;
                    }
                }

                return param4.apply(param2);
            }
        }
    }
}
