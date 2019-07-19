package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeCollection {
    private final List<Recipe<?>> recipes = Lists.newArrayList();
    private final Set<Recipe<?>> craftable = Sets.newHashSet();
    private final Set<Recipe<?>> fitsDimensions = Sets.newHashSet();
    private final Set<Recipe<?>> known = Sets.newHashSet();
    private boolean singleResultItem = true;

    public boolean hasKnownRecipes() {
        return !this.known.isEmpty();
    }

    public void updateKnownRecipes(RecipeBook param0) {
        for(Recipe<?> var0 : this.recipes) {
            if (param0.contains(var0)) {
                this.known.add(var0);
            }
        }

    }

    public void canCraft(StackedContents param0, int param1, int param2, RecipeBook param3) {
        for(int var0 = 0; var0 < this.recipes.size(); ++var0) {
            Recipe<?> var1 = this.recipes.get(var0);
            boolean var2 = var1.canCraftInDimensions(param1, param2) && param3.contains(var1);
            if (var2) {
                this.fitsDimensions.add(var1);
            } else {
                this.fitsDimensions.remove(var1);
            }

            if (var2 && param0.canCraft(var1, null)) {
                this.craftable.add(var1);
            } else {
                this.craftable.remove(var1);
            }
        }

    }

    public boolean isCraftable(Recipe<?> param0) {
        return this.craftable.contains(param0);
    }

    public boolean hasCraftable() {
        return !this.craftable.isEmpty();
    }

    public boolean hasFitting() {
        return !this.fitsDimensions.isEmpty();
    }

    public List<Recipe<?>> getRecipes() {
        return this.recipes;
    }

    public List<Recipe<?>> getRecipes(boolean param0) {
        List<Recipe<?>> var0 = Lists.newArrayList();
        Set<Recipe<?>> var1 = param0 ? this.craftable : this.fitsDimensions;

        for(Recipe<?> var2 : this.recipes) {
            if (var1.contains(var2)) {
                var0.add(var2);
            }
        }

        return var0;
    }

    public List<Recipe<?>> getDisplayRecipes(boolean param0) {
        List<Recipe<?>> var0 = Lists.newArrayList();

        for(Recipe<?> var1 : this.recipes) {
            if (this.fitsDimensions.contains(var1) && this.craftable.contains(var1) == param0) {
                var0.add(var1);
            }
        }

        return var0;
    }

    public void add(Recipe<?> param0) {
        this.recipes.add(param0);
        if (this.singleResultItem) {
            ItemStack var0 = this.recipes.get(0).getResultItem();
            ItemStack var1 = param0.getResultItem();
            this.singleResultItem = ItemStack.isSame(var0, var1) && ItemStack.tagMatches(var0, var1);
        }

    }

    public boolean hasSingleResultItem() {
        return this.singleResultItem;
    }
}
