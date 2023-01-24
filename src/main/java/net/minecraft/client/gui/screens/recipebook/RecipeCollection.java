package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeCollection {
    private final RegistryAccess registryAccess;
    private final List<Recipe<?>> recipes;
    private final boolean singleResultItem;
    private final Set<Recipe<?>> craftable = Sets.newHashSet();
    private final Set<Recipe<?>> fitsDimensions = Sets.newHashSet();
    private final Set<Recipe<?>> known = Sets.newHashSet();

    public RecipeCollection(RegistryAccess param0, List<Recipe<?>> param1) {
        this.registryAccess = param0;
        this.recipes = ImmutableList.copyOf(param1);
        if (param1.size() <= 1) {
            this.singleResultItem = true;
        } else {
            this.singleResultItem = allRecipesHaveSameResult(param0, param1);
        }

    }

    private static boolean allRecipesHaveSameResult(RegistryAccess param0, List<Recipe<?>> param1) {
        int var0 = param1.size();
        ItemStack var1 = param1.get(0).getResultItem(param0);

        for(int var2 = 1; var2 < var0; ++var2) {
            ItemStack var3 = param1.get(var2).getResultItem(param0);
            if (!ItemStack.isSame(var1, var3) || !ItemStack.tagMatches(var1, var3)) {
                return false;
            }
        }

        return true;
    }

    public RegistryAccess registryAccess() {
        return this.registryAccess;
    }

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
        for(Recipe<?> var0 : this.recipes) {
            boolean var1 = var0.canCraftInDimensions(param1, param2) && param3.contains(var0);
            if (var1) {
                this.fitsDimensions.add(var0);
            } else {
                this.fitsDimensions.remove(var0);
            }

            if (var1 && param0.canCraft(var0, null)) {
                this.craftable.add(var0);
            } else {
                this.craftable.remove(var0);
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

    public boolean hasSingleResultItem() {
        return this.singleResultItem;
    }
}
