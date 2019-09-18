package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerTradeItemLayer<T extends LivingEntity> extends RenderLayer<T, VillagerModel<T>> {
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public VillagerTradeItemLayer(RenderLayerParent<T, VillagerModel<T>> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        ItemStack var0 = param0.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!var0.isEmpty()) {
            Item var1 = var0.getItem();
            Block var2 = Block.byItem(var1);
            RenderSystem.pushMatrix();
            boolean var3 = this.itemRenderer.isGui3d(var0) && RenderType.getRenderLayer(var2.defaultBlockState()) == RenderType.TRANSLUCENT;
            if (var3) {
                RenderSystem.depthMask(false);
            }

            RenderSystem.translatef(0.0F, 0.4F, -0.4F);
            RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
            this.itemRenderer.renderWithMobState(var0, param0, ItemTransforms.TransformType.GROUND, false);
            if (var3) {
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
