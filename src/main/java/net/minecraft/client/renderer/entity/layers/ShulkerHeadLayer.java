package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerHeadLayer extends RenderLayer<Shulker, ShulkerModel<Shulker>> {
    public ShulkerHeadLayer(RenderLayerParent<Shulker, ShulkerModel<Shulker>> param0) {
        super(param0);
    }

    public void render(Shulker param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        RenderSystem.pushMatrix();
        switch(param0.getAttachFace()) {
            case DOWN:
            default:
                break;
            case EAST:
                RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.translatef(1.0F, -1.0F, 0.0F);
                RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case WEST:
                RenderSystem.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.translatef(-1.0F, -1.0F, 0.0F);
                RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case NORTH:
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.translatef(0.0F, -1.0F, -1.0F);
                break;
            case SOUTH:
                RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.translatef(0.0F, -1.0F, 1.0F);
                break;
            case UP:
                RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.translatef(0.0F, -2.0F, 0.0F);
        }

        ModelPart var0 = this.getParentModel().getHead();
        var0.yRot = param5 * (float) (Math.PI / 180.0);
        var0.xRot = param6 * (float) (Math.PI / 180.0);
        DyeColor var1 = param0.getColor();
        if (var1 == null) {
            this.bindTexture(ShulkerRenderer.DEFAULT_TEXTURE_LOCATION);
        } else {
            this.bindTexture(ShulkerRenderer.TEXTURE_LOCATION[var1.getId()]);
        }

        var0.render(param7);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
