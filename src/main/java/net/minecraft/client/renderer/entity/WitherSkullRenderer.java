package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.SkullModel;
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

    public void render(WitherSkull param0, double param1, double param2, double param3, float param4, float param5) {
        RenderSystem.pushMatrix();
        RenderSystem.disableCull();
        float var0 = this.rotlerp(param0.yRotO, param0.yRot, param5);
        float var1 = Mth.lerp(param5, param0.xRotO, param0.xRot);
        RenderSystem.translatef((float)param1, (float)param2, (float)param3);
        float var2 = 0.0625F;
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
        this.bindTexture(param0);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        this.model.render(0.0F, 0.0F, 0.0F, var0, var1, 0.0625F);
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        RenderSystem.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(WitherSkull param0) {
        return param0.isDangerous() ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }
}
