package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.BoatModel;
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

    public void render(Boat param0, double param1, double param2, double param3, float param4, float param5) {
        GlStateManager.pushMatrix();
        this.setupTranslation(param1, param2, param3);
        this.setupRotation(param0, param4, param5);
        this.bindTexture(param0);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        this.model.render(param0, param5, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public void setupRotation(Boat param0, float param1, float param2) {
        GlStateManager.rotatef(180.0F - param1, 0.0F, 1.0F, 0.0F);
        float var0 = (float)param0.getHurtTime() - param2;
        float var1 = param0.getDamage() - param2;
        if (var1 < 0.0F) {
            var1 = 0.0F;
        }

        if (var0 > 0.0F) {
            GlStateManager.rotatef(Mth.sin(var0) * var0 * var1 / 10.0F * (float)param0.getHurtDir(), 1.0F, 0.0F, 0.0F);
        }

        float var2 = param0.getBubbleAngle(param2);
        if (!Mth.equal(var2, 0.0F)) {
            GlStateManager.rotatef(param0.getBubbleAngle(param2), 1.0F, 0.0F, 1.0F);
        }

        GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
    }

    public void setupTranslation(double param0, double param1, double param2) {
        GlStateManager.translatef((float)param0, (float)param1 + 0.375F, (float)param2);
    }

    protected ResourceLocation getTextureLocation(Boat param0) {
        return BOAT_TEXTURE_LOCATIONS[param0.getBoatType().ordinal()];
    }

    @Override
    public boolean hasSecondPass() {
        return true;
    }

    public void renderSecondPass(Boat param0, double param1, double param2, double param3, float param4, float param5) {
        GlStateManager.pushMatrix();
        this.setupTranslation(param1, param2, param3);
        this.setupRotation(param0, param4, param5);
        this.bindTexture(param0);
        this.model.renderSecondPass(param0, param5, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}
