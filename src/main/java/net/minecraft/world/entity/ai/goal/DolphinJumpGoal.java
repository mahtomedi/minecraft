package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class DolphinJumpGoal extends JumpGoal {
    private static final int[] STEPS_TO_CHECK = new int[]{0, 1, 4, 5, 6, 7};
    private final Dolphin dolphin;
    private final int interval;
    private boolean breached;

    public DolphinJumpGoal(Dolphin param0, int param1) {
        this.dolphin = param0;
        this.interval = reducedTickDelay(param1);
    }

    @Override
    public boolean canUse() {
        if (this.dolphin.getRandom().nextInt(this.interval) != 0) {
            return false;
        } else {
            Direction var0 = this.dolphin.getMotionDirection();
            int var1 = var0.getStepX();
            int var2 = var0.getStepZ();
            BlockPos var3 = this.dolphin.blockPosition();

            for(int var4 : STEPS_TO_CHECK) {
                if (!this.waterIsClear(var3, var1, var2, var4) || !this.surfaceIsClear(var3, var1, var2, var4)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean waterIsClear(BlockPos param0, int param1, int param2, int param3) {
        BlockPos var0 = param0.offset(param1 * param3, 0, param2 * param3);
        return this.dolphin.level.getFluidState(var0).is(FluidTags.WATER) && !this.dolphin.level.getBlockState(var0).getMaterial().blocksMotion();
    }

    private boolean surfaceIsClear(BlockPos param0, int param1, int param2, int param3) {
        return this.dolphin.level.getBlockState(param0.offset(param1 * param3, 1, param2 * param3)).isAir()
            && this.dolphin.level.getBlockState(param0.offset(param1 * param3, 2, param2 * param3)).isAir();
    }

    @Override
    public boolean canContinueToUse() {
        double var0 = this.dolphin.getDeltaMovement().y;
        return (!(var0 * var0 < 0.03F) || this.dolphin.getXRot() == 0.0F || !(Math.abs(this.dolphin.getXRot()) < 10.0F) || !this.dolphin.isInWater())
            && !this.dolphin.isOnGround();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        Direction var0 = this.dolphin.getMotionDirection();
        this.dolphin.setDeltaMovement(this.dolphin.getDeltaMovement().add((double)var0.getStepX() * 0.6, 0.7, (double)var0.getStepZ() * 0.6));
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.dolphin.setXRot(0.0F);
    }

    @Override
    public void tick() {
        boolean var0 = this.breached;
        if (!var0) {
            FluidState var1 = this.dolphin.level.getFluidState(this.dolphin.blockPosition());
            this.breached = var1.is(FluidTags.WATER);
        }

        if (this.breached && !var0) {
            this.dolphin.playSound(SoundEvents.DOLPHIN_JUMP, 1.0F, 1.0F);
        }

        Vec3 var2 = this.dolphin.getDeltaMovement();
        if (var2.y * var2.y < 0.03F && this.dolphin.getXRot() != 0.0F) {
            this.dolphin.setXRot(Mth.rotlerp(this.dolphin.getXRot(), 0.0F, 0.2F));
        } else if (var2.length() > 1.0E-5F) {
            double var3 = var2.horizontalDistance();
            double var4 = Math.atan2(-var2.y, var3) * 180.0F / (float)Math.PI;
            this.dolphin.setXRot((float)var4);
        }

    }
}
