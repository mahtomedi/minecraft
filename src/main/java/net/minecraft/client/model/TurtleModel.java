package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TurtleModel<T extends Turtle> extends QuadrupedModel<T> {
    private final ModelPart eggBelly;

    public TurtleModel(float param0) {
        super(12, param0);
        this.texWidth = 128;
        this.texHeight = 64;
        this.head = new ModelPart(this, 3, 0);
        this.head.addBox(-3.0F, -1.0F, -3.0F, 6, 5, 6, 0.0F);
        this.head.setPos(0.0F, 19.0F, -10.0F);
        this.body = new ModelPart(this);
        this.body.texOffs(7, 37).addBox(-9.5F, 3.0F, -10.0F, 19, 20, 6, 0.0F);
        this.body.texOffs(31, 1).addBox(-5.5F, 3.0F, -13.0F, 11, 18, 3, 0.0F);
        this.body.setPos(0.0F, 11.0F, -10.0F);
        this.eggBelly = new ModelPart(this);
        this.eggBelly.texOffs(70, 33).addBox(-4.5F, 3.0F, -14.0F, 9, 18, 1, 0.0F);
        this.eggBelly.setPos(0.0F, 11.0F, -10.0F);
        int var0 = 1;
        this.leg0 = new ModelPart(this, 1, 23);
        this.leg0.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 10, 0.0F);
        this.leg0.setPos(-3.5F, 22.0F, 11.0F);
        this.leg1 = new ModelPart(this, 1, 12);
        this.leg1.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 10, 0.0F);
        this.leg1.setPos(3.5F, 22.0F, 11.0F);
        this.leg2 = new ModelPart(this, 27, 30);
        this.leg2.addBox(-13.0F, 0.0F, -2.0F, 13, 1, 5, 0.0F);
        this.leg2.setPos(-5.0F, 21.0F, -4.0F);
        this.leg3 = new ModelPart(this, 27, 24);
        this.leg3.addBox(0.0F, 0.0F, -2.0F, 13, 1, 5, 0.0F);
        this.leg3.setPos(5.0F, 21.0F, -4.0F);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (this.young) {
            float var0 = 6.0F;
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.16666667F, 0.16666667F, 0.16666667F);
            GlStateManager.translatef(0.0F, 120.0F * param6, 0.0F);
            this.head.render(param6);
            this.body.render(param6);
            this.leg0.render(param6);
            this.leg1.render(param6);
            this.leg2.render(param6);
            this.leg3.render(param6);
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            if (param0.hasEgg()) {
                GlStateManager.translatef(0.0F, -0.08F, 0.0F);
            }

            this.head.render(param6);
            this.body.render(param6);
            GlStateManager.pushMatrix();
            this.leg0.render(param6);
            this.leg1.render(param6);
            GlStateManager.popMatrix();
            this.leg2.render(param6);
            this.leg3.render(param6);
            if (param0.hasEgg()) {
                this.eggBelly.render(param6);
            }

            GlStateManager.popMatrix();
        }

    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.leg0.xRot = Mth.cos(param1 * 0.6662F * 0.6F) * 0.5F * param2;
        this.leg1.xRot = Mth.cos(param1 * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * param2;
        this.leg2.zRot = Mth.cos(param1 * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * param2;
        this.leg3.zRot = Mth.cos(param1 * 0.6662F * 0.6F) * 0.5F * param2;
        this.leg2.xRot = 0.0F;
        this.leg3.xRot = 0.0F;
        this.leg2.yRot = 0.0F;
        this.leg3.yRot = 0.0F;
        this.leg0.yRot = 0.0F;
        this.leg1.yRot = 0.0F;
        this.eggBelly.xRot = (float) (Math.PI / 2);
        if (!param0.isInWater() && param0.onGround) {
            float var0 = param0.isLayingEgg() ? 4.0F : 1.0F;
            float var1 = param0.isLayingEgg() ? 2.0F : 1.0F;
            float var2 = 5.0F;
            this.leg2.yRot = Mth.cos(var0 * param1 * 5.0F + (float) Math.PI) * 8.0F * param2 * var1;
            this.leg2.zRot = 0.0F;
            this.leg3.yRot = Mth.cos(var0 * param1 * 5.0F) * 8.0F * param2 * var1;
            this.leg3.zRot = 0.0F;
            this.leg0.yRot = Mth.cos(param1 * 5.0F + (float) Math.PI) * 3.0F * param2;
            this.leg0.xRot = 0.0F;
            this.leg1.yRot = Mth.cos(param1 * 5.0F) * 3.0F * param2;
            this.leg1.xRot = 0.0F;
        }

    }
}
