package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CampfireRenderer extends BlockEntityRenderer<CampfireBlockEntity> {
    public void render(CampfireBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        Direction var0 = param0.getBlockState().getValue(CampfireBlock.FACING);
        NonNullList<ItemStack> var1 = param0.getItems();

        for(int var2 = 0; var2 < var1.size(); ++var2) {
            ItemStack var3 = var1.get(var2);
            if (var3 != ItemStack.EMPTY) {
                GlStateManager.pushMatrix();
                GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.44921875F, (float)param3 + 0.5F);
                Direction var4 = Direction.from2DDataValue((var2 + var0.get2DDataValue()) % 4);
                GlStateManager.rotatef(-var4.toYRot(), 0.0F, 1.0F, 0.0F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.translatef(-0.3125F, -0.3125F, 0.0F);
                GlStateManager.scalef(0.375F, 0.375F, 0.375F);
                Minecraft.getInstance().getItemRenderer().renderStatic(var3, ItemTransforms.TransformType.FIXED);
                GlStateManager.popMatrix();
            }
        }

    }
}
