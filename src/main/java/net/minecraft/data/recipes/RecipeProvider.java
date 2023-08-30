package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class RecipeProvider implements DataProvider {
    final PackOutput.PathProvider recipePathProvider;
    final PackOutput.PathProvider advancementPathProvider;
    private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> SHAPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>>builder(
            
        )
        .put(BlockFamily.Variant.BUTTON, (param0, param1) -> buttonBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.CHISELED, (param0, param1) -> chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.CUT, (param0, param1) -> cutBuilder(RecipeCategory.BUILDING_BLOCKS, param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.DOOR, (param0, param1) -> doorBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.CUSTOM_FENCE, (param0, param1) -> fenceBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.FENCE, (param0, param1) -> fenceBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (param0, param1) -> fenceGateBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.FENCE_GATE, (param0, param1) -> fenceGateBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.SIGN, (param0, param1) -> signBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.SLAB, (param0, param1) -> slabBuilder(RecipeCategory.BUILDING_BLOCKS, param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.STAIRS, (param0, param1) -> stairBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.PRESSURE_PLATE, (param0, param1) -> pressurePlateBuilder(RecipeCategory.REDSTONE, param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.POLISHED, (param0, param1) -> polishedBuilder(RecipeCategory.BUILDING_BLOCKS, param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.TRAPDOOR, (param0, param1) -> trapdoorBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.WALL, (param0, param1) -> wallBuilder(RecipeCategory.DECORATIONS, param0, Ingredient.of(param1)))
        .build();

    public RecipeProvider(PackOutput param0) {
        this.recipePathProvider = param0.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.advancementPathProvider = param0.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput param0) {
        final Set<ResourceLocation> var0 = Sets.newHashSet();
        final List<CompletableFuture<?>> var1 = new ArrayList<>();
        this.buildRecipes(new RecipeOutput() {
            @Override
            public void accept(FinishedRecipe param0x) {
                if (!var0.add(param0.id())) {
                    throw new IllegalStateException("Duplicate recipe " + param0.id());
                } else {
                    var1.add(DataProvider.saveStable(param0, param0.serializeRecipe(), RecipeProvider.this.recipePathProvider.json(param0.id())));
                    AdvancementHolder var0 = param0.advancement();
                    if (var0 != null) {
                        JsonObject var1 = var0.value().serializeToJson();
                        var1.add(DataProvider.saveStable(param0, var1, RecipeProvider.this.advancementPathProvider.json(var0.id())));
                    }

                }
            }

            @Override
            public Advancement.Builder advancement() {
                return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
            }
        });
        return CompletableFuture.allOf(var1.toArray(param0x -> new CompletableFuture[param0x]));
    }

    protected CompletableFuture<?> buildAdvancement(CachedOutput param0, AdvancementHolder param1) {
        return DataProvider.saveStable(param0, param1.value().serializeToJson(), this.advancementPathProvider.json(param1.id()));
    }

    protected abstract void buildRecipes(RecipeOutput var1);

    protected static void generateForEnabledBlockFamilies(RecipeOutput param0, FeatureFlagSet param1) {
        BlockFamilies.getAllFamilies().filter(param1x -> param1x.shouldGenerateRecipe(param1)).forEach(param1x -> generateRecipes(param0, param1x));
    }

    protected static void oneToOneConversionRecipe(RecipeOutput param0, ItemLike param1, ItemLike param2, @Nullable String param3) {
        oneToOneConversionRecipe(param0, param1, param2, param3, 1);
    }

    protected static void oneToOneConversionRecipe(RecipeOutput param0, ItemLike param1, ItemLike param2, @Nullable String param3, int param4) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, param1, param4)
            .requires(param2)
            .group(param3)
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0, getConversionRecipeName(param1, param2));
    }

    protected static void oreSmelting(
        RecipeOutput param0, List<ItemLike> param1, RecipeCategory param2, ItemLike param3, float param4, int param5, String param6
    ) {
        oreCooking(param0, RecipeSerializer.SMELTING_RECIPE, param1, param2, param3, param4, param5, param6, "_from_smelting");
    }

    protected static void oreBlasting(
        RecipeOutput param0, List<ItemLike> param1, RecipeCategory param2, ItemLike param3, float param4, int param5, String param6
    ) {
        oreCooking(param0, RecipeSerializer.BLASTING_RECIPE, param1, param2, param3, param4, param5, param6, "_from_blasting");
    }

    private static void oreCooking(
        RecipeOutput param0,
        RecipeSerializer<? extends AbstractCookingRecipe> param1,
        List<ItemLike> param2,
        RecipeCategory param3,
        ItemLike param4,
        float param5,
        int param6,
        String param7,
        String param8
    ) {
        for(ItemLike var0 : param2) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(var0), param3, param4, param5, param6, param1)
                .group(param7)
                .unlockedBy(getHasName(var0), has(var0))
                .save(param0, getItemName(param4) + param8 + "_" + getItemName(var0));
        }

    }

    protected static void netheriteSmithing(RecipeOutput param0, Item param1, RecipeCategory param2, Item param3) {
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(param1), Ingredient.of(Items.NETHERITE_INGOT), param2, param3
            )
            .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
            .save(param0, getItemName(param3) + "_smithing");
    }

    protected static void trimSmithing(RecipeOutput param0, Item param1, ResourceLocation param2) {
        SmithingTrimRecipeBuilder.smithingTrim(
                Ingredient.of(param1), Ingredient.of(ItemTags.TRIMMABLE_ARMOR), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC
            )
            .unlocks("has_smithing_trim_template", has(param1))
            .save(param0, param2);
    }

    protected static void twoByTwoPacker(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        ShapedRecipeBuilder.shaped(param1, param2, 1).define('#', param3).pattern("##").pattern("##").unlockedBy(getHasName(param3), has(param3)).save(param0);
    }

    protected static void threeByThreePacker(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3, String param4) {
        ShapelessRecipeBuilder.shapeless(param1, param2).requires(param3, 9).unlockedBy(param4, has(param3)).save(param0);
    }

    protected static void threeByThreePacker(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        threeByThreePacker(param0, param1, param2, param3, getHasName(param3));
    }

    protected static void planksFromLog(RecipeOutput param0, ItemLike param1, TagKey<Item> param2, int param3) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, param1, param3)
            .requires(param2)
            .group("planks")
            .unlockedBy("has_log", has(param2))
            .save(param0);
    }

    protected static void planksFromLogs(RecipeOutput param0, ItemLike param1, TagKey<Item> param2, int param3) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, param1, param3)
            .requires(param2)
            .group("planks")
            .unlockedBy("has_logs", has(param2))
            .save(param0);
    }

    protected static void woodFromLogs(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, param1, 3)
            .define('#', param2)
            .pattern("##")
            .pattern("##")
            .group("bark")
            .unlockedBy("has_log", has(param2))
            .save(param0);
    }

    protected static void woodenBoat(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, param1)
            .define('#', param2)
            .pattern("# #")
            .pattern("###")
            .group("boat")
            .unlockedBy("in_water", insideOf(Blocks.WATER))
            .save(param0);
    }

    protected static void chestBoat(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, param1)
            .requires(Blocks.CHEST)
            .requires(param2)
            .group("chest_boat")
            .unlockedBy("has_boat", has(ItemTags.BOATS))
            .save(param0);
    }

    private static RecipeBuilder buttonBuilder(ItemLike param0, Ingredient param1) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, param0).requires(param1);
    }

    protected static RecipeBuilder doorBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, param0, 3).define('#', param1).pattern("##").pattern("##").pattern("##");
    }

    private static RecipeBuilder fenceBuilder(ItemLike param0, Ingredient param1) {
        int var0 = param0 == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item var1 = param0 == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, param0, var0).define('W', param1).define('#', var1).pattern("W#W").pattern("W#W");
    }

    private static RecipeBuilder fenceGateBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, param0).define('#', Items.STICK).define('W', param1).pattern("#W#").pattern("#W#");
    }

    protected static void pressurePlate(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        pressurePlateBuilder(RecipeCategory.REDSTONE, param1, Ingredient.of(param2)).unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    private static RecipeBuilder pressurePlateBuilder(RecipeCategory param0, ItemLike param1, Ingredient param2) {
        return ShapedRecipeBuilder.shaped(param0, param1).define('#', param2).pattern("##");
    }

    protected static void slab(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        slabBuilder(param1, param2, Ingredient.of(param3)).unlockedBy(getHasName(param3), has(param3)).save(param0);
    }

    protected static RecipeBuilder slabBuilder(RecipeCategory param0, ItemLike param1, Ingredient param2) {
        return ShapedRecipeBuilder.shaped(param0, param1, 6).define('#', param2).pattern("###");
    }

    protected static RecipeBuilder stairBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, param0, 4).define('#', param1).pattern("#  ").pattern("## ").pattern("###");
    }

    private static RecipeBuilder trapdoorBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, param0, 2).define('#', param1).pattern("###").pattern("###");
    }

    private static RecipeBuilder signBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, param0, 3)
            .group("sign")
            .define('#', param1)
            .define('X', Items.STICK)
            .pattern("###")
            .pattern("###")
            .pattern(" X ");
    }

    protected static void hangingSign(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, param1, 6)
            .group("hanging_sign")
            .define('#', param2)
            .define('X', Items.CHAIN)
            .pattern("X X")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_stripped_logs", has(param2))
            .save(param0);
    }

    protected static void colorBlockWithDye(RecipeOutput param0, List<Item> param1, List<Item> param2, String param3) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            Item var1 = param1.get(var0);
            Item var2 = param2.get(var0);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, var2)
                .requires(var1)
                .requires(Ingredient.of(param2.stream().filter(param1x -> !param1x.equals(var2)).map(ItemStack::new)))
                .group(param3)
                .unlockedBy("has_needed_dye", has(var1))
                .save(param0, "dye_" + getItemName(var2));
        }

    }

    protected static void carpet(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, param1, 3)
            .define('#', param2)
            .pattern("##")
            .group("carpet")
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0);
    }

    protected static void bedFromPlanksAndWool(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, param1)
            .define('#', param2)
            .define('X', ItemTags.PLANKS)
            .pattern("###")
            .pattern("XXX")
            .group("bed")
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0);
    }

    protected static void banner(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, param1)
            .define('#', param2)
            .define('|', Items.STICK)
            .pattern("###")
            .pattern("###")
            .pattern(" | ")
            .group("banner")
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0);
    }

    protected static void stainedGlassFromGlassAndDye(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, param1, 8)
            .define('#', Blocks.GLASS)
            .define('X', param2)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .group("stained_glass")
            .unlockedBy("has_glass", has(Blocks.GLASS))
            .save(param0);
    }

    protected static void stainedGlassPaneFromStainedGlass(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, param1, 16)
            .define('#', param2)
            .pattern("###")
            .pattern("###")
            .group("stained_glass_pane")
            .unlockedBy("has_glass", has(param2))
            .save(param0);
    }

    protected static void stainedGlassPaneFromGlassPaneAndDye(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, param1, 8)
            .define('#', Blocks.GLASS_PANE)
            .define('$', param2)
            .pattern("###")
            .pattern("#$#")
            .pattern("###")
            .group("stained_glass_pane")
            .unlockedBy("has_glass_pane", has(Blocks.GLASS_PANE))
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0, getConversionRecipeName(param1, Blocks.GLASS_PANE));
    }

    protected static void coloredTerracottaFromTerracottaAndDye(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, param1, 8)
            .define('#', Blocks.TERRACOTTA)
            .define('X', param2)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .group("stained_terracotta")
            .unlockedBy("has_terracotta", has(Blocks.TERRACOTTA))
            .save(param0);
    }

    protected static void concretePowder(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, param1, 8)
            .requires(param2)
            .requires(Blocks.SAND, 4)
            .requires(Blocks.GRAVEL, 4)
            .group("concrete_powder")
            .unlockedBy("has_sand", has(Blocks.SAND))
            .unlockedBy("has_gravel", has(Blocks.GRAVEL))
            .save(param0);
    }

    protected static void candle(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, param1)
            .requires(Blocks.CANDLE)
            .requires(param2)
            .group("dyed_candle")
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0);
    }

    protected static void wall(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        wallBuilder(param1, param2, Ingredient.of(param3)).unlockedBy(getHasName(param3), has(param3)).save(param0);
    }

    private static RecipeBuilder wallBuilder(RecipeCategory param0, ItemLike param1, Ingredient param2) {
        return ShapedRecipeBuilder.shaped(param0, param1, 6).define('#', param2).pattern("###").pattern("###");
    }

    protected static void polished(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        polishedBuilder(param1, param2, Ingredient.of(param3)).unlockedBy(getHasName(param3), has(param3)).save(param0);
    }

    private static RecipeBuilder polishedBuilder(RecipeCategory param0, ItemLike param1, Ingredient param2) {
        return ShapedRecipeBuilder.shaped(param0, param1, 4).define('S', param2).pattern("SS").pattern("SS");
    }

    protected static void cut(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        cutBuilder(param1, param2, Ingredient.of(param3)).unlockedBy(getHasName(param3), has(param3)).save(param0);
    }

    private static ShapedRecipeBuilder cutBuilder(RecipeCategory param0, ItemLike param1, Ingredient param2) {
        return ShapedRecipeBuilder.shaped(param0, param1, 4).define('#', param2).pattern("##").pattern("##");
    }

    protected static void chiseled(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        chiseledBuilder(param1, param2, Ingredient.of(param3)).unlockedBy(getHasName(param3), has(param3)).save(param0);
    }

    protected static void mosaicBuilder(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        ShapedRecipeBuilder.shaped(param1, param2).define('#', param3).pattern("#").pattern("#").unlockedBy(getHasName(param3), has(param3)).save(param0);
    }

    protected static ShapedRecipeBuilder chiseledBuilder(RecipeCategory param0, ItemLike param1, Ingredient param2) {
        return ShapedRecipeBuilder.shaped(param0, param1).define('#', param2).pattern("#").pattern("#");
    }

    protected static void stonecutterResultFromBase(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3) {
        stonecutterResultFromBase(param0, param1, param2, param3, 1);
    }

    protected static void stonecutterResultFromBase(RecipeOutput param0, RecipeCategory param1, ItemLike param2, ItemLike param3, int param4) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(param3), param1, param2, param4)
            .unlockedBy(getHasName(param3), has(param3))
            .save(param0, getConversionRecipeName(param2, param3) + "_stonecutting");
    }

    private static void smeltingResultFromBase(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(param2), RecipeCategory.BUILDING_BLOCKS, param1, 0.1F, 200)
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0);
    }

    protected static void nineBlockStorageRecipes(RecipeOutput param0, RecipeCategory param1, ItemLike param2, RecipeCategory param3, ItemLike param4) {
        nineBlockStorageRecipes(param0, param1, param2, param3, param4, getSimpleRecipeName(param4), null, getSimpleRecipeName(param2), null);
    }

    protected static void nineBlockStorageRecipesWithCustomPacking(
        RecipeOutput param0, RecipeCategory param1, ItemLike param2, RecipeCategory param3, ItemLike param4, String param5, String param6
    ) {
        nineBlockStorageRecipes(param0, param1, param2, param3, param4, param5, param6, getSimpleRecipeName(param2), null);
    }

    protected static void nineBlockStorageRecipesRecipesWithCustomUnpacking(
        RecipeOutput param0, RecipeCategory param1, ItemLike param2, RecipeCategory param3, ItemLike param4, String param5, String param6
    ) {
        nineBlockStorageRecipes(param0, param1, param2, param3, param4, getSimpleRecipeName(param4), null, param5, param6);
    }

    private static void nineBlockStorageRecipes(
        RecipeOutput param0,
        RecipeCategory param1,
        ItemLike param2,
        RecipeCategory param3,
        ItemLike param4,
        String param5,
        @Nullable String param6,
        String param7,
        @Nullable String param8
    ) {
        ShapelessRecipeBuilder.shapeless(param1, param2, 9)
            .requires(param4)
            .group(param8)
            .unlockedBy(getHasName(param4), has(param4))
            .save(param0, new ResourceLocation(param7));
        ShapedRecipeBuilder.shaped(param3, param4)
            .define('#', param2)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .group(param6)
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0, new ResourceLocation(param5));
    }

    protected static void copySmithingTemplate(RecipeOutput param0, ItemLike param1, TagKey<Item> param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, param1, 2)
            .define('#', Items.DIAMOND)
            .define('C', param2)
            .define('S', param1)
            .pattern("#S#")
            .pattern("#C#")
            .pattern("###")
            .unlockedBy(getHasName(param1), has(param1))
            .save(param0);
    }

    protected static void copySmithingTemplate(RecipeOutput param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, param1, 2)
            .define('#', Items.DIAMOND)
            .define('C', param2)
            .define('S', param1)
            .pattern("#S#")
            .pattern("#C#")
            .pattern("###")
            .unlockedBy(getHasName(param1), has(param1))
            .save(param0);
    }

    protected static void cookRecipes(RecipeOutput param0, String param1, RecipeSerializer<? extends AbstractCookingRecipe> param2, int param3) {
        simpleCookingRecipe(param0, param1, param2, param3, Items.BEEF, Items.COOKED_BEEF, 0.35F);
        simpleCookingRecipe(param0, param1, param2, param3, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
        simpleCookingRecipe(param0, param1, param2, param3, Items.COD, Items.COOKED_COD, 0.35F);
        simpleCookingRecipe(param0, param1, param2, param3, Items.KELP, Items.DRIED_KELP, 0.1F);
        simpleCookingRecipe(param0, param1, param2, param3, Items.SALMON, Items.COOKED_SALMON, 0.35F);
        simpleCookingRecipe(param0, param1, param2, param3, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
        simpleCookingRecipe(param0, param1, param2, param3, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
        simpleCookingRecipe(param0, param1, param2, param3, Items.POTATO, Items.BAKED_POTATO, 0.35F);
        simpleCookingRecipe(param0, param1, param2, param3, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
    }

    private static void simpleCookingRecipe(
        RecipeOutput param0,
        String param1,
        RecipeSerializer<? extends AbstractCookingRecipe> param2,
        int param3,
        ItemLike param4,
        ItemLike param5,
        float param6
    ) {
        SimpleCookingRecipeBuilder.generic(Ingredient.of(param4), RecipeCategory.FOOD, param5, param6, param3, param2)
            .unlockedBy(getHasName(param4), has(param4))
            .save(param0, getItemName(param5) + "_from_" + param1);
    }

    protected static void waxRecipes(RecipeOutput param0) {
        HoneycombItem.WAXABLES
            .get()
            .forEach(
                (param1, param2) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, param2)
                        .requires(param1)
                        .requires(Items.HONEYCOMB)
                        .group(getItemName(param2))
                        .unlockedBy(getHasName(param1), has(param1))
                        .save(param0, getConversionRecipeName(param2, Items.HONEYCOMB))
            );
    }

    protected static void generateRecipes(RecipeOutput param0, BlockFamily param1) {
        param1.getVariants()
            .forEach(
                (param2, param3) -> {
                    BiFunction<ItemLike, ItemLike, RecipeBuilder> var0x = SHAPE_BUILDERS.get(param2);
                    ItemLike var1x = getBaseBlock(param1, param2);
                    if (var0x != null) {
                        RecipeBuilder var2 = (RecipeBuilder)var0x.apply(param3, var1x);
                        param1.getRecipeGroupPrefix()
                            .ifPresent(param2x -> var2.group(param2x + (param2 == BlockFamily.Variant.CUT ? "" : "_" + param2.getRecipeGroup())));
                        var2.unlockedBy(param1.getRecipeUnlockedBy().orElseGet(() -> getHasName(var1x)), has(var1x));
                        var2.save(param0);
                    }
        
                    if (param2 == BlockFamily.Variant.CRACKED) {
                        smeltingResultFromBase(param0, param3, var1x);
                    }
        
                }
            );
    }

    private static Block getBaseBlock(BlockFamily param0, BlockFamily.Variant param1) {
        if (param1 == BlockFamily.Variant.CHISELED) {
            if (!param0.getVariants().containsKey(BlockFamily.Variant.SLAB)) {
                throw new IllegalStateException("Slab is not defined for the family.");
            } else {
                return param0.get(BlockFamily.Variant.SLAB);
            }
        } else {
            return param0.getBaseBlock();
        }
    }

    private static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block param0) {
        return CriteriaTriggers.ENTER_BLOCK.createCriterion(new EnterBlockTrigger.TriggerInstance(Optional.empty(), param0, Optional.empty()));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints param0, ItemLike param1) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(param1).withCount(param0));
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike param0) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(param0));
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> param0) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(param0));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... param0) {
        return inventoryTrigger(Arrays.stream(param0).map(ItemPredicate.Builder::build).toArray(param0x -> new ItemPredicate[param0x]));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... param0) {
        return CriteriaTriggers.INVENTORY_CHANGED
            .createCriterion(
                new InventoryChangeTrigger.TriggerInstance(
                    Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, List.of(param0)
                )
            );
    }

    protected static String getHasName(ItemLike param0) {
        return "has_" + getItemName(param0);
    }

    protected static String getItemName(ItemLike param0) {
        return BuiltInRegistries.ITEM.getKey(param0.asItem()).getPath();
    }

    protected static String getSimpleRecipeName(ItemLike param0) {
        return getItemName(param0);
    }

    protected static String getConversionRecipeName(ItemLike param0, ItemLike param1) {
        return getItemName(param0) + "_from_" + getItemName(param1);
    }

    protected static String getSmeltingRecipeName(ItemLike param0) {
        return getItemName(param0) + "_from_smelting";
    }

    protected static String getBlastingRecipeName(ItemLike param0) {
        return getItemName(param0) + "_from_blasting";
    }

    @Override
    public final String getName() {
        return "Recipes";
    }
}
