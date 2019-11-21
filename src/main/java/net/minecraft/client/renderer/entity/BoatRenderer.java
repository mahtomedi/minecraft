package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoatRenderer extends EntityRenderer<Boat> {
    private static final ResourceLocation[] BOAT_TEXTURE_LOCATIONS = new ResourceLocation[]{
        new ResourceLocation("textures/entity/boat/oak.png"),
        new ResourceLocation("textures/entity/boat/spruce.png"),
        new ResourceLocation("textures/entity/boat/birch.png"),
        new ResourceLocation("textures/entity/boat/jungle.png"),
        new ResourceLocation("textures/entity/boat/acacia.png"),
        new ResourceLocation("textures/entity/boat/dark_oak.png")
    };
    protected final BoatModel model = new BoatModel();

    public BoatRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.8F;
    }

    public void render(Boat param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.translate(0.0, 0.375, 0.0);
        param3.mulPose(Vector3f.YP.rotationDegrees(180.0F - param1));
        float var0 = (float)param0.getHurtTime() - param2;
        float var1 = param0.getDamage() - param2;
        if (var1 < 0.0F) {
            var1 = 0.0F;
        }

        if (var0 > 0.0F) {
            param3.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(var0) * var0 * var1 / 10.0F * (float)param0.getHurtDir()));
        }

        float var2 = param0.getBubbleAngle(param2);
        if (!Mth.equal(var2, 0.0F)) {
            param3.mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 1.0F), param0.getBubbleAngle(param2), true));
        }

        param3.scale(-1.0F, -1.0F, 1.0F);
        param3.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        this.model.setupAnim(param0, param2, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer var3 = param4.getBuffer(this.model.renderType(this.getTextureLocation(param0)));
        this.model.renderToBuffer(param3, var3, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        VertexConsumer var4 = param4.getBuffer(RenderType.waterMask());
        this.model.waterPatch().render(param3, var4, param5, OverlayTexture.NO_OVERLAY);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(Boat param0) {
        return BOAT_TEXTURE_LOCATIONS[param0.getBoatType().ordinal()];
    }
}
