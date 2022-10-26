package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class EndCrystalRenderer extends EntityRenderer<EndCrystal> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    private static final float SIN_45 = (float)Math.sin(Math.PI / 4);
    private static final String GLASS = "glass";
    private static final String BASE = "base";
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public EndCrystalRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.shadowRadius = 0.5F;
        ModelPart var0 = param0.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = var0.getChild("glass");
        this.cube = var0.getChild("cube");
        this.base = var0.getChild("base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        var1.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        var1.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
        return LayerDefinition.create(var0, 64, 32);
    }

    public void render(EndCrystal param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        float var0 = getY(param0, param2);
        float var1 = ((float)param0.time + param2) * 3.0F;
        VertexConsumer var2 = param4.getBuffer(RENDER_TYPE);
        param3.pushPose();
        param3.scale(2.0F, 2.0F, 2.0F);
        param3.translate(0.0F, -0.5F, 0.0F);
        int var3 = OverlayTexture.NO_OVERLAY;
        if (param0.showsBottom()) {
            this.base.render(param3, var2, param5, var3);
        }

        param3.mulPose(Axis.YP.rotationDegrees(var1));
        param3.translate(0.0F, 1.5F + var0 / 2.0F, 0.0F);
        param3.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
        this.glass.render(param3, var2, param5, var3);
        float var4 = 0.875F;
        param3.scale(0.875F, 0.875F, 0.875F);
        param3.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
        param3.mulPose(Axis.YP.rotationDegrees(var1));
        this.glass.render(param3, var2, param5, var3);
        param3.scale(0.875F, 0.875F, 0.875F);
        param3.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
        param3.mulPose(Axis.YP.rotationDegrees(var1));
        this.cube.render(param3, var2, param5, var3);
        param3.popPose();
        param3.popPose();
        BlockPos var5 = param0.getBeamTarget();
        if (var5 != null) {
            float var6 = (float)var5.getX() + 0.5F;
            float var7 = (float)var5.getY() + 0.5F;
            float var8 = (float)var5.getZ() + 0.5F;
            float var9 = (float)((double)var6 - param0.getX());
            float var10 = (float)((double)var7 - param0.getY());
            float var11 = (float)((double)var8 - param0.getZ());
            param3.translate(var9, var10, var11);
            EnderDragonRenderer.renderCrystalBeams(-var9, -var10 + var0, -var11, param2, param0.time, param3, param4, param5);
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    public static float getY(EndCrystal param0, float param1) {
        float var0 = (float)param0.time + param1;
        float var1 = Mth.sin(var0 * 0.2F) / 2.0F + 0.5F;
        var1 = (var1 * var1 + var1) * 0.4F;
        return var1 - 1.4F;
    }

    public ResourceLocation getTextureLocation(EndCrystal param0) {
        return END_CRYSTAL_LOCATION;
    }

    public boolean shouldRender(EndCrystal param0, Frustum param1, double param2, double param3, double param4) {
        return super.shouldRender(param0, param1, param2, param3, param4) || param0.getBeamTarget() != null;
    }
}
