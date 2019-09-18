package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxModel<T extends Fox> extends EntityModel<T> {
    public final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart nose;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart tail;
    private float legMotionPos;

    public FoxModel() {
        this.texWidth = 48;
        this.texHeight = 32;
        this.head = new ModelPart(this, 1, 5);
        this.head.addBox(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F);
        this.head.setPos(-1.0F, 16.5F, -3.0F);
        this.earL = new ModelPart(this, 8, 1);
        this.earL.addBox(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F);
        this.earR = new ModelPart(this, 15, 1);
        this.earR.addBox(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F);
        this.nose = new ModelPart(this, 6, 18);
        this.nose.addBox(-1.0F, 2.01F, -8.0F, 4.0F, 2.0F, 3.0F);
        this.head.addChild(this.earL);
        this.head.addChild(this.earR);
        this.head.addChild(this.nose);
        this.body = new ModelPart(this, 24, 15);
        this.body.addBox(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F);
        this.body.setPos(0.0F, 16.0F, -6.0F);
        float var0 = 0.001F;
        this.leg0 = new ModelPart(this, 13, 24);
        this.leg0.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
        this.leg0.setPos(-5.0F, 17.5F, 7.0F);
        this.leg1 = new ModelPart(this, 4, 24);
        this.leg1.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
        this.leg1.setPos(-1.0F, 17.5F, 7.0F);
        this.leg2 = new ModelPart(this, 13, 24);
        this.leg2.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
        this.leg2.setPos(-5.0F, 17.5F, 0.0F);
        this.leg3 = new ModelPart(this, 4, 24);
        this.leg3.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
        this.leg3.setPos(-1.0F, 17.5F, 0.0F);
        this.tail = new ModelPart(this, 30, 0);
        this.tail.addBox(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F);
        this.tail.setPos(-4.0F, 15.0F, -1.0F);
        this.body.addChild(this.tail);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        this.body.xRot = (float) (Math.PI / 2);
        this.tail.xRot = -0.05235988F;
        this.leg0.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leg1.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg2.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg3.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.head.setPos(-1.0F, 16.5F, -3.0F);
        this.head.yRot = 0.0F;
        this.head.zRot = param0.getHeadRollAngle(param3);
        this.leg0.visible = true;
        this.leg1.visible = true;
        this.leg2.visible = true;
        this.leg3.visible = true;
        this.body.setPos(0.0F, 16.0F, -6.0F);
        this.body.zRot = 0.0F;
        this.leg0.setPos(-5.0F, 17.5F, 7.0F);
        this.leg1.setPos(-1.0F, 17.5F, 7.0F);
        if (param0.isCrouching()) {
            this.body.xRot = 1.6755161F;
            float var0 = param0.getCrouchAmount(param3);
            this.body.setPos(0.0F, 16.0F + param0.getCrouchAmount(param3), -6.0F);
            this.head.setPos(-1.0F, 16.5F + var0, -3.0F);
            this.head.yRot = 0.0F;
        } else if (param0.isSleeping()) {
            this.body.zRot = (float) (-Math.PI / 2);
            this.body.setPos(0.0F, 21.0F, -6.0F);
            this.tail.xRot = (float) (-Math.PI * 5.0 / 6.0);
            if (this.young) {
                this.tail.xRot = -2.1816616F;
                this.body.setPos(0.0F, 21.0F, -2.0F);
            }

            this.head.setPos(1.0F, 19.49F, -3.0F);
            this.head.xRot = 0.0F;
            this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
            this.head.zRot = 0.0F;
            this.leg0.visible = false;
            this.leg1.visible = false;
            this.leg2.visible = false;
            this.leg3.visible = false;
        } else if (param0.isSitting()) {
            this.body.xRot = (float) (Math.PI / 6);
            this.body.setPos(0.0F, 9.0F, -3.0F);
            this.tail.xRot = (float) (Math.PI / 4);
            this.tail.setPos(-4.0F, 15.0F, -2.0F);
            this.head.setPos(-1.0F, 10.0F, -0.25F);
            this.head.xRot = 0.0F;
            this.head.yRot = 0.0F;
            if (this.young) {
                this.head.setPos(-1.0F, 13.0F, -3.75F);
            }

            this.leg0.xRot = (float) (-Math.PI * 5.0 / 12.0);
            this.leg0.setPos(-5.0F, 21.5F, 6.75F);
            this.leg1.xRot = (float) (-Math.PI * 5.0 / 12.0);
            this.leg1.setPos(-1.0F, 21.5F, 6.75F);
            this.leg2.xRot = (float) (-Math.PI / 12);
            this.leg3.xRot = (float) (-Math.PI / 12);
        }

    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.render(param0, param1, param2, param3, param4, param5, param6);
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (this.young) {
            RenderSystem.pushMatrix();
            float var0 = 0.75F;
            RenderSystem.scalef(0.75F, 0.75F, 0.75F);
            RenderSystem.translatef(0.0F, 8.0F * param6, 3.35F * param6);
            this.head.render(param6);
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            float var1 = 0.5F;
            RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            RenderSystem.translatef(0.0F, 24.0F * param6, 0.0F);
            this.body.render(param6);
            this.leg0.render(param6);
            this.leg1.render(param6);
            this.leg2.render(param6);
            this.leg3.render(param6);
            RenderSystem.popMatrix();
        } else {
            RenderSystem.pushMatrix();
            this.head.render(param6);
            this.body.render(param6);
            this.leg0.render(param6);
            this.leg1.render(param6);
            this.leg2.render(param6);
            this.leg3.render(param6);
            RenderSystem.popMatrix();
        }

    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (!param0.isSleeping() && !param0.isFaceplanted() && !param0.isCrouching()) {
            this.head.xRot = param5 * (float) (Math.PI / 180.0);
            this.head.yRot = param4 * (float) (Math.PI / 180.0);
        }

        if (param0.isSleeping()) {
            this.head.xRot = 0.0F;
            this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
            this.head.zRot = Mth.cos(param3 * 0.027F) / 22.0F;
        }

        if (param0.isCrouching()) {
            float var0 = Mth.cos(param3) * 0.01F;
            this.body.yRot = var0;
            this.leg0.zRot = var0;
            this.leg1.zRot = var0;
            this.leg2.zRot = var0 / 2.0F;
            this.leg3.zRot = var0 / 2.0F;
        }

        if (param0.isFaceplanted()) {
            float var1 = 0.1F;
            this.legMotionPos += 0.67F;
            this.leg0.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
            this.leg1.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
            this.leg2.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
            this.leg3.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
        }

    }
}
