package net.minecraft.client.gui.screens.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeBookTabButton extends StateSwitchingButton {
    private final RecipeBookCategories category;
    private float animationTime;

    public RecipeBookTabButton(RecipeBookCategories param0) {
        super(0, 0, 35, 27, false);
        this.category = param0;
        this.initTextureValues(153, 2, 35, 0, RecipeBookComponent.RECIPE_BOOK_LOCATION);
    }

    public void startAnimation(Minecraft param0) {
        ClientRecipeBook var0 = param0.player.getRecipeBook();
        List<RecipeCollection> var1 = var0.getCollection(this.category);
        if (param0.player.containerMenu instanceof RecipeBookMenu) {
            for(RecipeCollection var2 : var1) {
                for(Recipe<?> var3 : var2.getRecipes(var0.isFilteringCraftable((RecipeBookMenu<?>)param0.player.containerMenu))) {
                    if (var0.willHighlight(var3)) {
                        this.animationTime = 15.0F;
                        return;
                    }
                }
            }

        }
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        if (this.animationTime > 0.0F) {
            float var0 = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)(this.x + 8), (float)(this.y + 12), 0.0F);
            RenderSystem.scalef(1.0F, var0, 1.0F);
            RenderSystem.translatef((float)(-(this.x + 8)), (float)(-(this.y + 12)), 0.0F);
        }

        Minecraft var1 = Minecraft.getInstance();
        var1.getTextureManager().bind(this.resourceLocation);
        RenderSystem.disableDepthTest();
        int var2 = this.xTexStart;
        int var3 = this.yTexStart;
        if (this.isStateTriggered) {
            var2 += this.xDiffTex;
        }

        if (this.isHovered()) {
            var3 += this.yDiffTex;
        }

        int var4 = this.x;
        if (this.isStateTriggered) {
            var4 -= 2;
        }

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.blit(param0, var4, this.y, var2, var3, this.width, this.height);
        RenderSystem.enableDepthTest();
        this.renderIcon(var1.getItemRenderer());
        if (this.animationTime > 0.0F) {
            RenderSystem.popMatrix();
            this.animationTime -= param3;
        }

    }

    private void renderIcon(ItemRenderer param0) {
        List<ItemStack> var0 = this.category.getIconItems();
        int var1 = this.isStateTriggered ? -2 : 0;
        if (var0.size() == 1) {
            param0.renderAndDecorateItem(var0.get(0), this.x + 9 + var1, this.y + 5);
        } else if (var0.size() == 2) {
            param0.renderAndDecorateItem(var0.get(0), this.x + 3 + var1, this.y + 5);
            param0.renderAndDecorateItem(var0.get(1), this.x + 14 + var1, this.y + 5);
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
