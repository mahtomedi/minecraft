package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConduitRenderer extends BlockEntityRenderer<ConduitBlockEntity> {
    public static final ResourceLocation SHELL_TEXTURE = new ResourceLocation("entity/conduit/base");
    public static final ResourceLocation ACTIVE_SHELL_TEXTURE = new ResourceLocation("entity/conduit/cage");
    public static final ResourceLocation WIND_TEXTURE = new ResourceLocation("entity/conduit/wind");
    public static final ResourceLocation VERTICAL_WIND_TEXTURE = new ResourceLocation("entity/conduit/wind_vertical");
    public static final ResourceLocation OPEN_EYE_TEXTURE = new ResourceLocation("entity/conduit/open_eye");
    public static final ResourceLocation CLOSED_EYE_TEXTURE = new ResourceLocation("entity/conduit/closed_eye");
    private final ModelPart eye = new ModelPart(16, 16, 0, 0);
    private final ModelPart wind;
    private final ModelPart shell;
    private final ModelPart cage;

    public ConduitRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
        this.eye.addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, 0.01F);
        this.wind = new ModelPart(64, 32, 0, 0);
        this.wind.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
        this.shell = new ModelPart(32, 16, 0, 0);
        this.shell.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);
        this.cage = new ModelPart(32, 16, 0, 0);
        this.cage.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
    }

    public void render(ConduitBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        float var0 = (float)param0.tickCount + param1;
        if (!param0.isActive()) {
            float var1 = param0.getActiveRotation(0.0F);
            VertexConsumer var2 = param3.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
            param2.pushPose();
            param2.translate(0.5, 0.5, 0.5);
            param2.mulPose(Vector3f.YP.rotationDegrees(var1));
            this.shell.render(param2, var2, param4, param5, this.getSprite(SHELL_TEXTURE));
            param2.popPose();
        } else {
            VertexConsumer var3 = param3.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
            float var4 = param0.getActiveRotation(param1) * (180.0F / (float)Math.PI);
            float var5 = Mth.sin(var0 * 0.1F) / 2.0F + 0.5F;
            var5 = var5 * var5 + var5;
            param2.pushPose();
            param2.translate(0.5, (double)(0.3F + var5 * 0.2F), 0.5);
            Vector3f var6 = new Vector3f(0.5F, 1.0F, 0.5F);
            var6.normalize();
            param2.mulPose(new Quaternion(var6, var4, true));
            this.cage.render(param2, var3, param4, param5, this.getSprite(ACTIVE_SHELL_TEXTURE));
            param2.popPose();
            int var7 = param0.tickCount / 66 % 3;
            param2.pushPose();
            param2.translate(0.5, 0.5, 0.5);
            if (var7 == 1) {
                param2.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            } else if (var7 == 2) {
                param2.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            }

            TextureAtlasSprite var8 = this.getSprite(var7 == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE);
            this.wind.render(param2, var3, param4, param5, var8);
            param2.popPose();
            param2.pushPose();
            param2.translate(0.5, 0.5, 0.5);
            param2.scale(0.875F, 0.875F, 0.875F);
            param2.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            param2.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            this.wind.render(param2, var3, param4, param5, var8);
            param2.popPose();
            Camera var9 = this.renderer.camera;
            param2.pushPose();
            param2.translate(0.5, (double)(0.3F + var5 * 0.2F), 0.5);
            param2.scale(0.5F, 0.5F, 0.5F);
            float var10 = -var9.getYRot();
            param2.mulPose(Vector3f.YP.rotationDegrees(var10));
            param2.mulPose(Vector3f.XP.rotationDegrees(var9.getXRot()));
            param2.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            float var11 = 1.3333334F;
            param2.scale(1.3333334F, 1.3333334F, 1.3333334F);
            this.eye.render(param2, var3, param4, param5, this.getSprite(param0.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE));
            param2.popPose();
        }
    }
}
