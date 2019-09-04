package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfModel<T extends Wolf> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart tail;
    private final ModelPart upperBody;

    public WolfModel() {
        float var0 = 0.0F;
        float var1 = 13.5F;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-2.0F, -3.0F, -2.0F, 6, 6, 4, 0.0F);
        this.head.setPos(-1.0F, 13.5F, -7.0F);
        this.body = new ModelPart(this, 18, 14);
        this.body.addBox(-3.0F, -2.0F, -3.0F, 6, 9, 6, 0.0F);
        this.body.setPos(0.0F, 14.0F, 2.0F);
        this.upperBody = new ModelPart(this, 21, 0);
        this.upperBody.addBox(-3.0F, -3.0F, -3.0F, 8, 6, 7, 0.0F);
        this.upperBody.setPos(-1.0F, 14.0F, 2.0F);
        this.leg0 = new ModelPart(this, 0, 18);
        this.leg0.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.leg0.setPos(-2.5F, 16.0F, 7.0F);
        this.leg1 = new ModelPart(this, 0, 18);
        this.leg1.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.leg1.setPos(0.5F, 16.0F, 7.0F);
        this.leg2 = new ModelPart(this, 0, 18);
        this.leg2.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.leg2.setPos(-2.5F, 16.0F, -4.0F);
        this.leg3 = new ModelPart(this, 0, 18);
        this.leg3.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.leg3.setPos(0.5F, 16.0F, -4.0F);
        this.tail = new ModelPart(this, 9, 18);
        this.tail.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.tail.setPos(-1.0F, 12.0F, 8.0F);
        this.head.texOffs(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2, 2, 1, 0.0F);
        this.head.texOffs(16, 14).addBox(2.0F, -5.0F, 0.0F, 2, 2, 1, 0.0F);
        this.head.texOffs(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3, 3, 4, 0.0F);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.render(param0, param1, param2, param3, param4, param5, param6);
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (this.young) {
            float var0 = 2.0F;
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 5.0F * param6, 2.0F * param6);
            this.head.renderRollable(param6);
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            RenderSystem.translatef(0.0F, 24.0F * param6, 0.0F);
            this.body.render(param6);
            this.leg0.render(param6);
            this.leg1.render(param6);
            this.leg2.render(param6);
            this.leg3.render(param6);
            this.tail.renderRollable(param6);
            this.upperBody.render(param6);
            RenderSystem.popMatrix();
        } else {
            this.head.renderRollable(param6);
            this.body.render(param6);
            this.leg0.render(param6);
            this.leg1.render(param6);
            this.leg2.render(param6);
            this.leg3.render(param6);
            this.tail.renderRollable(param6);
            this.upperBody.render(param6);
        }

    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        if (param0.isAngry()) {
            this.tail.yRot = 0.0F;
        } else {
            this.tail.yRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        }

        if (param0.isSitting()) {
            this.upperBody.setPos(-1.0F, 16.0F, -3.0F);
            this.upperBody.xRot = (float) (Math.PI * 2.0 / 5.0);
            this.upperBody.yRot = 0.0F;
            this.body.setPos(0.0F, 18.0F, 0.0F);
            this.body.xRot = (float) (Math.PI / 4);
            this.tail.setPos(-1.0F, 21.0F, 6.0F);
            this.leg0.setPos(-2.5F, 22.0F, 2.0F);
            this.leg0.xRot = (float) (Math.PI * 3.0 / 2.0);
            this.leg1.setPos(0.5F, 22.0F, 2.0F);
            this.leg1.xRot = (float) (Math.PI * 3.0 / 2.0);
            this.leg2.xRot = 5.811947F;
            this.leg2.setPos(-2.49F, 17.0F, -4.0F);
            this.leg3.xRot = 5.811947F;
            this.leg3.setPos(0.51F, 17.0F, -4.0F);
        } else {
            this.body.setPos(0.0F, 14.0F, 2.0F);
            this.body.xRot = (float) (Math.PI / 2);
            this.upperBody.setPos(-1.0F, 14.0F, -3.0F);
            this.upperBody.xRot = this.body.xRot;
            this.tail.setPos(-1.0F, 12.0F, 8.0F);
            this.leg0.setPos(-2.5F, 16.0F, 7.0F);
            this.leg1.setPos(0.5F, 16.0F, 7.0F);
            this.leg2.setPos(-2.5F, 16.0F, -4.0F);
            this.leg3.setPos(0.5F, 16.0F, -4.0F);
            this.leg0.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
            this.leg1.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
            this.leg2.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
            this.leg3.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        }

        this.head.zRot = param0.getHeadRollAngle(param3) + param0.getBodyRollAngle(param3, 0.0F);
        this.upperBody.zRot = param0.getBodyRollAngle(param3, -0.08F);
        this.body.zRot = param0.getBodyRollAngle(param3, -0.16F);
        this.tail.zRot = param0.getBodyRollAngle(param3, -0.2F);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.tail.xRot = param3;
    }
}
