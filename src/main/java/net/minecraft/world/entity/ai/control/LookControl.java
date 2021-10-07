package net.minecraft.world.entity.ai.control;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LookControl implements Control {
    protected final Mob mob;
    protected float yMaxRotSpeed;
    protected float xMaxRotAngle;
    protected int lookAtCooldown;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public LookControl(Mob param0) {
        this.mob = param0;
    }

    public void setLookAt(Vec3 param0) {
        this.setLookAt(param0.x, param0.y, param0.z);
    }

    public void setLookAt(Entity param0) {
        this.setLookAt(param0.getX(), getWantedY(param0), param0.getZ());
    }

    public void setLookAt(Entity param0, float param1, float param2) {
        this.setLookAt(param0.getX(), getWantedY(param0), param0.getZ(), param1, param2);
    }

    public void setLookAt(double param0, double param1, double param2) {
        this.setLookAt(param0, param1, param2, (float)this.mob.getHeadRotSpeed(), (float)this.mob.getMaxHeadXRot());
    }

    public void setLookAt(double param0, double param1, double param2, float param3, float param4) {
        this.wantedX = param0;
        this.wantedY = param1;
        this.wantedZ = param2;
        this.yMaxRotSpeed = param3;
        this.xMaxRotAngle = param4;
        this.lookAtCooldown = 2;
    }

    public void tick() {
        if (this.resetXRotOnTick()) {
            this.mob.setXRot(0.0F);
        }

        if (this.lookAtCooldown > 0) {
            --this.lookAtCooldown;
            this.getYRotD().ifPresent(param0 -> this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, param0, this.yMaxRotSpeed));
            this.getXRotD().ifPresent(param0 -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), param0, this.xMaxRotAngle)));
        } else {
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
        }

        this.clampHeadRotationToBody();
    }

    protected void clampHeadRotationToBody() {
        if (!this.mob.getNavigation().isDone()) {
            this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float)this.mob.getMaxHeadYRot());
        }

    }

    protected boolean resetXRotOnTick() {
        return true;
    }

    public boolean isLookingAtTarget() {
        return this.lookAtCooldown > 0;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    protected Optional<Float> getXRotD() {
        double var0 = this.wantedX - this.mob.getX();
        double var1 = this.wantedY - this.mob.getEyeY();
        double var2 = this.wantedZ - this.mob.getZ();
        double var3 = Math.sqrt(var0 * var0 + var2 * var2);
        return !(Math.abs(var1) > 1.0E-5F) && !(Math.abs(var3) > 1.0E-5F)
            ? Optional.empty()
            : Optional.of((float)(-(Mth.atan2(var1, var3) * 180.0F / (float)Math.PI)));
    }

    protected Optional<Float> getYRotD() {
        double var0 = this.wantedX - this.mob.getX();
        double var1 = this.wantedZ - this.mob.getZ();
        return !(Math.abs(var1) > 1.0E-5F) && !(Math.abs(var0) > 1.0E-5F)
            ? Optional.empty()
            : Optional.of((float)(Mth.atan2(var1, var0) * 180.0F / (float)Math.PI) - 90.0F);
    }

    protected float rotateTowards(float param0, float param1, float param2) {
        float var0 = Mth.degreesDifference(param0, param1);
        float var1 = Mth.clamp(var0, -param2, param2);
        return param0 + var1;
    }

    private static double getWantedY(Entity param0) {
        return param0 instanceof LivingEntity ? param0.getEyeY() : (param0.getBoundingBox().minY + param0.getBoundingBox().maxY) / 2.0;
    }
}
