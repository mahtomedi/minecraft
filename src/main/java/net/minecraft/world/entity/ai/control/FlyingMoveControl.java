package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FlyingMoveControl extends MoveControl {
    private final int maxTurn;
    private final boolean hoversInPlace;

    public FlyingMoveControl(Mob param0, int param1, boolean param2) {
        super(param0);
        this.maxTurn = param1;
        this.hoversInPlace = param2;
    }

    @Override
    public void tick() {
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            this.mob.setNoGravity(true);
            double var0 = this.wantedX - this.mob.getX();
            double var1 = this.wantedY - this.mob.getY();
            double var2 = this.wantedZ - this.mob.getZ();
            double var3 = var0 * var0 + var1 * var1 + var2 * var2;
            if (var3 < 2.5000003E-7F) {
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
                return;
            }

            float var4 = (float)(Mth.atan2(var2, var0) * 180.0F / (float)Math.PI) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), var4, 90.0F));
            float var5;
            if (this.mob.isOnGround()) {
                var5 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            } else {
                var5 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
            }

            this.mob.setSpeed(var5);
            double var7 = Math.sqrt(var0 * var0 + var2 * var2);
            float var8 = (float)(-(Mth.atan2(var1, var7) * 180.0F / (float)Math.PI));
            this.mob.setXRot(this.rotlerp(this.mob.getXRot(), var8, (float)this.maxTurn));
            this.mob.setYya(var1 > 0.0 ? var5 : -var5);
        } else {
            if (!this.hoversInPlace) {
                this.mob.setNoGravity(false);
            }

            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
        }

    }
}
