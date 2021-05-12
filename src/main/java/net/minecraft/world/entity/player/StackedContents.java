package net.minecraft.world.entity.player;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class StackedContents {
    private static final int EMPTY = 0;
    public final Int2IntMap contents = new Int2IntOpenHashMap();

    public void accountSimpleStack(ItemStack param0) {
        if (!param0.isDamaged() && !param0.isEnchanted() && !param0.hasCustomHoverName()) {
            this.accountStack(param0);
        }

    }

    public void accountStack(ItemStack param0) {
        this.accountStack(param0, 64);
    }

    public void accountStack(ItemStack param0, int param1) {
        if (!param0.isEmpty()) {
            int var0 = getStackingIndex(param0);
            int var1 = Math.min(param1, param0.getCount());
            this.put(var0, var1);
        }

    }

    public static int getStackingIndex(ItemStack param0) {
        return Registry.ITEM.getId(param0.getItem());
    }

    boolean has(int param0) {
        return this.contents.get(param0) > 0;
    }

    int take(int param0, int param1) {
        int var0 = this.contents.get(param0);
        if (var0 >= param1) {
            this.contents.put(param0, var0 - param1);
            return param0;
        } else {
            return 0;
        }
    }

    void put(int param0, int param1) {
        this.contents.put(param0, this.contents.get(param0) + param1);
    }

    public boolean canCraft(Recipe<?> param0, @Nullable IntList param1) {
        return this.canCraft(param0, param1, 1);
    }

    public boolean canCraft(Recipe<?> param0, @Nullable IntList param1, int param2) {
        return new StackedContents.RecipePicker(param0).tryPick(param2, param1);
    }

    public int getBiggestCraftableStack(Recipe<?> param0, @Nullable IntList param1) {
        return this.getBiggestCraftableStack(param0, Integer.MAX_VALUE, param1);
    }

    public int getBiggestCraftableStack(Recipe<?> param0, int param1, @Nullable IntList param2) {
        return new StackedContents.RecipePicker(param0).tryPickAll(param1, param2);
    }

    public static ItemStack fromStackingIndex(int param0) {
        return param0 == 0 ? ItemStack.EMPTY : new ItemStack(Item.byId(param0));
    }

    public void clear() {
        this.contents.clear();
    }

    class RecipePicker {
        private final Recipe<?> recipe;
        private final List<Ingredient> ingredients = Lists.newArrayList();
        private final int ingredientCount;
        private final int[] items;
        private final int itemCount;
        private final BitSet data;
        private final IntList path = new IntArrayList();

        public RecipePicker(Recipe<?> param0) {
            this.recipe = param0;
            this.ingredients.addAll(param0.getIngredients());
            this.ingredients.removeIf(Ingredient::isEmpty);
            this.ingredientCount = this.ingredients.size();
            this.items = this.getUniqueAvailableIngredientItems();
            this.itemCount = this.items.length;
            this.data = new BitSet(this.ingredientCount + this.itemCount + this.ingredientCount + this.ingredientCount * this.itemCount);

            for(int param1 = 0; param1 < this.ingredients.size(); ++param1) {
                IntList var0 = this.ingredients.get(param1).getStackingIds();

                for(int var1 = 0; var1 < this.itemCount; ++var1) {
                    if (var0.contains(this.items[var1])) {
                        this.data.set(this.getIndex(true, var1, param1));
                    }
                }
            }

        }

        public boolean tryPick(int param0, @Nullable IntList param1) {
            if (param0 <= 0) {
                return true;
            } else {
                int var0;
                for(var0 = 0; this.dfs(param0); ++var0) {
                    StackedContents.this.take(this.items[this.path.getInt(0)], param0);
                    int var1 = this.path.size() - 1;
                    this.setSatisfied(this.path.getInt(var1));

                    for(int var2 = 0; var2 < var1; ++var2) {
                        this.toggleResidual((var2 & 1) == 0, this.path.get(var2), this.path.get(var2 + 1));
                    }

                    this.path.clear();
                    this.data.clear(0, this.ingredientCount + this.itemCount);
                }

                boolean var3 = var0 == this.ingredientCount;
                boolean var4 = var3 && param1 != null;
                if (var4) {
                    param1.clear();
                }

                this.data.clear(0, this.ingredientCount + this.itemCount + this.ingredientCount);
                int var5 = 0;
                List<Ingredient> var6 = this.recipe.getIngredients();

                for(int var7 = 0; var7 < var6.size(); ++var7) {
                    if (var4 && var6.get(var7).isEmpty()) {
                        param1.add(0);
                    } else {
                        for(int var8 = 0; var8 < this.itemCount; ++var8) {
                            if (this.hasResidual(false, var5, var8)) {
                                this.toggleResidual(true, var8, var5);
                                StackedContents.this.put(this.items[var8], param0);
                                if (var4) {
                                    param1.add(this.items[var8]);
                                }
                            }
                        }

                        ++var5;
                    }
                }

                return var3;
            }
        }

        private int[] getUniqueAvailableIngredientItems() {
            IntCollection var0 = new IntAVLTreeSet();

            for(Ingredient var1 : this.ingredients) {
                var0.addAll(var1.getStackingIds());
            }

            IntIterator var2 = var0.iterator();

            while(var2.hasNext()) {
                if (!StackedContents.this.has(var2.nextInt())) {
                    var2.remove();
                }
            }

            return var0.toIntArray();
        }

        private boolean dfs(int param0) {
            int var0 = this.itemCount;

            for(int var1 = 0; var1 < var0; ++var1) {
                if (StackedContents.this.contents.get(this.items[var1]) >= param0) {
                    this.visit(false, var1);

                    while(!this.path.isEmpty()) {
                        int var2 = this.path.size();
                        boolean var3 = (var2 & 1) == 1;
                        int var4 = this.path.getInt(var2 - 1);
                        if (!var3 && !this.isSatisfied(var4)) {
                            break;
                        }

                        int var5 = var3 ? this.ingredientCount : var0;
                        int var6 = 0;

                        while(true) {
                            if (var6 < var5) {
                                if (this.hasVisited(var3, var6) || !this.hasConnection(var3, var4, var6) || !this.hasResidual(var3, var4, var6)) {
                                    ++var6;
                                    continue;
                                }

                                this.visit(var3, var6);
                            }

                            var6 = this.path.size();
                            if (var6 == var2) {
                                this.path.removeInt(var6 - 1);
                            }
                            break;
                        }
                    }

                    if (!this.path.isEmpty()) {
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean isSatisfied(int param0) {
            return this.data.get(this.getSatisfiedIndex(param0));
        }

        private void setSatisfied(int param0) {
            this.data.set(this.getSatisfiedIndex(param0));
        }

        private int getSatisfiedIndex(int param0) {
            return this.ingredientCount + this.itemCount + param0;
        }

        private boolean hasConnection(boolean param0, int param1, int param2) {
            return this.data.get(this.getIndex(param0, param1, param2));
        }

        private boolean hasResidual(boolean param0, int param1, int param2) {
            return param0 != this.data.get(1 + this.getIndex(param0, param1, param2));
        }

        private void toggleResidual(boolean param0, int param1, int param2) {
            this.data.flip(1 + this.getIndex(param0, param1, param2));
        }

        private int getIndex(boolean param0, int param1, int param2) {
            int var0 = param0 ? param1 * this.ingredientCount + param2 : param2 * this.ingredientCount + param1;
            return this.ingredientCount + this.itemCount + this.ingredientCount + 2 * var0;
        }

        private void visit(boolean param0, int param1) {
            this.data.set(this.getVisitedIndex(param0, param1));
            this.path.add(param1);
        }

        private boolean hasVisited(boolean param0, int param1) {
            return this.data.get(this.getVisitedIndex(param0, param1));
        }

        private int getVisitedIndex(boolean param0, int param1) {
            return (param0 ? 0 : this.ingredientCount) + param1;
        }

        public int tryPickAll(int param0, @Nullable IntList param1) {
            int var0 = 0;
            int var1 = Math.min(param0, this.getMinIngredientCount()) + 1;

            while(true) {
                int var2 = (var0 + var1) / 2;
                if (this.tryPick(var2, null)) {
                    if (var1 - var0 <= 1) {
                        if (var2 > 0) {
                            this.tryPick(var2, param1);
                        }

                        return var2;
                    }

                    var0 = var2;
                } else {
                    var1 = var2;
                }
            }
        }

        private int getMinIngredientCount() {
            int var0 = Integer.MAX_VALUE;

            for(Ingredient var1 : this.ingredients) {
                int var2 = 0;

                for(int var3 : var1.getStackingIds()) {
                    var2 = Math.max(var2, StackedContents.this.contents.get(var3));
                }

                if (var0 > 0) {
                    var0 = Math.min(var0, var2);
                }
            }

            return var0;
        }
    }
}
