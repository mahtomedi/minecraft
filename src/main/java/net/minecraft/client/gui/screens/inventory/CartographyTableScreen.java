package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.GameRenderer;
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        ItemStack var2 = this.menu.getSlot(1).getItem();
        boolean var3 = var2.is(Items.MAP);
        boolean var4 = var2.is(Items.PAPER);
        boolean var5 = var2.is(Items.GLASS_PANE);
        ItemStack var6 = this.menu.getSlot(0).getItem();
        boolean var7 = false;
        Integer var8;
        MapItemSavedData var9;
        if (var6.is(Items.FILLED_MAP)) {
            var8 = MapItem.getMapId(var6);
            var9 = MapItem.getSavedData(var8, this.minecraft.level);
            if (var9 != null) {
                if (var9.locked) {
                    var7 = true;
                    if (var4 || var5) {
                        this.blit(param0, var0 + 35, var1 + 31, this.imageWidth + 50, 132, 28, 21);
                    }
                }

                if (var4 && var9.scale >= 4) {
                    var7 = true;
                    this.blit(param0, var0 + 35, var1 + 31, this.imageWidth + 50, 132, 28, 21);
                }
            }
        } else {
            var8 = null;
            var9 = null;
        }

        this.renderResultingMap(param0, var8, var9, var3, var4, var5, var7);
    }

    private void renderResultingMap(
        PoseStack param0, @Nullable Integer param1, @Nullable MapItemSavedData param2, boolean param3, boolean param4, boolean param5, boolean param6
    ) {
        int var0 = this.leftPos;
        int var1 = this.topPos;
        if (param4 && !param6) {
            this.blit(param0, var0 + 67, var1 + 13, this.imageWidth, 66, 66, 66);
            this.renderMap(param0, param1, param2, var0 + 85, var1 + 31, 0.226F);
        } else if (param3) {
            this.blit(param0, var0 + 67 + 16, var1 + 13, this.imageWidth, 132, 50, 66);
            this.renderMap(param0, param1, param2, var0 + 86, var1 + 16, 0.34F);
            RenderSystem.setShaderTexture(0, BG_LOCATION);
            param0.pushPose();
            param0.translate(0.0F, 0.0F, 1.0F);
            this.blit(param0, var0 + 67, var1 + 13 + 16, this.imageWidth, 132, 50, 66);
            this.renderMap(param0, param1, param2, var0 + 70, var1 + 32, 0.34F);
            param0.popPose();
        } else if (param5) {
            this.blit(param0, var0 + 67, var1 + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(param0, param1, param2, var0 + 71, var1 + 17, 0.45F);
            RenderSystem.setShaderTexture(0, BG_LOCATION);
            param0.pushPose();
            param0.translate(0.0F, 0.0F, 1.0F);
            this.blit(param0, var0 + 66, var1 + 12, 0, this.imageHeight, 66, 66);
            param0.popPose();
        } else {
            this.blit(param0, var0 + 67, var1 + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(param0, param1, param2, var0 + 71, var1 + 17, 0.45F);
        }

    }

    private void renderMap(PoseStack param0, @Nullable Integer param1, @Nullable MapItemSavedData param2, int param3, int param4, float param5) {
        if (param1 != null && param2 != null) {
            param0.pushPose();
            param0.translate((float)param3, (float)param4, 1.0F);
            param0.scale(param5, param5, 1.0F);
            MultiBufferSource.BufferSource var0 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            this.minecraft.gameRenderer.getMapRenderer().render(param0, var0, param1, param2, true, 15728880);
            var0.endBatch();
            param0.popPose();
        }

    }
}
