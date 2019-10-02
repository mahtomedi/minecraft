package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeStingerLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
    private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

    public BeeStingerLayer(LivingEntityRenderer<T, M> param0) {
        super(param0);
    }

    @Override
    protected int numStuck(T param0) {
        return param0.getStingerCount();
    }

    @Override
    protected void renderStuckItem(PoseStack param0, MultiBufferSource param1, Entity param2, float param3, float param4, float param5, float param6) {
        float var0 = Mth.sqrt(param3 * param3 + param5 * param5);
        float var1 = (float)(Math.atan2((double)param3, (double)param5) * 180.0F / (float)Math.PI);
        float var2 = (float)(Math.atan2((double)param4, (double)var0) * 180.0F / (float)Math.PI);
        param0.translate(0.0, 0.0, 0.0);
        param0.mulPose(Vector3f.YP.rotation(var1 - 90.0F, true));
        param0.mulPose(Vector3f.ZP.rotation(var2, true));
        float var3 = 0.0F;
        float var4 = 0.125F;
        float var5 = 0.0F;
        float var6 = 0.0625F;
        float var7 = 0.03125F;
        param0.mulPose(Vector3f.XP.rotation(45.0F, true));
        param0.scale(0.03125F, 0.03125F, 0.03125F);
        param0.translate(2.5, 0.0, 0.0);
        int var8 = param2.getLightColor();
        VertexConsumer var9 = param1.getBuffer(RenderType.NEW_ENTITY(BEE_STINGER_LOCATION));
        OverlayTexture.setDefault(var9);

        for(int var10 = 0; var10 < 4; ++var10) {
            param0.mulPose(Vector3f.XP.rotation(90.0F, true));
            Matrix4f var11 = param0.getPose();
            vertex(var9, var11, -4.5F, -1, 0.0F, 0.0F, var8);
            vertex(var9, var11, 4.5F, -1, 0.125F, 0.0F, var8);
            vertex(var9, var11, 4.5F, 1, 0.125F, 0.0625F, var8);
            vertex(var9, var11, -4.5F, 1, 0.0F, 0.0625F, var8);
        }

        var9.unsetDefaultOverlayCoords();
    }

    private static void vertex(VertexConsumer param0, Matrix4f param1, float param2, int param3, float param4, float param5, int param6) {
        param0.vertex(param1, param2, (float)param3, 0.0F).color(255, 255, 255, 255).uv(param4, param5).uv2(param6).normal(0.0F, 1.0F, 0.0F).endVertex();
    }
}
