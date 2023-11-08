package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractCookingRecipe implements Recipe<Container> {
    protected final RecipeType<?> type;
    protected final CookingBookCategory category;
    protected final String group;
    protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final float experience;
    protected final int cookingTime;

    public AbstractCookingRecipe(RecipeType<?> param0, String param1, CookingBookCategory param2, Ingredient param3, ItemStack param4, float param5, int param6) {
        this.type = param0;
        this.category = param2;
        this.group = param1;
        this.ingredient = param3;
        this.result = param4;
        this.experience = param5;
        this.cookingTime = param6;
    }

    @Override
    public boolean matches(Container param0, Level param1) {
        return this.ingredient.test(param0.getItem(0));
    }

    @Override
    public ItemStack assemble(Container param0, RegistryAccess param1) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> var0 = NonNullList.create();
        var0.add(this.ingredient);
        return var0;
    }

    public float getExperience() {
        return this.experience;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess param0) {
        return this.result;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    public int getCookingTime() {
        return this.cookingTime;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    public CookingBookCategory category() {
        return this.category;
    }

    public interface Factory<T extends AbstractCookingRecipe> {
        T create(String var1, CookingBookCategory var2, Ingredient var3, ItemStack var4, float var5, int var6);
    }
}
