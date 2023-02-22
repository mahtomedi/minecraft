package net.minecraft.data.recipes.packs;

import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
        hangingSign(param0, Items.CHERRY_HANGING_SIGN, Blocks.STRIPPED_CHERRY_LOG);
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
        trimSmithing(param0, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
        trimSmithing(param0, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
        netheriteSmithing(param0, Items.DIAMOND_CHESTPLATE, RecipeCategory.COMBAT, Items.NETHERITE_CHESTPLATE);
        netheriteSmithing(param0, Items.DIAMOND_LEGGINGS, RecipeCategory.COMBAT, Items.NETHERITE_LEGGINGS);
        netheriteSmithing(param0, Items.DIAMOND_HELMET, RecipeCategory.COMBAT, Items.NETHERITE_HELMET);
        netheriteSmithing(param0, Items.DIAMOND_BOOTS, RecipeCategory.COMBAT, Items.NETHERITE_BOOTS);
        netheriteSmithing(param0, Items.DIAMOND_SWORD, RecipeCategory.COMBAT, Items.NETHERITE_SWORD);
        netheriteSmithing(param0, Items.DIAMOND_AXE, RecipeCategory.TOOLS, Items.NETHERITE_AXE);
        netheriteSmithing(param0, Items.DIAMOND_PICKAXE, RecipeCategory.TOOLS, Items.NETHERITE_PICKAXE);
        netheriteSmithing(param0, Items.DIAMOND_HOE, RecipeCategory.TOOLS, Items.NETHERITE_HOE);
        netheriteSmithing(param0, Items.DIAMOND_SHOVEL, RecipeCategory.TOOLS, Items.NETHERITE_SHOVEL);
        copySmithingTemplate(param0, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.NETHERRACK);
        copySmithingTemplate(param0, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
        copySmithingTemplate(param0, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SANDSTONE);
        copySmithingTemplate(param0, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
        copySmithingTemplate(param0, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.MOSSY_COBBLESTONE);
        copySmithingTemplate(param0, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLED_DEEPSLATE);
        copySmithingTemplate(param0, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.END_STONE);
        copySmithingTemplate(param0, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COBBLESTONE);
        copySmithingTemplate(param0, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.PRISMARINE);
        copySmithingTemplate(param0, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BLACKSTONE);
        copySmithingTemplate(param0, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.NETHERRACK);
        copySmithingTemplate(param0, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.PURPUR_BLOCK);
        oneToOneConversionRecipe(param0, Items.ORANGE_DYE, Blocks.TORCHFLOWER, "orange_dye");
        planksFromLog(param0, Blocks.CHERRY_PLANKS, ItemTags.CHERRY_LOGS, 4);
        woodFromLogs(param0, Blocks.CHERRY_WOOD, Blocks.CHERRY_LOG);
        woodFromLogs(param0, Blocks.STRIPPED_CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG);
        woodenBoat(param0, Items.CHERRY_BOAT, Blocks.CHERRY_PLANKS);
        chestBoat(param0, Items.CHERRY_CHEST_BOAT, Items.CHERRY_BOAT);
        oneToOneConversionRecipe(param0, Items.PINK_DYE, Items.PINK_PETALS, "pink_dye", 1);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.BRUSH)
            .define('X', Items.FEATHER)
            .define('#', Items.COPPER_INGOT)
            .define('I', Items.STICK)
            .pattern("X")
            .pattern("#")
            .pattern("I")
            .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.DECORATED_POT)
            .define('#', Items.BRICK)
            .pattern(" # ")
            .pattern("# #")
            .pattern(" # ")
            .unlockedBy("has_brick", has(ItemTags.DECORATED_POT_SHARDS))
            .save(param0, "decorated_pot_simple");
        SpecialRecipeBuilder.special(RecipeSerializer.DECORATED_POT_RECIPE).save(param0, "decorated_pot");
    }
}
