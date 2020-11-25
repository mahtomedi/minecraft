package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
        int var0 = param0.getClientSideTeleportInterpolation();
        if (var0 > 0 && param0.hasValidInterpolationPositions()) {
            BlockPos var1 = param0.getAttachPosition();
            BlockPos var2 = param0.getOldAttachPosition();
            double var3 = (double)((float)var0 - param1) / 6.0;
            var3 *= var3;
            double var4 = (double)(var1.getX() - var2.getX()) * var3;
            double var5 = (double)(var1.getY() - var2.getY()) * var3;
            double var6 = (double)(var1.getZ() - var2.getZ()) * var3;
            return new Vec3(-var4, -var5, -var6);
        } else {
            return super.getRenderOffset(param0, param1);
        }
    }

    public boolean shouldRender(Shulker param0, Frustum param1, double param2, double param3, double param4) {
        if (super.shouldRender(param0, param1, param2, param3, param4)) {
            return true;
        } else {
            if (param0.getClientSideTeleportInterpolation() > 0 && param0.hasValidInterpolationPositions()) {
                Vec3 var0 = Vec3.atLowerCornerOf(param0.getAttachPosition());
                Vec3 var1 = Vec3.atLowerCornerOf(param0.getOldAttachPosition());
                if (param1.isVisible(new AABB(var1.x, var1.y, var1.z, var0.x, var0.y, var0.z))) {
                    return true;
                }
            }

            return false;
        }
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
