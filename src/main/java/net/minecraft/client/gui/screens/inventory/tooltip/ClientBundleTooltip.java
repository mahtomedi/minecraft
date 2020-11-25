package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientBundleTooltip implements ClientTooltipComponent {
    private final NonNullList<ItemStack> items;
    private final boolean showExtensionSlot;

    public ClientBundleTooltip(BundleTooltip param0) {
        this.items = param0.getItems();
        this.showExtensionSlot = param0.showEmptySlot();
    }

    @Override
    public int getHeight() {
        return 18 * (1 + (this.getSlotCount() - 1) / this.itemsPerRow()) + 4;
    }

    @Override
    public int getWidth(Font param0) {
        return this.itemsPerRow() * 18;
    }

    private int getSlotCount() {
        return this.items.size() + (this.showExtensionSlot ? 1 : 0);
    }

    @Override
    public void renderImage(Font param0, int param1, int param2, PoseStack param3, ItemRenderer param4, int param5, TextureManager param6) {
        int var0 = 0;
        int var1 = 0;
        int var2 = this.itemsPerRow();

        for(int var3 = 0; var3 < this.items.size(); ++var3) {
            ItemStack var4 = this.items.get(var3);
            this.blitSlotBg(param3, var0 + param1 - 1, var1 + param2 - 1, param5, param6, false);
            param4.renderAndDecorateItem(var4, param1 + var0, param2 + var1, var3);
            param4.renderGuiItemDecorations(param0, var4, param1 + var0, param2 + var1);
            var0 += 18;
            if (var0 >= 18 * var2) {
                var0 = 0;
                var1 += 18;
            }
        }

        if (this.showExtensionSlot) {
            this.blitSlotBg(param3, var0 + param1 - 1, var1 + param2 - 1, param5, param6, true);
        }

    }

    private void blitSlotBg(PoseStack param0, int param1, int param2, int param3, TextureManager param4, boolean param5) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        param4.bind(GuiComponent.STATS_ICON_LOCATION);
        GuiComponent.blit(param0, param1, param2, param3, 0.0F, param5 ? 36.0F : 0.0F, 18, 18, 128, 128);
    }

    private int itemsPerRow() {
        return Mth.ceil(Math.sqrt((double)this.getSlotCount()));
    }
}
