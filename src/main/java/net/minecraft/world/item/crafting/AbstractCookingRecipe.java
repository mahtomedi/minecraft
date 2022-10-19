package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractCookingRecipe implements Recipe<Container> {
    protected final RecipeType<?> type;
    protected final ResourceLocation id;
    private final CookingBookCategory category;
    protected final String group;
    protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final float experience;
    protected final int cookingTime;

    public AbstractCookingRecipe(
        RecipeType<?> param0, ResourceLocation param1, String param2, CookingBookCategory param3, Ingredient param4, ItemStack param5, float param6, int param7
    ) {
        this.type = param0;
        this.category = param3;
        this.id = param1;
        this.group = param2;
        this.ingredient = param4;
        this.result = param5;
        this.experience = param6;
        this.cookingTime = param7;
    }

    @Override
    public boolean matches(Container param0, Level param1) {
        return this.ingredient.test(param0.getItem(0));
    }

    @Override
    public ItemStack assemble(Container param0) {
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
    public ItemStack getResultItem() {
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
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    public CookingBookCategory category() {
        return this.category;
    }
}
