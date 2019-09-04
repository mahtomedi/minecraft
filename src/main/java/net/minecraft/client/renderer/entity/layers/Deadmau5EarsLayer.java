package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Deadmau5EarsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public Deadmau5EarsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> param0) {
        super(param0);
    }

    public void render(AbstractClientPlayer param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if ("deadmau5".equals(param0.getName().getString()) && param0.isSkinLoaded() && !param0.isInvisible()) {
            this.bindTexture(param0.getSkinTextureLocation());

            for(int var0 = 0; var0 < 2; ++var0) {
                float var1 = Mth.lerp(param3, param0.yRotO, param0.yRot) - Mth.lerp(param3, param0.yBodyRotO, param0.yBodyRot);
                float var2 = Mth.lerp(param3, param0.xRotO, param0.xRot);
                RenderSystem.pushMatrix();
                RenderSystem.rotatef(var1, 0.0F, 1.0F, 0.0F);
                RenderSystem.rotatef(var2, 1.0F, 0.0F, 0.0F);
                RenderSystem.translatef(0.375F * (float)(var0 * 2 - 1), 0.0F, 0.0F);
                RenderSystem.translatef(0.0F, -0.375F, 0.0F);
                RenderSystem.rotatef(-var2, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(-var1, 0.0F, 1.0F, 0.0F);
                float var3 = 1.3333334F;
                RenderSystem.scalef(1.3333334F, 1.3333334F, 1.3333334F);
                this.getParentModel().renderEars(0.0625F);
                RenderSystem.popMatrix();
            }

        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}
