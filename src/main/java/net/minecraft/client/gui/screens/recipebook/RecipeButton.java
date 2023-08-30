package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeButton extends AbstractWidget {
    private static final ResourceLocation SLOT_MANY_CRAFTABLE_SPRITE = new ResourceLocation("recipe_book/slot_many_craftable");
    private static final ResourceLocation SLOT_CRAFTABLE_SPRITE = new ResourceLocation("recipe_book/slot_craftable");
    private static final ResourceLocation SLOT_MANY_UNCRAFTABLE_SPRITE = new ResourceLocation("recipe_book/slot_many_uncraftable");
    private static final ResourceLocation SLOT_UNCRAFTABLE_SPRITE = new ResourceLocation("recipe_book/slot_uncraftable");
    private static final float ANIMATION_TIME = 15.0F;
    private static final int BACKGROUND_SIZE = 25;
    public static final int TICKS_TO_SWAP = 30;
    private static final Component MORE_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.moreRecipes");
    private RecipeBookMenu<?> menu;
    private RecipeBook book;
    private RecipeCollection collection;
    private float time;
    private float animationTime;
    private int currentIndex;

    public RecipeButton() {
        super(0, 0, 25, 25, CommonComponents.EMPTY);
    }

    public void init(RecipeCollection param0, RecipeBookPage param1) {
        this.collection = param0;
        this.menu = (RecipeBookMenu)param1.getMinecraft().player.containerMenu;
        this.book = param1.getRecipeBook();
        List<RecipeHolder<?>> var0 = param0.getRecipes(this.book.isFiltering(this.menu));

        for(RecipeHolder<?> var1 : var0) {
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

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        if (!Screen.hasControlDown()) {
            this.time += param3;
        }

        ResourceLocation var0;
        if (this.collection.hasCraftable()) {
            if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
                var0 = SLOT_MANY_CRAFTABLE_SPRITE;
            } else {
                var0 = SLOT_CRAFTABLE_SPRITE;
            }
        } else if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            var0 = SLOT_MANY_UNCRAFTABLE_SPRITE;
        } else {
            var0 = SLOT_UNCRAFTABLE_SPRITE;
        }

        boolean var4 = this.animationTime > 0.0F;
        if (var4) {
            float var5 = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
            param0.pose().pushPose();
            param0.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
            param0.pose().scale(var5, var5, 1.0F);
            param0.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
            this.animationTime -= param3;
        }

        param0.blitSprite(var0, this.getX(), this.getY(), this.width, this.height);
        List<RecipeHolder<?>> var6 = this.getOrderedRecipes();
        this.currentIndex = Mth.floor(this.time / 30.0F) % var6.size();
        ItemStack var7 = var6.get(this.currentIndex).value().getResultItem(this.collection.registryAccess());
        int var8 = 4;
        if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
            param0.renderItem(var7, this.getX() + var8 + 1, this.getY() + var8 + 1, 0, 10);
            --var8;
        }

        param0.renderFakeItem(var7, this.getX() + var8, this.getY() + var8);
        if (var4) {
            param0.pose().popPose();
        }

    }

    private List<RecipeHolder<?>> getOrderedRecipes() {
        List<RecipeHolder<?>> var0 = this.collection.getDisplayRecipes(true);
        if (!this.book.isFiltering(this.menu)) {
            var0.addAll(this.collection.getDisplayRecipes(false));
        }

        return var0;
    }

    public boolean isOnlyOption() {
        return this.getOrderedRecipes().size() == 1;
    }

    public RecipeHolder<?> getRecipe() {
        List<RecipeHolder<?>> var0 = this.getOrderedRecipes();
        return var0.get(this.currentIndex);
    }

    public List<Component> getTooltipText() {
        ItemStack var0 = this.getOrderedRecipes().get(this.currentIndex).value().getResultItem(this.collection.registryAccess());
        List<Component> var1 = Lists.newArrayList(Screen.getTooltipFromItem(Minecraft.getInstance(), var0));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            var1.add(MORE_RECIPES_TOOLTIP);
        }

        return var1;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        ItemStack var0 = this.getOrderedRecipes().get(this.currentIndex).value().getResultItem(this.collection.registryAccess());
        param0.add(NarratedElementType.TITLE, (Component)Component.translatable("narration.recipe", var0.getHoverName()));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            param0.add(
                NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"), Component.translatable("narration.recipe.usage.more")
            );
        } else {
            param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.hovered"));
        }

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
