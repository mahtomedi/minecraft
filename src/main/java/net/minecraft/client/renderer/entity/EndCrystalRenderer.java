package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelPart;
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

@OnlyIn(Dist.CLIENT)
public class EndCrystalRenderer extends EntityRenderer<EndCrystal> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
    public static final float SIN_45 = (float)Math.sin(Math.PI / 4);
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public EndCrystalRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.5F;
        this.glass = new ModelPart(64, 32, 0, 0);
        this.glass.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
        this.cube = new ModelPart(64, 32, 32, 0);
        this.cube.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
        this.base = new ModelPart(64, 32, 0, 16);
        this.base.addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F);
    }

    public void render(EndCrystal param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        param6.pushPose();
        float var0 = getY(param0, param5);
        float var1 = 0.0625F;
        float var2 = ((float)param0.time + param5) * 3.0F;
        int var3 = param0.getLightColor();
        VertexConsumer var4 = param7.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(param0)));
        OverlayTexture.setDefault(var4);
        param6.pushPose();
        param6.scale(2.0F, 2.0F, 2.0F);
        param6.translate(0.0, -0.5, 0.0);
        if (param0.showsBottom()) {
            this.base.render(param6, var4, 0.0625F, var3, null);
        }

        param6.mulPose(Vector3f.YP.rotation(var2, true));
        param6.translate(0.0, (double)(1.5F + var0 / 2.0F), 0.0);
        param6.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        this.glass.render(param6, var4, 0.0625F, var3, null);
        float var5 = 0.875F;
        param6.scale(0.875F, 0.875F, 0.875F);
        param6.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        param6.mulPose(Vector3f.YP.rotation(var2, true));
        this.glass.render(param6, var4, 0.0625F, var3, null);
        param6.scale(0.875F, 0.875F, 0.875F);
        param6.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        param6.mulPose(Vector3f.YP.rotation(var2, true));
        this.cube.render(param6, var4, 0.0625F, var3, null);
        param6.popPose();
        param6.popPose();
        var4.unsetDefaultOverlayCoords();
        BlockPos var6 = param0.getBeamTarget();
        if (var6 != null) {
            float var7 = (float)var6.getX() + 0.5F;
            float var8 = (float)var6.getY() + 0.5F;
            float var9 = (float)var6.getZ() + 0.5F;
            float var10 = (float)((double)var7 - param0.x);
            float var11 = (float)((double)var8 - param0.y);
            float var12 = (float)((double)var9 - param0.z);
            param6.translate((double)var10, (double)var11, (double)var12);
            EnderDragonRenderer.renderCrystalBeams(-var10, -var11 + var0, -var12, param5, param0.time, param6, param7, var3);
        }

        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
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
