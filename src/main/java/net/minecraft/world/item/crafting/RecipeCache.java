package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RecipeCache {
    private final RecipeCache.Entry[] entries;
    private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference<>(null);

    public RecipeCache(int param0) {
        this.entries = new RecipeCache.Entry[param0];
    }

    public Optional<CraftingRecipe> get(Level param0, CraftingContainer param1) {
        if (param1.isEmpty()) {
            return Optional.empty();
        } else {
            this.validateRecipeManager(param0);

            for(int var0 = 0; var0 < this.entries.length; ++var0) {
                RecipeCache.Entry var1 = this.entries[var0];
                if (var1 != null && var1.matches(param1.getItems())) {
                    this.moveEntryToFront(var0);
                    return Optional.ofNullable(var1.value());
                }
            }

            return this.compute(param1, param0);
        }
    }

    private void validateRecipeManager(Level param0) {
        RecipeManager var0 = param0.getRecipeManager();
        if (var0 != this.cachedRecipeManager.get()) {
            this.cachedRecipeManager = new WeakReference<>(var0);
            Arrays.fill(this.entries, null);
        }

    }

    private Optional<CraftingRecipe> compute(CraftingContainer param0, Level param1) {
        Optional<RecipeHolder<CraftingRecipe>> var0 = param1.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, param0, param1);
        this.insert(param0.getItems(), var0.map(RecipeHolder::value).orElse(null));
        return var0.map(RecipeHolder::value);
    }

    private void moveEntryToFront(int param0) {
        if (param0 > 0) {
            RecipeCache.Entry var0 = this.entries[param0];
            System.arraycopy(this.entries, 0, this.entries, 1, param0);
            this.entries[0] = var0;
        }

    }

    private void insert(List<ItemStack> param0, @Nullable CraftingRecipe param1) {
        NonNullList<ItemStack> var0 = NonNullList.withSize(param0.size(), ItemStack.EMPTY);

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            var0.set(var1, param0.get(var1).copyWithCount(1));
        }

        System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
        this.entries[0] = new RecipeCache.Entry(var0, param1);
    }

    static record Entry(NonNullList<ItemStack> key, @Nullable CraftingRecipe value) {
        public boolean matches(List<ItemStack> param0) {
            if (this.key.size() != param0.size()) {
                return false;
            } else {
                for(int var0 = 0; var0 < this.key.size(); ++var0) {
                    if (!ItemStack.isSameItemSameTags(this.key.get(var0), param0.get(var0))) {
                        return false;
                    }
                }

                return true;
            }
        }
    }
}
