package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;

public class FlyingMoveControl extends MoveControl {
    public FlyingMoveControl(Mob param0) {
        super(param0);
    }

    @Override
    public void tick() {
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            this.mob.setNoGravity(true);
            double var0 = this.wantedX - this.mob.x;
            double var1 = this.wantedY - this.mob.y;
            double var2 = this.wantedZ - this.mob.z;
            double var3 = var0 * var0 + var1 * var1 + var2 * var2;
            if (var3 < 2.5000003E-7F) {
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
                return;
            }

            float var4 = (float)(Mth.atan2(var2, var0) * 180.0F / (float)Math.PI) - 90.0F;
            this.mob.yRot = this.rotlerp(this.mob.yRot, var4, 10.0F);
            float var5;
            if (this.mob.onGround) {
                var5 = (float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
            } else {
                var5 = (float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.FLYING_SPEED).getValue());
            }

            this.mob.setSpeed(var5);
            double var7 = (double)Mth.sqrt(var0 * var0 + var2 * var2);
            float var8 = (float)(-(Mth.atan2(var1, var7) * 180.0F / (float)Math.PI));
            this.mob.xRot = this.rotlerp(this.mob.xRot, var8, 10.0F);
            this.mob.setYya(var1 > 0.0 ? var5 : -var5);
        } else {
            this.mob.setNoGravity(false);
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
        }

    }
}
