package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownTridentRenderer extends EntityRenderer<ThrownTrident> {
    public static final ResourceLocation TRIDENT_LOCATION = new ResourceLocation("textures/entity/trident.png");
    private final TridentModel model = new TridentModel();

    public ThrownTridentRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(
        ThrownTrident param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        param6.pushPose();
        param6.mulPose(Vector3f.YP.rotation(Mth.lerp(param5, param0.yRotO, param0.yRot) - 90.0F, true));
        param6.mulPose(Vector3f.ZP.rotation(Mth.lerp(param5, param0.xRotO, param0.xRot) + 90.0F, true));
        int var0 = param0.getLightColor();
        VertexConsumer var1 = param7.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(param0)));
        OverlayTexture.setDefault(var1);
        this.model.render(param6, var1, var0);
        var1.unsetDefaultOverlayCoords();
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(ThrownTrident param0) {
        return TRIDENT_LOCATION;
    }
}
