package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class BodyRotationControl {
    private final Mob mob;
    private int headStableTime;
    private float lastStableYHeadRot;

    public BodyRotationControl(Mob param0) {
        this.mob = param0;
    }

    public void clientTick() {
        if (this.isMoving()) {
            this.mob.yBodyRot = this.mob.yRot;
            this.rotateHeadIfNecessary();
            this.lastStableYHeadRot = this.mob.yHeadRot;
            this.headStableTime = 0;
        } else {
            if (this.notCarryingMobPassengers()) {
                if (Math.abs(this.mob.yHeadRot - this.lastStableYHeadRot) > 15.0F) {
                    this.headStableTime = 0;
                    this.lastStableYHeadRot = this.mob.yHeadRot;
                    this.rotateBodyIfNecessary();
                } else {
                    ++this.headStableTime;
                    if (this.headStableTime > 10) {
                        this.rotateHeadTowardsFront();
                    }
                }
            }

        }
    }

    private void rotateBodyIfNecessary() {
        this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, (float)this.mob.getMaxHeadYRot());
    }

    private void rotateHeadIfNecessary() {
        this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float)this.mob.getMaxHeadYRot());
    }

    private void rotateHeadTowardsFront() {
        int var0 = this.headStableTime - 10;
        float var1 = Mth.clamp((float)var0 / 10.0F, 0.0F, 1.0F);
        float var2 = (float)this.mob.getMaxHeadYRot() * (1.0F - var1);
        this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, var2);
    }

    private boolean notCarryingMobPassengers() {
        return this.mob.getPassengers().isEmpty() || !(this.mob.getPassengers().get(0) instanceof Mob);
    }

    private boolean isMoving() {
        double var0 = this.mob.x - this.mob.xo;
        double var1 = this.mob.z - this.mob.zo;
        return var0 * var0 + var1 * var1 > 2.5000003E-7F;
    }
}
