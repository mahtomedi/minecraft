package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaModel<T extends Panda> extends QuadrupedModel<T> {
    private float sitAmount;
    private float lieOnBackAmount;
    private float rollAmount;

    public PandaModel(int param0, float param1) {
        super(param0, param1);
        this.texWidth = 64;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 6);
        this.head.addBox(-6.5F, -5.0F, -4.0F, 13, 10, 9);
        this.head.setPos(0.0F, 11.5F, -17.0F);
        this.head.texOffs(45, 16).addBox(-3.5F, 0.0F, -6.0F, 7, 5, 2);
        this.head.texOffs(52, 25).addBox(-8.5F, -8.0F, -1.0F, 5, 4, 1);
        this.head.texOffs(52, 25).addBox(3.5F, -8.0F, -1.0F, 5, 4, 1);
        this.body = new ModelPart(this, 0, 25);
        this.body.addBox(-9.5F, -13.0F, -6.5F, 19, 26, 13);
        this.body.setPos(0.0F, 10.0F, 0.0F);
        int var0 = 9;
        int var1 = 6;
        this.leg0 = new ModelPart(this, 40, 0);
        this.leg0.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
        this.leg0.setPos(-5.5F, 15.0F, 9.0F);
        this.leg1 = new ModelPart(this, 40, 0);
        this.leg1.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
        this.leg1.setPos(5.5F, 15.0F, 9.0F);
        this.leg2 = new ModelPart(this, 40, 0);
        this.leg2.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
        this.leg2.setPos(-5.5F, 15.0F, -9.0F);
        this.leg3 = new ModelPart(this, 40, 0);
        this.leg3.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
        this.leg3.setPos(5.5F, 15.0F, -9.0F);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        this.sitAmount = param0.getSitAmount(param3);
        this.lieOnBackAmount = param0.getLieOnBackAmount(param3);
        this.rollAmount = param0.isBaby() ? 0.0F : param0.getRollAmount(param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        boolean var0 = param0.getUnhappyCounter() > 0;
        boolean var1 = param0.isSneezing();
        int var2 = param0.getSneezeCounter();
        boolean var3 = param0.isEating();
        boolean var4 = param0.isScared();
        if (var0) {
            this.head.yRot = 0.35F * Mth.sin(0.6F * param3);
            this.head.zRot = 0.35F * Mth.sin(0.6F * param3);
            this.leg2.xRot = -0.75F * Mth.sin(0.3F * param3);
            this.leg3.xRot = 0.75F * Mth.sin(0.3F * param3);
        } else {
            this.head.zRot = 0.0F;
        }

        if (var1) {
            if (var2 < 15) {
                this.head.xRot = ((float) (-Math.PI / 4)) * (float)var2 / 14.0F;
            } else if (var2 < 20) {
                float var5 = (float)((var2 - 15) / 5);
                this.head.xRot = (float) (-Math.PI / 4) + ((float) (Math.PI / 4)) * var5;
            }
        }

        if (this.sitAmount > 0.0F) {
            this.body.xRot = this.rotlerpRad(this.body.xRot, 1.7407963F, this.sitAmount);
            this.head.xRot = this.rotlerpRad(this.head.xRot, (float) (Math.PI / 2), this.sitAmount);
            this.leg2.zRot = -0.27079642F;
            this.leg3.zRot = 0.27079642F;
            this.leg0.zRot = 0.5707964F;
            this.leg1.zRot = -0.5707964F;
            if (var3) {
                this.head.xRot = (float) (Math.PI / 2) + 0.2F * Mth.sin(param3 * 0.6F);
                this.leg2.xRot = -0.4F - 0.2F * Mth.sin(param3 * 0.6F);
                this.leg3.xRot = -0.4F - 0.2F * Mth.sin(param3 * 0.6F);
            }

            if (var4) {
                this.head.xRot = 2.1707964F;
                this.leg2.xRot = -0.9F;
                this.leg3.xRot = -0.9F;
            }
        } else {
            this.leg0.zRot = 0.0F;
            this.leg1.zRot = 0.0F;
            this.leg2.zRot = 0.0F;
            this.leg3.zRot = 0.0F;
        }

        if (this.lieOnBackAmount > 0.0F) {
            this.leg0.xRot = -0.6F * Mth.sin(param3 * 0.15F);
            this.leg1.xRot = 0.6F * Mth.sin(param3 * 0.15F);
            this.leg2.xRot = 0.3F * Mth.sin(param3 * 0.25F);
            this.leg3.xRot = -0.3F * Mth.sin(param3 * 0.25F);
            this.head.xRot = this.rotlerpRad(this.head.xRot, (float) (Math.PI / 2), this.lieOnBackAmount);
        }

        if (this.rollAmount > 0.0F) {
            this.head.xRot = this.rotlerpRad(this.head.xRot, 2.0561945F, this.rollAmount);
            this.leg0.xRot = -0.5F * Mth.sin(param3 * 0.5F);
            this.leg1.xRot = 0.5F * Mth.sin(param3 * 0.5F);
            this.leg2.xRot = 0.5F * Mth.sin(param3 * 0.5F);
            this.leg3.xRot = -0.5F * Mth.sin(param3 * 0.5F);
        }

    }

    protected float rotlerpRad(float param0, float param1, float param2) {
        float var0 = param1 - param0;

        while(var0 < (float) -Math.PI) {
            var0 += (float) (Math.PI * 2);
        }

        while(var0 >= (float) Math.PI) {
            var0 -= (float) (Math.PI * 2);
        }

        return param0 + param2 * var0;
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (this.young) {
            float var0 = 3.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, this.yHeadOffs * param6, this.zHeadOffs * param6);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float var1 = 0.6F;
            GlStateManager.scalef(0.5555555F, 0.5555555F, 0.5555555F);
            GlStateManager.translatef(0.0F, 23.0F * param6, 0.3F);
            this.head.render(param6);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.translatef(0.0F, 49.0F * param6, 0.0F);
            this.body.render(param6);
            this.leg0.render(param6);
            this.leg1.render(param6);
            this.leg2.render(param6);
            this.leg3.render(param6);
            GlStateManager.popMatrix();
        } else {
            this.head.render(param6);
            this.body.render(param6);
            this.leg0.render(param6);
            this.leg1.render(param6);
            this.leg2.render(param6);
            this.leg3.render(param6);
        }

    }
}
