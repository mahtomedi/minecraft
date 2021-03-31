package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CampfireRenderer implements BlockEntityRenderer<CampfireBlockEntity> {
    private static final float SIZE = 0.375F;

    public CampfireRenderer(BlockEntityRendererProvider.Context param0) {
    }

    public void render(CampfireBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        Direction var0 = param0.getBlockState().getValue(CampfireBlock.FACING);
        NonNullList<ItemStack> var1 = param0.getItems();
        int var2 = (int)param0.getBlockPos().asLong();

        for(int var3 = 0; var3 < var1.size(); ++var3) {
            ItemStack var4 = var1.get(var3);
            if (var4 != ItemStack.EMPTY) {
                param2.pushPose();
                param2.translate(0.5, 0.44921875, 0.5);
                Direction var5 = Direction.from2DDataValue((var3 + var0.get2DDataValue()) % 4);
                float var6 = -var5.toYRot();
                param2.mulPose(Vector3f.YP.rotationDegrees(var6));
                param2.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                param2.translate(-0.3125, -0.3125, 0.0);
                param2.scale(0.375F, 0.375F, 0.375F);
                Minecraft.getInstance().getItemRenderer().renderStatic(var4, ItemTransforms.TransformType.FIXED, param4, param5, param2, param3, var2 + var3);
                param2.popPose();
            }
        }

    }
}
