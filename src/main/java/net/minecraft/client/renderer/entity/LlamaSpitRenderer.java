package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit> {
    private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
    private final LlamaSpitModel<LlamaSpit> model = new LlamaSpitModel<>();

    public LlamaSpitRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(LlamaSpit param0, double param1, double param2, double param3, float param4, float param5) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)param1, (float)param2 + 0.15F, (float)param3);
        RenderSystem.rotatef(Mth.lerp(param5, param0.yRotO, param0.yRot) - 90.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(Mth.lerp(param5, param0.xRotO, param0.xRot), 0.0F, 0.0F, 1.0F);
        this.bindTexture(param0);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        this.model.render(param0, param5, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        RenderSystem.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(LlamaSpit param0) {
        return LLAMA_SPIT_LOCATION;
    }
}
