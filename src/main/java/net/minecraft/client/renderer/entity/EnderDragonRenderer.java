package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderDragonRenderer extends EntityRenderer<EnderDragon> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final EnderDragonRenderer.DragonModel model = new EnderDragonRenderer.DragonModel(0.0F);

    public EnderDragonRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.5F;
    }

    public void render(EnderDragon param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        param6.pushPose();
        float var0 = (float)param0.getLatencyPos(7, param5)[0];
        float var1 = (float)(param0.getLatencyPos(5, param5)[1] - param0.getLatencyPos(10, param5)[1]);
        param6.mulPose(Vector3f.YP.rotationDegrees(-var0));
        param6.mulPose(Vector3f.XP.rotationDegrees(var1 * 10.0F));
        param6.translate(0.0, 0.0, 1.0);
        param6.scale(-1.0F, -1.0F, 1.0F);
        float var2 = 0.0625F;
        param6.translate(0.0, -1.501F, 0.0);
        boolean var3 = param0.hurtTime > 0;
        int var4 = param0.getLightColor();
        this.model.prepareMobModel(param0, 0.0F, 0.0F, param5);
        if (param0.dragonDeathTime > 0) {
            float var5 = (float)param0.dragonDeathTime / 200.0F;
            VertexConsumer var6 = param7.getBuffer(RenderType.entityAlpha(DRAGON_EXPLODING_LOCATION, var5));
            this.model.renderToBuffer(param6, var6, var4, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
            VertexConsumer var7 = param7.getBuffer(RenderType.entityDecal(DRAGON_LOCATION));
            this.model.renderToBuffer(param6, var7, var4, OverlayTexture.pack(0.0F, var3), 1.0F, 1.0F, 1.0F);
        } else {
            VertexConsumer var8 = param7.getBuffer(this.model.renderType(DRAGON_LOCATION));
            this.model.renderToBuffer(param6, var8, var4, OverlayTexture.pack(0.0F, var3), 1.0F, 1.0F, 1.0F);
        }

        VertexConsumer var9 = param7.getBuffer(RenderType.eyes(DRAGON_EYES_LOCATION));
        this.model.renderToBuffer(param6, var9, var4, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
        if (param0.dragonDeathTime > 0) {
            float var10 = ((float)param0.dragonDeathTime + param5) / 200.0F;
            float var11 = 0.0F;
            if (var10 > 0.8F) {
                var11 = (var10 - 0.8F) / 0.2F;
            }

            Random var12 = new Random(432L);
            VertexConsumer var13 = param7.getBuffer(RenderType.lightning());
            param6.pushPose();
            param6.translate(0.0, -1.0, -2.0);

            for(int var14 = 0; (float)var14 < (var10 + var10 * var10) / 2.0F * 60.0F; ++var14) {
                param6.mulPose(Vector3f.XP.rotationDegrees(var12.nextFloat() * 360.0F));
                param6.mulPose(Vector3f.YP.rotationDegrees(var12.nextFloat() * 360.0F));
                param6.mulPose(Vector3f.ZP.rotationDegrees(var12.nextFloat() * 360.0F));
                param6.mulPose(Vector3f.XP.rotationDegrees(var12.nextFloat() * 360.0F));
                param6.mulPose(Vector3f.YP.rotationDegrees(var12.nextFloat() * 360.0F));
                param6.mulPose(Vector3f.ZP.rotationDegrees(var12.nextFloat() * 360.0F + var10 * 90.0F));
                float var15 = var12.nextFloat() * 20.0F + 5.0F + var11 * 10.0F;
                float var16 = var12.nextFloat() * 2.0F + 1.0F + var11 * 2.0F;
                Matrix4f var17 = param6.getPose();
                int var18 = (int)(255.0F * (1.0F - var11));
                vertex01(var13, var17, var18);
                vertex2(var13, var17, var15, var16);
                vertex3(var13, var17, var15, var16);
                vertex01(var13, var17, var18);
                vertex3(var13, var17, var15, var16);
                vertex4(var13, var17, var15, var16);
                vertex01(var13, var17, var18);
                vertex4(var13, var17, var15, var16);
                vertex2(var13, var17, var15, var16);
            }

            param6.popPose();
        }

        param6.popPose();
        if (param0.nearestCrystal != null) {
            param6.pushPose();
            float var19 = (float)(param0.nearestCrystal.getX() - Mth.lerp((double)param5, param0.xo, param0.getX()));
            float var20 = (float)(param0.nearestCrystal.getY() - Mth.lerp((double)param5, param0.yo, param0.getY()));
            float var21 = (float)(param0.nearestCrystal.getZ() - Mth.lerp((double)param5, param0.zo, param0.getZ()));
            renderCrystalBeams(var19, var20 + EndCrystalRenderer.getY(param0.nearestCrystal, param5), var21, param5, param0.tickCount, param6, param7, var4);
            param6.popPose();
        }

        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    private static void vertex01(VertexConsumer param0, Matrix4f param1, int param2) {
        param0.vertex(param1, 0.0F, 0.0F, 0.0F).color(255, 255, 255, param2).endVertex();
        param0.vertex(param1, 0.0F, 0.0F, 0.0F).color(255, 255, 255, param2).endVertex();
    }

    private static void vertex2(VertexConsumer param0, Matrix4f param1, float param2, float param3) {
        param0.vertex(param1, -HALF_SQRT_3 * param3, param2, -0.5F * param3).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex3(VertexConsumer param0, Matrix4f param1, float param2, float param3) {
        param0.vertex(param1, HALF_SQRT_3 * param3, param2, -0.5F * param3).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex4(VertexConsumer param0, Matrix4f param1, float param2, float param3) {
        param0.vertex(param1, 0.0F, param2, 1.0F * param3).color(255, 0, 255, 0).endVertex();
    }

    public static void renderCrystalBeams(
        float param0, float param1, float param2, float param3, int param4, PoseStack param5, MultiBufferSource param6, int param7
    ) {
        float var0 = Mth.sqrt(param0 * param0 + param2 * param2);
        float var1 = Mth.sqrt(param0 * param0 + param1 * param1 + param2 * param2);
        param5.pushPose();
        param5.translate(0.0, 2.0, 0.0);
        param5.mulPose(Vector3f.YP.rotation((float)(-Math.atan2((double)param2, (double)param0)) - (float) (Math.PI / 2)));
        param5.mulPose(Vector3f.XP.rotation((float)(-Math.atan2((double)var0, (double)param1)) - (float) (Math.PI / 2)));
        VertexConsumer var2 = param6.getBuffer(RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION));
        float var3 = 0.0F - ((float)param4 + param3) * 0.01F;
        float var4 = Mth.sqrt(param0 * param0 + param1 * param1 + param2 * param2) / 32.0F - ((float)param4 + param3) * 0.01F;
        int var5 = 8;
        float var6 = 0.0F;
        float var7 = 0.75F;
        float var8 = 0.0F;
        Matrix4f var9 = param5.getPose();

        for(int var10 = 1; var10 <= 8; ++var10) {
            float var11 = Mth.sin((float)(var10 % 8) * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float var12 = Mth.cos((float)(var10 % 8) * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float var13 = (float)(var10 % 8) / 8.0F;
            var2.vertex(var9, var6 * 0.2F, var7 * 0.2F, 0.0F)
                .color(0, 0, 0, 255)
                .uv(var8, var3)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(param7)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            var2.vertex(var9, var6, var7, var1)
                .color(255, 255, 255, 255)
                .uv(var8, var4)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(param7)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            var2.vertex(var9, var11, var12, var1)
                .color(255, 255, 255, 255)
                .uv(var13, var4)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(param7)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            var2.vertex(var9, var11 * 0.2F, var12 * 0.2F, 0.0F)
                .color(0, 0, 0, 255)
                .uv(var13, var3)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(param7)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            var6 = var11;
            var7 = var12;
            var8 = var13;
        }

        param5.popPose();
    }

    public ResourceLocation getTextureLocation(EnderDragon param0) {
        return DRAGON_LOCATION;
    }

    @OnlyIn(Dist.CLIENT)
    public static class DragonModel extends EntityModel<EnderDragon> {
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
        @Nullable
        private EnderDragon entity;
        private float a;

        public DragonModel(float param0) {
            this.texWidth = 256;
            this.texHeight = 256;
            float var0 = -16.0F;
            this.head = new ModelPart(this);
            this.head.addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, param0, 176, 44);
            this.head.addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, param0, 112, 30);
            this.head.mirror = true;
            this.head.addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, param0, 0, 0);
            this.head.addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, param0, 112, 0);
            this.head.mirror = false;
            this.head.addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, param0, 0, 0);
            this.head.addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, param0, 112, 0);
            this.jaw = new ModelPart(this);
            this.jaw.setPos(0.0F, 4.0F, -8.0F);
            this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, param0, 176, 65);
            this.head.addChild(this.jaw);
            this.neck = new ModelPart(this);
            this.neck.addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, param0, 192, 104);
            this.neck.addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, param0, 48, 0);
            this.body = new ModelPart(this);
            this.body.setPos(0.0F, 4.0F, 8.0F);
            this.body.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64, param0, 0, 0);
            this.body.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, param0, 220, 53);
            this.body.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, param0, 220, 53);
            this.body.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, param0, 220, 53);
            this.wing = new ModelPart(this);
            this.wing.setPos(-12.0F, 5.0F, 2.0F);
            this.wing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, param0, 112, 88);
            this.wing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, param0, -56, 88);
            this.wingTip = new ModelPart(this);
            this.wingTip.setPos(-56.0F, 0.0F, 0.0F);
            this.wingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, param0, 112, 136);
            this.wingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, param0, -56, 144);
            this.wing.addChild(this.wingTip);
            this.frontLeg = new ModelPart(this);
            this.frontLeg.setPos(-12.0F, 20.0F, 2.0F);
            this.frontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, param0, 112, 104);
            this.frontLegTip = new ModelPart(this);
            this.frontLegTip.setPos(0.0F, 20.0F, -1.0F);
            this.frontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, param0, 226, 138);
            this.frontLeg.addChild(this.frontLegTip);
            this.frontFoot = new ModelPart(this);
            this.frontFoot.setPos(0.0F, 23.0F, 0.0F);
            this.frontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, param0, 144, 104);
            this.frontLegTip.addChild(this.frontFoot);
            this.rearLeg = new ModelPart(this);
            this.rearLeg.setPos(-16.0F, 16.0F, 42.0F);
            this.rearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, param0, 0, 0);
            this.rearLegTip = new ModelPart(this);
            this.rearLegTip.setPos(0.0F, 32.0F, -4.0F);
            this.rearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, param0, 196, 0);
            this.rearLeg.addChild(this.rearLegTip);
            this.rearFoot = new ModelPart(this);
            this.rearFoot.setPos(0.0F, 31.0F, 4.0F);
            this.rearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, param0, 112, 0);
            this.rearLegTip.addChild(this.rearFoot);
        }

        public void prepareMobModel(EnderDragon param0, float param1, float param2, float param3) {
            this.entity = param0;
            this.a = param3;
        }

        public void setupAnim(EnderDragon param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        }

        @Override
        public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6) {
            float var0 = 0.0625F;
            param0.pushPose();
            float var1 = Mth.lerp(this.a, this.entity.oFlapTime, this.entity.flapTime);
            this.jaw.xRot = (float)(Math.sin((double)(var1 * (float) (Math.PI * 2))) + 1.0) * 0.2F;
            float var2 = (float)(Math.sin((double)(var1 * (float) (Math.PI * 2) - 1.0F)) + 1.0);
            var2 = (var2 * var2 + var2 * 2.0F) * 0.05F;
            param0.translate(0.0, (double)(var2 - 2.0F), -3.0);
            param0.mulPose(Vector3f.XP.rotationDegrees(var2 * 2.0F));
            float var3 = 0.0F;
            float var4 = 20.0F;
            float var5 = -12.0F;
            float var6 = 1.5F;
            double[] var7 = this.entity.getLatencyPos(6, this.a);
            float var8 = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]);
            float var9 = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] + (double)(var8 / 2.0F));
            float var10 = var1 * (float) (Math.PI * 2);

            for(int var11 = 0; var11 < 5; ++var11) {
                double[] var12 = this.entity.getLatencyPos(5 - var11, this.a);
                float var13 = (float)Math.cos((double)((float)var11 * 0.45F + var10)) * 0.15F;
                this.neck.yRot = Mth.rotWrap(var12[0] - var7[0]) * (float) (Math.PI / 180.0) * 1.5F;
                this.neck.xRot = var13 + this.entity.getHeadPartYOffset(var11, var7, var12) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
                this.neck.zRot = -Mth.rotWrap(var12[0] - (double)var9) * (float) (Math.PI / 180.0) * 1.5F;
                this.neck.y = var4;
                this.neck.z = var5;
                this.neck.x = var3;
                var4 = (float)((double)var4 + Math.sin((double)this.neck.xRot) * 10.0);
                var5 = (float)((double)var5 - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
                var3 = (float)((double)var3 - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
                this.neck.render(param0, param1, 0.0625F, param2, param3, null);
            }

            this.head.y = var4;
            this.head.z = var5;
            this.head.x = var3;
            double[] var14 = this.entity.getLatencyPos(0, this.a);
            this.head.yRot = Mth.rotWrap(var14[0] - var7[0]) * (float) (Math.PI / 180.0);
            this.head.xRot = Mth.rotWrap((double)this.entity.getHeadPartYOffset(6, var7, var14)) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
            this.head.zRot = -Mth.rotWrap(var14[0] - (double)var9) * (float) (Math.PI / 180.0);
            this.head.render(param0, param1, 0.0625F, param2, param3, null);
            param0.pushPose();
            param0.translate(0.0, 1.0, 0.0);
            param0.mulPose(Vector3f.ZP.rotationDegrees(-var8 * 1.5F));
            param0.translate(0.0, -1.0, 0.0);
            this.body.zRot = 0.0F;
            this.body.render(param0, param1, 0.0625F, param2, param3, null);

            for(int var15 = 0; var15 < 2; ++var15) {
                float var16 = var1 * (float) (Math.PI * 2);
                this.wing.xRot = 0.125F - (float)Math.cos((double)var16) * 0.2F;
                this.wing.yRot = 0.25F;
                this.wing.zRot = (float)(Math.sin((double)var16) + 0.125) * 0.8F;
                this.wingTip.zRot = -((float)(Math.sin((double)(var16 + 2.0F)) + 0.5)) * 0.75F;
                this.rearLeg.xRot = 1.0F + var2 * 0.1F;
                this.rearLegTip.xRot = 0.5F + var2 * 0.1F;
                this.rearFoot.xRot = 0.75F + var2 * 0.1F;
                this.frontLeg.xRot = 1.3F + var2 * 0.1F;
                this.frontLegTip.xRot = -0.5F - var2 * 0.1F;
                this.frontFoot.xRot = 0.75F + var2 * 0.1F;
                this.wing.render(param0, param1, 0.0625F, param2, param3, null);
                this.frontLeg.render(param0, param1, 0.0625F, param2, param3, null);
                this.rearLeg.render(param0, param1, 0.0625F, param2, param3, null);
                param0.scale(-1.0F, 1.0F, 1.0F);
            }

            param0.popPose();
            float var17 = -((float)Math.sin((double)(var1 * (float) (Math.PI * 2)))) * 0.0F;
            var10 = var1 * (float) (Math.PI * 2);
            var4 = 10.0F;
            var5 = 60.0F;
            var3 = 0.0F;
            var7 = this.entity.getLatencyPos(11, this.a);

            for(int var18 = 0; var18 < 12; ++var18) {
                var14 = this.entity.getLatencyPos(12 + var18, this.a);
                var17 = (float)((double)var17 + Math.sin((double)((float)var18 * 0.45F + var10)) * 0.05F);
                this.neck.yRot = (Mth.rotWrap(var14[0] - var7[0]) * 1.5F + 180.0F) * (float) (Math.PI / 180.0);
                this.neck.xRot = var17 + (float)(var14[1] - var7[1]) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
                this.neck.zRot = Mth.rotWrap(var14[0] - (double)var9) * (float) (Math.PI / 180.0) * 1.5F;
                this.neck.y = var4;
                this.neck.z = var5;
                this.neck.x = var3;
                var4 = (float)((double)var4 + Math.sin((double)this.neck.xRot) * 10.0);
                var5 = (float)((double)var5 - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
                var3 = (float)((double)var3 - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
                this.neck.render(param0, param1, 0.0625F, param2, param3, null);
            }

            param0.popPose();
        }
    }
}
