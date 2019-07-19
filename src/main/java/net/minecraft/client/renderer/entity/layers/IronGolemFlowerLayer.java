package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemFlowerLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
    public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> param0) {
        super(param0);
    }

    public void render(IronGolem param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.getOfferFlowerTick() != 0) {
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(5.0F + 180.0F * this.getParentModel().getFlowerHoldingArm().xRot / (float) Math.PI, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translatef(-0.9375F, -0.625F, -0.9375F);
            float var0 = 0.5F;
            GlStateManager.scalef(0.5F, -0.5F, 0.5F);
            int var1 = param0.getLightColor();
            int var2 = var1 % 65536;
            int var3 = var1 / 65536;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var2, (float)var3);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.POPPY.defaultBlockState(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
