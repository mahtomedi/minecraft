package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ItemSteerable {
    boolean boost();

    void travelWithInput(Vec3 var1);

    float getSteeringSpeed();

    default boolean travel(Mob param0, ItemBasedSteering param1, Vec3 param2) {
        if (!param0.isAlive()) {
            return false;
        } else {
            Entity var0 = param0.getFirstPassenger();
            if (param0.isVehicle() && param0.canBeControlledByRider() && var0 instanceof Player) {
                param0.setYRot(var0.getYRot());
                param0.yRotO = param0.getYRot();
                param0.setXRot(var0.getXRot() * 0.5F);
                param0.setRot(param0.getYRot(), param0.getXRot());
                param0.yBodyRot = param0.getYRot();
                param0.yHeadRot = param0.getYRot();
                param0.maxUpStep = 1.0F;
                param0.flyingSpeed = param0.getSpeed() * 0.1F;
                if (param1.boosting && param1.boostTime++ > param1.boostTimeTotal) {
                    param1.boosting = false;
                }

                if (param0.isControlledByLocalInstance()) {
                    float var1 = this.getSteeringSpeed();
                    if (param1.boosting) {
                        var1 += var1 * 1.15F * Mth.sin((float)param1.boostTime / (float)param1.boostTimeTotal * (float) Math.PI);
                    }

                    param0.setSpeed(var1);
                    this.travelWithInput(new Vec3(0.0, 0.0, 1.0));
                    param0.lerpSteps = 0;
                } else {
                    param0.calculateEntityAnimation(param0, false);
                    param0.setDeltaMovement(Vec3.ZERO);
                }

                return true;
            } else {
                param0.maxUpStep = 0.5F;
                param0.flyingSpeed = 0.02F;
                this.travelWithInput(param2);
                return false;
            }
        }
    }
}
