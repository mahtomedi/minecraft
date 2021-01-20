package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerRenderer extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
    public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation(
        "textures/" + Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.texture().getPath() + ".png"
    );
    public static final ResourceLocation[] TEXTURE_LOCATION = Sheets.SHULKER_TEXTURE_LOCATION
        .stream()
        .map(param0 -> new ResourceLocation("textures/" + param0.texture().getPath() + ".png"))
        .toArray(param0 -> new ResourceLocation[param0]);

    public ShulkerRenderer(EntityRendererProvider.Context param0) {
        super(param0, new ShulkerModel<>(param0.bakeLayer(ModelLayers.SHULKER)), 0.0F);
        this.addLayer(new ShulkerHeadLayer(this));
    }

    public Vec3 getRenderOffset(Shulker param0, float param1) {
        return param0.getRenderPosition(param1).orElse(super.getRenderOffset(param0, param1));
    }

    public boolean shouldRender(Shulker param0, Frustum param1, double param2, double param3, double param4) {
        return super.shouldRender(param0, param1, param2, param3, param4)
            ? true
            : param0.getRenderPosition(0.0F)
                .filter(
                    param2x -> {
                        EntityType<?> var0 = param0.getType();
                        float var1x = var0.getHeight() / 2.0F;
                        float var2x = var0.getWidth() / 2.0F;
                        Vec3 var3x = Vec3.atBottomCenterOf(param0.blockPosition());
                        return param1.isVisible(
                            new AABB(param2x.x, param2x.y + (double)var1x, param2x.z, var3x.x, var3x.y + (double)var1x, var3x.z)
                                .inflate((double)var2x, (double)var1x, (double)var2x)
                        );
                    }
                )
                .isPresent();
    }

    public ResourceLocation getTextureLocation(Shulker param0) {
        return param0.getColor() == null ? DEFAULT_TEXTURE_LOCATION : TEXTURE_LOCATION[param0.getColor().getId()];
    }

    protected void setupRotations(Shulker param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3 + 180.0F, param4);
        param1.translate(0.0, 0.5, 0.0);
        param1.mulPose(param0.getAttachFace().getOpposite().getRotation());
        param1.translate(0.0, -0.5, 0.0);
    }
}
