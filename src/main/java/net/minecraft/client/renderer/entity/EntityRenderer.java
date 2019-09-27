package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    protected final EntityRenderDispatcher entityRenderDispatcher;
    protected float shadowRadius;
    protected float shadowStrength = 1.0F;

    protected EntityRenderer(EntityRenderDispatcher param0) {
        this.entityRenderDispatcher = param0;
    }

    public boolean shouldRender(T param0, Frustum param1, double param2, double param3, double param4) {
        if (!param0.shouldRender(param2, param3, param4)) {
            return false;
        } else if (param0.noCulling) {
            return true;
        } else {
            AABB var0 = param0.getBoundingBoxForCulling().inflate(0.5);
            if (var0.hasNaN() || var0.getSize() == 0.0) {
                var0 = new AABB(param0.x - 2.0, param0.y - 2.0, param0.z - 2.0, param0.x + 2.0, param0.y + 2.0, param0.z + 2.0);
            }

            return param1.isVisible(var0);
        }
    }

    public Vec3 getRenderOffset(T param0, double param1, double param2, double param3, float param4) {
        return Vec3.ZERO;
    }

    public void render(T param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        if (this.shouldShowName(param0)) {
            this.renderNameTag(param0, param0.getDisplayName().getColoredString(), param6, param7);
        }
    }

    protected boolean shouldShowName(T param0) {
        return param0.shouldShowName() && param0.hasCustomName();
    }

    public abstract ResourceLocation getTextureLocation(T var1);

    public Font getFont() {
        return this.entityRenderDispatcher.getFont();
    }

    protected void renderNameTag(T param0, String param1, PoseStack param2, MultiBufferSource param3) {
        double var0 = this.entityRenderDispatcher.distanceToSqr(param0);
        if (!(var0 > 4096.0)) {
            int var1 = param0.getLightColor();
            if (param0.isOnFire()) {
                var1 = 15728880;
            }

            boolean var2 = !param0.isDiscrete();
            float var3 = param0.getBbHeight() + 0.5F;
            int var4 = "deadmau5".equals(param1) ? -10 : 0;
            param2.pushPose();
            param2.translate(0.0, (double)var3, 0.0);
            param2.mulPose(Vector3f.YP.rotation(-this.entityRenderDispatcher.playerRotY, true));
            param2.mulPose(Vector3f.XP.rotation(this.entityRenderDispatcher.playerRotX, true));
            param2.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f var5 = param2.getPose();
            float var6 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int var7 = (int)(var6 * 255.0F) << 24;
            Font var8 = this.getFont();
            float var9 = (float)(-var8.width(param1) / 2);
            var8.drawInBatch(param1, var9, (float)var4, 553648127, false, var5, param3, var2, var7, var1);
            if (var2) {
                var8.drawInBatch(param1, var9, (float)var4, -1, false, var5, param3, false, 0, var1);
            }

            param2.popPose();
        }
    }

    public EntityRenderDispatcher getDispatcher() {
        return this.entityRenderDispatcher;
    }
}
