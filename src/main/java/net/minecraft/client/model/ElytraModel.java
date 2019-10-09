package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElytraModel<T extends LivingEntity> extends AgeableListModel<T> {
    private final ModelPart rightWing;
    private final ModelPart leftWing = new ModelPart(this, 22, 0);

    public ElytraModel() {
        super(RenderType::entityCutoutNoCull, false, 5.0F, 2.0F, 2.0F, 2.0F, 24.0F);
        this.leftWing.addBox(-10.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, 1.0F);
        this.rightWing = new ModelPart(this, 22, 0);
        this.rightWing.mirror = true;
        this.rightWing.addBox(0.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, 1.0F);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.leftWing, this.rightWing);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        float var0 = (float) (Math.PI / 12);
        float var1 = (float) (-Math.PI / 12);
        float var2 = 0.0F;
        float var3 = 0.0F;
        if (param0.isFallFlying()) {
            float var4 = 1.0F;
            Vec3 var5 = param0.getDeltaMovement();
            if (var5.y < 0.0) {
                Vec3 var6 = var5.normalize();
                var4 = 1.0F - (float)Math.pow(-var6.y, 1.5);
            }

            var0 = var4 * (float) (Math.PI / 9) + (1.0F - var4) * var0;
            var1 = var4 * (float) (-Math.PI / 2) + (1.0F - var4) * var1;
        } else if (param0.isCrouching()) {
            var0 = (float) (Math.PI * 2.0 / 9.0);
            var1 = (float) (-Math.PI / 4);
            var2 = 3.0F;
            var3 = 0.08726646F;
        }

        this.leftWing.x = 5.0F;
        this.leftWing.y = var2;
        if (param0 instanceof AbstractClientPlayer) {
            AbstractClientPlayer var7 = (AbstractClientPlayer)param0;
            var7.elytraRotX = (float)((double)var7.elytraRotX + (double)(var0 - var7.elytraRotX) * 0.1);
            var7.elytraRotY = (float)((double)var7.elytraRotY + (double)(var3 - var7.elytraRotY) * 0.1);
            var7.elytraRotZ = (float)((double)var7.elytraRotZ + (double)(var1 - var7.elytraRotZ) * 0.1);
            this.leftWing.xRot = var7.elytraRotX;
            this.leftWing.yRot = var7.elytraRotY;
            this.leftWing.zRot = var7.elytraRotZ;
        } else {
            this.leftWing.xRot = var0;
            this.leftWing.zRot = var1;
            this.leftWing.yRot = var3;
        }

        this.rightWing.x = -this.leftWing.x;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.rightWing.y = this.leftWing.y;
        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.zRot = -this.leftWing.zRot;
    }
}
