package net.minecraft.world.level;

import java.util.function.BiFunction;
import java.util.function.Function;
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

public interface BlockGetter {
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

    default int getMaxBuildHeight() {
        return 256;
    }

    default Stream<BlockState> getBlockStates(AABB param0) {
        return BlockPos.betweenClosedStream(param0).map(this::getBlockState);
    }

    default BlockHitResult clip(ClipContext param0) {
        return traverseBlocks(param0, (param0x, param1) -> {
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

    static <T> T traverseBlocks(ClipContext param0, BiFunction<ClipContext, BlockPos, T> param1, Function<ClipContext, T> param2) {
        Vec3 var0 = param0.getFrom();
        Vec3 var1 = param0.getTo();
        if (var0.equals(var1)) {
            return param2.apply(param0);
        } else {
            double var2 = Mth.lerp(-1.0E-7, var1.x, var0.x);
            double var3 = Mth.lerp(-1.0E-7, var1.y, var0.y);
            double var4 = Mth.lerp(-1.0E-7, var1.z, var0.z);
            double var5 = Mth.lerp(-1.0E-7, var0.x, var1.x);
            double var6 = Mth.lerp(-1.0E-7, var0.y, var1.y);
            double var7 = Mth.lerp(-1.0E-7, var0.z, var1.z);
            int var8 = Mth.floor(var5);
            int var9 = Mth.floor(var6);
            int var10 = Mth.floor(var7);
            BlockPos.MutableBlockPos var11 = new BlockPos.MutableBlockPos(var8, var9, var10);
            T var12 = param1.apply(param0, var11);
            if (var12 != null) {
                return var12;
            } else {
                double var13 = var2 - var5;
                double var14 = var3 - var6;
                double var15 = var4 - var7;
                int var16 = Mth.sign(var13);
                int var17 = Mth.sign(var14);
                int var18 = Mth.sign(var15);
                double var19 = var16 == 0 ? Double.MAX_VALUE : (double)var16 / var13;
                double var20 = var17 == 0 ? Double.MAX_VALUE : (double)var17 / var14;
                double var21 = var18 == 0 ? Double.MAX_VALUE : (double)var18 / var15;
                double var22 = var19 * (var16 > 0 ? 1.0 - Mth.frac(var5) : Mth.frac(var5));
                double var23 = var20 * (var17 > 0 ? 1.0 - Mth.frac(var6) : Mth.frac(var6));
                double var24 = var21 * (var18 > 0 ? 1.0 - Mth.frac(var7) : Mth.frac(var7));

                while(var22 <= 1.0 || var23 <= 1.0 || var24 <= 1.0) {
                    if (var22 < var23) {
                        if (var22 < var24) {
                            var8 += var16;
                            var22 += var19;
                        } else {
                            var10 += var18;
                            var24 += var21;
                        }
                    } else if (var23 < var24) {
                        var9 += var17;
                        var23 += var20;
                    } else {
                        var10 += var18;
                        var24 += var21;
                    }

                    T var25 = param1.apply(param0, var11.set(var8, var9, var10));
                    if (var25 != null) {
                        return var25;
                    }
                }

                return param2.apply(param0);
            }
        }
    }
}
