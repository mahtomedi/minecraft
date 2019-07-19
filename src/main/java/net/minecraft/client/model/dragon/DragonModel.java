package net.minecraft.client.model.dragon;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DragonModel extends EntityModel<EnderDragon> {
    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart jaw;
    private final ModelPart body;
    private final ModelPart rearLeg;
    private final ModelPart frontLeg;
    private final ModelPart rearLegTip;
    private final ModelPart frontLegTip;
    private final ModelPart rearFoot;
    private final ModelPart frontFoot;
    private final ModelPart wing;
    private final ModelPart wingTip;
    private float a;

    public DragonModel(float param0) {
        this.texWidth = 256;
        this.texHeight = 256;
        float var0 = -16.0F;
        this.head = new ModelPart(this, "head");
        this.head.addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, param0, 176, 44);
        this.head.addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, param0, 112, 30);
        this.head.mirror = true;
        this.head.addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, param0, 0, 0);
        this.head.addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, param0, 112, 0);
        this.head.mirror = false;
        this.head.addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, param0, 0, 0);
        this.head.addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, param0, 112, 0);
        this.jaw = new ModelPart(this, "jaw");
        this.jaw.setPos(0.0F, 4.0F, -8.0F);
        this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, param0, 176, 65);
        this.head.addChild(this.jaw);
        this.neck = new ModelPart(this, "neck");
        this.neck.addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, param0, 192, 104);
        this.neck.addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, param0, 48, 0);
        this.body = new ModelPart(this, "body");
        this.body.setPos(0.0F, 4.0F, 8.0F);
        this.body.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64, param0, 0, 0);
        this.body.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, param0, 220, 53);
        this.body.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, param0, 220, 53);
        this.body.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, param0, 220, 53);
        this.wing = new ModelPart(this, "wing");
        this.wing.setPos(-12.0F, 5.0F, 2.0F);
        this.wing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, param0, 112, 88);
        this.wing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, param0, -56, 88);
        this.wingTip = new ModelPart(this, "wingtip");
        this.wingTip.setPos(-56.0F, 0.0F, 0.0F);
        this.wingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, param0, 112, 136);
        this.wingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, param0, -56, 144);
        this.wing.addChild(this.wingTip);
        this.frontLeg = new ModelPart(this, "frontleg");
        this.frontLeg.setPos(-12.0F, 20.0F, 2.0F);
        this.frontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, param0, 112, 104);
        this.frontLegTip = new ModelPart(this, "frontlegtip");
        this.frontLegTip.setPos(0.0F, 20.0F, -1.0F);
        this.frontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, param0, 226, 138);
        this.frontLeg.addChild(this.frontLegTip);
        this.frontFoot = new ModelPart(this, "frontfoot");
        this.frontFoot.setPos(0.0F, 23.0F, 0.0F);
        this.frontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, param0, 144, 104);
        this.frontLegTip.addChild(this.frontFoot);
        this.rearLeg = new ModelPart(this, "rearleg");
        this.rearLeg.setPos(-16.0F, 16.0F, 42.0F);
        this.rearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, param0, 0, 0);
        this.rearLegTip = new ModelPart(this, "rearlegtip");
        this.rearLegTip.setPos(0.0F, 32.0F, -4.0F);
        this.rearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, param0, 196, 0);
        this.rearLeg.addChild(this.rearLegTip);
        this.rearFoot = new ModelPart(this, "rearfoot");
        this.rearFoot.setPos(0.0F, 31.0F, 4.0F);
        this.rearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, param0, 112, 0);
        this.rearLegTip.addChild(this.rearFoot);
    }

    public void prepareMobModel(EnderDragon param0, float param1, float param2, float param3) {
        this.a = param3;
    }

    public void render(EnderDragon param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        GlStateManager.pushMatrix();
        float var0 = Mth.lerp(this.a, param0.oFlapTime, param0.flapTime);
        this.jaw.xRot = (float)(Math.sin((double)(var0 * (float) (Math.PI * 2))) + 1.0) * 0.2F;
        float var1 = (float)(Math.sin((double)(var0 * (float) (Math.PI * 2) - 1.0F)) + 1.0);
        var1 = (var1 * var1 + var1 * 2.0F) * 0.05F;
        GlStateManager.translatef(0.0F, var1 - 2.0F, -3.0F);
        GlStateManager.rotatef(var1 * 2.0F, 1.0F, 0.0F, 0.0F);
        float var2 = 0.0F;
        float var3 = 20.0F;
        float var4 = -12.0F;
        float var5 = 1.5F;
        double[] var6 = param0.getLatencyPos(6, this.a);
        float var7 = this.rotWrap(param0.getLatencyPos(5, this.a)[0] - param0.getLatencyPos(10, this.a)[0]);
        float var8 = this.rotWrap(param0.getLatencyPos(5, this.a)[0] + (double)(var7 / 2.0F));
        float var9 = var0 * (float) (Math.PI * 2);

        for(int var10 = 0; var10 < 5; ++var10) {
            double[] var11 = param0.getLatencyPos(5 - var10, this.a);
            float var12 = (float)Math.cos((double)((float)var10 * 0.45F + var9)) * 0.15F;
            this.neck.yRot = this.rotWrap(var11[0] - var6[0]) * (float) (Math.PI / 180.0) * 1.5F;
            this.neck.xRot = var12 + param0.getHeadPartYOffset(var10, var6, var11) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
            this.neck.zRot = -this.rotWrap(var11[0] - (double)var8) * (float) (Math.PI / 180.0) * 1.5F;
            this.neck.y = var3;
            this.neck.z = var4;
            this.neck.x = var2;
            var3 = (float)((double)var3 + Math.sin((double)this.neck.xRot) * 10.0);
            var4 = (float)((double)var4 - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
            var2 = (float)((double)var2 - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
            this.neck.render(param6);
        }

        this.head.y = var3;
        this.head.z = var4;
        this.head.x = var2;
        double[] var13 = param0.getLatencyPos(0, this.a);
        this.head.yRot = this.rotWrap(var13[0] - var6[0]) * (float) (Math.PI / 180.0);
        this.head.xRot = this.rotWrap((double)param0.getHeadPartYOffset(6, var6, var13)) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
        this.head.zRot = -this.rotWrap(var13[0] - (double)var8) * (float) (Math.PI / 180.0);
        this.head.render(param6);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(-var7 * 1.5F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translatef(0.0F, -1.0F, 0.0F);
        this.body.zRot = 0.0F;
        this.body.render(param6);

        for(int var14 = 0; var14 < 2; ++var14) {
            GlStateManager.enableCull();
            float var15 = var0 * (float) (Math.PI * 2);
            this.wing.xRot = 0.125F - (float)Math.cos((double)var15) * 0.2F;
            this.wing.yRot = 0.25F;
            this.wing.zRot = (float)(Math.sin((double)var15) + 0.125) * 0.8F;
            this.wingTip.zRot = -((float)(Math.sin((double)(var15 + 2.0F)) + 0.5)) * 0.75F;
            this.rearLeg.xRot = 1.0F + var1 * 0.1F;
            this.rearLegTip.xRot = 0.5F + var1 * 0.1F;
            this.rearFoot.xRot = 0.75F + var1 * 0.1F;
            this.frontLeg.xRot = 1.3F + var1 * 0.1F;
            this.frontLegTip.xRot = -0.5F - var1 * 0.1F;
            this.frontFoot.xRot = 0.75F + var1 * 0.1F;
            this.wing.render(param6);
            this.frontLeg.render(param6);
            this.rearLeg.render(param6);
            GlStateManager.scalef(-1.0F, 1.0F, 1.0F);
            if (var14 == 0) {
                GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableCull();
        float var16 = -((float)Math.sin((double)(var0 * (float) (Math.PI * 2)))) * 0.0F;
        var9 = var0 * (float) (Math.PI * 2);
        var3 = 10.0F;
        var4 = 60.0F;
        var2 = 0.0F;
        var6 = param0.getLatencyPos(11, this.a);

        for(int var17 = 0; var17 < 12; ++var17) {
            var13 = param0.getLatencyPos(12 + var17, this.a);
            var16 = (float)((double)var16 + Math.sin((double)((float)var17 * 0.45F + var9)) * 0.05F);
            this.neck.yRot = (this.rotWrap(var13[0] - var6[0]) * 1.5F + 180.0F) * (float) (Math.PI / 180.0);
            this.neck.xRot = var16 + (float)(var13[1] - var6[1]) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
            this.neck.zRot = this.rotWrap(var13[0] - (double)var8) * (float) (Math.PI / 180.0) * 1.5F;
            this.neck.y = var3;
            this.neck.z = var4;
            this.neck.x = var2;
            var3 = (float)((double)var3 + Math.sin((double)this.neck.xRot) * 10.0);
            var4 = (float)((double)var4 - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
            var2 = (float)((double)var2 - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
            this.neck.render(param6);
        }

        GlStateManager.popMatrix();
    }

    private float rotWrap(double param0) {
        while(param0 >= 180.0) {
            param0 -= 360.0;
        }

        while(param0 < -180.0) {
            param0 += 360.0;
        }

        return (float)param0;
    }
}
