package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RemotePlayer extends AbstractClientPlayer {
    private Vec3 lerpDeltaMovement = Vec3.ZERO;
    private int lerpDeltaMovementSteps;

    public RemotePlayer(ClientLevel param0, GameProfile param1) {
        super(param0, param1);
        this.setMaxUpStep(1.0F);
        this.noPhysics = true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(var0)) {
            var0 = 1.0;
        }

        var0 *= 64.0 * getViewScale();
        return param0 < var0 * var0;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.calculateEntityAnimation(false);
    }

    @Override
    public void aiStep() {
        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            --this.lerpSteps;
        }

        if (this.lerpHeadSteps > 0) {
            this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
            --this.lerpHeadSteps;
        }

        if (this.lerpDeltaMovementSteps > 0) {
            this.addDeltaMovement(
                new Vec3(
                    (this.lerpDeltaMovement.x - this.getDeltaMovement().x) / (double)this.lerpDeltaMovementSteps,
                    (this.lerpDeltaMovement.y - this.getDeltaMovement().y) / (double)this.lerpDeltaMovementSteps,
                    (this.lerpDeltaMovement.z - this.getDeltaMovement().z) / (double)this.lerpDeltaMovementSteps
                )
            );
            --this.lerpDeltaMovementSteps;
        }

        this.oBob = this.bob;
        this.updateSwingTime();
        float var1;
        if (this.onGround() && !this.isDeadOrDying()) {
            var1 = (float)Math.min(0.1, this.getDeltaMovement().horizontalDistance());
        } else {
            var1 = 0.0F;
        }

        this.bob += (var1 - this.bob) * 0.4F;
        this.level().getProfiler().push("push");
        this.pushEntities();
        this.level().getProfiler().pop();
    }

    @Override
    public void lerpMotion(double param0, double param1, double param2) {
        this.lerpDeltaMovement = new Vec3(param0, param1, param2);
        this.lerpDeltaMovementSteps = this.getType().updateInterval() + 1;
    }

    @Override
    protected void updatePlayerPose() {
    }

    @Override
    public void sendSystemMessage(Component param0) {
        Minecraft var0 = Minecraft.getInstance();
        var0.gui.getChat().addMessage(param0);
    }
}
