package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity> {
    private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
    private final LeashKnotModel<LeashFenceKnotEntity> model = new LeashKnotModel<>();

    public LeashKnotRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(LeashFenceKnotEntity param0, double param1, double param2, double param3, float param4, float param5) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.translatef((float)param1, (float)param2, (float)param3);
        float var0 = 0.0625F;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
        GlStateManager.enableAlphaTest();
        this.bindTexture(param0);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        this.model.render(param0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(LeashFenceKnotEntity param0) {
        return KNOT_LOCATION;
    }
}
