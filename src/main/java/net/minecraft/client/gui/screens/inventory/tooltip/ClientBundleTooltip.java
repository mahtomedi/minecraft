package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientBundleTooltip implements ClientTooltipComponent {
    private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("container/bundle/background");
    private static final int MARGIN_Y = 4;
    private static final int BORDER_WIDTH = 1;
    private static final int SLOT_SIZE_X = 18;
    private static final int SLOT_SIZE_Y = 20;
    private final NonNullList<ItemStack> items;
    private final int weight;

    public ClientBundleTooltip(BundleTooltip param0) {
        this.items = param0.getItems();
        this.weight = param0.getWeight();
    }

    @Override
    public int getHeight() {
        return this.backgroundHeight() + 4;
    }

    @Override
    public int getWidth(Font param0) {
        return this.backgroundWidth();
    }

    private int backgroundWidth() {
        return this.gridSizeX() * 18 + 2;
    }

    private int backgroundHeight() {
        return this.gridSizeY() * 20 + 2;
    }

    @Override
    public void renderImage(Font param0, int param1, int param2, GuiGraphics param3) {
        int var0 = this.gridSizeX();
        int var1 = this.gridSizeY();
        param3.blitSprite(BACKGROUND_SPRITE, param1, param2, this.backgroundWidth(), this.backgroundHeight());
        boolean var2 = this.weight >= 64;
        int var3 = 0;

        for(int var4 = 0; var4 < var1; ++var4) {
            for(int var5 = 0; var5 < var0; ++var5) {
                int var6 = param1 + var5 * 18 + 1;
                int var7 = param2 + var4 * 20 + 1;
                this.renderSlot(var6, var7, var3++, var2, param3, param0);
            }
        }

    }

    private void renderSlot(int param0, int param1, int param2, boolean param3, GuiGraphics param4, Font param5) {
        if (param2 >= this.items.size()) {
            this.blit(param4, param0, param1, param3 ? ClientBundleTooltip.Texture.BLOCKED_SLOT : ClientBundleTooltip.Texture.SLOT);
        } else {
            ItemStack var0 = this.items.get(param2);
            this.blit(param4, param0, param1, ClientBundleTooltip.Texture.SLOT);
            param4.renderItem(var0, param0 + 1, param1 + 1, param2);
            param4.renderItemDecorations(param5, var0, param0 + 1, param1 + 1);
            if (param2 == 0) {
                AbstractContainerScreen.renderSlotHighlight(param4, param0 + 1, param1 + 1, 0);
            }

        }
    }

    private void blit(GuiGraphics param0, int param1, int param2, ClientBundleTooltip.Texture param3) {
        param0.blitSprite(param3.sprite, param1, param2, 0, param3.w, param3.h);
    }

    private int gridSizeX() {
        return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.items.size() + 1.0)));
    }

    private int gridSizeY() {
        return (int)Math.ceil(((double)this.items.size() + 1.0) / (double)this.gridSizeX());
    }

    @OnlyIn(Dist.CLIENT)
    static enum Texture {
        BLOCKED_SLOT(new ResourceLocation("container/bundle/blocked_slot"), 18, 20),
        SLOT(new ResourceLocation("container/bundle/slot"), 18, 20);

        public final ResourceLocation sprite;
        public final int w;
        public final int h;

        private Texture(ResourceLocation param0, int param1, int param2) {
            this.sprite = param0;
            this.w = param1;
            this.h = param2;
        }
    }
}
