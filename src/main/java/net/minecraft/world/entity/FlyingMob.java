package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class FlyingMob extends Mob {
    protected FlyingMob(EntityType<? extends FlyingMob> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public void causeFallDamage(float param0, float param1) {
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
    }

    @Override
    public void travel(Vec3 param0) {
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
            if (this.onGround) {
                var0 = this.level.getBlockState(new BlockPos(this.x, this.getBoundingBox().minY - 1.0, this.z)).getBlock().getFriction() * 0.91F;
            }

            float var1 = 0.16277137F / (var0 * var0 * var0);
            var0 = 0.91F;
            if (this.onGround) {
                var0 = this.level.getBlockState(new BlockPos(this.x, this.getBoundingBox().minY - 1.0, this.z)).getBlock().getFriction() * 0.91F;
            }

            this.moveRelative(this.onGround ? 0.1F * var1 : 0.02F, param0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale((double)var0));
        }

        this.animationSpeedOld = this.animationSpeed;
        double var2 = this.x - this.xo;
        double var3 = this.z - this.zo;
        float var4 = Mth.sqrt(var2 * var2 + var3 * var3) * 4.0F;
        if (var4 > 1.0F) {
            var4 = 1.0F;
        }

        this.animationSpeed += (var4 - this.animationSpeed) * 0.4F;
        this.animationPosition += this.animationSpeed;
    }

    @Override
    public boolean onLadder() {
        return false;
    }
}
