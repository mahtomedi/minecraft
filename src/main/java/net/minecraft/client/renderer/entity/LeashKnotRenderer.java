package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
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
        RenderSystem.pushMatrix();
        RenderSystem.disableCull();
        RenderSystem.translatef((float)param1, (float)param2, (float)param3);
        float var0 = 0.0625F;
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
        this.bindTexture(param0);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        this.model.render(param0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        RenderSystem.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(LeashFenceKnotEntity param0) {
        return KNOT_LOCATION;
    }
}
