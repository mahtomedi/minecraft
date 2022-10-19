package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SmoothSwimmingMoveControl extends MoveControl {
    private static final float FULL_SPEED_TURN_THRESHOLD = 10.0F;
    private static final float STOP_TURN_THRESHOLD = 60.0F;
    private final int maxTurnX;
    private final int maxTurnY;
    private final float inWaterSpeedModifier;
    private final float outsideWaterSpeedModifier;
    private final boolean applyGravity;

    public SmoothSwimmingMoveControl(Mob param0, int param1, int param2, float param3, float param4, boolean param5) {
        super(param0);
        this.maxTurnX = param1;
        this.maxTurnY = param2;
        this.inWaterSpeedModifier = param3;
        this.outsideWaterSpeedModifier = param4;
        this.applyGravity = param5;
    }

    @Override
    public void tick() {
        if (this.applyGravity && this.mob.isInWater()) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, 0.005, 0.0));
        }

        if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
            double var0 = this.wantedX - this.mob.getX();
            double var1 = this.wantedY - this.mob.getY();
            double var2 = this.wantedZ - this.mob.getZ();
            double var3 = var0 * var0 + var1 * var1 + var2 * var2;
            if (var3 < 2.5000003E-7F) {
                this.mob.setZza(0.0F);
            } else {
                float var4 = (float)(Mth.atan2(var2, var0) * 180.0F / (float)Math.PI) - 90.0F;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), var4, (float)this.maxTurnY));
                this.mob.yBodyRot = this.mob.getYRot();
                this.mob.yHeadRot = this.mob.getYRot();
                float var5 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                if (this.mob.isInWater()) {
                    this.mob.setSpeed(var5 * this.inWaterSpeedModifier);
                    double var6 = Math.sqrt(var0 * var0 + var2 * var2);
                    if (Math.abs(var1) > 1.0E-5F || Math.abs(var6) > 1.0E-5F) {
                        float var7 = -((float)(Mth.atan2(var1, var6) * 180.0F / (float)Math.PI));
                        var7 = Mth.clamp(Mth.wrapDegrees(var7), (float)(-this.maxTurnX), (float)this.maxTurnX);
                        this.mob.setXRot(this.rotlerp(this.mob.getXRot(), var7, 5.0F));
                    }

                    float var8 = Mth.cos(this.mob.getXRot() * (float) (Math.PI / 180.0));
                    float var9 = Mth.sin(this.mob.getXRot() * (float) (Math.PI / 180.0));
                    this.mob.zza = var8 * var5;
                    this.mob.yya = -var9 * var5;
                } else {
                    float var10 = Math.abs(Mth.wrapDegrees(this.mob.getYRot() - var4));
                    float var11 = getTurningSpeedFactor(var10);
                    this.mob.setSpeed(var5 * this.outsideWaterSpeedModifier * var11);
                }

            }
        } else {
            this.mob.setSpeed(0.0F);
            this.mob.setXxa(0.0F);
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
        }
    }

    private static float getTurningSpeedFactor(float param0) {
        return 1.0F - Mth.clamp((param0 - 10.0F) / 50.0F, 0.0F, 1.0F);
    }
}
