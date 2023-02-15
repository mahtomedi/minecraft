package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SuspiciousSandBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuspiciousSandRenderer implements BlockEntityRenderer<SuspiciousSandBlockEntity> {
    private final ItemRenderer itemRenderer;

    public SuspiciousSandRenderer(BlockEntityRendererProvider.Context param0) {
        this.itemRenderer = param0.getItemRenderer();
    }

    public void render(SuspiciousSandBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        if (param0.getLevel() != null) {
            int var0 = param0.getBlockState().getValue(BlockStateProperties.DUSTED);
            if (var0 > 0) {
                Direction var1 = param0.getHitDirection();
                if (var1 != null) {
                    ItemStack var2 = param0.getItem();
                    if (!var2.isEmpty()) {
                        param2.pushPose();
                        param2.translate(0.0F, 0.5F, 0.0F);
                        float[] var3 = this.translations(var1, var0);
                        param2.translate(var3[0], var3[1], var3[2]);
                        param2.mulPose(Axis.YP.rotationDegrees(75.0F));
                        boolean var4 = var1 == Direction.EAST || var1 == Direction.WEST;
                        param2.mulPose(Axis.YP.rotationDegrees((float)((var4 ? 90 : 0) + 11)));
                        param2.scale(0.5F, 0.5F, 0.5F);
                        int var5 = LevelRenderer.getLightColor(param0.getLevel(), param0.getBlockState(), param0.getBlockPos().relative(var1));
                        this.itemRenderer.renderStatic(var2, ItemDisplayContext.FIXED, var5, OverlayTexture.NO_OVERLAY, param2, param3, param0.getLevel(), 0);
                        param2.popPose();
                    }
                }
            }
        }
    }

    private float[] translations(Direction param0, int param1) {
        float[] var0 = new float[]{0.5F, 0.0F, 0.5F};
        float var1 = (float)param1 / 10.0F * 0.75F;
        switch(param0) {
            case EAST:
                var0[0] = 0.73F + var1;
                break;
            case WEST:
                var0[0] = 0.25F - var1;
                break;
            case UP:
                var0[1] = 0.25F + var1;
                break;
            case DOWN:
                var0[1] = -0.23F - var1;
                break;
            case NORTH:
                var0[2] = 0.25F - var1;
                break;
            case SOUTH:
                var0[2] = 0.73F + var1;
        }

        return var0;
    }
}
