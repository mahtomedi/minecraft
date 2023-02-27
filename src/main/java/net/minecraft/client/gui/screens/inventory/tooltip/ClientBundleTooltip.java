package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientBundleTooltip implements ClientTooltipComponent {
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
    private static final int MARGIN_Y = 4;
    private static final int BORDER_WIDTH = 1;
    private static final int TEX_SIZE = 128;
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
        return this.gridSizeY() * 20 + 2 + 4;
    }

    @Override
    public int getWidth(Font param0) {
        return this.gridSizeX() * 18 + 2;
    }

    @Override
    public void renderImage(Font param0, int param1, int param2, PoseStack param3, ItemRenderer param4) {
        int var0 = this.gridSizeX();
        int var1 = this.gridSizeY();
        boolean var2 = this.weight >= 64;
        int var3 = 0;

        for(int var4 = 0; var4 < var1; ++var4) {
            for(int var5 = 0; var5 < var0; ++var5) {
                int var6 = param1 + var5 * 18 + 1;
                int var7 = param2 + var4 * 20 + 1;
                this.renderSlot(var6, var7, var3++, var2, param0, param3, param4);
            }
        }

        this.drawBorder(param1, param2, var0, var1, param3);
    }

    private void renderSlot(int param0, int param1, int param2, boolean param3, Font param4, PoseStack param5, ItemRenderer param6) {
        if (param2 >= this.items.size()) {
            this.blit(param5, param0, param1, param3 ? ClientBundleTooltip.Texture.BLOCKED_SLOT : ClientBundleTooltip.Texture.SLOT);
        } else {
            ItemStack var0 = this.items.get(param2);
            this.blit(param5, param0, param1, ClientBundleTooltip.Texture.SLOT);
            param6.renderAndDecorateItem(param5, var0, param0 + 1, param1 + 1, param2);
            param6.renderGuiItemDecorations(param5, param4, var0, param0 + 1, param1 + 1);
            if (param2 == 0) {
                AbstractContainerScreen.renderSlotHighlight(param5, param0 + 1, param1 + 1, 0);
            }

        }
    }

    private void drawBorder(int param0, int param1, int param2, int param3, PoseStack param4) {
        this.blit(param4, param0, param1, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);
        this.blit(param4, param0 + param2 * 18 + 1, param1, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);

        for(int var0 = 0; var0 < param2; ++var0) {
            this.blit(param4, param0 + 1 + var0 * 18, param1, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_TOP);
            this.blit(param4, param0 + 1 + var0 * 18, param1 + param3 * 20, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_BOTTOM);
        }

        for(int var1 = 0; var1 < param3; ++var1) {
            this.blit(param4, param0, param1 + var1 * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
            this.blit(param4, param0 + param2 * 18 + 1, param1 + var1 * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
        }

        this.blit(param4, param0, param1 + param3 * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
        this.blit(param4, param0 + param2 * 18 + 1, param1 + param3 * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
    }

    private void blit(PoseStack param0, int param1, int param2, ClientBundleTooltip.Texture param3) {
        RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
        GuiComponent.blit(param0, param1, param2, 0, (float)param3.x, (float)param3.y, param3.w, param3.h, 128, 128);
    }

    private int gridSizeX() {
        return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.items.size() + 1.0)));
    }

    private int gridSizeY() {
        return (int)Math.ceil(((double)this.items.size() + 1.0) / (double)this.gridSizeX());
    }

    @OnlyIn(Dist.CLIENT)
    static enum Texture {
        SLOT(0, 0, 18, 20),
        BLOCKED_SLOT(0, 40, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        public final int x;
        public final int y;
        public final int w;
        public final int h;

        private Texture(int param0, int param1, int param2, int param3) {
            this.x = param0;
            this.y = param1;
            this.w = param2;
            this.h = param3;
        }
    }
}
