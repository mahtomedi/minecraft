package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DragonFireballRenderer extends EntityRenderer<DragonFireball> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");

    public DragonFireballRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(DragonFireball param0, double param1, double param2, double param3, float param4, float param5) {
        RenderSystem.pushMatrix();
        this.bindTexture(param0);
        RenderSystem.translatef((float)param1, (float)param2, (float)param3);
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(2.0F, 2.0F, 2.0F);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        float var2 = 1.0F;
        float var3 = 0.5F;
        float var4 = 0.25F;
        RenderSystem.rotatef(180.0F - this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(
            (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F
        );
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        var1.begin(7, DefaultVertexFormat.POSITION_TEX_NORMAL);
        var1.vertex(-0.5, -0.25, 0.0).uv(0.0, 1.0).normal(0.0F, 1.0F, 0.0F).endVertex();
        var1.vertex(0.5, -0.25, 0.0).uv(1.0, 1.0).normal(0.0F, 1.0F, 0.0F).endVertex();
        var1.vertex(0.5, 0.75, 0.0).uv(1.0, 0.0).normal(0.0F, 1.0F, 0.0F).endVertex();
        var1.vertex(-0.5, 0.75, 0.0).uv(0.0, 0.0).normal(0.0F, 1.0F, 0.0F).endVertex();
        var0.end();
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(DragonFireball param0) {
        return TEXTURE_LOCATION;
    }
}
