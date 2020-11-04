package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CartographyTableScreen extends AbstractContainerScreen<CartographyTableMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/cartography_table.png");

    public CartographyTableScreen(CartographyTableMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        this.titleLabelY -= 2;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        this.renderBackground(param0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(BG_LOCATION);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        ItemStack var2 = this.menu.getSlot(1).getItem();
        boolean var3 = var2.is(Items.MAP);
        boolean var4 = var2.is(Items.PAPER);
        boolean var5 = var2.is(Items.GLASS_PANE);
        ItemStack var6 = this.menu.getSlot(0).getItem();
        boolean var7 = false;
        MapItemSavedData var8;
        if (var6.is(Items.FILLED_MAP)) {
            var8 = MapItem.getSavedData(var6, this.minecraft.level);
            if (var8 != null) {
                if (var8.locked) {
                    var7 = true;
                    if (var4 || var5) {
                        this.blit(param0, var0 + 35, var1 + 31, this.imageWidth + 50, 132, 28, 21);
                    }
                }

                if (var4 && var8.scale >= 4) {
                    var7 = true;
                    this.blit(param0, var0 + 35, var1 + 31, this.imageWidth + 50, 132, 28, 21);
                }
            }
        } else {
            var8 = null;
        }

        this.renderResultingMap(param0, var8, var3, var4, var5, var7);
    }

    private void renderResultingMap(PoseStack param0, @Nullable MapItemSavedData param1, boolean param2, boolean param3, boolean param4, boolean param5) {
        int var0 = this.leftPos;
        int var1 = this.topPos;
        if (param3 && !param5) {
            this.blit(param0, var0 + 67, var1 + 13, this.imageWidth, 66, 66, 66);
            this.renderMap(param1, var0 + 85, var1 + 31, 0.226F);
        } else if (param2) {
            this.blit(param0, var0 + 67 + 16, var1 + 13, this.imageWidth, 132, 50, 66);
            this.renderMap(param1, var0 + 86, var1 + 16, 0.34F);
            this.minecraft.getTextureManager().bind(BG_LOCATION);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 1.0F);
            this.blit(param0, var0 + 67, var1 + 13 + 16, this.imageWidth, 132, 50, 66);
            this.renderMap(param1, var0 + 70, var1 + 32, 0.34F);
            RenderSystem.popMatrix();
        } else if (param4) {
            this.blit(param0, var0 + 67, var1 + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(param1, var0 + 71, var1 + 17, 0.45F);
            this.minecraft.getTextureManager().bind(BG_LOCATION);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 1.0F);
            this.blit(param0, var0 + 66, var1 + 12, 0, this.imageHeight, 66, 66);
            RenderSystem.popMatrix();
        } else {
            this.blit(param0, var0 + 67, var1 + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(param1, var0 + 71, var1 + 17, 0.45F);
        }

    }

    private void renderMap(@Nullable MapItemSavedData param0, int param1, int param2, float param3) {
        if (param0 != null) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)param1, (float)param2, 1.0F);
            RenderSystem.scalef(param3, param3, 1.0F);
            MultiBufferSource.BufferSource var0 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            this.minecraft.gameRenderer.getMapRenderer().render(new PoseStack(), var0, param0, true, 15728880);
            var0.endBatch();
            RenderSystem.popMatrix();
        }

    }
}
