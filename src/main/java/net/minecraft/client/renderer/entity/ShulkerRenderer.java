package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.culling.Culler;
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
    public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/shulker.png");
    public static final ResourceLocation[] TEXTURE_LOCATION = new ResourceLocation[]{
        new ResourceLocation("textures/entity/shulker/shulker_white.png"),
        new ResourceLocation("textures/entity/shulker/shulker_orange.png"),
        new ResourceLocation("textures/entity/shulker/shulker_magenta.png"),
        new ResourceLocation("textures/entity/shulker/shulker_light_blue.png"),
        new ResourceLocation("textures/entity/shulker/shulker_yellow.png"),
        new ResourceLocation("textures/entity/shulker/shulker_lime.png"),
        new ResourceLocation("textures/entity/shulker/shulker_pink.png"),
        new ResourceLocation("textures/entity/shulker/shulker_gray.png"),
        new ResourceLocation("textures/entity/shulker/shulker_light_gray.png"),
        new ResourceLocation("textures/entity/shulker/shulker_cyan.png"),
        new ResourceLocation("textures/entity/shulker/shulker_purple.png"),
        new ResourceLocation("textures/entity/shulker/shulker_blue.png"),
        new ResourceLocation("textures/entity/shulker/shulker_brown.png"),
        new ResourceLocation("textures/entity/shulker/shulker_green.png"),
        new ResourceLocation("textures/entity/shulker/shulker_red.png"),
        new ResourceLocation("textures/entity/shulker/shulker_black.png")
    };

    public ShulkerRenderer(EntityRenderDispatcher param0) {
        super(param0, new ShulkerModel<>(), 0.0F);
        this.addLayer(new ShulkerHeadLayer(this));
    }

    public void render(Shulker param0, double param1, double param2, double param3, float param4, float param5) {
        int var0 = param0.getClientSideTeleportInterpolation();
        if (var0 > 0 && param0.hasValidInterpolationPositions()) {
            BlockPos var1 = param0.getAttachPosition();
            BlockPos var2 = param0.getOldAttachPosition();
            double var3 = (double)((float)var0 - param5) / 6.0;
            var3 *= var3;
            double var4 = (double)(var1.getX() - var2.getX()) * var3;
            double var5 = (double)(var1.getY() - var2.getY()) * var3;
            double var6 = (double)(var1.getZ() - var2.getZ()) * var3;
            super.render(param0, param1 - var4, param2 - var5, param3 - var6, param4, param5);
        } else {
            super.render(param0, param1, param2, param3, param4, param5);
        }

    }

    public boolean shouldRender(Shulker param0, Culler param1, double param2, double param3, double param4) {
        if (super.shouldRender(param0, param1, param2, param3, param4)) {
            return true;
        } else {
            if (param0.getClientSideTeleportInterpolation() > 0 && param0.hasValidInterpolationPositions()) {
                BlockPos var0 = param0.getOldAttachPosition();
                BlockPos var1 = param0.getAttachPosition();
                Vec3 var2 = new Vec3((double)var1.getX(), (double)var1.getY(), (double)var1.getZ());
                Vec3 var3 = new Vec3((double)var0.getX(), (double)var0.getY(), (double)var0.getZ());
                if (param1.isVisible(new AABB(var3.x, var3.y, var3.z, var2.x, var2.y, var2.z))) {
                    return true;
                }
            }

            return false;
        }
    }

    protected ResourceLocation getTextureLocation(Shulker param0) {
        return param0.getColor() == null ? DEFAULT_TEXTURE_LOCATION : TEXTURE_LOCATION[param0.getColor().getId()];
    }

    protected void setupRotations(Shulker param0, float param1, float param2, float param3) {
        super.setupRotations(param0, param1, param2, param3);
        switch(param0.getAttachFace()) {
            case DOWN:
            default:
                break;
            case EAST:
                RenderSystem.translatef(0.5F, 0.5F, 0.0F);
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
                break;
            case WEST:
                RenderSystem.translatef(-0.5F, 0.5F, 0.0F);
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                break;
            case NORTH:
                RenderSystem.translatef(0.0F, 0.5F, -0.5F);
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case SOUTH:
                RenderSystem.translatef(0.0F, 0.5F, 0.5F);
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
                break;
            case UP:
                RenderSystem.translatef(0.0F, 1.0F, 0.0F);
                RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
        }

    }

    protected void scale(Shulker param0, float param1) {
        float var0 = 0.999F;
        RenderSystem.scalef(0.999F, 0.999F, 0.999F);
    }
}
