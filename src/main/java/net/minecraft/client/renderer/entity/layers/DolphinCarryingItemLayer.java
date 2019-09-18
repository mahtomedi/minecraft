package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.DolphinModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinCarryingItemLayer extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> param0) {
        super(param0);
    }

    public void render(Dolphin param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        boolean var0 = param0.getMainArm() == HumanoidArm.RIGHT;
        ItemStack var1 = var0 ? param0.getOffhandItem() : param0.getMainHandItem();
        ItemStack var2 = var0 ? param0.getMainHandItem() : param0.getOffhandItem();
        if (!var1.isEmpty() || !var2.isEmpty()) {
            this.renderItemOnNose(param0, var2);
        }
    }

    private void renderItemOnNose(LivingEntity param0, ItemStack param1) {
        if (!param1.isEmpty()) {
            Item var0 = param1.getItem();
            Block var1 = Block.byItem(var0);
            RenderSystem.pushMatrix();
            boolean var2 = this.itemRenderer.isGui3d(param1) && RenderType.getRenderLayer(var1.defaultBlockState()) == RenderType.TRANSLUCENT;
            if (var2) {
                RenderSystem.depthMask(false);
            }

            float var3 = 1.0F;
            float var4 = -1.0F;
            float var5 = Mth.abs(param0.xRot) / 60.0F;
            if (param0.xRot < 0.0F) {
                RenderSystem.translatef(0.0F, 1.0F - var5 * 0.5F, -1.0F + var5 * 0.5F);
            } else {
                RenderSystem.translatef(0.0F, 1.0F + var5 * 0.8F, -1.0F + var5 * 0.2F);
            }

            this.itemRenderer.renderWithMobState(param1, param0, ItemTransforms.TransformType.GROUND, false);
            if (var2) {
                RenderSystem.depthMask(true);
            }

            RenderSystem.popMatrix();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
