package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StonecutterScreen extends AbstractContainerScreen<StonecutterMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    public StonecutterScreen(StonecutterMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        param0.registerUpdateListener(this::containerChanged);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        super.render(param0, param1, param2);
        this.renderTooltip(param0, param1);
    }

    @Override
    protected void renderLabels(int param0, int param1) {
        this.font.draw(this.title.getColoredString(), 8.0F, 4.0F, 4210752);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 94), 4210752);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        this.renderBackground();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(BG_LOCATION);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.blit(var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        int var2 = (int)(41.0F * this.scrollOffs);
        this.blit(var0 + 119, var1 + 15 + var2, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);
        int var3 = this.leftPos + 52;
        int var4 = this.topPos + 14;
        int var5 = this.startIndex + 12;
        this.renderButtons(param1, param2, var3, var4, var5);
        this.renderRecipes(var3, var4, var5);
    }

    private void renderButtons(int param0, int param1, int param2, int param3, int param4) {
        for(int var0 = this.startIndex; var0 < param4 && var0 < this.menu.getNumRecipes(); ++var0) {
            int var1 = var0 - this.startIndex;
            int var2 = param2 + var1 % 4 * 16;
            int var3 = var1 / 4;
            int var4 = param3 + var3 * 18 + 2;
            int var5 = this.imageHeight;
            if (var0 == this.menu.getSelectedRecipeIndex()) {
                var5 += 18;
            } else if (param0 >= var2 && param1 >= var4 && param0 < var2 + 16 && param1 < var4 + 18) {
                var5 += 36;
            }

            this.blit(var2, var4 - 1, 0, var5, 16, 18);
        }

    }

    private void renderRecipes(int param0, int param1, int param2) {
        List<StonecutterRecipe> var0 = this.menu.getRecipes();

        for(int var1 = this.startIndex; var1 < param2 && var1 < this.menu.getNumRecipes(); ++var1) {
            int var2 = var1 - this.startIndex;
            int var3 = param0 + var2 % 4 * 16;
            int var4 = var2 / 4;
            int var5 = param1 + var4 * 18 + 2;
            this.minecraft.getItemRenderer().renderAndDecorateItem(var0.get(var1).getResultItem(), var3, var5);
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        this.scrolling = false;
        if (this.displayRecipes) {
            int var0 = this.leftPos + 52;
            int var1 = this.topPos + 14;
            int var2 = this.startIndex + 12;

            for(int var3 = this.startIndex; var3 < var2; ++var3) {
                int var4 = var3 - this.startIndex;
                double var5 = param0 - (double)(var0 + var4 % 4 * 16);
                double var6 = param1 - (double)(var1 + var4 / 4 * 18);
                if (var5 >= 0.0 && var6 >= 0.0 && var5 < 16.0 && var6 < 18.0 && this.menu.clickMenuButton(this.minecraft.player, var3)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, var3);
                    return true;
                }
            }

            var0 = this.leftPos + 119;
            var1 = this.topPos + 9;
            if (param0 >= (double)var0 && param0 < (double)(var0 + 12) && param1 >= (double)var1 && param1 < (double)(var1 + 54)) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (this.scrolling && this.isScrollBarActive()) {
            int var0 = this.topPos + 14;
            int var1 = var0 + 54;
            this.scrollOffs = ((float)param1 - (float)var0 - 7.5F) / ((float)(var1 - var0) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5) * 4;
            return true;
        } else {
            return super.mouseDragged(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (this.isScrollBarActive()) {
            int var0 = this.getOffscreenRows();
            this.scrollOffs = (float)((double)this.scrollOffs - param2 / (double)var0);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)var0) + 0.5) * 4;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.displayRecipes && this.menu.getNumRecipes() > 12;
    }

    protected int getOffscreenRows() {
        return (this.menu.getNumRecipes() + 4 - 1) / 4 - 3;
    }

    private void containerChanged() {
        this.displayRecipes = this.menu.hasInputItem();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }

    }
}
