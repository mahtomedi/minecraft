package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
    public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (!param0.isBaby() && !param0.isInvisible()) {
            BlockState var0 = param0.getMushroomType().getBlockState();
            this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
            RenderSystem.enableCull();
            RenderSystem.cullFace(GlStateManager.CullFace.FRONT);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(1.0F, -1.0F, 1.0F);
            RenderSystem.translatef(0.2F, 0.35F, 0.5F);
            RenderSystem.rotatef(42.0F, 0.0F, 1.0F, 0.0F);
            BlockRenderDispatcher var1 = Minecraft.getInstance().getBlockRenderer();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(-0.5F, -0.5F, 0.5F);
            var1.renderSingleBlock(var0, 1.0F);
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.1F, 0.0F, -0.6F);
            RenderSystem.rotatef(42.0F, 0.0F, 1.0F, 0.0F);
            RenderSystem.translatef(-0.5F, -0.5F, 0.5F);
            var1.renderSingleBlock(var0, 1.0F);
            RenderSystem.popMatrix();
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            this.getParentModel().getHead().translateTo(0.0625F);
            RenderSystem.scalef(1.0F, -1.0F, 1.0F);
            RenderSystem.translatef(0.0F, 0.7F, -0.2F);
            RenderSystem.rotatef(12.0F, 0.0F, 1.0F, 0.0F);
            RenderSystem.translatef(-0.5F, -0.5F, 0.5F);
            var1.renderSingleBlock(var0, 1.0F);
            RenderSystem.popMatrix();
            RenderSystem.cullFace(GlStateManager.CullFace.BACK);
            RenderSystem.disableCull();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}
