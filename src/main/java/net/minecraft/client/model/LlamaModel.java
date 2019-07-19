package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaModel<T extends AbstractChestedHorse> extends QuadrupedModel<T> {
    private final ModelPart chest1;
    private final ModelPart chest2;

    public LlamaModel(float param0) {
        super(15, param0);
        this.texWidth = 128;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-2.0F, -14.0F, -10.0F, 4, 4, 9, param0);
        this.head.setPos(0.0F, 7.0F, -6.0F);
        this.head.texOffs(0, 14).addBox(-4.0F, -16.0F, -6.0F, 8, 18, 6, param0);
        this.head.texOffs(17, 0).addBox(-4.0F, -19.0F, -4.0F, 3, 3, 2, param0);
        this.head.texOffs(17, 0).addBox(1.0F, -19.0F, -4.0F, 3, 3, 2, param0);
        this.body = new ModelPart(this, 29, 0);
        this.body.addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10, param0);
        this.body.setPos(0.0F, 5.0F, 2.0F);
        this.chest1 = new ModelPart(this, 45, 28);
        this.chest1.addBox(-3.0F, 0.0F, 0.0F, 8, 8, 3, param0);
        this.chest1.setPos(-8.5F, 3.0F, 3.0F);
        this.chest1.yRot = (float) (Math.PI / 2);
        this.chest2 = new ModelPart(this, 45, 41);
        this.chest2.addBox(-3.0F, 0.0F, 0.0F, 8, 8, 3, param0);
        this.chest2.setPos(5.5F, 3.0F, 3.0F);
        this.chest2.yRot = (float) (Math.PI / 2);
        int var0 = 4;
        int var1 = 14;
        this.leg0 = new ModelPart(this, 29, 29);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, param0);
        this.leg0.setPos(-2.5F, 10.0F, 6.0F);
        this.leg1 = new ModelPart(this, 29, 29);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, param0);
        this.leg1.setPos(2.5F, 10.0F, 6.0F);
        this.leg2 = new ModelPart(this, 29, 29);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, param0);
        this.leg2.setPos(-2.5F, 10.0F, -4.0F);
        this.leg3 = new ModelPart(this, 29, 29);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, 14, 4, param0);
        this.leg3.setPos(2.5F, 10.0F, -4.0F);
        --this.leg0.x;
        ++this.leg1.x;
        this.leg0.z += 0.0F;
        this.leg1.z += 0.0F;
        --this.leg2.x;
        ++this.leg3.x;
        --this.leg2.z;
        --this.leg3.z;
        this.zHeadOffs += 2.0F;
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        boolean var0 = !param0.isBaby() && param0.hasChest();
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (this.young) {
            float var1 = 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, this.yHeadOffs * param6, this.zHeadOffs * param6);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float var2 = 0.7F;
            GlStateManager.scalef(0.71428573F, 0.64935064F, 0.7936508F);
            GlStateManager.translatef(0.0F, 21.0F * param6, 0.22F);
            this.head.render(param6);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float var3 = 1.1F;
            GlStateManager.scalef(0.625F, 0.45454544F, 0.45454544F);
            GlStateManager.translatef(0.0F, 33.0F * param6, 0.0F);
            this.body.render(param6);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.45454544F, 0.41322312F, 0.45454544F);
            GlStateManager.translatef(0.0F, 33.0F * param6, 0.0F);
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

        if (var0) {
            this.chest1.render(param6);
            this.chest2.render(param6);
        }

    }
}
