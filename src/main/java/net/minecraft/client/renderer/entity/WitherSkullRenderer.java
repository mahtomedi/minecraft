package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherSkullRenderer extends EntityRenderer<WitherSkull> {
    private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");
    private final SkullModel model = new SkullModel();

    public WitherSkullRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(WitherSkull param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        param6.pushPose();
        float var0 = 0.0625F;
        param6.scale(-1.0F, -1.0F, 1.0F);
        float var1 = Mth.rotlerp(param0.yRotO, param0.yRot, param5);
        float var2 = Mth.lerp(param5, param0.xRotO, param0.xRot);
        int var3 = param0.getLightColor();
        VertexConsumer var4 = param7.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(param0)));
        OverlayTexture.setDefault(var4);
        this.model.render(param6, var4, 0.0F, var1, var2, 0.0625F, var3);
        var4.unsetDefaultOverlayCoords();
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(WitherSkull param0) {
        return param0.isDangerous() ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }
}
