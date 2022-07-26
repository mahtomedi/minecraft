package net.minecraft.data.recipes.packs;

import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyRecipeProvider extends RecipeProvider {
    public UpdateOneTwentyRecipeProvider(PackOutput param0) {
        super(param0);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> param0) {
        generateForEnabledBlockFamilies(param0, FeatureFlagSet.of(FeatureFlags.UPDATE_1_20));
        threeByThreePacker(param0, RecipeCategory.BUILDING_BLOCKS, Blocks.BAMBOO_BLOCK, Items.BAMBOO);
        planksFromLogs(param0, Blocks.BAMBOO_PLANKS, ItemTags.BAMBOO_BLOCKS, 2);
        mosaicBuilder(param0, RecipeCategory.DECORATIONS, Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_SLAB);
        woodenBoat(param0, Items.BAMBOO_RAFT, Blocks.BAMBOO_PLANKS);
        chestBoat(param0, Items.BAMBOO_CHEST_RAFT, Items.BAMBOO_RAFT);
        hangingSign(param0, Items.OAK_HANGING_SIGN, Blocks.STRIPPED_OAK_LOG);
        hangingSign(param0, Items.SPRUCE_HANGING_SIGN, Blocks.STRIPPED_SPRUCE_LOG);
        hangingSign(param0, Items.BIRCH_HANGING_SIGN, Blocks.STRIPPED_BIRCH_LOG);
        hangingSign(param0, Items.JUNGLE_HANGING_SIGN, Blocks.STRIPPED_JUNGLE_LOG);
        hangingSign(param0, Items.ACACIA_HANGING_SIGN, Blocks.STRIPPED_ACACIA_LOG);
        hangingSign(param0, Items.DARK_OAK_HANGING_SIGN, Blocks.STRIPPED_DARK_OAK_LOG);
        hangingSign(param0, Items.MANGROVE_HANGING_SIGN, Blocks.STRIPPED_MANGROVE_LOG);
        hangingSign(param0, Items.BAMBOO_HANGING_SIGN, Items.STRIPPED_BAMBOO_BLOCK);
        hangingSign(param0, Items.CRIMSON_HANGING_SIGN, Blocks.STRIPPED_CRIMSON_STEM);
        hangingSign(param0, Items.WARPED_HANGING_SIGN, Blocks.STRIPPED_WARPED_STEM);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_BOOKSHELF)
            .define('#', ItemTags.PLANKS)
            .define('X', ItemTags.WOODEN_SLABS)
            .pattern("###")
            .pattern("XXX")
            .pattern("###")
            .unlockedBy("has_book", has(Items.BOOK))
            .save(param0);
    }
}
