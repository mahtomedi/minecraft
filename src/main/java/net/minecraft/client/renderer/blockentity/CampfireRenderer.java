package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CampfireRenderer implements BlockEntityRenderer<CampfireBlockEntity> {
    private static final float SIZE = 0.375F;
    private final ItemRenderer itemRenderer;

    public CampfireRenderer(BlockEntityRendererProvider.Context param0) {
        this.itemRenderer = param0.getItemRenderer();
    }

    public void render(CampfireBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        Direction var0 = param0.getBlockState().getValue(CampfireBlock.FACING);
        NonNullList<ItemStack> var1 = param0.getItems();
        int var2 = (int)param0.getBlockPos().asLong();

        for(int var3 = 0; var3 < var1.size(); ++var3) {
            ItemStack var4 = var1.get(var3);
            if (var4 != ItemStack.EMPTY) {
                param2.pushPose();
                param2.translate(0.5F, 0.44921875F, 0.5F);
                Direction var5 = Direction.from2DDataValue((var3 + var0.get2DDataValue()) % 4);
                float var6 = -var5.toYRot();
                param2.mulPose(Axis.YP.rotationDegrees(var6));
                param2.mulPose(Axis.XP.rotationDegrees(90.0F));
                param2.translate(-0.3125F, -0.3125F, 0.0F);
                param2.scale(0.375F, 0.375F, 0.375F);
                this.itemRenderer.renderStatic(var4, ItemDisplayContext.FIXED, param4, param5, param2, param3, param0.getLevel(), var2 + var3);
                param2.popPose();
            }
        }

    }
}
