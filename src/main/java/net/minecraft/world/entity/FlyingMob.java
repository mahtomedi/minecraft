package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class FlyingMob extends Mob {
    protected FlyingMob(EntityType<? extends FlyingMob> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, param0);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, param0);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                float var0 = 0.91F;
                if (this.onGround()) {
                    var0 = this.level().getBlockState(BlockPos.containing(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.91F;
                }

                float var1 = 0.16277137F / (var0 * var0 * var0);
                var0 = 0.91F;
                if (this.onGround()) {
                    var0 = this.level().getBlockState(BlockPos.containing(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.91F;
                }

                this.moveRelative(this.onGround() ? 0.1F * var1 : 0.02F, param0);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale((double)var0));
            }
        }

        this.calculateEntityAnimation(false);
    }

    @Override
    public boolean onClimbable() {
        return false;
    }
}
