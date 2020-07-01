package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeButton extends AbstractWidget {
    private static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
    private RecipeBookMenu<?> menu;
    private RecipeBook book;
    private RecipeCollection collection;
    private float time;
    private float animationTime;
    private int currentIndex;

    public RecipeButton() {
        super(0, 0, 25, 25, TextComponent.EMPTY);
    }

    public void init(RecipeCollection param0, RecipeBookPage param1) {
        this.collection = param0;
        this.menu = (RecipeBookMenu)param1.getMinecraft().player.containerMenu;
        this.book = param1.getRecipeBook();
        List<Recipe<?>> var0 = param0.getRecipes(this.book.isFiltering(this.menu));

        for(Recipe<?> var1 : var0) {
            if (this.book.willHighlight(var1)) {
                param1.recipesShown(var0);
                this.animationTime = 15.0F;
                break;
            }
        }

    }

    public RecipeCollection getCollection() {
        return this.collection;
    }

    public void setPosition(int param0, int param1) {
        this.x = param0;
        this.y = param1;
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        if (!Screen.hasControlDown()) {
            this.time += param3;
        }

        Minecraft var0 = Minecraft.getInstance();
        var0.getTextureManager().bind(RECIPE_BOOK_LOCATION);
        int var1 = 29;
        if (!this.collection.hasCraftable()) {
            var1 += 25;
        }

        int var2 = 206;
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            var2 += 25;
        }

        boolean var3 = this.animationTime > 0.0F;
        if (var3) {
            float var4 = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)(this.x + 8), (float)(this.y + 12), 0.0F);
            RenderSystem.scalef(var4, var4, 1.0F);
            RenderSystem.translatef((float)(-(this.x + 8)), (float)(-(this.y + 12)), 0.0F);
            this.animationTime -= param3;
        }

        this.blit(param0, this.x, this.y, var1, var2, this.width, this.height);
        List<Recipe<?>> var5 = this.getOrderedRecipes();
        this.currentIndex = Mth.floor(this.time / 30.0F) % var5.size();
        ItemStack var6 = var5.get(this.currentIndex).getResultItem();
        int var7 = 4;
        if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
            var0.getItemRenderer().renderAndDecorateItem(var6, this.x + var7 + 1, this.y + var7 + 1);
            --var7;
        }

        var0.getItemRenderer().renderAndDecorateFakeItem(var6, this.x + var7, this.y + var7);
        if (var3) {
            RenderSystem.popMatrix();
        }

    }

    private List<Recipe<?>> getOrderedRecipes() {
        List<Recipe<?>> var0 = this.collection.getDisplayRecipes(true);
        if (!this.book.isFiltering(this.menu)) {
            var0.addAll(this.collection.getDisplayRecipes(false));
        }

        return var0;
    }

    public boolean isOnlyOption() {
        return this.getOrderedRecipes().size() == 1;
    }

    public Recipe<?> getRecipe() {
        List<Recipe<?>> var0 = this.getOrderedRecipes();
        return var0.get(this.currentIndex);
    }

    public List<FormattedText> getTooltipText(Screen param0) {
        ItemStack var0 = this.getOrderedRecipes().get(this.currentIndex).getResultItem();
        List<FormattedText> var1 = Lists.newArrayList(param0.getTooltipFromItem(var0));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            var1.add(new TranslatableComponent("gui.recipebook.moreRecipes"));
        }

        return var1;
    }

    @Override
    public int getWidth() {
        return 25;
    }

    @Override
    protected boolean isValidClickButton(int param0) {
        return param0 == 0 || param0 == 1;
    }
}
