package net.minecraft.client.gui.screens.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeBookTabButton extends StateSwitchingButton {
    private static final WidgetSprites SPRITES = new WidgetSprites(new ResourceLocation("recipe_book/tab"), new ResourceLocation("recipe_book/tab_selected"));
    private final RecipeBookCategories category;
    private static final float ANIMATION_TIME = 15.0F;
    private float animationTime;

    public RecipeBookTabButton(RecipeBookCategories param0) {
        super(0, 0, 35, 27, false);
        this.category = param0;
        this.initTextureValues(SPRITES);
    }

    public void startAnimation(Minecraft param0) {
        ClientRecipeBook var0 = param0.player.getRecipeBook();
        List<RecipeCollection> var1 = var0.getCollection(this.category);
        if (param0.player.containerMenu instanceof RecipeBookMenu) {
            for(RecipeCollection var2 : var1) {
                for(Recipe<?> var3 : var2.getRecipes(var0.isFiltering((RecipeBookMenu<?>)param0.player.containerMenu))) {
                    if (var0.willHighlight(var3)) {
                        this.animationTime = 15.0F;
                        return;
                    }
                }
            }

        }
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.sprites != null) {
            if (this.animationTime > 0.0F) {
                float var0 = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
                param0.pose().pushPose();
                param0.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
                param0.pose().scale(1.0F, var0, 1.0F);
                param0.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
            }

            Minecraft var1 = Minecraft.getInstance();
            RenderSystem.disableDepthTest();
            ResourceLocation var2 = this.sprites.get(true, this.isStateTriggered);
            int var3 = this.getX();
            if (this.isStateTriggered) {
                var3 -= 2;
            }

            param0.blitSprite(var2, var3, this.getY(), this.width, this.height);
            RenderSystem.enableDepthTest();
            this.renderIcon(param0, var1.getItemRenderer());
            if (this.animationTime > 0.0F) {
                param0.pose().popPose();
                this.animationTime -= param3;
            }

        }
    }

    private void renderIcon(GuiGraphics param0, ItemRenderer param1) {
        List<ItemStack> var0 = this.category.getIconItems();
        int var1 = this.isStateTriggered ? -2 : 0;
        if (var0.size() == 1) {
            param0.renderFakeItem(var0.get(0), this.getX() + 9 + var1, this.getY() + 5);
        } else if (var0.size() == 2) {
            param0.renderFakeItem(var0.get(0), this.getX() + 3 + var1, this.getY() + 5);
            param0.renderFakeItem(var0.get(1), this.getX() + 14 + var1, this.getY() + 5);
        }

    }

    public RecipeBookCategories getCategory() {
        return this.category;
    }

    public boolean updateVisibility(ClientRecipeBook param0) {
        List<RecipeCollection> var0 = param0.getCollection(this.category);
        this.visible = false;
        if (var0 != null) {
            for(RecipeCollection var1 : var0) {
                if (var1.hasKnownRecipes() && var1.hasFitting()) {
                    this.visible = true;
                    break;
                }
            }
        }

        return this.visible;
    }
}
