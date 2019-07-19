package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.client.model.BedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BedRenderer extends BlockEntityRenderer<BedBlockEntity> {
    private static final ResourceLocation[] TEXTURES = Arrays.stream(DyeColor.values())
        .sorted(Comparator.comparingInt(DyeColor::getId))
        .map(param0 -> new ResourceLocation("textures/entity/bed/" + param0.getName() + ".png"))
        .toArray(param0 -> new ResourceLocation[param0]);
    private final BedModel bedModel = new BedModel();

    public void render(BedBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        if (param5 >= 0) {
            this.bindTexture(BREAKING_LOCATIONS[param5]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(4.0F, 4.0F, 1.0F);
            GlStateManager.translatef(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            ResourceLocation var0 = TEXTURES[param0.getColor().getId()];
            if (var0 != null) {
                this.bindTexture(var0);
            }
        }

        if (param0.hasLevel()) {
            BlockState var1 = param0.getBlockState();
            this.renderPiece(var1.getValue(BedBlock.PART) == BedPart.HEAD, param1, param2, param3, var1.getValue(BedBlock.FACING));
        } else {
            this.renderPiece(true, param1, param2, param3, Direction.SOUTH);
            this.renderPiece(false, param1, param2, param3 - 1.0, Direction.SOUTH);
        }

        if (param5 >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }

    }

    private void renderPiece(boolean param0, double param1, double param2, double param3, Direction param4) {
        this.bedModel.preparePiece(param0);
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)param1, (float)param2 + 0.5625F, (float)param3);
        GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translatef(0.5F, 0.5F, 0.5F);
        GlStateManager.rotatef(180.0F + param4.toYRot(), 0.0F, 0.0F, 1.0F);
        GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
        GlStateManager.enableRescaleNormal();
        this.bedModel.render();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
