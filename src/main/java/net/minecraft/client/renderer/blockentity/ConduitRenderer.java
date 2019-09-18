package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConduitRenderer extends BatchedBlockEntityRenderer<ConduitBlockEntity> {
    public static final ResourceLocation SHELL_TEXTURE = new ResourceLocation("entity/conduit/base");
    public static final ResourceLocation ACTIVE_SHELL_TEXTURE = new ResourceLocation("entity/conduit/cage");
    public static final ResourceLocation WIND_TEXTURE = new ResourceLocation("entity/conduit/wind");
    public static final ResourceLocation VERTICAL_WIND_TEXTURE = new ResourceLocation("entity/conduit/wind_vertical");
    public static final ResourceLocation OPEN_EYE_TEXTURE = new ResourceLocation("entity/conduit/open_eye");
    public static final ResourceLocation CLOSED_EYE_TEXTURE = new ResourceLocation("entity/conduit/closed_eye");
    private final ModelPart eye = new ModelPart(8, 8, 0, 0);
    private final ModelPart wind;
    private final ModelPart shell;
    private final ModelPart cage;

    public ConduitRenderer() {
        this.eye.addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, 0.01F);
        this.wind = new ModelPart(64, 32, 0, 0);
        this.wind.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
        this.shell = new ModelPart(32, 16, 0, 0);
        this.shell.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);
        this.cage = new ModelPart(32, 16, 0, 0);
        this.cage.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
    }

    protected void renderToBuffer(
        ConduitBlockEntity param0,
        double param1,
        double param2,
        double param3,
        float param4,
        int param5,
        RenderType param6,
        BufferBuilder param7,
        int param8,
        int param9
    ) {
        float var0 = (float)param0.tickCount + param4;
        if (!param0.isActive()) {
            float var1 = param0.getActiveRotation(0.0F);
            param7.pushPose();
            param7.translate(0.5, 0.5, 0.5);
            param7.multiplyPose(new Quaternion(Vector3f.YP, var1, true));
            this.shell.render(param7, 0.0625F, param8, param9, this.getSprite(SHELL_TEXTURE));
            param7.popPose();
        } else {
            float var2 = param0.getActiveRotation(param4) * (180.0F / (float)Math.PI);
            float var3 = Mth.sin(var0 * 0.1F) / 2.0F + 0.5F;
            var3 = var3 * var3 + var3;
            param7.pushPose();
            param7.translate(0.5, (double)(0.3F + var3 * 0.2F), 0.5);
            Vector3f var4 = new Vector3f(0.5F, 1.0F, 0.5F);
            var4.normalize();
            param7.multiplyPose(new Quaternion(var4, var2, true));
            this.cage.render(param7, 0.0625F, param8, param9, this.getSprite(ACTIVE_SHELL_TEXTURE));
            param7.popPose();
            int var5 = param0.tickCount / 66 % 3;
            param7.pushPose();
            param7.translate(0.5, 0.5, 0.5);
            if (var5 == 1) {
                param7.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
            } else if (var5 == 2) {
                param7.multiplyPose(new Quaternion(Vector3f.ZP, 90.0F, true));
            }

            this.wind.render(param7, 0.0625F, param8, param9, this.getSprite(var5 == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE));
            param7.popPose();
            param7.pushPose();
            param7.translate(0.5, 0.5, 0.5);
            param7.scale(0.875F, 0.875F, 0.875F);
            param7.multiplyPose(new Quaternion(Vector3f.XP, 180.0F, true));
            param7.multiplyPose(new Quaternion(Vector3f.ZP, 180.0F, true));
            this.wind.render(0.0625F);
            param7.popPose();
            Camera var6 = this.blockEntityRenderDispatcher.camera;
            param7.pushPose();
            param7.translate(0.5, (double)(0.3F + var3 * 0.2F), 0.5);
            param7.scale(0.5F, 0.5F, 0.5F);
            param7.multiplyPose(new Quaternion(Vector3f.YP, -var6.getYRot(), true));
            param7.multiplyPose(new Quaternion(Vector3f.XP, var6.getXRot(), true));
            param7.multiplyPose(new Quaternion(Vector3f.ZP, 180.0F, true));
            this.eye.render(param7, 0.083333336F, param9, param8, this.getSprite(param0.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE));
            param7.popPose();
        }
    }
}
