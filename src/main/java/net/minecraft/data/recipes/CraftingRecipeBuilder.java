package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.CraftingBookCategory;

public abstract class CraftingRecipeBuilder {
    protected static CraftingBookCategory determineBookCategory(RecipeCategory param0) {
        return switch(param0) {
            case BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
            case TOOLS, COMBAT -> CraftingBookCategory.EQUIPMENT;
            case REDSTONE -> CraftingBookCategory.REDSTONE;
            default -> CraftingBookCategory.MISC;
        };
    }

    protected abstract static class CraftingResult implements FinishedRecipe {
        private final CraftingBookCategory category;

        protected CraftingResult(CraftingBookCategory param0) {
            this.category = param0;
        }

        @Override
        public void serializeRecipeData(JsonObject param0) {
            param0.addProperty("category", this.category.getSerializedName());
        }
    }
}
