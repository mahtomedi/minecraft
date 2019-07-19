package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseModel<T extends AbstractHorse> extends EntityModel<T> {
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart leg1A;
    private final ModelPart leg2A;
    private final ModelPart leg3A;
    private final ModelPart leg4A;
    private final ModelPart tail;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public HorseModel(float param0) {
        this.texWidth = 64;
        this.texHeight = 64;
        this.body = new ModelPart(this, 0, 32);
        this.body.addBox(-5.0F, -8.0F, -17.0F, 10, 10, 22, 0.05F);
        this.body.setPos(0.0F, 11.0F, 5.0F);
        this.headParts = new ModelPart(this, 0, 35);
        this.headParts.addBox(-2.05F, -6.0F, -2.0F, 4, 12, 7);
        this.headParts.xRot = (float) (Math.PI / 6);
        ModelPart var0 = new ModelPart(this, 0, 13);
        var0.addBox(-3.0F, -11.0F, -2.0F, 6, 5, 7, param0);
        ModelPart var1 = new ModelPart(this, 56, 36);
        var1.addBox(-1.0F, -11.0F, 5.01F, 2, 16, 2, param0);
        ModelPart var2 = new ModelPart(this, 0, 25);
        var2.addBox(-2.0F, -11.0F, -7.0F, 4, 5, 5, param0);
        this.headParts.addChild(var0);
        this.headParts.addChild(var1);
        this.headParts.addChild(var2);
        this.addEarModels(this.headParts);
        this.leg1A = new ModelPart(this, 48, 21);
        this.leg1A.mirror = true;
        this.leg1A.addBox(-3.0F, -1.01F, -1.0F, 4, 11, 4, param0);
        this.leg1A.setPos(4.0F, 14.0F, 7.0F);
        this.leg2A = new ModelPart(this, 48, 21);
        this.leg2A.addBox(-1.0F, -1.01F, -1.0F, 4, 11, 4, param0);
        this.leg2A.setPos(-4.0F, 14.0F, 7.0F);
        this.leg3A = new ModelPart(this, 48, 21);
        this.leg3A.mirror = true;
        this.leg3A.addBox(-3.0F, -1.01F, -1.9F, 4, 11, 4, param0);
        this.leg3A.setPos(4.0F, 6.0F, -12.0F);
        this.leg4A = new ModelPart(this, 48, 21);
        this.leg4A.addBox(-1.0F, -1.01F, -1.9F, 4, 11, 4, param0);
        this.leg4A.setPos(-4.0F, 6.0F, -12.0F);
        this.tail = new ModelPart(this, 42, 36);
        this.tail.addBox(-1.5F, 0.0F, 0.0F, 3, 14, 4, param0);
        this.tail.setPos(0.0F, -5.0F, 2.0F);
        this.tail.xRot = (float) (Math.PI / 6);
        this.body.addChild(this.tail);
        ModelPart var3 = new ModelPart(this, 26, 0);
        var3.addBox(-5.0F, -8.0F, -9.0F, 10, 9, 9, 0.5F);
        this.body.addChild(var3);
        ModelPart var4 = new ModelPart(this, 29, 5);
        var4.addBox(2.0F, -9.0F, -6.0F, 1, 2, 2, param0);
        this.headParts.addChild(var4);
        ModelPart var5 = new ModelPart(this, 29, 5);
        var5.addBox(-3.0F, -9.0F, -6.0F, 1, 2, 2, param0);
        this.headParts.addChild(var5);
        ModelPart var6 = new ModelPart(this, 32, 2);
        var6.addBox(3.1F, -6.0F, -8.0F, 0, 3, 16, param0);
        var6.xRot = (float) (-Math.PI / 6);
        this.headParts.addChild(var6);
        ModelPart var7 = new ModelPart(this, 32, 2);
        var7.addBox(-3.1F, -6.0F, -8.0F, 0, 3, 16, param0);
        var7.xRot = (float) (-Math.PI / 6);
        this.headParts.addChild(var7);
        ModelPart var8 = new ModelPart(this, 1, 1);
        var8.addBox(-3.0F, -11.0F, -1.9F, 6, 5, 6, 0.2F);
        this.headParts.addChild(var8);
        ModelPart var9 = new ModelPart(this, 19, 0);
        var9.addBox(-2.0F, -11.0F, -4.0F, 4, 5, 2, 0.2F);
        this.headParts.addChild(var9);
        this.saddleParts = new ModelPart[]{var3, var4, var5, var8, var9};
        this.ridingParts = new ModelPart[]{var6, var7};
    }

    protected void addEarModels(ModelPart param0) {
        ModelPart var0 = new ModelPart(this, 19, 16);
        var0.addBox(0.55F, -13.0F, 4.0F, 2, 3, 1, -0.001F);
        ModelPart var1 = new ModelPart(this, 19, 16);
        var1.addBox(-2.55F, -13.0F, 4.0F, 2, 3, 1, -0.001F);
        param0.addChild(var0);
        param0.addChild(var1);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        boolean var0 = param0.isBaby();
        float var1 = param0.getScale();
        boolean var2 = param0.isSaddled();
        boolean var3 = param0.isVehicle();

        for(ModelPart var4 : this.saddleParts) {
            var4.visible = var2;
        }

        for(ModelPart var5 : this.ridingParts) {
            var5.visible = var3 && var2;
        }

        if (var0) {
            GlStateManager.pushMatrix();
            GlStateManager.scalef(var1, 0.5F + var1 * 0.5F, var1);
            GlStateManager.translatef(0.0F, 0.95F * (1.0F - var1), 0.0F);
        }

        this.leg1A.render(param6);
        this.leg2A.render(param6);
        this.leg3A.render(param6);
        this.leg4A.render(param6);
        if (var0) {
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(var1, var1, var1);
            GlStateManager.translatef(0.0F, 2.3F * (1.0F - var1), 0.0F);
        }

        this.body.render(param6);
        if (var0) {
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float var6 = var1 + 0.1F * var1;
            GlStateManager.scalef(var6, var6, var6);
            GlStateManager.translatef(0.0F, 2.25F * (1.0F - var6), 0.1F * (1.4F - var6));
        }

        this.headParts.render(param6);
        if (var0) {
            GlStateManager.popMatrix();
        }

    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        float var0 = this.rotlerp(param0.yBodyRotO, param0.yBodyRot, param3);
        float var1 = this.rotlerp(param0.yHeadRotO, param0.yHeadRot, param3);
        float var2 = Mth.lerp(param3, param0.xRotO, param0.xRot);
        float var3 = var1 - var0;
        float var4 = var2 * (float) (Math.PI / 180.0);
        if (var3 > 20.0F) {
            var3 = 20.0F;
        }

        if (var3 < -20.0F) {
            var3 = -20.0F;
        }

        if (param2 > 0.2F) {
            var4 += Mth.cos(param1 * 0.4F) * 0.15F * param2;
        }

        float var5 = param0.getEatAnim(param3);
        float var6 = param0.getStandAnim(param3);
        float var7 = 1.0F - var6;
        float var8 = param0.getMouthAnim(param3);
        boolean var9 = param0.tailCounter != 0;
        float var10 = (float)param0.tickCount + param3;
        this.headParts.y = 4.0F;
        this.headParts.z = -12.0F;
        this.body.xRot = 0.0F;
        this.headParts.xRot = (float) (Math.PI / 6) + var4;
        this.headParts.yRot = var3 * (float) (Math.PI / 180.0);
        float var11 = param0.isInWater() ? 0.2F : 1.0F;
        float var12 = Mth.cos(var11 * param1 * 0.6662F + (float) Math.PI);
        float var13 = var12 * 0.8F * param2;
        float var14 = (1.0F - Math.max(var6, var5)) * ((float) (Math.PI / 6) + var4 + var8 * Mth.sin(var10) * 0.05F);
        this.headParts.xRot = var6 * (((float) (Math.PI / 12)) + var4) + var5 * (2.1816616F + Mth.sin(var10) * 0.05F) + var14;
        this.headParts.yRot = var6 * var3 * (float) (Math.PI / 180.0) + (1.0F - Math.max(var6, var5)) * this.headParts.yRot;
        this.headParts.y = var6 * -4.0F + var5 * 11.0F + (1.0F - Math.max(var6, var5)) * this.headParts.y;
        this.headParts.z = var6 * -4.0F + var5 * -12.0F + (1.0F - Math.max(var6, var5)) * this.headParts.z;
        this.body.xRot = var6 * (float) (-Math.PI / 4) + var7 * this.body.xRot;
        float var15 = (float) (Math.PI / 12) * var6;
        float var16 = Mth.cos(var10 * 0.6F + (float) Math.PI);
        this.leg3A.y = 2.0F * var6 + 14.0F * var7;
        this.leg3A.z = -6.0F * var6 - 10.0F * var7;
        this.leg4A.y = this.leg3A.y;
        this.leg4A.z = this.leg3A.z;
        float var17 = (((float) (-Math.PI / 3)) + var16) * var6 + var13 * var7;
        float var18 = ((float) (-Math.PI / 3) - var16) * var6 - var13 * var7;
        this.leg1A.xRot = var15 - var12 * 0.5F * param2 * var7;
        this.leg2A.xRot = var15 + var12 * 0.5F * param2 * var7;
        this.leg3A.xRot = var17;
        this.leg4A.xRot = var18;
        this.tail.xRot = (float) (Math.PI / 6) + param2 * 0.75F;
        this.tail.y = -5.0F + param2;
        this.tail.z = 2.0F + param2 * 2.0F;
        if (var9) {
            this.tail.yRot = Mth.cos(var10 * 0.7F);
        } else {
            this.tail.yRot = 0.0F;
        }

    }

    private float rotlerp(float param0, float param1, float param2) {
        float var0 = param1 - param0;

        while(var0 < -180.0F) {
            var0 += 360.0F;
        }

        while(var0 >= 180.0F) {
            var0 -= 360.0F;
        }

        return param0 + param2 * var0;
    }
}
