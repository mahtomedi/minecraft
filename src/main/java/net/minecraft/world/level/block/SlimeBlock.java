package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SlimeBlock extends HalfTransparentBlock {
    public SlimeBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.TRANSLUCENT;
    }

    @Override
    public void fallOn(Level param0, BlockPos param1, Entity param2, float param3) {
        if (param2.isSneaking()) {
            super.fallOn(param0, param1, param2, param3);
        } else {
            param2.causeFallDamage(param3, 0.0F);
        }

    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter param0, Entity param1) {
        if (param1.isSneaking()) {
            super.updateEntityAfterFallOn(param0, param1);
        } else {
            Vec3 var0 = param1.getDeltaMovement();
            if (var0.y < 0.0) {
                double var1 = param1 instanceof LivingEntity ? 1.0 : 0.8;
                param1.setDeltaMovement(var0.x, -var0.y * var1, var0.z);
            }
        }

    }

    @Override
    public void stepOn(Level param0, BlockPos param1, Entity param2) {
        double var0 = Math.abs(param2.getDeltaMovement().y);
        if (var0 < 0.1 && !param2.isSneaking()) {
            double var1 = 0.4 + var0 * 0.2;
            param2.setDeltaMovement(param2.getDeltaMovement().multiply(var1, 1.0, var1));
        }

        super.stepOn(param0, param1, param2);
    }
}
