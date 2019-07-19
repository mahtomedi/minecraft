package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBulletRenderer extends EntityRenderer<ShulkerBullet> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
    private final ShulkerBulletModel<ShulkerBullet> model = new ShulkerBulletModel<>();

    public ShulkerBulletRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    private float rotlerp(float param0, float param1, float param2) {
        float var0 = param1 - param0;

        while(var0 < -180.0F) {
            var0 += 360.0F;
        }

        while(var0 >= 180.0F) {
            var0 -= 360.0F;
        }

        return param0 + param2 * var0;
    }

    public void render(ShulkerBullet param0, double param1, double param2, double param3, float param4, float param5) {
        GlStateManager.pushMatrix();
        float var0 = this.rotlerp(param0.yRotO, param0.yRot, param5);
        float var1 = Mth.lerp(param5, param0.xRotO, param0.xRot);
        float var2 = (float)param0.tickCount + param5;
        GlStateManager.translatef((float)param1, (float)param2 + 0.15F, (float)param3);
        GlStateManager.rotatef(Mth.sin(var2 * 0.1F) * 180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(Mth.cos(var2 * 0.1F) * 180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(Mth.sin(var2 * 0.15F) * 360.0F, 0.0F, 0.0F, 1.0F);
        float var3 = 0.03125F;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
        this.bindTexture(param0);
        this.model.render(param0, 0.0F, 0.0F, 0.0F, var0, var1, 0.03125F);
        GlStateManager.enableBlend();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.scalef(1.5F, 1.5F, 1.5F);
        this.model.render(param0, 0.0F, 0.0F, 0.0F, var0, var1, 0.03125F);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(ShulkerBullet param0) {
        return TEXTURE_LOCATION;
    }
}
