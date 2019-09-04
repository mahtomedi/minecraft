package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.dragon.EndCrystalModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndCrystalRenderer extends EntityRenderer<EndCrystal> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
    private final EntityModel<EndCrystal> model = new EndCrystalModel<>(0.0F, true);
    private final EntityModel<EndCrystal> modelWithoutBottom = new EndCrystalModel<>(0.0F, false);

    public EndCrystalRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.5F;
    }

    public void render(EndCrystal param0, double param1, double param2, double param3, float param4, float param5) {
        float var0 = (float)param0.time + param5;
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)param1, (float)param2, (float)param3);
        this.bindTexture(END_CRYSTAL_LOCATION);
        float var1 = Mth.sin(var0 * 0.2F) / 2.0F + 0.5F;
        var1 = var1 * var1 + var1;
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        if (param0.showsBottom()) {
            this.model.render(param0, 0.0F, var0 * 3.0F, var1 * 0.2F, 0.0F, 0.0F, 0.0625F);
        } else {
            this.modelWithoutBottom.render(param0, 0.0F, var0 * 3.0F, var1 * 0.2F, 0.0F, 0.0F, 0.0625F);
        }

        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        RenderSystem.popMatrix();
        BlockPos var2 = param0.getBeamTarget();
        if (var2 != null) {
            this.bindTexture(EnderDragonRenderer.CRYSTAL_BEAM_LOCATION);
            float var3 = (float)var2.getX() + 0.5F;
            float var4 = (float)var2.getY() + 0.5F;
            float var5 = (float)var2.getZ() + 0.5F;
            double var6 = (double)var3 - param0.x;
            double var7 = (double)var4 - param0.y;
            double var8 = (double)var5 - param0.z;
            EnderDragonRenderer.renderCrystalBeams(
                param1 + var6,
                param2 - 0.3 + (double)(var1 * 0.4F) + var7,
                param3 + var8,
                param5,
                (double)var3,
                (double)var4,
                (double)var5,
                param0.time,
                param0.x,
                param0.y,
                param0.z
            );
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(EndCrystal param0) {
        return END_CRYSTAL_LOCATION;
    }

    public boolean shouldRender(EndCrystal param0, Culler param1, double param2, double param3, double param4) {
        return super.shouldRender(param0, param1, param2, param3, param4) || param0.getBeamTarget() != null;
    }
}
