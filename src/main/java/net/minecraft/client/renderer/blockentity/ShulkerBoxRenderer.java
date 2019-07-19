package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxRenderer extends BlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerModel<?> model;

    public ShulkerBoxRenderer(ShulkerModel<?> param0) {
        this.model = param0;
    }

    public void render(ShulkerBoxBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        Direction var0 = Direction.UP;
        if (param0.hasLevel()) {
            BlockState var1 = this.getLevel().getBlockState(param0.getBlockPos());
            if (var1.getBlock() instanceof ShulkerBoxBlock) {
                var0 = var1.getValue(ShulkerBoxBlock.FACING);
            }
        }

        GlStateManager.enableDepthTest();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        if (param5 >= 0) {
            this.bindTexture(BREAKING_LOCATIONS[param5]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(4.0F, 4.0F, 1.0F);
            GlStateManager.translatef(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            DyeColor var2 = param0.getColor();
            if (var2 == null) {
                this.bindTexture(ShulkerRenderer.DEFAULT_TEXTURE_LOCATION);
            } else {
                this.bindTexture(ShulkerRenderer.TEXTURE_LOCATION[var2.getId()]);
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        if (param5 < 0) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 1.5F, (float)param3 + 0.5F);
        GlStateManager.scalef(1.0F, -1.0F, -1.0F);
        GlStateManager.translatef(0.0F, 1.0F, 0.0F);
        float var3 = 0.9995F;
        GlStateManager.scalef(0.9995F, 0.9995F, 0.9995F);
        GlStateManager.translatef(0.0F, -1.0F, 0.0F);
        switch(var0) {
            case DOWN:
                GlStateManager.translatef(0.0F, 2.0F, 0.0F);
                GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
            case UP:
            default:
                break;
            case NORTH:
                GlStateManager.translatef(0.0F, 1.0F, 1.0F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
                break;
            case SOUTH:
                GlStateManager.translatef(0.0F, 1.0F, -1.0F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case WEST:
                GlStateManager.translatef(-1.0F, 1.0F, 0.0F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                break;
            case EAST:
                GlStateManager.translatef(1.0F, 1.0F, 0.0F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
        }

        this.model.getBase().render(0.0625F);
        GlStateManager.translatef(0.0F, -param0.getProgress(param4) * 0.5F, 0.0F);
        GlStateManager.rotatef(270.0F * param0.getProgress(param4), 0.0F, 1.0F, 0.0F);
        this.model.getLid().render(0.0625F);
        GlStateManager.enableCull();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (param5 >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }

    }
}
