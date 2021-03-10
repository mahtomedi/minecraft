package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(DRAGON_LOCATION);
    private static final RenderType DECAL = RenderType.entityDecal(DRAGON_LOCATION);
    private static final RenderType EYES = RenderType.eyes(DRAGON_EYES_LOCATION);
    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final EnderDragonRenderer.DragonModel model;

    public EnderDragonRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.shadowRadius = 0.5F;
        this.model = new EnderDragonRenderer.DragonModel(param0.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    public void render(EnderDragon param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        float var0 = (float)param0.getLatencyPos(7, param2)[0];
        float var1 = (float)(param0.getLatencyPos(5, param2)[1] - param0.getLatencyPos(10, param2)[1]);
        param3.mulPose(Vector3f.YP.rotationDegrees(-var0));
        param3.mulPose(Vector3f.XP.rotationDegrees(var1 * 10.0F));
        param3.translate(0.0, 0.0, 1.0);
        param3.scale(-1.0F, -1.0F, 1.0F);
        param3.translate(0.0, -1.501F, 0.0);
        boolean var2 = param0.hurtTime > 0;
        this.model.prepareMobModel(param0, 0.0F, 0.0F, param2);
        if (param0.dragonDeathTime > 0) {
            float var3 = (float)param0.dragonDeathTime / 200.0F;
            VertexConsumer var4 = param4.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
            this.model.renderToBuffer(param3, var4, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, var3);
            VertexConsumer var5 = param4.getBuffer(DECAL);
            this.model.renderToBuffer(param3, var5, param5, OverlayTexture.pack(0.0F, var2), 1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            VertexConsumer var6 = param4.getBuffer(RENDER_TYPE);
            this.model.renderToBuffer(param3, var6, param5, OverlayTexture.pack(0.0F, var2), 1.0F, 1.0F, 1.0F, 1.0F);
        }

        VertexConsumer var7 = param4.getBuffer(EYES);
        this.model.renderToBuffer(param3, var7, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        if (param0.dragonDeathTime > 0) {
            float var8 = ((float)param0.dragonDeathTime + param2) / 200.0F;
            float var9 = Math.min(var8 > 0.8F ? (var8 - 0.8F) / 0.2F : 0.0F, 1.0F);
            Random var10 = new Random(432L);
            VertexConsumer var11 = param4.getBuffer(RenderType.lightning());
            param3.pushPose();
            param3.translate(0.0, -1.0, -2.0);

            for(int var12 = 0; (float)var12 < (var8 + var8 * var8) / 2.0F * 60.0F; ++var12) {
                param3.mulPose(Vector3f.XP.rotationDegrees(var10.nextFloat() * 360.0F));
                param3.mulPose(Vector3f.YP.rotationDegrees(var10.nextFloat() * 360.0F));
                param3.mulPose(Vector3f.ZP.rotationDegrees(var10.nextFloat() * 360.0F));
                param3.mulPose(Vector3f.XP.rotationDegrees(var10.nextFloat() * 360.0F));
                param3.mulPose(Vector3f.YP.rotationDegrees(var10.nextFloat() * 360.0F));
                param3.mulPose(Vector3f.ZP.rotationDegrees(var10.nextFloat() * 360.0F + var8 * 90.0F));
                float var13 = var10.nextFloat() * 20.0F + 5.0F + var9 * 10.0F;
                float var14 = var10.nextFloat() * 2.0F + 1.0F + var9 * 2.0F;
                Matrix4f var15 = param3.last().pose();
                int var16 = (int)(255.0F * (1.0F - var9));
                vertex01(var11, var15, var16);
                vertex2(var11, var15, var13, var14);
                vertex3(var11, var15, var13, var14);
                vertex01(var11, var15, var16);
                vertex3(var11, var15, var13, var14);
                vertex4(var11, var15, var13, var14);
                vertex01(var11, var15, var16);
                vertex4(var11, var15, var13, var14);
                vertex2(var11, var15, var13, var14);
            }

            param3.popPose();
        }

        param3.popPose();
        if (param0.nearestCrystal != null) {
            param3.pushPose();
            float var17 = (float)(param0.nearestCrystal.getX() - Mth.lerp((double)param2, param0.xo, param0.getX()));
            float var18 = (float)(param0.nearestCrystal.getY() - Mth.lerp((double)param2, param0.yo, param0.getY()));
            float var19 = (float)(param0.nearestCrystal.getZ() - Mth.lerp((double)param2, param0.zo, param0.getZ()));
            renderCrystalBeams(var17, var18 + EndCrystalRenderer.getY(param0.nearestCrystal, param2), var19, param2, param0.tickCount, param3, param4, param5);
            param3.popPose();
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    private static void vertex01(VertexConsumer param0, Matrix4f param1, int param2) {
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
        VertexConsumer var2 = param6.getBuffer(BEAM);
        float var3 = 0.0F - ((float)param4 + param3) * 0.01F;
        float var4 = Mth.sqrt(param0 * param0 + param1 * param1 + param2 * param2) / 32.0F - ((float)param4 + param3) * 0.01F;
        int var5 = 8;
        float var6 = 0.0F;
        float var7 = 0.75F;
        float var8 = 0.0F;
        PoseStack.Pose var9 = param5.last();
        Matrix4f var10 = var9.pose();
        Matrix3f var11 = var9.normal();

        for(int var12 = 1; var12 <= 8; ++var12) {
            float var13 = Mth.sin((float)var12 * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float var14 = Mth.cos((float)var12 * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float var15 = (float)var12 / 8.0F;
            var2.vertex(var10, var6 * 0.2F, var7 * 0.2F, 0.0F)
                .color(0, 0, 0, 255)
                .uv(var8, var3)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(param7)
                .normal(var11, 0.0F, -1.0F, 0.0F)
                .endVertex();
            var2.vertex(var10, var6, var7, var1)
                .color(255, 255, 255, 255)
                .uv(var8, var4)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(param7)
                .normal(var11, 0.0F, -1.0F, 0.0F)
                .endVertex();
            var2.vertex(var10, var13, var14, var1)
                .color(255, 255, 255, 255)
                .uv(var15, var4)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(param7)
                .normal(var11, 0.0F, -1.0F, 0.0F)
                .endVertex();
            var2.vertex(var10, var13 * 0.2F, var14 * 0.2F, 0.0F)
                .color(0, 0, 0, 255)
                .uv(var15, var3)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(param7)
                .normal(var11, 0.0F, -1.0F, 0.0F)
                .endVertex();
            var6 = var13;
            var7 = var14;
            var8 = var15;
        }

        param5.popPose();
    }

    public ResourceLocation getTextureLocation(EnderDragon param0) {
        return DRAGON_LOCATION;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        float var2 = -16.0F;
        PartDefinition var3 = var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, 176, 44)
                .addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, 112, 30)
                .mirror()
                .addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0)
                .addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0)
                .mirror()
                .addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0)
                .addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0),
            PartPose.ZERO
        );
        var3.addOrReplaceChild("jaw", CubeListBuilder.create().addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, 176, 65), PartPose.offset(0.0F, 4.0F, -8.0F));
        var1.addOrReplaceChild(
            "neck",
            CubeListBuilder.create().addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, 192, 104).addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, 48, 0),
            PartPose.ZERO
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64, 0, 0)
                .addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, 220, 53)
                .addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, 220, 53)
                .addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, 220, 53),
            PartPose.offset(0.0F, 4.0F, 8.0F)
        );
        PartDefinition var4 = var1.addOrReplaceChild(
            "left_wing",
            CubeListBuilder.create().mirror().addBox("bone", 0.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88),
            PartPose.offset(12.0F, 5.0F, 2.0F)
        );
        var4.addOrReplaceChild(
            "left_wing_tip",
            CubeListBuilder.create().mirror().addBox("bone", 0.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144),
            PartPose.offset(56.0F, 0.0F, 0.0F)
        );
        PartDefinition var5 = var1.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 112, 104), PartPose.offset(12.0F, 20.0F, 2.0F)
        );
        PartDefinition var6 = var5.addOrReplaceChild(
            "left_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 226, 138), PartPose.offset(0.0F, 20.0F, -1.0F)
        );
        var6.addOrReplaceChild(
            "left_front_foot", CubeListBuilder.create().addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 144, 104), PartPose.offset(0.0F, 23.0F, 0.0F)
        );
        PartDefinition var7 = var1.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0, 0), PartPose.offset(16.0F, 16.0F, 42.0F)
        );
        PartDefinition var8 = var7.addOrReplaceChild(
            "left_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 196, 0), PartPose.offset(0.0F, 32.0F, -4.0F)
        );
        var8.addOrReplaceChild(
            "left_hind_foot", CubeListBuilder.create().addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 112, 0), PartPose.offset(0.0F, 31.0F, 4.0F)
        );
        PartDefinition var9 = var1.addOrReplaceChild(
            "right_wing",
            CubeListBuilder.create().addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88),
            PartPose.offset(-12.0F, 5.0F, 2.0F)
        );
        var9.addOrReplaceChild(
            "right_wing_tip",
            CubeListBuilder.create().addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144),
            PartPose.offset(-56.0F, 0.0F, 0.0F)
        );
        PartDefinition var10 = var1.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 112, 104), PartPose.offset(-12.0F, 20.0F, 2.0F)
        );
        PartDefinition var11 = var10.addOrReplaceChild(
            "right_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 226, 138), PartPose.offset(0.0F, 20.0F, -1.0F)
        );
        var11.addOrReplaceChild(
            "right_front_foot", CubeListBuilder.create().addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 144, 104), PartPose.offset(0.0F, 23.0F, 0.0F)
        );
        PartDefinition var12 = var1.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0, 0), PartPose.offset(-16.0F, 16.0F, 42.0F)
        );
        PartDefinition var13 = var12.addOrReplaceChild(
            "right_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 196, 0), PartPose.offset(0.0F, 32.0F, -4.0F)
        );
        var13.addOrReplaceChild(
            "right_hind_foot", CubeListBuilder.create().addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 112, 0), PartPose.offset(0.0F, 31.0F, 4.0F)
        );
        return LayerDefinition.create(var0, 256, 256);
    }

    @OnlyIn(Dist.CLIENT)
    public static class DragonModel extends EntityModel<EnderDragon> {
        private final ModelPart head;
        private final ModelPart neck;
        private final ModelPart jaw;
        private final ModelPart body;
        private final ModelPart leftWing;
        private final ModelPart leftWingTip;
        private final ModelPart leftFrontLeg;
        private final ModelPart leftFrontLegTip;
        private final ModelPart leftFrontFoot;
        private final ModelPart leftRearLeg;
        private final ModelPart leftRearLegTip;
        private final ModelPart leftRearFoot;
        private final ModelPart rightWing;
        private final ModelPart rightWingTip;
        private final ModelPart rightFrontLeg;
        private final ModelPart rightFrontLegTip;
        private final ModelPart rightFrontFoot;
        private final ModelPart rightRearLeg;
        private final ModelPart rightRearLegTip;
        private final ModelPart rightRearFoot;
        @Nullable
        private EnderDragon entity;
        private float a;

        public DragonModel(ModelPart param0) {
            this.head = param0.getChild("head");
            this.jaw = this.head.getChild("jaw");
            this.neck = param0.getChild("neck");
            this.body = param0.getChild("body");
            this.leftWing = param0.getChild("left_wing");
            this.leftWingTip = this.leftWing.getChild("left_wing_tip");
            this.leftFrontLeg = param0.getChild("left_front_leg");
            this.leftFrontLegTip = this.leftFrontLeg.getChild("left_front_leg_tip");
            this.leftFrontFoot = this.leftFrontLegTip.getChild("left_front_foot");
            this.leftRearLeg = param0.getChild("left_hind_leg");
            this.leftRearLegTip = this.leftRearLeg.getChild("left_hind_leg_tip");
            this.leftRearFoot = this.leftRearLegTip.getChild("left_hind_foot");
            this.rightWing = param0.getChild("right_wing");
            this.rightWingTip = this.rightWing.getChild("right_wing_tip");
            this.rightFrontLeg = param0.getChild("right_front_leg");
            this.rightFrontLegTip = this.rightFrontLeg.getChild("right_front_leg_tip");
            this.rightFrontFoot = this.rightFrontLegTip.getChild("right_front_foot");
            this.rightRearLeg = param0.getChild("right_hind_leg");
            this.rightRearLegTip = this.rightRearLeg.getChild("right_hind_leg_tip");
            this.rightRearFoot = this.rightRearLegTip.getChild("right_hind_foot");
        }

        public void prepareMobModel(EnderDragon param0, float param1, float param2, float param3) {
            this.entity = param0;
            this.a = param3;
        }

        public void setupAnim(EnderDragon param0, float param1, float param2, float param3, float param4, float param5) {
        }

        @Override
        public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
            param0.pushPose();
            float var0 = Mth.lerp(this.a, this.entity.oFlapTime, this.entity.flapTime);
            this.jaw.xRot = (float)(Math.sin((double)(var0 * (float) (Math.PI * 2))) + 1.0) * 0.2F;
            float var1 = (float)(Math.sin((double)(var0 * (float) (Math.PI * 2) - 1.0F)) + 1.0);
            var1 = (var1 * var1 + var1 * 2.0F) * 0.05F;
            param0.translate(0.0, (double)(var1 - 2.0F), -3.0);
            param0.mulPose(Vector3f.XP.rotationDegrees(var1 * 2.0F));
            float var2 = 0.0F;
            float var3 = 20.0F;
            float var4 = -12.0F;
            float var5 = 1.5F;
            double[] var6 = this.entity.getLatencyPos(6, this.a);
            float var7 = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]);
            float var8 = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] + (double)(var7 / 2.0F));
            float var9 = var0 * (float) (Math.PI * 2);

            for(int var10 = 0; var10 < 5; ++var10) {
                double[] var11 = this.entity.getLatencyPos(5 - var10, this.a);
                float var12 = (float)Math.cos((double)((float)var10 * 0.45F + var9)) * 0.15F;
                this.neck.yRot = Mth.rotWrap(var11[0] - var6[0]) * (float) (Math.PI / 180.0) * 1.5F;
                this.neck.xRot = var12 + this.entity.getHeadPartYOffset(var10, var6, var11) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
                this.neck.zRot = -Mth.rotWrap(var11[0] - (double)var8) * (float) (Math.PI / 180.0) * 1.5F;
                this.neck.y = var3;
                this.neck.z = var4;
                this.neck.x = var2;
                var3 = (float)((double)var3 + Math.sin((double)this.neck.xRot) * 10.0);
                var4 = (float)((double)var4 - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
                var2 = (float)((double)var2 - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
                this.neck.render(param0, param1, param2, param3, 1.0F, 1.0F, 1.0F, param7);
            }

            this.head.y = var3;
            this.head.z = var4;
            this.head.x = var2;
            double[] var13 = this.entity.getLatencyPos(0, this.a);
            this.head.yRot = Mth.rotWrap(var13[0] - var6[0]) * (float) (Math.PI / 180.0);
            this.head.xRot = Mth.rotWrap((double)this.entity.getHeadPartYOffset(6, var6, var13)) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
            this.head.zRot = -Mth.rotWrap(var13[0] - (double)var8) * (float) (Math.PI / 180.0);
            this.head.render(param0, param1, param2, param3, 1.0F, 1.0F, 1.0F, param7);
            param0.pushPose();
            param0.translate(0.0, 1.0, 0.0);
            param0.mulPose(Vector3f.ZP.rotationDegrees(-var7 * 1.5F));
            param0.translate(0.0, -1.0, 0.0);
            this.body.zRot = 0.0F;
            this.body.render(param0, param1, param2, param3, 1.0F, 1.0F, 1.0F, param7);
            float var14 = var0 * (float) (Math.PI * 2);
            this.leftWing.xRot = 0.125F - (float)Math.cos((double)var14) * 0.2F;
            this.leftWing.yRot = -0.25F;
            this.leftWing.zRot = -((float)(Math.sin((double)var14) + 0.125)) * 0.8F;
            this.leftWingTip.zRot = (float)(Math.sin((double)(var14 + 2.0F)) + 0.5) * 0.75F;
            this.rightWing.xRot = this.leftWing.xRot;
            this.rightWing.yRot = -this.leftWing.yRot;
            this.rightWing.zRot = -this.leftWing.zRot;
            this.rightWingTip.zRot = -this.leftWingTip.zRot;
            this.renderSide(
                param0,
                param1,
                param2,
                param3,
                var1,
                this.leftWing,
                this.leftFrontLeg,
                this.leftFrontLegTip,
                this.leftFrontFoot,
                this.leftRearLeg,
                this.leftRearLegTip,
                this.leftRearFoot,
                param7
            );
            this.renderSide(
                param0,
                param1,
                param2,
                param3,
                var1,
                this.rightWing,
                this.rightFrontLeg,
                this.rightFrontLegTip,
                this.rightFrontFoot,
                this.rightRearLeg,
                this.rightRearLegTip,
                this.rightRearFoot,
                param7
            );
            param0.popPose();
            float var15 = -((float)Math.sin((double)(var0 * (float) (Math.PI * 2)))) * 0.0F;
            var9 = var0 * (float) (Math.PI * 2);
            var3 = 10.0F;
            var4 = 60.0F;
            var2 = 0.0F;
            var6 = this.entity.getLatencyPos(11, this.a);

            for(int var16 = 0; var16 < 12; ++var16) {
                var13 = this.entity.getLatencyPos(12 + var16, this.a);
                var15 = (float)((double)var15 + Math.sin((double)((float)var16 * 0.45F + var9)) * 0.05F);
                this.neck.yRot = (Mth.rotWrap(var13[0] - var6[0]) * 1.5F + 180.0F) * (float) (Math.PI / 180.0);
                this.neck.xRot = var15 + (float)(var13[1] - var6[1]) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
                this.neck.zRot = Mth.rotWrap(var13[0] - (double)var8) * (float) (Math.PI / 180.0) * 1.5F;
                this.neck.y = var3;
                this.neck.z = var4;
                this.neck.x = var2;
                var3 = (float)((double)var3 + Math.sin((double)this.neck.xRot) * 10.0);
                var4 = (float)((double)var4 - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
                var2 = (float)((double)var2 - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
                this.neck.render(param0, param1, param2, param3, 1.0F, 1.0F, 1.0F, param7);
            }

            param0.popPose();
        }

        private void renderSide(
            PoseStack param0,
            VertexConsumer param1,
            int param2,
            int param3,
            float param4,
            ModelPart param5,
            ModelPart param6,
            ModelPart param7,
            ModelPart param8,
            ModelPart param9,
            ModelPart param10,
            ModelPart param11,
            float param12
        ) {
            param9.xRot = 1.0F + param4 * 0.1F;
            param10.xRot = 0.5F + param4 * 0.1F;
            param11.xRot = 0.75F + param4 * 0.1F;
            param6.xRot = 1.3F + param4 * 0.1F;
            param7.xRot = -0.5F - param4 * 0.1F;
            param8.xRot = 0.75F + param4 * 0.1F;
            param5.render(param0, param1, param2, param3, 1.0F, 1.0F, 1.0F, param12);
            param6.render(param0, param1, param2, param3, 1.0F, 1.0F, 1.0F, param12);
            param9.render(param0, param1, param2, param3, 1.0F, 1.0F, 1.0F, param12);
        }
    }
}
