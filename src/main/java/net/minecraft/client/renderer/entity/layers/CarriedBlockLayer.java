package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarriedBlockLayer extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
    public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> param0) {
        super(param0);
    }

    public void render(EnderMan param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        BlockState var0 = param0.getCarriedBlock();
        if (var0 != null) {
            RenderSystem.enableRescaleNormal();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.6875F, -0.75F);
            RenderSystem.rotatef(20.0F, 1.0F, 0.0F, 0.0F);
            RenderSystem.rotatef(45.0F, 0.0F, 1.0F, 0.0F);
            RenderSystem.translatef(0.25F, 0.1875F, 0.25F);
            float var1 = 0.5F;
            RenderSystem.scalef(-0.5F, -0.5F, 0.5F);
            int var2 = param0.getLightColor();
            int var3 = var2 % 65536;
            int var4 = var2 / 65536;
            RenderSystem.glMultiTexCoord2f(33985, (float)var3, (float)var4);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(var0, 1.0F);
            RenderSystem.popMatrix();
            RenderSystem.disableRescaleNormal();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
