package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RemotePlayer extends AbstractClientPlayer {
    public RemotePlayer(ClientLevel param0, GameProfile param1) {
        super(param0, param1);
        this.maxUpStep = 1.0F;
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
        this.animationSpeedOld = this.animationSpeed;
        double var0 = this.getX() - this.xo;
        double var1 = this.getZ() - this.zo;
        float var2 = Mth.sqrt(var0 * var0 + var1 * var1) * 4.0F;
        if (var2 > 1.0F) {
            var2 = 1.0F;
        }

        this.animationSpeed += (var2 - this.animationSpeed) * 0.4F;
        this.animationPosition += this.animationSpeed;
    }

    @Override
    public void aiStep() {
        if (this.lerpSteps > 0) {
            double var0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
            double var1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
            double var2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
            this.yRot = (float)((double)this.yRot + Mth.wrapDegrees(this.lerpYRot - (double)this.yRot) / (double)this.lerpSteps);
            this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
            --this.lerpSteps;
            this.setPos(var0, var1, var2);
            this.setRot(this.yRot, this.xRot);
        }

        if (this.lerpHeadSteps > 0) {
            this.yHeadRot = (float)((double)this.yHeadRot + Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
            --this.lerpHeadSteps;
        }

        this.oBob = this.bob;
        this.updateSwingTime();
        float var4;
        if (this.onGround && !(this.getHealth() <= 0.0F)) {
            var4 = Math.min(0.1F, Mth.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement())));
        } else {
            var4 = 0.0F;
        }

        if (!this.onGround && !(this.getHealth() <= 0.0F)) {
            float var6 = (float)Math.atan(-this.getDeltaMovement().y * 0.2F) * 15.0F;
        } else {
            float var5 = 0.0F;
        }

        this.bob += (var4 - this.bob) * 0.4F;
        this.level.getProfiler().push("push");
        this.pushEntities();
        this.level.getProfiler().pop();
    }

    @Override
    protected void updatePlayerPose() {
    }

    @Override
    public void sendMessage(Component param0) {
        Minecraft.getInstance().gui.getChat().addMessage(param0);
    }
}
