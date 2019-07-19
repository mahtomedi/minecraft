package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuadrupedModel<T extends Entity> extends EntityModel<T> {
    protected ModelPart head;
    protected ModelPart body;
    protected ModelPart leg0;
    protected ModelPart leg1;
    protected ModelPart leg2;
    protected ModelPart leg3;
    protected float yHeadOffs = 8.0F;
    protected float zHeadOffs = 4.0F;

    public QuadrupedModel(int param0, float param1) {
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, param1);
        this.head.setPos(0.0F, (float)(18 - param0), -6.0F);
        this.body = new ModelPart(this, 28, 8);
        this.body.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, param1);
        this.body.setPos(0.0F, (float)(17 - param0), 2.0F);
        this.leg0 = new ModelPart(this, 0, 16);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4, param0, 4, param1);
        this.leg0.setPos(-3.0F, (float)(24 - param0), 7.0F);
        this.leg1 = new ModelPart(this, 0, 16);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, param0, 4, param1);
        this.leg1.setPos(3.0F, (float)(24 - param0), 7.0F);
        this.leg2 = new ModelPart(this, 0, 16);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, param0, 4, param1);
        this.leg2.setPos(-3.0F, (float)(24 - param0), -5.0F);
        this.leg3 = new ModelPart(this, 0, 16);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, param0, 4, param1);
        this.leg3.setPos(3.0F, (float)(24 - param0), -5.0F);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (this.young) {
            float var0 = 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, this.yHeadOffs * param6, this.zHeadOffs * param6);
            this.head.render(param6);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            GlStateManager.translatef(0.0F, 24.0F * param6, 0.0F);
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

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.body.xRot = (float) (Math.PI / 2);
        this.leg0.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leg1.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg2.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg3.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
    }
}
