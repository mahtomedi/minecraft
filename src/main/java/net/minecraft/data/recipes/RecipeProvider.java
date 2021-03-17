package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeProvider implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;
    private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> shapeBuilders = ImmutableMap.<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>>builder(
            
        )
        .put(BlockFamily.Variant.BUTTON, (param0, param1) -> buttonBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.CHISELED, (param0, param1) -> chiseledBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.DOOR, (param0, param1) -> doorBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.FENCE, (param0, param1) -> fenceBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.FENCE_GATE, (param0, param1) -> fenceGateBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.SIGN, (param0, param1) -> signBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.SLAB, (param0, param1) -> slabBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.STAIRS, (param0, param1) -> stairBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.PRESSURE_PLATE, (param0, param1) -> pressurePlateBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.POLISHED, (param0, param1) -> polishedBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.TRAPDOOR, (param0, param1) -> trapdoorBuilder(param0, Ingredient.of(param1)))
        .put(BlockFamily.Variant.WALL, (param0, param1) -> wallBuilder(param0, Ingredient.of(param1)))
        .build();

    public RecipeProvider(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(HashCache param0) {
        Path var0 = this.generator.getOutputFolder();
        Set<ResourceLocation> var1 = Sets.newHashSet();
        buildCraftingRecipes(
            param3 -> {
                if (!var1.add(param3.getId())) {
                    throw new IllegalStateException("Duplicate recipe " + param3.getId());
                } else {
                    saveRecipe(
                        param0,
                        param3.serializeRecipe(),
                        var0.resolve("data/" + param3.getId().getNamespace() + "/recipes/" + param3.getId().getPath() + ".json")
                    );
                    JsonObject var0x = param3.serializeAdvancement();
                    if (var0x != null) {
                        saveAdvancement(
                            param0,
                            var0x,
                            var0.resolve("data/" + param3.getId().getNamespace() + "/advancements/" + param3.getAdvancementId().getPath() + ".json")
                        );
                    }
    
                }
            }
        );
        saveAdvancement(
            param0,
            Advancement.Builder.advancement().addCriterion("impossible", new ImpossibleTrigger.TriggerInstance()).serializeToJson(),
            var0.resolve("data/minecraft/advancements/recipes/root.json")
        );
    }

    private static void saveRecipe(HashCache param0, JsonObject param1, Path param2) {
        try {
            String var0 = GSON.toJson((JsonElement)param1);
            String var1 = SHA1.hashUnencodedChars(var0).toString();
            if (!Objects.equals(param0.getHash(param2), var1) || !Files.exists(param2)) {
                Files.createDirectories(param2.getParent());

                try (BufferedWriter var2 = Files.newBufferedWriter(param2)) {
                    var2.write(var0);
                }
            }

            param0.putNew(param2, var1);
        } catch (IOException var18) {
            LOGGER.error("Couldn't save recipe {}", param2, var18);
        }

    }

    private static void saveAdvancement(HashCache param0, JsonObject param1, Path param2) {
        try {
            String var0 = GSON.toJson((JsonElement)param1);
            String var1 = SHA1.hashUnencodedChars(var0).toString();
            if (!Objects.equals(param0.getHash(param2), var1) || !Files.exists(param2)) {
                Files.createDirectories(param2.getParent());

                try (BufferedWriter var2 = Files.newBufferedWriter(param2)) {
                    var2.write(var0);
                }
            }

            param0.putNew(param2, var1);
        } catch (IOException var18) {
            LOGGER.error("Couldn't save recipe advancement {}", param2, var18);
        }

    }

    private static void buildCraftingRecipes(Consumer<FinishedRecipe> param0) {
        BlockFamilies.getAllFamilies().filter(BlockFamily::shouldGenerateRecipe).forEach(param1 -> generateRecipes(param0, param1));
        planksFromLog(param0, Blocks.ACACIA_PLANKS, ItemTags.ACACIA_LOGS);
        planksFromLogs(param0, Blocks.BIRCH_PLANKS, ItemTags.BIRCH_LOGS);
        planksFromLogs(param0, Blocks.CRIMSON_PLANKS, ItemTags.CRIMSON_STEMS);
        planksFromLog(param0, Blocks.DARK_OAK_PLANKS, ItemTags.DARK_OAK_LOGS);
        planksFromLogs(param0, Blocks.JUNGLE_PLANKS, ItemTags.JUNGLE_LOGS);
        planksFromLogs(param0, Blocks.OAK_PLANKS, ItemTags.OAK_LOGS);
        planksFromLogs(param0, Blocks.SPRUCE_PLANKS, ItemTags.SPRUCE_LOGS);
        planksFromLogs(param0, Blocks.WARPED_PLANKS, ItemTags.WARPED_STEMS);
        woodFromLogs(param0, Blocks.ACACIA_WOOD, Blocks.ACACIA_LOG);
        woodFromLogs(param0, Blocks.BIRCH_WOOD, Blocks.BIRCH_LOG);
        woodFromLogs(param0, Blocks.DARK_OAK_WOOD, Blocks.DARK_OAK_LOG);
        woodFromLogs(param0, Blocks.JUNGLE_WOOD, Blocks.JUNGLE_LOG);
        woodFromLogs(param0, Blocks.OAK_WOOD, Blocks.OAK_LOG);
        woodFromLogs(param0, Blocks.SPRUCE_WOOD, Blocks.SPRUCE_LOG);
        woodFromLogs(param0, Blocks.CRIMSON_HYPHAE, Blocks.CRIMSON_STEM);
        woodFromLogs(param0, Blocks.WARPED_HYPHAE, Blocks.WARPED_STEM);
        woodFromLogs(param0, Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG);
        woodFromLogs(param0, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG);
        woodFromLogs(param0, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG);
        woodFromLogs(param0, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG);
        woodFromLogs(param0, Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_OAK_LOG);
        woodFromLogs(param0, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG);
        woodFromLogs(param0, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM);
        woodFromLogs(param0, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM);
        woodenBoat(param0, Items.ACACIA_BOAT, Blocks.ACACIA_PLANKS);
        woodenBoat(param0, Items.BIRCH_BOAT, Blocks.BIRCH_PLANKS);
        woodenBoat(param0, Items.DARK_OAK_BOAT, Blocks.DARK_OAK_PLANKS);
        woodenBoat(param0, Items.JUNGLE_BOAT, Blocks.JUNGLE_PLANKS);
        woodenBoat(param0, Items.OAK_BOAT, Blocks.OAK_PLANKS);
        woodenBoat(param0, Items.SPRUCE_BOAT, Blocks.SPRUCE_PLANKS);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.BLACK_WOOL, Items.BLACK_DYE);
        carpet(param0, Blocks.BLACK_CARPET, Blocks.BLACK_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.BLACK_CARPET, Items.BLACK_DYE);
        bedFromPlanksAndWool(param0, Items.BLACK_BED, Blocks.BLACK_WOOL);
        bedFromWhiteBedAndDye(param0, Items.BLACK_BED, Items.BLACK_DYE);
        banner(param0, Items.BLACK_BANNER, Blocks.BLACK_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.BLUE_WOOL, Items.BLUE_DYE);
        carpet(param0, Blocks.BLUE_CARPET, Blocks.BLUE_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.BLUE_CARPET, Items.BLUE_DYE);
        bedFromPlanksAndWool(param0, Items.BLUE_BED, Blocks.BLUE_WOOL);
        bedFromWhiteBedAndDye(param0, Items.BLUE_BED, Items.BLUE_DYE);
        banner(param0, Items.BLUE_BANNER, Blocks.BLUE_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.BROWN_WOOL, Items.BROWN_DYE);
        carpet(param0, Blocks.BROWN_CARPET, Blocks.BROWN_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.BROWN_CARPET, Items.BROWN_DYE);
        bedFromPlanksAndWool(param0, Items.BROWN_BED, Blocks.BROWN_WOOL);
        bedFromWhiteBedAndDye(param0, Items.BROWN_BED, Items.BROWN_DYE);
        banner(param0, Items.BROWN_BANNER, Blocks.BROWN_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.CYAN_WOOL, Items.CYAN_DYE);
        carpet(param0, Blocks.CYAN_CARPET, Blocks.CYAN_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.CYAN_CARPET, Items.CYAN_DYE);
        bedFromPlanksAndWool(param0, Items.CYAN_BED, Blocks.CYAN_WOOL);
        bedFromWhiteBedAndDye(param0, Items.CYAN_BED, Items.CYAN_DYE);
        banner(param0, Items.CYAN_BANNER, Blocks.CYAN_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.GRAY_WOOL, Items.GRAY_DYE);
        carpet(param0, Blocks.GRAY_CARPET, Blocks.GRAY_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.GRAY_CARPET, Items.GRAY_DYE);
        bedFromPlanksAndWool(param0, Items.GRAY_BED, Blocks.GRAY_WOOL);
        bedFromWhiteBedAndDye(param0, Items.GRAY_BED, Items.GRAY_DYE);
        banner(param0, Items.GRAY_BANNER, Blocks.GRAY_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.GREEN_WOOL, Items.GREEN_DYE);
        carpet(param0, Blocks.GREEN_CARPET, Blocks.GREEN_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.GREEN_CARPET, Items.GREEN_DYE);
        bedFromPlanksAndWool(param0, Items.GREEN_BED, Blocks.GREEN_WOOL);
        bedFromWhiteBedAndDye(param0, Items.GREEN_BED, Items.GREEN_DYE);
        banner(param0, Items.GREEN_BANNER, Blocks.GREEN_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.LIGHT_BLUE_WOOL, Items.LIGHT_BLUE_DYE);
        carpet(param0, Blocks.LIGHT_BLUE_CARPET, Blocks.LIGHT_BLUE_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.LIGHT_BLUE_CARPET, Items.LIGHT_BLUE_DYE);
        bedFromPlanksAndWool(param0, Items.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL);
        bedFromWhiteBedAndDye(param0, Items.LIGHT_BLUE_BED, Items.LIGHT_BLUE_DYE);
        banner(param0, Items.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.LIGHT_GRAY_WOOL, Items.LIGHT_GRAY_DYE);
        carpet(param0, Blocks.LIGHT_GRAY_CARPET, Blocks.LIGHT_GRAY_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.LIGHT_GRAY_CARPET, Items.LIGHT_GRAY_DYE);
        bedFromPlanksAndWool(param0, Items.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL);
        bedFromWhiteBedAndDye(param0, Items.LIGHT_GRAY_BED, Items.LIGHT_GRAY_DYE);
        banner(param0, Items.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.LIME_WOOL, Items.LIME_DYE);
        carpet(param0, Blocks.LIME_CARPET, Blocks.LIME_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.LIME_CARPET, Items.LIME_DYE);
        bedFromPlanksAndWool(param0, Items.LIME_BED, Blocks.LIME_WOOL);
        bedFromWhiteBedAndDye(param0, Items.LIME_BED, Items.LIME_DYE);
        banner(param0, Items.LIME_BANNER, Blocks.LIME_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.MAGENTA_WOOL, Items.MAGENTA_DYE);
        carpet(param0, Blocks.MAGENTA_CARPET, Blocks.MAGENTA_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.MAGENTA_CARPET, Items.MAGENTA_DYE);
        bedFromPlanksAndWool(param0, Items.MAGENTA_BED, Blocks.MAGENTA_WOOL);
        bedFromWhiteBedAndDye(param0, Items.MAGENTA_BED, Items.MAGENTA_DYE);
        banner(param0, Items.MAGENTA_BANNER, Blocks.MAGENTA_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.ORANGE_WOOL, Items.ORANGE_DYE);
        carpet(param0, Blocks.ORANGE_CARPET, Blocks.ORANGE_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.ORANGE_CARPET, Items.ORANGE_DYE);
        bedFromPlanksAndWool(param0, Items.ORANGE_BED, Blocks.ORANGE_WOOL);
        bedFromWhiteBedAndDye(param0, Items.ORANGE_BED, Items.ORANGE_DYE);
        banner(param0, Items.ORANGE_BANNER, Blocks.ORANGE_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.PINK_WOOL, Items.PINK_DYE);
        carpet(param0, Blocks.PINK_CARPET, Blocks.PINK_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.PINK_CARPET, Items.PINK_DYE);
        bedFromPlanksAndWool(param0, Items.PINK_BED, Blocks.PINK_WOOL);
        bedFromWhiteBedAndDye(param0, Items.PINK_BED, Items.PINK_DYE);
        banner(param0, Items.PINK_BANNER, Blocks.PINK_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.PURPLE_WOOL, Items.PURPLE_DYE);
        carpet(param0, Blocks.PURPLE_CARPET, Blocks.PURPLE_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.PURPLE_CARPET, Items.PURPLE_DYE);
        bedFromPlanksAndWool(param0, Items.PURPLE_BED, Blocks.PURPLE_WOOL);
        bedFromWhiteBedAndDye(param0, Items.PURPLE_BED, Items.PURPLE_DYE);
        banner(param0, Items.PURPLE_BANNER, Blocks.PURPLE_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.RED_WOOL, Items.RED_DYE);
        carpet(param0, Blocks.RED_CARPET, Blocks.RED_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.RED_CARPET, Items.RED_DYE);
        bedFromPlanksAndWool(param0, Items.RED_BED, Blocks.RED_WOOL);
        bedFromWhiteBedAndDye(param0, Items.RED_BED, Items.RED_DYE);
        banner(param0, Items.RED_BANNER, Blocks.RED_WOOL);
        carpet(param0, Blocks.WHITE_CARPET, Blocks.WHITE_WOOL);
        bedFromPlanksAndWool(param0, Items.WHITE_BED, Blocks.WHITE_WOOL);
        banner(param0, Items.WHITE_BANNER, Blocks.WHITE_WOOL);
        coloredWoolFromWhiteWoolAndDye(param0, Blocks.YELLOW_WOOL, Items.YELLOW_DYE);
        carpet(param0, Blocks.YELLOW_CARPET, Blocks.YELLOW_WOOL);
        coloredCarpetFromWhiteCarpetAndDye(param0, Blocks.YELLOW_CARPET, Items.YELLOW_DYE);
        bedFromPlanksAndWool(param0, Items.YELLOW_BED, Blocks.YELLOW_WOOL);
        bedFromWhiteBedAndDye(param0, Items.YELLOW_BED, Items.YELLOW_DYE);
        banner(param0, Items.YELLOW_BANNER, Blocks.YELLOW_WOOL);
        carpet(param0, Blocks.MOSS_CARPET, Blocks.MOSS_BLOCK);
        stainedGlassFromGlassAndDye(param0, Blocks.BLACK_STAINED_GLASS, Items.BLACK_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.BLACK_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.BLACK_STAINED_GLASS_PANE, Items.BLACK_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.BLUE_STAINED_GLASS, Items.BLUE_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.BLUE_STAINED_GLASS_PANE, Items.BLUE_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.BROWN_STAINED_GLASS, Items.BROWN_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.BROWN_STAINED_GLASS_PANE, Items.BROWN_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.CYAN_STAINED_GLASS, Items.CYAN_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.CYAN_STAINED_GLASS_PANE, Items.CYAN_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.GRAY_STAINED_GLASS, Items.GRAY_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.GRAY_STAINED_GLASS_PANE, Items.GRAY_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.GREEN_STAINED_GLASS, Items.GREEN_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.GREEN_STAINED_GLASS_PANE, Items.GREEN_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Items.LIGHT_BLUE_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.LIGHT_GRAY_STAINED_GLASS, Items.LIGHT_GRAY_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Items.LIGHT_GRAY_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.LIME_STAINED_GLASS, Items.LIME_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.LIME_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.LIME_STAINED_GLASS_PANE, Items.LIME_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.MAGENTA_STAINED_GLASS, Items.MAGENTA_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.MAGENTA_STAINED_GLASS_PANE, Items.MAGENTA_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.ORANGE_STAINED_GLASS, Items.ORANGE_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.ORANGE_STAINED_GLASS_PANE, Items.ORANGE_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.PINK_STAINED_GLASS, Items.PINK_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.PINK_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.PINK_STAINED_GLASS_PANE, Items.PINK_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.PURPLE_STAINED_GLASS, Items.PURPLE_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.PURPLE_STAINED_GLASS_PANE, Items.PURPLE_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.RED_STAINED_GLASS, Items.RED_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.RED_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.RED_STAINED_GLASS_PANE, Items.RED_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.WHITE_STAINED_GLASS, Items.WHITE_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.WHITE_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.WHITE_STAINED_GLASS_PANE, Items.WHITE_DYE);
        stainedGlassFromGlassAndDye(param0, Blocks.YELLOW_STAINED_GLASS, Items.YELLOW_DYE);
        stainedGlassPaneFromStainedGlass(param0, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS);
        stainedGlassPaneFromGlassPaneAndDye(param0, Blocks.YELLOW_STAINED_GLASS_PANE, Items.YELLOW_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.BLACK_TERRACOTTA, Items.BLACK_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.BLUE_TERRACOTTA, Items.BLUE_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.BROWN_TERRACOTTA, Items.BROWN_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.CYAN_TERRACOTTA, Items.CYAN_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.GRAY_TERRACOTTA, Items.GRAY_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.GREEN_TERRACOTTA, Items.GREEN_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.LIGHT_GRAY_TERRACOTTA, Items.LIGHT_GRAY_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.LIME_TERRACOTTA, Items.LIME_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.MAGENTA_TERRACOTTA, Items.MAGENTA_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.ORANGE_TERRACOTTA, Items.ORANGE_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.PINK_TERRACOTTA, Items.PINK_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.PURPLE_TERRACOTTA, Items.PURPLE_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.RED_TERRACOTTA, Items.RED_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.WHITE_TERRACOTTA, Items.WHITE_DYE);
        coloredTerracottaFromTerracottaAndDye(param0, Blocks.YELLOW_TERRACOTTA, Items.YELLOW_DYE);
        concretePowder(param0, Blocks.BLACK_CONCRETE_POWDER, Items.BLACK_DYE);
        concretePowder(param0, Blocks.BLUE_CONCRETE_POWDER, Items.BLUE_DYE);
        concretePowder(param0, Blocks.BROWN_CONCRETE_POWDER, Items.BROWN_DYE);
        concretePowder(param0, Blocks.CYAN_CONCRETE_POWDER, Items.CYAN_DYE);
        concretePowder(param0, Blocks.GRAY_CONCRETE_POWDER, Items.GRAY_DYE);
        concretePowder(param0, Blocks.GREEN_CONCRETE_POWDER, Items.GREEN_DYE);
        concretePowder(param0, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_DYE);
        concretePowder(param0, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_DYE);
        concretePowder(param0, Blocks.LIME_CONCRETE_POWDER, Items.LIME_DYE);
        concretePowder(param0, Blocks.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_DYE);
        concretePowder(param0, Blocks.ORANGE_CONCRETE_POWDER, Items.ORANGE_DYE);
        concretePowder(param0, Blocks.PINK_CONCRETE_POWDER, Items.PINK_DYE);
        concretePowder(param0, Blocks.PURPLE_CONCRETE_POWDER, Items.PURPLE_DYE);
        concretePowder(param0, Blocks.RED_CONCRETE_POWDER, Items.RED_DYE);
        concretePowder(param0, Blocks.WHITE_CONCRETE_POWDER, Items.WHITE_DYE);
        concretePowder(param0, Blocks.YELLOW_CONCRETE_POWDER, Items.YELLOW_DYE);
        candle(param0, Blocks.BLACK_CANDLE, Items.BLACK_DYE);
        candle(param0, Blocks.BLUE_CANDLE, Items.BLUE_DYE);
        candle(param0, Blocks.BROWN_CANDLE, Items.BROWN_DYE);
        candle(param0, Blocks.CYAN_CANDLE, Items.CYAN_DYE);
        candle(param0, Blocks.GRAY_CANDLE, Items.GRAY_DYE);
        candle(param0, Blocks.GREEN_CANDLE, Items.GREEN_DYE);
        candle(param0, Blocks.LIGHT_BLUE_CANDLE, Items.LIGHT_BLUE_DYE);
        candle(param0, Blocks.LIGHT_GRAY_CANDLE, Items.LIGHT_GRAY_DYE);
        candle(param0, Blocks.LIME_CANDLE, Items.LIME_DYE);
        candle(param0, Blocks.MAGENTA_CANDLE, Items.MAGENTA_DYE);
        candle(param0, Blocks.ORANGE_CANDLE, Items.ORANGE_DYE);
        candle(param0, Blocks.PINK_CANDLE, Items.PINK_DYE);
        candle(param0, Blocks.PURPLE_CANDLE, Items.PURPLE_DYE);
        candle(param0, Blocks.RED_CANDLE, Items.RED_DYE);
        candle(param0, Blocks.WHITE_CANDLE, Items.WHITE_DYE);
        candle(param0, Blocks.YELLOW_CANDLE, Items.YELLOW_DYE);
        ShapedRecipeBuilder.shaped(Blocks.ACTIVATOR_RAIL, 6)
            .define('#', Blocks.REDSTONE_TORCH)
            .define('S', Items.STICK)
            .define('X', Items.IRON_INGOT)
            .pattern("XSX")
            .pattern("X#X")
            .pattern("XSX")
            .unlockedBy("has_rail", has(Blocks.RAIL))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Blocks.ANDESITE, 2)
            .requires(Blocks.DIORITE)
            .requires(Blocks.COBBLESTONE)
            .unlockedBy("has_stone", has(Blocks.DIORITE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.ANVIL)
            .define('I', Blocks.IRON_BLOCK)
            .define('i', Items.IRON_INGOT)
            .pattern("III")
            .pattern(" i ")
            .pattern("iii")
            .unlockedBy("has_iron_block", has(Blocks.IRON_BLOCK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.ARMOR_STAND)
            .define('/', Items.STICK)
            .define('_', Blocks.SMOOTH_STONE_SLAB)
            .pattern("///")
            .pattern(" / ")
            .pattern("/_/")
            .unlockedBy("has_stone_slab", has(Blocks.SMOOTH_STONE_SLAB))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.ARROW, 4)
            .define('#', Items.STICK)
            .define('X', Items.FLINT)
            .define('Y', Items.FEATHER)
            .pattern("X")
            .pattern("#")
            .pattern("Y")
            .unlockedBy("has_feather", has(Items.FEATHER))
            .unlockedBy("has_flint", has(Items.FLINT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.BARREL, 1)
            .define('P', ItemTags.PLANKS)
            .define('S', ItemTags.WOODEN_SLABS)
            .pattern("PSP")
            .pattern("P P")
            .pattern("PSP")
            .unlockedBy("has_planks", has(ItemTags.PLANKS))
            .unlockedBy("has_wood_slab", has(ItemTags.WOODEN_SLABS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.BEACON)
            .define('S', Items.NETHER_STAR)
            .define('G', Blocks.GLASS)
            .define('O', Blocks.OBSIDIAN)
            .pattern("GGG")
            .pattern("GSG")
            .pattern("OOO")
            .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.BEEHIVE)
            .define('P', ItemTags.PLANKS)
            .define('H', Items.HONEYCOMB)
            .pattern("PPP")
            .pattern("HHH")
            .pattern("PPP")
            .unlockedBy("has_honeycomb", has(Items.HONEYCOMB))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.BEETROOT_SOUP)
            .requires(Items.BOWL)
            .requires(Items.BEETROOT, 6)
            .unlockedBy("has_beetroot", has(Items.BEETROOT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.BLACK_DYE).requires(Items.INK_SAC).group("black_dye").unlockedBy("has_ink_sac", has(Items.INK_SAC)).save(param0);
        ShapelessRecipeBuilder.shapeless(Items.BLACK_DYE)
            .requires(Blocks.WITHER_ROSE)
            .group("black_dye")
            .unlockedBy("has_black_flower", has(Blocks.WITHER_ROSE))
            .save(param0, "black_dye_from_wither_rose");
        ShapelessRecipeBuilder.shapeless(Items.BLAZE_POWDER, 2).requires(Items.BLAZE_ROD).unlockedBy("has_blaze_rod", has(Items.BLAZE_ROD)).save(param0);
        ShapelessRecipeBuilder.shapeless(Items.BLUE_DYE)
            .requires(Items.LAPIS_LAZULI)
            .group("blue_dye")
            .unlockedBy("has_lapis_lazuli", has(Items.LAPIS_LAZULI))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.BLUE_DYE)
            .requires(Blocks.CORNFLOWER)
            .group("blue_dye")
            .unlockedBy("has_blue_flower", has(Blocks.CORNFLOWER))
            .save(param0, "blue_dye_from_cornflower");
        ShapedRecipeBuilder.shaped(Blocks.BLUE_ICE)
            .define('#', Blocks.PACKED_ICE)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_packed_ice", has(Blocks.PACKED_ICE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.BONE_BLOCK)
            .define('X', Items.BONE_MEAL)
            .pattern("XXX")
            .pattern("XXX")
            .pattern("XXX")
            .unlockedBy("has_bonemeal", has(Items.BONE_MEAL))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.BONE_MEAL, 3).requires(Items.BONE).group("bonemeal").unlockedBy("has_bone", has(Items.BONE)).save(param0);
        ShapelessRecipeBuilder.shapeless(Items.BONE_MEAL, 9)
            .requires(Blocks.BONE_BLOCK)
            .group("bonemeal")
            .unlockedBy("has_bone_block", has(Blocks.BONE_BLOCK))
            .save(param0, "bone_meal_from_bone_block");
        ShapelessRecipeBuilder.shapeless(Items.BOOK).requires(Items.PAPER, 3).requires(Items.LEATHER).unlockedBy("has_paper", has(Items.PAPER)).save(param0);
        ShapedRecipeBuilder.shaped(Blocks.BOOKSHELF)
            .define('#', ItemTags.PLANKS)
            .define('X', Items.BOOK)
            .pattern("###")
            .pattern("XXX")
            .pattern("###")
            .unlockedBy("has_book", has(Items.BOOK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.BOW)
            .define('#', Items.STICK)
            .define('X', Items.STRING)
            .pattern(" #X")
            .pattern("# X")
            .pattern(" #X")
            .unlockedBy("has_string", has(Items.STRING))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.BOWL, 4)
            .define('#', ItemTags.PLANKS)
            .pattern("# #")
            .pattern(" # ")
            .unlockedBy("has_brown_mushroom", has(Blocks.BROWN_MUSHROOM))
            .unlockedBy("has_red_mushroom", has(Blocks.RED_MUSHROOM))
            .unlockedBy("has_mushroom_stew", has(Items.MUSHROOM_STEW))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.BREAD).define('#', Items.WHEAT).pattern("###").unlockedBy("has_wheat", has(Items.WHEAT)).save(param0);
        ShapedRecipeBuilder.shaped(Blocks.BREWING_STAND)
            .define('B', Items.BLAZE_ROD)
            .define('#', ItemTags.STONE_CRAFTING_MATERIALS)
            .pattern(" B ")
            .pattern("###")
            .unlockedBy("has_blaze_rod", has(Items.BLAZE_ROD))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.BRICKS).define('#', Items.BRICK).pattern("##").pattern("##").unlockedBy("has_brick", has(Items.BRICK)).save(param0);
        ShapelessRecipeBuilder.shapeless(Items.BROWN_DYE)
            .requires(Items.COCOA_BEANS)
            .group("brown_dye")
            .unlockedBy("has_cocoa_beans", has(Items.COCOA_BEANS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.BUCKET)
            .define('#', Items.IRON_INGOT)
            .pattern("# #")
            .pattern(" # ")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CAKE)
            .define('A', Items.MILK_BUCKET)
            .define('B', Items.SUGAR)
            .define('C', Items.WHEAT)
            .define('E', Items.EGG)
            .pattern("AAA")
            .pattern("BEB")
            .pattern("CCC")
            .unlockedBy("has_egg", has(Items.EGG))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CAMPFIRE)
            .define('L', ItemTags.LOGS)
            .define('S', Items.STICK)
            .define('C', ItemTags.COALS)
            .pattern(" S ")
            .pattern("SCS")
            .pattern("LLL")
            .unlockedBy("has_stick", has(Items.STICK))
            .unlockedBy("has_coal", has(ItemTags.COALS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.CARROT_ON_A_STICK)
            .define('#', Items.FISHING_ROD)
            .define('X', Items.CARROT)
            .pattern("# ")
            .pattern(" X")
            .unlockedBy("has_carrot", has(Items.CARROT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.WARPED_FUNGUS_ON_A_STICK)
            .define('#', Items.FISHING_ROD)
            .define('X', Items.WARPED_FUNGUS)
            .pattern("# ")
            .pattern(" X")
            .unlockedBy("has_warped_fungus", has(Items.WARPED_FUNGUS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CAULDRON)
            .define('#', Items.IRON_INGOT)
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .unlockedBy("has_water_bucket", has(Items.WATER_BUCKET))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.COMPOSTER)
            .define('#', ItemTags.WOODEN_SLABS)
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .unlockedBy("has_wood_slab", has(ItemTags.WOODEN_SLABS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CHEST)
            .define('#', ItemTags.PLANKS)
            .pattern("###")
            .pattern("# #")
            .pattern("###")
            .unlockedBy(
                "has_lots_of_items",
                new InventoryChangeTrigger.TriggerInstance(
                    EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(10), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, new ItemPredicate[0]
                )
            )
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.CHEST_MINECART)
            .define('A', Blocks.CHEST)
            .define('B', Items.MINECART)
            .pattern("A")
            .pattern("B")
            .unlockedBy("has_minecart", has(Items.MINECART))
            .save(param0);
        chiseledBuilder(Blocks.CHISELED_QUARTZ_BLOCK, Ingredient.of(Blocks.QUARTZ_SLAB))
            .unlockedBy("has_chiseled_quartz_block", has(Blocks.CHISELED_QUARTZ_BLOCK))
            .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .unlockedBy("has_quartz_pillar", has(Blocks.QUARTZ_PILLAR))
            .save(param0);
        chiseledBuilder(Blocks.CHISELED_STONE_BRICKS, Ingredient.of(Blocks.STONE_BRICK_SLAB)).unlockedBy("has_tag", has(ItemTags.STONE_BRICKS)).save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CLAY)
            .define('#', Items.CLAY_BALL)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.CLOCK)
            .define('#', Items.GOLD_INGOT)
            .define('X', Items.REDSTONE)
            .pattern(" # ")
            .pattern("#X#")
            .pattern(" # ")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.COAL, 9).requires(Blocks.COAL_BLOCK).unlockedBy("has_coal_block", has(Blocks.COAL_BLOCK)).save(param0);
        ShapedRecipeBuilder.shaped(Blocks.COAL_BLOCK)
            .define('#', Items.COAL)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_coal", has(Items.COAL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.COARSE_DIRT, 4)
            .define('D', Blocks.DIRT)
            .define('G', Blocks.GRAVEL)
            .pattern("DG")
            .pattern("GD")
            .unlockedBy("has_gravel", has(Blocks.GRAVEL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.COMPARATOR)
            .define('#', Blocks.REDSTONE_TORCH)
            .define('X', Items.QUARTZ)
            .define('I', Blocks.STONE)
            .pattern(" # ")
            .pattern("#X#")
            .pattern("III")
            .unlockedBy("has_quartz", has(Items.QUARTZ))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.COMPASS)
            .define('#', Items.IRON_INGOT)
            .define('X', Items.REDSTONE)
            .pattern(" # ")
            .pattern("#X#")
            .pattern(" # ")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.COOKIE, 8)
            .define('#', Items.WHEAT)
            .define('X', Items.COCOA_BEANS)
            .pattern("#X#")
            .unlockedBy("has_cocoa", has(Items.COCOA_BEANS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CRAFTING_TABLE)
            .define('#', ItemTags.PLANKS)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_planks", has(ItemTags.PLANKS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.CROSSBOW)
            .define('~', Items.STRING)
            .define('#', Items.STICK)
            .define('&', Items.IRON_INGOT)
            .define('$', Blocks.TRIPWIRE_HOOK)
            .pattern("#&#")
            .pattern("~$~")
            .pattern(" # ")
            .unlockedBy("has_string", has(Items.STRING))
            .unlockedBy("has_stick", has(Items.STICK))
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .unlockedBy("has_tripwire_hook", has(Blocks.TRIPWIRE_HOOK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.LOOM)
            .define('#', ItemTags.PLANKS)
            .define('@', Items.STRING)
            .pattern("@@")
            .pattern("##")
            .unlockedBy("has_string", has(Items.STRING))
            .save(param0);
        chiseledBuilder(Blocks.CHISELED_RED_SANDSTONE, Ingredient.of(Blocks.RED_SANDSTONE_SLAB))
            .unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .unlockedBy("has_chiseled_red_sandstone", has(Blocks.CHISELED_RED_SANDSTONE))
            .unlockedBy("has_cut_red_sandstone", has(Blocks.CUT_RED_SANDSTONE))
            .save(param0);
        chiseled(param0, Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE_SLAB);
        ShapedRecipeBuilder.shaped(Blocks.COPPER_BLOCK)
            .define('#', Items.COPPER_INGOT)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.COPPER_INGOT, 4)
            .requires(Blocks.COPPER_BLOCK)
            .group(getBlockName(Items.COPPER_INGOT))
            .unlockedBy(getHasName(Blocks.COPPER_BLOCK), has(Blocks.COPPER_BLOCK))
            .save(param0, getFromName(Items.COPPER_INGOT, Blocks.COPPER_BLOCK));
        ShapelessRecipeBuilder.shapeless(Items.COPPER_INGOT, 4)
            .requires(Blocks.WAXED_COPPER_BLOCK)
            .group(getBlockName(Items.COPPER_INGOT))
            .unlockedBy(getHasName(Blocks.WAXED_COPPER_BLOCK), has(Blocks.WAXED_COPPER_BLOCK))
            .save(param0, getFromName(Items.COPPER_INGOT, Blocks.WAXED_COPPER_BLOCK));
        cut(param0, Blocks.CUT_COPPER, Blocks.COPPER_BLOCK);
        cut(param0, Blocks.EXPOSED_CUT_COPPER, Blocks.EXPOSED_COPPER);
        cut(param0, Blocks.WEATHERED_CUT_COPPER, Blocks.WEATHERED_COPPER);
        cut(param0, Blocks.OXIDIZED_CUT_COPPER, Blocks.OXIDIZED_COPPER);
        waxRecipes(param0);
        cut(param0, Blocks.WAXED_CUT_COPPER, Blocks.WAXED_COPPER_BLOCK);
        cut(param0, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_COPPER);
        cut(param0, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_COPPER);
        ShapelessRecipeBuilder.shapeless(Items.CYAN_DYE, 2)
            .requires(Items.BLUE_DYE)
            .requires(Items.GREEN_DYE)
            .unlockedBy("has_green_dye", has(Items.GREEN_DYE))
            .unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DARK_PRISMARINE)
            .define('S', Items.PRISMARINE_SHARD)
            .define('I', Items.BLACK_DYE)
            .pattern("SSS")
            .pattern("SIS")
            .pattern("SSS")
            .unlockedBy("has_prismarine_shard", has(Items.PRISMARINE_SHARD))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DAYLIGHT_DETECTOR)
            .define('Q', Items.QUARTZ)
            .define('G', Blocks.GLASS)
            .define('W', Ingredient.of(ItemTags.WOODEN_SLABS))
            .pattern("GGG")
            .pattern("QQQ")
            .pattern("WWW")
            .unlockedBy("has_quartz", has(Items.QUARTZ))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DEEPSLATE_BRICKS, 4)
            .define('S', Blocks.POLISHED_DEEPSLATE)
            .pattern("SS")
            .pattern("SS")
            .unlockedBy("has_polished_deepslate", has(Blocks.POLISHED_DEEPSLATE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DEEPSLATE_TILES, 4)
            .define('S', Blocks.DEEPSLATE_BRICKS)
            .pattern("SS")
            .pattern("SS")
            .unlockedBy("has_deepslate_bricks", has(Blocks.DEEPSLATE_BRICKS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DETECTOR_RAIL, 6)
            .define('R', Items.REDSTONE)
            .define('#', Blocks.STONE_PRESSURE_PLATE)
            .define('X', Items.IRON_INGOT)
            .pattern("X X")
            .pattern("X#X")
            .pattern("XRX")
            .unlockedBy("has_rail", has(Blocks.RAIL))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.DIAMOND, 9)
            .requires(Blocks.DIAMOND_BLOCK)
            .unlockedBy("has_diamond_block", has(Blocks.DIAMOND_BLOCK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_AXE)
            .define('#', Items.STICK)
            .define('X', Items.DIAMOND)
            .pattern("XX")
            .pattern("X#")
            .pattern(" #")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DIAMOND_BLOCK)
            .define('#', Items.DIAMOND)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_BOOTS)
            .define('X', Items.DIAMOND)
            .pattern("X X")
            .pattern("X X")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_CHESTPLATE)
            .define('X', Items.DIAMOND)
            .pattern("X X")
            .pattern("XXX")
            .pattern("XXX")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_HELMET)
            .define('X', Items.DIAMOND)
            .pattern("XXX")
            .pattern("X X")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_HOE)
            .define('#', Items.STICK)
            .define('X', Items.DIAMOND)
            .pattern("XX")
            .pattern(" #")
            .pattern(" #")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_LEGGINGS)
            .define('X', Items.DIAMOND)
            .pattern("XXX")
            .pattern("X X")
            .pattern("X X")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_PICKAXE)
            .define('#', Items.STICK)
            .define('X', Items.DIAMOND)
            .pattern("XXX")
            .pattern(" # ")
            .pattern(" # ")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_SHOVEL)
            .define('#', Items.STICK)
            .define('X', Items.DIAMOND)
            .pattern("X")
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.DIAMOND_SWORD)
            .define('#', Items.STICK)
            .define('X', Items.DIAMOND)
            .pattern("X")
            .pattern("X")
            .pattern("#")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DIORITE, 2)
            .define('Q', Items.QUARTZ)
            .define('C', Blocks.COBBLESTONE)
            .pattern("CQ")
            .pattern("QC")
            .unlockedBy("has_quartz", has(Items.QUARTZ))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DISPENSER)
            .define('R', Items.REDSTONE)
            .define('#', Blocks.COBBLESTONE)
            .define('X', Items.BOW)
            .pattern("###")
            .pattern("#X#")
            .pattern("#R#")
            .unlockedBy("has_bow", has(Items.BOW))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DRIPSTONE_BLOCK)
            .define('#', Items.POINTED_DRIPSTONE)
            .pattern("##")
            .pattern("##")
            .group("pointed_dripstone")
            .unlockedBy("has_pointed_dripstone", has(Items.POINTED_DRIPSTONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.DROPPER)
            .define('R', Items.REDSTONE)
            .define('#', Blocks.COBBLESTONE)
            .pattern("###")
            .pattern("# #")
            .pattern("#R#")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.EMERALD, 9)
            .requires(Blocks.EMERALD_BLOCK)
            .unlockedBy("has_emerald_block", has(Blocks.EMERALD_BLOCK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.EMERALD_BLOCK)
            .define('#', Items.EMERALD)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_emerald", has(Items.EMERALD))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.ENCHANTING_TABLE)
            .define('B', Items.BOOK)
            .define('#', Blocks.OBSIDIAN)
            .define('D', Items.DIAMOND)
            .pattern(" B ")
            .pattern("D#D")
            .pattern("###")
            .unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.ENDER_CHEST)
            .define('#', Blocks.OBSIDIAN)
            .define('E', Items.ENDER_EYE)
            .pattern("###")
            .pattern("#E#")
            .pattern("###")
            .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.ENDER_EYE)
            .requires(Items.ENDER_PEARL)
            .requires(Items.BLAZE_POWDER)
            .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.END_STONE_BRICKS, 4)
            .define('#', Blocks.END_STONE)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_end_stone", has(Blocks.END_STONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.END_CRYSTAL)
            .define('T', Items.GHAST_TEAR)
            .define('E', Items.ENDER_EYE)
            .define('G', Blocks.GLASS)
            .pattern("GGG")
            .pattern("GEG")
            .pattern("GTG")
            .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.END_ROD, 4)
            .define('#', Items.POPPED_CHORUS_FRUIT)
            .define('/', Items.BLAZE_ROD)
            .pattern("/")
            .pattern("#")
            .unlockedBy("has_chorus_fruit_popped", has(Items.POPPED_CHORUS_FRUIT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.FERMENTED_SPIDER_EYE)
            .requires(Items.SPIDER_EYE)
            .requires(Blocks.BROWN_MUSHROOM)
            .requires(Items.SUGAR)
            .unlockedBy("has_spider_eye", has(Items.SPIDER_EYE))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.FIRE_CHARGE, 3)
            .requires(Items.GUNPOWDER)
            .requires(Items.BLAZE_POWDER)
            .requires(Ingredient.of(Items.COAL, Items.CHARCOAL))
            .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.FIREWORK_ROCKET, 3)
            .requires(Items.GUNPOWDER)
            .requires(Items.PAPER)
            .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
            .save(param0, "firework_rocket_simple");
        ShapedRecipeBuilder.shaped(Items.FISHING_ROD)
            .define('#', Items.STICK)
            .define('X', Items.STRING)
            .pattern("  #")
            .pattern(" #X")
            .pattern("# X")
            .unlockedBy("has_string", has(Items.STRING))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.FLINT_AND_STEEL)
            .requires(Items.IRON_INGOT)
            .requires(Items.FLINT)
            .unlockedBy("has_flint", has(Items.FLINT))
            .unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.FLOWER_POT)
            .define('#', Items.BRICK)
            .pattern("# #")
            .pattern(" # ")
            .unlockedBy("has_brick", has(Items.BRICK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.FURNACE)
            .define('#', ItemTags.STONE_CRAFTING_MATERIALS)
            .pattern("###")
            .pattern("# #")
            .pattern("###")
            .unlockedBy("has_cobblestone", has(ItemTags.STONE_CRAFTING_MATERIALS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.FURNACE_MINECART)
            .define('A', Blocks.FURNACE)
            .define('B', Items.MINECART)
            .pattern("A")
            .pattern("B")
            .unlockedBy("has_minecart", has(Items.MINECART))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GLASS_BOTTLE, 3)
            .define('#', Blocks.GLASS)
            .pattern("# #")
            .pattern(" # ")
            .unlockedBy("has_glass", has(Blocks.GLASS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.GLASS_PANE, 16)
            .define('#', Blocks.GLASS)
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_glass", has(Blocks.GLASS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.GLOWSTONE)
            .define('#', Items.GLOWSTONE_DUST)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.GLOW_ITEM_FRAME)
            .requires(Items.ITEM_FRAME)
            .requires(Items.GLOW_INK_SAC)
            .unlockedBy("has_item_frame", has(Items.ITEM_FRAME))
            .unlockedBy("has_glow_ink_sac", has(Items.GLOW_INK_SAC))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_APPLE)
            .define('#', Items.GOLD_INGOT)
            .define('X', Items.APPLE)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_AXE)
            .define('#', Items.STICK)
            .define('X', Items.GOLD_INGOT)
            .pattern("XX")
            .pattern("X#")
            .pattern(" #")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_BOOTS)
            .define('X', Items.GOLD_INGOT)
            .pattern("X X")
            .pattern("X X")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_CARROT)
            .define('#', Items.GOLD_NUGGET)
            .define('X', Items.CARROT)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_gold_nugget", has(Items.GOLD_NUGGET))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_CHESTPLATE)
            .define('X', Items.GOLD_INGOT)
            .pattern("X X")
            .pattern("XXX")
            .pattern("XXX")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_HELMET)
            .define('X', Items.GOLD_INGOT)
            .pattern("XXX")
            .pattern("X X")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_HOE)
            .define('#', Items.STICK)
            .define('X', Items.GOLD_INGOT)
            .pattern("XX")
            .pattern(" #")
            .pattern(" #")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_LEGGINGS)
            .define('X', Items.GOLD_INGOT)
            .pattern("XXX")
            .pattern("X X")
            .pattern("X X")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_PICKAXE)
            .define('#', Items.STICK)
            .define('X', Items.GOLD_INGOT)
            .pattern("XXX")
            .pattern(" # ")
            .pattern(" # ")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.POWERED_RAIL, 6)
            .define('R', Items.REDSTONE)
            .define('#', Items.STICK)
            .define('X', Items.GOLD_INGOT)
            .pattern("X X")
            .pattern("X#X")
            .pattern("XRX")
            .unlockedBy("has_rail", has(Blocks.RAIL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_SHOVEL)
            .define('#', Items.STICK)
            .define('X', Items.GOLD_INGOT)
            .pattern("X")
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GOLDEN_SWORD)
            .define('#', Items.STICK)
            .define('X', Items.GOLD_INGOT)
            .pattern("X")
            .pattern("X")
            .pattern("#")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.GOLD_BLOCK)
            .define('#', Items.GOLD_INGOT)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.GOLD_INGOT, 9)
            .requires(Blocks.GOLD_BLOCK)
            .group("gold_ingot")
            .unlockedBy("has_gold_block", has(Blocks.GOLD_BLOCK))
            .save(param0, "gold_ingot_from_gold_block");
        ShapedRecipeBuilder.shaped(Items.GOLD_INGOT)
            .define('#', Items.GOLD_NUGGET)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .group("gold_ingot")
            .unlockedBy("has_gold_nugget", has(Items.GOLD_NUGGET))
            .save(param0, "gold_ingot_from_nuggets");
        ShapelessRecipeBuilder.shapeless(Items.GOLD_NUGGET, 9).requires(Items.GOLD_INGOT).unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT)).save(param0);
        ShapelessRecipeBuilder.shapeless(Blocks.GRANITE)
            .requires(Blocks.DIORITE)
            .requires(Items.QUARTZ)
            .unlockedBy("has_quartz", has(Items.QUARTZ))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.GRAY_DYE, 2)
            .requires(Items.BLACK_DYE)
            .requires(Items.WHITE_DYE)
            .unlockedBy("has_white_dye", has(Items.WHITE_DYE))
            .unlockedBy("has_black_dye", has(Items.BLACK_DYE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.HAY_BLOCK)
            .define('#', Items.WHEAT)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_wheat", has(Items.WHEAT))
            .save(param0);
        pressurePlate(param0, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.IRON_INGOT);
        ShapelessRecipeBuilder.shapeless(Items.HONEY_BOTTLE, 4)
            .requires(Items.HONEY_BLOCK)
            .requires(Items.GLASS_BOTTLE, 4)
            .unlockedBy("has_honey_block", has(Blocks.HONEY_BLOCK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.HONEY_BLOCK, 1)
            .define('S', Items.HONEY_BOTTLE)
            .pattern("SS")
            .pattern("SS")
            .unlockedBy("has_honey_bottle", has(Items.HONEY_BOTTLE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.HONEYCOMB_BLOCK)
            .define('H', Items.HONEYCOMB)
            .pattern("HH")
            .pattern("HH")
            .unlockedBy("has_honeycomb", has(Items.HONEYCOMB))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.HOPPER)
            .define('C', Blocks.CHEST)
            .define('I', Items.IRON_INGOT)
            .pattern("I I")
            .pattern("ICI")
            .pattern(" I ")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.HOPPER_MINECART)
            .define('A', Blocks.HOPPER)
            .define('B', Items.MINECART)
            .pattern("A")
            .pattern("B")
            .unlockedBy("has_minecart", has(Items.MINECART))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.IRON_AXE)
            .define('#', Items.STICK)
            .define('X', Items.IRON_INGOT)
            .pattern("XX")
            .pattern("X#")
            .pattern(" #")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.IRON_BARS, 16)
            .define('#', Items.IRON_INGOT)
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.IRON_BLOCK)
            .define('#', Items.IRON_INGOT)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.IRON_BOOTS)
            .define('X', Items.IRON_INGOT)
            .pattern("X X")
            .pattern("X X")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.IRON_CHESTPLATE)
            .define('X', Items.IRON_INGOT)
            .pattern("X X")
            .pattern("XXX")
            .pattern("XXX")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        doorBuilder(Blocks.IRON_DOOR, Ingredient.of(Items.IRON_INGOT)).unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT)).save(param0);
        ShapedRecipeBuilder.shaped(Items.IRON_HELMET)
            .define('X', Items.IRON_INGOT)
            .pattern("XXX")
            .pattern("X X")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.IRON_HOE)
            .define('#', Items.STICK)
            .define('X', Items.IRON_INGOT)
            .pattern("XX")
            .pattern(" #")
            .pattern(" #")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.IRON_INGOT, 9)
            .requires(Blocks.IRON_BLOCK)
            .group("iron_ingot")
            .unlockedBy("has_iron_block", has(Blocks.IRON_BLOCK))
            .save(param0, "iron_ingot_from_iron_block");
        ShapedRecipeBuilder.shaped(Items.IRON_INGOT)
            .define('#', Items.IRON_NUGGET)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .group("iron_ingot")
            .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
            .save(param0, "iron_ingot_from_nuggets");
        ShapedRecipeBuilder.shaped(Items.IRON_LEGGINGS)
            .define('X', Items.IRON_INGOT)
            .pattern("XXX")
            .pattern("X X")
            .pattern("X X")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.IRON_NUGGET, 9).requires(Items.IRON_INGOT).unlockedBy("has_iron_ingot", has(Items.IRON_INGOT)).save(param0);
        ShapedRecipeBuilder.shaped(Items.IRON_PICKAXE)
            .define('#', Items.STICK)
            .define('X', Items.IRON_INGOT)
            .pattern("XXX")
            .pattern(" # ")
            .pattern(" # ")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.IRON_SHOVEL)
            .define('#', Items.STICK)
            .define('X', Items.IRON_INGOT)
            .pattern("X")
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.IRON_SWORD)
            .define('#', Items.STICK)
            .define('X', Items.IRON_INGOT)
            .pattern("X")
            .pattern("X")
            .pattern("#")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.IRON_TRAPDOOR)
            .define('#', Items.IRON_INGOT)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.ITEM_FRAME)
            .define('#', Items.STICK)
            .define('X', Items.LEATHER)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_leather", has(Items.LEATHER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.JUKEBOX)
            .define('#', ItemTags.PLANKS)
            .define('X', Items.DIAMOND)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.LADDER, 3)
            .define('#', Items.STICK)
            .pattern("# #")
            .pattern("###")
            .pattern("# #")
            .unlockedBy("has_stick", has(Items.STICK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.LAPIS_BLOCK)
            .define('#', Items.LAPIS_LAZULI)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_lapis", has(Items.LAPIS_LAZULI))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.LAPIS_LAZULI, 9)
            .requires(Blocks.LAPIS_BLOCK)
            .unlockedBy("has_lapis_block", has(Blocks.LAPIS_BLOCK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.LEAD, 2)
            .define('~', Items.STRING)
            .define('O', Items.SLIME_BALL)
            .pattern("~~ ")
            .pattern("~O ")
            .pattern("  ~")
            .unlockedBy("has_slime_ball", has(Items.SLIME_BALL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.LEATHER)
            .define('#', Items.RABBIT_HIDE)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_rabbit_hide", has(Items.RABBIT_HIDE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.LEATHER_BOOTS)
            .define('X', Items.LEATHER)
            .pattern("X X")
            .pattern("X X")
            .unlockedBy("has_leather", has(Items.LEATHER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.LEATHER_CHESTPLATE)
            .define('X', Items.LEATHER)
            .pattern("X X")
            .pattern("XXX")
            .pattern("XXX")
            .unlockedBy("has_leather", has(Items.LEATHER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.LEATHER_HELMET)
            .define('X', Items.LEATHER)
            .pattern("XXX")
            .pattern("X X")
            .unlockedBy("has_leather", has(Items.LEATHER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.LEATHER_LEGGINGS)
            .define('X', Items.LEATHER)
            .pattern("XXX")
            .pattern("X X")
            .pattern("X X")
            .unlockedBy("has_leather", has(Items.LEATHER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.LEATHER_HORSE_ARMOR)
            .define('X', Items.LEATHER)
            .pattern("X X")
            .pattern("XXX")
            .pattern("X X")
            .unlockedBy("has_leather", has(Items.LEATHER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.LECTERN)
            .define('S', ItemTags.WOODEN_SLABS)
            .define('B', Blocks.BOOKSHELF)
            .pattern("SSS")
            .pattern(" B ")
            .pattern(" S ")
            .unlockedBy("has_book", has(Items.BOOK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.LEVER)
            .define('#', Blocks.COBBLESTONE)
            .define('X', Items.STICK)
            .pattern("X")
            .pattern("#")
            .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_BLUE_DYE)
            .requires(Blocks.BLUE_ORCHID)
            .group("light_blue_dye")
            .unlockedBy("has_red_flower", has(Blocks.BLUE_ORCHID))
            .save(param0, "light_blue_dye_from_blue_orchid");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_BLUE_DYE, 2)
            .requires(Items.BLUE_DYE)
            .requires(Items.WHITE_DYE)
            .group("light_blue_dye")
            .unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
            .unlockedBy("has_white_dye", has(Items.WHITE_DYE))
            .save(param0, "light_blue_dye_from_blue_white_dye");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE)
            .requires(Blocks.AZURE_BLUET)
            .group("light_gray_dye")
            .unlockedBy("has_red_flower", has(Blocks.AZURE_BLUET))
            .save(param0, "light_gray_dye_from_azure_bluet");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE, 2)
            .requires(Items.GRAY_DYE)
            .requires(Items.WHITE_DYE)
            .group("light_gray_dye")
            .unlockedBy("has_gray_dye", has(Items.GRAY_DYE))
            .unlockedBy("has_white_dye", has(Items.WHITE_DYE))
            .save(param0, "light_gray_dye_from_gray_white_dye");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE, 3)
            .requires(Items.BLACK_DYE)
            .requires(Items.WHITE_DYE, 2)
            .group("light_gray_dye")
            .unlockedBy("has_white_dye", has(Items.WHITE_DYE))
            .unlockedBy("has_black_dye", has(Items.BLACK_DYE))
            .save(param0, "light_gray_dye_from_black_white_dye");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE)
            .requires(Blocks.OXEYE_DAISY)
            .group("light_gray_dye")
            .unlockedBy("has_red_flower", has(Blocks.OXEYE_DAISY))
            .save(param0, "light_gray_dye_from_oxeye_daisy");
        ShapelessRecipeBuilder.shapeless(Items.LIGHT_GRAY_DYE)
            .requires(Blocks.WHITE_TULIP)
            .group("light_gray_dye")
            .unlockedBy("has_red_flower", has(Blocks.WHITE_TULIP))
            .save(param0, "light_gray_dye_from_white_tulip");
        pressurePlate(param0, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.GOLD_INGOT);
        ShapedRecipeBuilder.shaped(Blocks.LIGHTNING_ROD)
            .define('#', Items.COPPER_INGOT)
            .pattern("#")
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.LIME_DYE, 2)
            .requires(Items.GREEN_DYE)
            .requires(Items.WHITE_DYE)
            .unlockedBy("has_green_dye", has(Items.GREEN_DYE))
            .unlockedBy("has_white_dye", has(Items.WHITE_DYE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.JACK_O_LANTERN)
            .define('A', Blocks.CARVED_PUMPKIN)
            .define('B', Blocks.TORCH)
            .pattern("A")
            .pattern("B")
            .unlockedBy("has_carved_pumpkin", has(Blocks.CARVED_PUMPKIN))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE)
            .requires(Blocks.ALLIUM)
            .group("magenta_dye")
            .unlockedBy("has_red_flower", has(Blocks.ALLIUM))
            .save(param0, "magenta_dye_from_allium");
        ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 4)
            .requires(Items.BLUE_DYE)
            .requires(Items.RED_DYE, 2)
            .requires(Items.WHITE_DYE)
            .group("magenta_dye")
            .unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
            .unlockedBy("has_rose_red", has(Items.RED_DYE))
            .unlockedBy("has_white_dye", has(Items.WHITE_DYE))
            .save(param0, "magenta_dye_from_blue_red_white_dye");
        ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 3)
            .requires(Items.BLUE_DYE)
            .requires(Items.RED_DYE)
            .requires(Items.PINK_DYE)
            .group("magenta_dye")
            .unlockedBy("has_pink_dye", has(Items.PINK_DYE))
            .unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
            .unlockedBy("has_red_dye", has(Items.RED_DYE))
            .save(param0, "magenta_dye_from_blue_red_pink");
        ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 2)
            .requires(Blocks.LILAC)
            .group("magenta_dye")
            .unlockedBy("has_double_plant", has(Blocks.LILAC))
            .save(param0, "magenta_dye_from_lilac");
        ShapelessRecipeBuilder.shapeless(Items.MAGENTA_DYE, 2)
            .requires(Items.PURPLE_DYE)
            .requires(Items.PINK_DYE)
            .group("magenta_dye")
            .unlockedBy("has_pink_dye", has(Items.PINK_DYE))
            .unlockedBy("has_purple_dye", has(Items.PURPLE_DYE))
            .save(param0, "magenta_dye_from_purple_and_pink");
        ShapedRecipeBuilder.shaped(Blocks.MAGMA_BLOCK)
            .define('#', Items.MAGMA_CREAM)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_magma_cream", has(Items.MAGMA_CREAM))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.MAGMA_CREAM)
            .requires(Items.BLAZE_POWDER)
            .requires(Items.SLIME_BALL)
            .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.MAP)
            .define('#', Items.PAPER)
            .define('X', Items.COMPASS)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_compass", has(Items.COMPASS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.MELON)
            .define('M', Items.MELON_SLICE)
            .pattern("MMM")
            .pattern("MMM")
            .pattern("MMM")
            .unlockedBy("has_melon", has(Items.MELON_SLICE))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.MELON_SEEDS).requires(Items.MELON_SLICE).unlockedBy("has_melon", has(Items.MELON_SLICE)).save(param0);
        ShapedRecipeBuilder.shaped(Items.MINECART)
            .define('#', Items.IRON_INGOT)
            .pattern("# #")
            .pattern("###")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_COBBLESTONE)
            .requires(Blocks.COBBLESTONE)
            .requires(Blocks.VINE)
            .unlockedBy("has_vine", has(Blocks.VINE))
            .save(param0, getFromName(Blocks.MOSSY_COBBLESTONE, Blocks.VINE));
        ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_STONE_BRICKS)
            .requires(Blocks.STONE_BRICKS)
            .requires(Blocks.VINE)
            .unlockedBy("has_mossy_cobblestone", has(Blocks.MOSSY_COBBLESTONE))
            .save(param0, getFromName(Blocks.MOSSY_STONE_BRICKS, Blocks.VINE));
        ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_COBBLESTONE)
            .requires(Blocks.COBBLESTONE)
            .requires(Blocks.MOSS_BLOCK)
            .unlockedBy("has_moss_block", has(Blocks.MOSS_BLOCK))
            .save(param0, getFromName(Blocks.MOSSY_COBBLESTONE, Blocks.MOSS_BLOCK));
        ShapelessRecipeBuilder.shapeless(Blocks.MOSSY_STONE_BRICKS)
            .requires(Blocks.STONE_BRICKS)
            .requires(Blocks.MOSS_BLOCK)
            .unlockedBy("has_mossy_cobblestone", has(Blocks.MOSSY_COBBLESTONE))
            .save(param0, getFromName(Blocks.MOSSY_STONE_BRICKS, Blocks.MOSS_BLOCK));
        ShapelessRecipeBuilder.shapeless(Items.MUSHROOM_STEW)
            .requires(Blocks.BROWN_MUSHROOM)
            .requires(Blocks.RED_MUSHROOM)
            .requires(Items.BOWL)
            .unlockedBy("has_mushroom_stew", has(Items.MUSHROOM_STEW))
            .unlockedBy("has_bowl", has(Items.BOWL))
            .unlockedBy("has_brown_mushroom", has(Blocks.BROWN_MUSHROOM))
            .unlockedBy("has_red_mushroom", has(Blocks.RED_MUSHROOM))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.NETHER_BRICKS)
            .define('N', Items.NETHER_BRICK)
            .pattern("NN")
            .pattern("NN")
            .unlockedBy("has_netherbrick", has(Items.NETHER_BRICK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.NETHER_WART_BLOCK)
            .define('#', Items.NETHER_WART)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_nether_wart", has(Items.NETHER_WART))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.NOTE_BLOCK)
            .define('#', ItemTags.PLANKS)
            .define('X', Items.REDSTONE)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.OBSERVER)
            .define('Q', Items.QUARTZ)
            .define('R', Items.REDSTONE)
            .define('#', Blocks.COBBLESTONE)
            .pattern("###")
            .pattern("RRQ")
            .pattern("###")
            .unlockedBy("has_quartz", has(Items.QUARTZ))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.ORANGE_DYE)
            .requires(Blocks.ORANGE_TULIP)
            .group("orange_dye")
            .unlockedBy("has_red_flower", has(Blocks.ORANGE_TULIP))
            .save(param0, "orange_dye_from_orange_tulip");
        ShapelessRecipeBuilder.shapeless(Items.ORANGE_DYE, 2)
            .requires(Items.RED_DYE)
            .requires(Items.YELLOW_DYE)
            .group("orange_dye")
            .unlockedBy("has_red_dye", has(Items.RED_DYE))
            .unlockedBy("has_yellow_dye", has(Items.YELLOW_DYE))
            .save(param0, "orange_dye_from_red_yellow");
        ShapedRecipeBuilder.shaped(Items.PAINTING)
            .define('#', Items.STICK)
            .define('X', Ingredient.of(ItemTags.WOOL))
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_wool", has(ItemTags.WOOL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.PAPER, 3).define('#', Blocks.SUGAR_CANE).pattern("###").unlockedBy("has_reeds", has(Blocks.SUGAR_CANE)).save(param0);
        ShapedRecipeBuilder.shaped(Blocks.QUARTZ_PILLAR, 2)
            .define('#', Blocks.QUARTZ_BLOCK)
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_chiseled_quartz_block", has(Blocks.CHISELED_QUARTZ_BLOCK))
            .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .unlockedBy("has_quartz_pillar", has(Blocks.QUARTZ_PILLAR))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Blocks.PACKED_ICE).requires(Blocks.ICE, 9).unlockedBy("has_ice", has(Blocks.ICE)).save(param0);
        ShapelessRecipeBuilder.shapeless(Items.PINK_DYE, 2)
            .requires(Blocks.PEONY)
            .group("pink_dye")
            .unlockedBy("has_double_plant", has(Blocks.PEONY))
            .save(param0, "pink_dye_from_peony");
        ShapelessRecipeBuilder.shapeless(Items.PINK_DYE)
            .requires(Blocks.PINK_TULIP)
            .group("pink_dye")
            .unlockedBy("has_red_flower", has(Blocks.PINK_TULIP))
            .save(param0, "pink_dye_from_pink_tulip");
        ShapelessRecipeBuilder.shapeless(Items.PINK_DYE, 2)
            .requires(Items.RED_DYE)
            .requires(Items.WHITE_DYE)
            .group("pink_dye")
            .unlockedBy("has_white_dye", has(Items.WHITE_DYE))
            .unlockedBy("has_red_dye", has(Items.RED_DYE))
            .save(param0, "pink_dye_from_red_white_dye");
        ShapedRecipeBuilder.shaped(Blocks.PISTON)
            .define('R', Items.REDSTONE)
            .define('#', Blocks.COBBLESTONE)
            .define('T', ItemTags.PLANKS)
            .define('X', Items.IRON_INGOT)
            .pattern("TTT")
            .pattern("#X#")
            .pattern("#R#")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .save(param0);
        polished(param0, Blocks.POLISHED_BASALT, Blocks.BASALT);
        ShapedRecipeBuilder.shaped(Blocks.PRISMARINE)
            .define('S', Items.PRISMARINE_SHARD)
            .pattern("SS")
            .pattern("SS")
            .unlockedBy("has_prismarine_shard", has(Items.PRISMARINE_SHARD))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.PRISMARINE_BRICKS)
            .define('S', Items.PRISMARINE_SHARD)
            .pattern("SSS")
            .pattern("SSS")
            .pattern("SSS")
            .unlockedBy("has_prismarine_shard", has(Items.PRISMARINE_SHARD))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.PUMPKIN_PIE)
            .requires(Blocks.PUMPKIN)
            .requires(Items.SUGAR)
            .requires(Items.EGG)
            .unlockedBy("has_carved_pumpkin", has(Blocks.CARVED_PUMPKIN))
            .unlockedBy("has_pumpkin", has(Blocks.PUMPKIN))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.PUMPKIN_SEEDS, 4).requires(Blocks.PUMPKIN).unlockedBy("has_pumpkin", has(Blocks.PUMPKIN)).save(param0);
        ShapelessRecipeBuilder.shapeless(Items.PURPLE_DYE, 2)
            .requires(Items.BLUE_DYE)
            .requires(Items.RED_DYE)
            .unlockedBy("has_blue_dye", has(Items.BLUE_DYE))
            .unlockedBy("has_red_dye", has(Items.RED_DYE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SHULKER_BOX)
            .define('#', Blocks.CHEST)
            .define('-', Items.SHULKER_SHELL)
            .pattern("-")
            .pattern("#")
            .pattern("-")
            .unlockedBy("has_shulker_shell", has(Items.SHULKER_SHELL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.PURPUR_BLOCK, 4)
            .define('F', Items.POPPED_CHORUS_FRUIT)
            .pattern("FF")
            .pattern("FF")
            .unlockedBy("has_chorus_fruit_popped", has(Items.POPPED_CHORUS_FRUIT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.PURPUR_PILLAR)
            .define('#', Blocks.PURPUR_SLAB)
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_purpur_block", has(Blocks.PURPUR_BLOCK))
            .save(param0);
        slabBuilder(Blocks.PURPUR_SLAB, Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR))
            .unlockedBy("has_purpur_block", has(Blocks.PURPUR_BLOCK))
            .save(param0);
        stairBuilder(Blocks.PURPUR_STAIRS, Ingredient.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR))
            .unlockedBy("has_purpur_block", has(Blocks.PURPUR_BLOCK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.QUARTZ_BLOCK)
            .define('#', Items.QUARTZ)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_quartz", has(Items.QUARTZ))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.QUARTZ_BRICKS, 4)
            .define('#', Blocks.QUARTZ_BLOCK)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .save(param0);
        slabBuilder(Blocks.QUARTZ_SLAB, Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR))
            .unlockedBy("has_chiseled_quartz_block", has(Blocks.CHISELED_QUARTZ_BLOCK))
            .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .unlockedBy("has_quartz_pillar", has(Blocks.QUARTZ_PILLAR))
            .save(param0);
        stairBuilder(Blocks.QUARTZ_STAIRS, Ingredient.of(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR))
            .unlockedBy("has_chiseled_quartz_block", has(Blocks.CHISELED_QUARTZ_BLOCK))
            .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .unlockedBy("has_quartz_pillar", has(Blocks.QUARTZ_PILLAR))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.RABBIT_STEW)
            .requires(Items.BAKED_POTATO)
            .requires(Items.COOKED_RABBIT)
            .requires(Items.BOWL)
            .requires(Items.CARROT)
            .requires(Blocks.BROWN_MUSHROOM)
            .group("rabbit_stew")
            .unlockedBy("has_cooked_rabbit", has(Items.COOKED_RABBIT))
            .save(param0, "rabbit_stew_from_brown_mushroom");
        ShapelessRecipeBuilder.shapeless(Items.RABBIT_STEW)
            .requires(Items.BAKED_POTATO)
            .requires(Items.COOKED_RABBIT)
            .requires(Items.BOWL)
            .requires(Items.CARROT)
            .requires(Blocks.RED_MUSHROOM)
            .group("rabbit_stew")
            .unlockedBy("has_cooked_rabbit", has(Items.COOKED_RABBIT))
            .save(param0, "rabbit_stew_from_red_mushroom");
        ShapedRecipeBuilder.shaped(Blocks.RAIL, 16)
            .define('#', Items.STICK)
            .define('X', Items.IRON_INGOT)
            .pattern("X X")
            .pattern("X#X")
            .pattern("X X")
            .unlockedBy("has_minecart", has(Items.MINECART))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.REDSTONE, 9)
            .requires(Blocks.REDSTONE_BLOCK)
            .unlockedBy("has_redstone_block", has(Blocks.REDSTONE_BLOCK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.REDSTONE_BLOCK)
            .define('#', Items.REDSTONE)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.REDSTONE_LAMP)
            .define('R', Items.REDSTONE)
            .define('G', Blocks.GLOWSTONE)
            .pattern(" R ")
            .pattern("RGR")
            .pattern(" R ")
            .unlockedBy("has_glowstone", has(Blocks.GLOWSTONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.REDSTONE_TORCH)
            .define('#', Items.STICK)
            .define('X', Items.REDSTONE)
            .pattern("X")
            .pattern("#")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.RED_DYE)
            .requires(Items.BEETROOT)
            .group("red_dye")
            .unlockedBy("has_beetroot", has(Items.BEETROOT))
            .save(param0, "red_dye_from_beetroot");
        ShapelessRecipeBuilder.shapeless(Items.RED_DYE)
            .requires(Blocks.POPPY)
            .group("red_dye")
            .unlockedBy("has_red_flower", has(Blocks.POPPY))
            .save(param0, "red_dye_from_poppy");
        ShapelessRecipeBuilder.shapeless(Items.RED_DYE, 2)
            .requires(Blocks.ROSE_BUSH)
            .group("red_dye")
            .unlockedBy("has_double_plant", has(Blocks.ROSE_BUSH))
            .save(param0, "red_dye_from_rose_bush");
        ShapelessRecipeBuilder.shapeless(Items.RED_DYE)
            .requires(Blocks.RED_TULIP)
            .group("red_dye")
            .unlockedBy("has_red_flower", has(Blocks.RED_TULIP))
            .save(param0, "red_dye_from_tulip");
        ShapedRecipeBuilder.shaped(Blocks.RED_NETHER_BRICKS)
            .define('W', Items.NETHER_WART)
            .define('N', Items.NETHER_BRICK)
            .pattern("NW")
            .pattern("WN")
            .unlockedBy("has_nether_wart", has(Items.NETHER_WART))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.RED_SANDSTONE)
            .define('#', Blocks.RED_SAND)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_sand", has(Blocks.RED_SAND))
            .save(param0);
        slabBuilder(Blocks.RED_SANDSTONE_SLAB, Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE))
            .unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .unlockedBy("has_chiseled_red_sandstone", has(Blocks.CHISELED_RED_SANDSTONE))
            .save(param0);
        stairBuilder(Blocks.RED_SANDSTONE_STAIRS, Ingredient.of(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE))
            .unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .unlockedBy("has_chiseled_red_sandstone", has(Blocks.CHISELED_RED_SANDSTONE))
            .unlockedBy("has_cut_red_sandstone", has(Blocks.CUT_RED_SANDSTONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.REPEATER)
            .define('#', Blocks.REDSTONE_TORCH)
            .define('X', Items.REDSTONE)
            .define('I', Blocks.STONE)
            .pattern("#X#")
            .pattern("III")
            .unlockedBy("has_redstone_torch", has(Blocks.REDSTONE_TORCH))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SANDSTONE).define('#', Blocks.SAND).pattern("##").pattern("##").unlockedBy("has_sand", has(Blocks.SAND)).save(param0);
        slabBuilder(Blocks.SANDSTONE_SLAB, Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE))
            .unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
            .unlockedBy("has_chiseled_sandstone", has(Blocks.CHISELED_SANDSTONE))
            .save(param0);
        stairBuilder(Blocks.SANDSTONE_STAIRS, Ingredient.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE))
            .unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
            .unlockedBy("has_chiseled_sandstone", has(Blocks.CHISELED_SANDSTONE))
            .unlockedBy("has_cut_sandstone", has(Blocks.CUT_SANDSTONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SEA_LANTERN)
            .define('S', Items.PRISMARINE_SHARD)
            .define('C', Items.PRISMARINE_CRYSTALS)
            .pattern("SCS")
            .pattern("CCC")
            .pattern("SCS")
            .unlockedBy("has_prismarine_crystals", has(Items.PRISMARINE_CRYSTALS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.SHEARS)
            .define('#', Items.IRON_INGOT)
            .pattern(" #")
            .pattern("# ")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.SHIELD)
            .define('W', ItemTags.PLANKS)
            .define('o', Items.IRON_INGOT)
            .pattern("WoW")
            .pattern("WWW")
            .pattern(" W ")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SLIME_BLOCK)
            .define('#', Items.SLIME_BALL)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_slime_ball", has(Items.SLIME_BALL))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.SLIME_BALL, 9).requires(Blocks.SLIME_BLOCK).unlockedBy("has_slime", has(Blocks.SLIME_BLOCK)).save(param0);
        cut(param0, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE);
        cut(param0, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE);
        ShapedRecipeBuilder.shaped(Blocks.SNOW_BLOCK)
            .define('#', Items.SNOWBALL)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_snowball", has(Items.SNOWBALL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SNOW, 6).define('#', Blocks.SNOW_BLOCK).pattern("###").unlockedBy("has_snowball", has(Items.SNOWBALL)).save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SOUL_CAMPFIRE)
            .define('L', ItemTags.LOGS)
            .define('S', Items.STICK)
            .define('#', ItemTags.SOUL_FIRE_BASE_BLOCKS)
            .pattern(" S ")
            .pattern("S#S")
            .pattern("LLL")
            .unlockedBy("has_stick", has(Items.STICK))
            .unlockedBy("has_soul_sand", has(ItemTags.SOUL_FIRE_BASE_BLOCKS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.GLISTERING_MELON_SLICE)
            .define('#', Items.GOLD_NUGGET)
            .define('X', Items.MELON_SLICE)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_melon", has(Items.MELON_SLICE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.SPECTRAL_ARROW, 2)
            .define('#', Items.GLOWSTONE_DUST)
            .define('X', Items.ARROW)
            .pattern(" # ")
            .pattern("#X#")
            .pattern(" # ")
            .unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.SPYGLASS)
            .define('#', Items.AMETHYST_SHARD)
            .define('X', Items.COPPER_INGOT)
            .pattern(" # ")
            .pattern(" X ")
            .pattern(" X ")
            .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.STICK, 4)
            .define('#', ItemTags.PLANKS)
            .pattern("#")
            .pattern("#")
            .group("sticks")
            .unlockedBy("has_planks", has(ItemTags.PLANKS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.STICK, 1)
            .define('#', Blocks.BAMBOO)
            .pattern("#")
            .pattern("#")
            .group("sticks")
            .unlockedBy("has_bamboo", has(Blocks.BAMBOO))
            .save(param0, "stick_from_bamboo_item");
        ShapedRecipeBuilder.shaped(Blocks.STICKY_PISTON)
            .define('P', Blocks.PISTON)
            .define('S', Items.SLIME_BALL)
            .pattern("S")
            .pattern("P")
            .unlockedBy("has_slime_ball", has(Items.SLIME_BALL))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.STONE_BRICKS, 4)
            .define('#', Blocks.STONE)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_stone", has(Blocks.STONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.STONE_AXE)
            .define('#', Items.STICK)
            .define('X', ItemTags.STONE_TOOL_MATERIALS)
            .pattern("XX")
            .pattern("X#")
            .pattern(" #")
            .unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
            .save(param0);
        slabBuilder(Blocks.STONE_BRICK_SLAB, Ingredient.of(Blocks.STONE_BRICKS)).unlockedBy("has_stone_bricks", has(ItemTags.STONE_BRICKS)).save(param0);
        stairBuilder(Blocks.STONE_BRICK_STAIRS, Ingredient.of(Blocks.STONE_BRICKS)).unlockedBy("has_stone_bricks", has(ItemTags.STONE_BRICKS)).save(param0);
        ShapedRecipeBuilder.shaped(Items.STONE_HOE)
            .define('#', Items.STICK)
            .define('X', ItemTags.STONE_TOOL_MATERIALS)
            .pattern("XX")
            .pattern(" #")
            .pattern(" #")
            .unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.STONE_PICKAXE)
            .define('#', Items.STICK)
            .define('X', ItemTags.STONE_TOOL_MATERIALS)
            .pattern("XXX")
            .pattern(" # ")
            .pattern(" # ")
            .unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.STONE_SHOVEL)
            .define('#', Items.STICK)
            .define('X', ItemTags.STONE_TOOL_MATERIALS)
            .pattern("X")
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
            .save(param0);
        slab(param0, Blocks.SMOOTH_STONE_SLAB, Blocks.SMOOTH_STONE);
        ShapedRecipeBuilder.shaped(Items.STONE_SWORD)
            .define('#', Items.STICK)
            .define('X', ItemTags.STONE_TOOL_MATERIALS)
            .pattern("X")
            .pattern("X")
            .pattern("#")
            .unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.WHITE_WOOL)
            .define('#', Items.STRING)
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_string", has(Items.STRING))
            .save(param0, "white_wool_from_string");
        ShapelessRecipeBuilder.shapeless(Items.SUGAR)
            .requires(Blocks.SUGAR_CANE)
            .group("sugar")
            .unlockedBy("has_reeds", has(Blocks.SUGAR_CANE))
            .save(param0, "sugar_from_sugar_cane");
        ShapelessRecipeBuilder.shapeless(Items.SUGAR, 3)
            .requires(Items.HONEY_BOTTLE)
            .group("sugar")
            .unlockedBy("has_honey_bottle", has(Items.HONEY_BOTTLE))
            .save(param0, "sugar_from_honey_bottle");
        ShapedRecipeBuilder.shaped(Blocks.TARGET)
            .define('H', Items.HAY_BLOCK)
            .define('R', Items.REDSTONE)
            .pattern(" R ")
            .pattern("RHR")
            .pattern(" R ")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .unlockedBy("has_hay_block", has(Blocks.HAY_BLOCK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.TNT)
            .define('#', Ingredient.of(Blocks.SAND, Blocks.RED_SAND))
            .define('X', Items.GUNPOWDER)
            .pattern("X#X")
            .pattern("#X#")
            .pattern("X#X")
            .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.TNT_MINECART)
            .define('A', Blocks.TNT)
            .define('B', Items.MINECART)
            .pattern("A")
            .pattern("B")
            .unlockedBy("has_minecart", has(Items.MINECART))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.TORCH, 4)
            .define('#', Items.STICK)
            .define('X', Ingredient.of(Items.COAL, Items.CHARCOAL))
            .pattern("X")
            .pattern("#")
            .unlockedBy("has_stone_pickaxe", has(Items.STONE_PICKAXE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SOUL_TORCH, 4)
            .define('X', Ingredient.of(Items.COAL, Items.CHARCOAL))
            .define('#', Items.STICK)
            .define('S', ItemTags.SOUL_FIRE_BASE_BLOCKS)
            .pattern("X")
            .pattern("#")
            .pattern("S")
            .unlockedBy("has_soul_sand", has(ItemTags.SOUL_FIRE_BASE_BLOCKS))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.LANTERN)
            .define('#', Items.TORCH)
            .define('X', Items.IRON_NUGGET)
            .pattern("XXX")
            .pattern("X#X")
            .pattern("XXX")
            .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SOUL_LANTERN)
            .define('#', Items.SOUL_TORCH)
            .define('X', Items.IRON_NUGGET)
            .pattern("XXX")
            .pattern("X#X")
            .pattern("XXX")
            .unlockedBy("has_soul_torch", has(Items.SOUL_TORCH))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Blocks.TRAPPED_CHEST)
            .requires(Blocks.CHEST)
            .requires(Blocks.TRIPWIRE_HOOK)
            .unlockedBy("has_tripwire_hook", has(Blocks.TRIPWIRE_HOOK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.TRIPWIRE_HOOK, 2)
            .define('#', ItemTags.PLANKS)
            .define('S', Items.STICK)
            .define('I', Items.IRON_INGOT)
            .pattern("I")
            .pattern("S")
            .pattern("#")
            .unlockedBy("has_string", has(Items.STRING))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.TURTLE_HELMET)
            .define('X', Items.SCUTE)
            .pattern("XXX")
            .pattern("X X")
            .unlockedBy("has_scute", has(Items.SCUTE))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.WHEAT, 9).requires(Blocks.HAY_BLOCK).unlockedBy("has_hay_block", has(Blocks.HAY_BLOCK)).save(param0);
        ShapelessRecipeBuilder.shapeless(Items.WHITE_DYE)
            .requires(Items.BONE_MEAL)
            .group("white_dye")
            .unlockedBy("has_bone_meal", has(Items.BONE_MEAL))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.WHITE_DYE)
            .requires(Blocks.LILY_OF_THE_VALLEY)
            .group("white_dye")
            .unlockedBy("has_white_flower", has(Blocks.LILY_OF_THE_VALLEY))
            .save(param0, "white_dye_from_lily_of_the_valley");
        ShapedRecipeBuilder.shaped(Items.WOODEN_AXE)
            .define('#', Items.STICK)
            .define('X', ItemTags.PLANKS)
            .pattern("XX")
            .pattern("X#")
            .pattern(" #")
            .unlockedBy("has_stick", has(Items.STICK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.WOODEN_HOE)
            .define('#', Items.STICK)
            .define('X', ItemTags.PLANKS)
            .pattern("XX")
            .pattern(" #")
            .pattern(" #")
            .unlockedBy("has_stick", has(Items.STICK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.WOODEN_PICKAXE)
            .define('#', Items.STICK)
            .define('X', ItemTags.PLANKS)
            .pattern("XXX")
            .pattern(" # ")
            .pattern(" # ")
            .unlockedBy("has_stick", has(Items.STICK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.WOODEN_SHOVEL)
            .define('#', Items.STICK)
            .define('X', ItemTags.PLANKS)
            .pattern("X")
            .pattern("#")
            .pattern("#")
            .unlockedBy("has_stick", has(Items.STICK))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.WOODEN_SWORD)
            .define('#', Items.STICK)
            .define('X', ItemTags.PLANKS)
            .pattern("X")
            .pattern("X")
            .pattern("#")
            .unlockedBy("has_stick", has(Items.STICK))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.WRITABLE_BOOK)
            .requires(Items.BOOK)
            .requires(Items.INK_SAC)
            .requires(Items.FEATHER)
            .unlockedBy("has_book", has(Items.BOOK))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.YELLOW_DYE)
            .requires(Blocks.DANDELION)
            .group("yellow_dye")
            .unlockedBy("has_yellow_flower", has(Blocks.DANDELION))
            .save(param0, "yellow_dye_from_dandelion");
        ShapelessRecipeBuilder.shapeless(Items.YELLOW_DYE, 2)
            .requires(Blocks.SUNFLOWER)
            .group("yellow_dye")
            .unlockedBy("has_double_plant", has(Blocks.SUNFLOWER))
            .save(param0, "yellow_dye_from_sunflower");
        ShapelessRecipeBuilder.shapeless(Items.DRIED_KELP, 9)
            .requires(Blocks.DRIED_KELP_BLOCK)
            .unlockedBy("has_dried_kelp_block", has(Blocks.DRIED_KELP_BLOCK))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Blocks.DRIED_KELP_BLOCK)
            .requires(Items.DRIED_KELP, 9)
            .unlockedBy("has_dried_kelp", has(Items.DRIED_KELP))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CONDUIT)
            .define('#', Items.NAUTILUS_SHELL)
            .define('X', Items.HEART_OF_THE_SEA)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .unlockedBy("has_nautilus_core", has(Items.HEART_OF_THE_SEA))
            .unlockedBy("has_nautilus_shell", has(Items.NAUTILUS_SHELL))
            .save(param0);
        wall(param0, Blocks.RED_SANDSTONE_WALL, Blocks.RED_SANDSTONE);
        wall(param0, Blocks.STONE_BRICK_WALL, Blocks.STONE_BRICKS);
        wall(param0, Blocks.SANDSTONE_WALL, Blocks.SANDSTONE);
        ShapelessRecipeBuilder.shapeless(Items.CREEPER_BANNER_PATTERN)
            .requires(Items.PAPER)
            .requires(Items.CREEPER_HEAD)
            .unlockedBy("has_creeper_head", has(Items.CREEPER_HEAD))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.SKULL_BANNER_PATTERN)
            .requires(Items.PAPER)
            .requires(Items.WITHER_SKELETON_SKULL)
            .unlockedBy("has_wither_skeleton_skull", has(Items.WITHER_SKELETON_SKULL))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.FLOWER_BANNER_PATTERN)
            .requires(Items.PAPER)
            .requires(Blocks.OXEYE_DAISY)
            .unlockedBy("has_oxeye_daisy", has(Blocks.OXEYE_DAISY))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.MOJANG_BANNER_PATTERN)
            .requires(Items.PAPER)
            .requires(Items.ENCHANTED_GOLDEN_APPLE)
            .unlockedBy("has_enchanted_golden_apple", has(Items.ENCHANTED_GOLDEN_APPLE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SCAFFOLDING, 6)
            .define('~', Items.STRING)
            .define('I', Blocks.BAMBOO)
            .pattern("I~I")
            .pattern("I I")
            .pattern("I I")
            .unlockedBy("has_bamboo", has(Blocks.BAMBOO))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.GRINDSTONE)
            .define('I', Items.STICK)
            .define('-', Blocks.STONE_SLAB)
            .define('#', ItemTags.PLANKS)
            .pattern("I-I")
            .pattern("# #")
            .unlockedBy("has_stone_slab", has(Blocks.STONE_SLAB))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.BLAST_FURNACE)
            .define('#', Blocks.SMOOTH_STONE)
            .define('X', Blocks.FURNACE)
            .define('I', Items.IRON_INGOT)
            .pattern("III")
            .pattern("IXI")
            .pattern("###")
            .unlockedBy("has_smooth_stone", has(Blocks.SMOOTH_STONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SMOKER)
            .define('#', ItemTags.LOGS)
            .define('X', Blocks.FURNACE)
            .pattern(" # ")
            .pattern("#X#")
            .pattern(" # ")
            .unlockedBy("has_furnace", has(Blocks.FURNACE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CARTOGRAPHY_TABLE)
            .define('#', ItemTags.PLANKS)
            .define('@', Items.PAPER)
            .pattern("@@")
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_paper", has(Items.PAPER))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.SMITHING_TABLE)
            .define('#', ItemTags.PLANKS)
            .define('@', Items.IRON_INGOT)
            .pattern("@@")
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.FLETCHING_TABLE)
            .define('#', ItemTags.PLANKS)
            .define('@', Items.FLINT)
            .pattern("@@")
            .pattern("##")
            .pattern("##")
            .unlockedBy("has_flint", has(Items.FLINT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.STONECUTTER)
            .define('I', Items.IRON_INGOT)
            .define('#', Blocks.STONE)
            .pattern(" I ")
            .pattern("###")
            .unlockedBy("has_stone", has(Blocks.STONE))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.LODESTONE)
            .define('S', Items.CHISELED_STONE_BRICKS)
            .define('#', Items.NETHERITE_INGOT)
            .pattern("SSS")
            .pattern("S#S")
            .pattern("SSS")
            .unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.NETHERITE_BLOCK)
            .define('#', Items.NETHERITE_INGOT)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
            .save(param0);
        ShapelessRecipeBuilder.shapeless(Items.NETHERITE_INGOT, 9)
            .requires(Blocks.NETHERITE_BLOCK)
            .group("netherite_ingot")
            .unlockedBy("has_netherite_block", has(Blocks.NETHERITE_BLOCK))
            .save(param0, "netherite_ingot_from_netherite_block");
        ShapelessRecipeBuilder.shapeless(Items.NETHERITE_INGOT)
            .requires(Items.NETHERITE_SCRAP, 4)
            .requires(Items.GOLD_INGOT, 4)
            .group("netherite_ingot")
            .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.RESPAWN_ANCHOR)
            .define('O', Blocks.CRYING_OBSIDIAN)
            .define('G', Blocks.GLOWSTONE)
            .pattern("OOO")
            .pattern("GGG")
            .pattern("OOO")
            .unlockedBy("has_obsidian", has(Blocks.CRYING_OBSIDIAN))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.CHAIN)
            .define('I', Items.IRON_INGOT)
            .define('N', Items.IRON_NUGGET)
            .pattern("N")
            .pattern("I")
            .pattern("N")
            .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(param0);
        ShapedRecipeBuilder.shaped(Items.CANDLE)
            .define('S', Items.STRING)
            .define('H', Items.HONEYCOMB)
            .pattern("S")
            .pattern("H")
            .unlockedBy("has_string", has(Items.STRING))
            .unlockedBy("has_honeycomb", has(Items.HONEYCOMB))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.TINTED_GLASS, 2)
            .define('G', Blocks.GLASS)
            .define('S', Items.AMETHYST_SHARD)
            .pattern(" S ")
            .pattern("SGS")
            .pattern(" S ")
            .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
            .save(param0);
        ShapedRecipeBuilder.shaped(Blocks.AMETHYST_BLOCK)
            .define('S', Items.AMETHYST_SHARD)
            .pattern("SS")
            .pattern("SS")
            .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
            .save(param0);
        SpecialRecipeBuilder.special(RecipeSerializer.ARMOR_DYE).save(param0, "armor_dye");
        SpecialRecipeBuilder.special(RecipeSerializer.BANNER_DUPLICATE).save(param0, "banner_duplicate");
        SpecialRecipeBuilder.special(RecipeSerializer.BOOK_CLONING).save(param0, "book_cloning");
        ShapedRecipeBuilder.shaped(Items.BUNDLE)
            .define('#', Items.RABBIT_HIDE)
            .define('-', Items.STRING)
            .pattern("-#-")
            .pattern("# #")
            .pattern("###")
            .unlockedBy("has_string", has(Items.STRING))
            .save(param0);
        SpecialRecipeBuilder.special(RecipeSerializer.FIREWORK_ROCKET).save(param0, "firework_rocket");
        SpecialRecipeBuilder.special(RecipeSerializer.FIREWORK_STAR).save(param0, "firework_star");
        SpecialRecipeBuilder.special(RecipeSerializer.FIREWORK_STAR_FADE).save(param0, "firework_star_fade");
        SpecialRecipeBuilder.special(RecipeSerializer.MAP_CLONING).save(param0, "map_cloning");
        SpecialRecipeBuilder.special(RecipeSerializer.MAP_EXTENDING).save(param0, "map_extending");
        SpecialRecipeBuilder.special(RecipeSerializer.REPAIR_ITEM).save(param0, "repair_item");
        SpecialRecipeBuilder.special(RecipeSerializer.SHIELD_DECORATION).save(param0, "shield_decoration");
        SpecialRecipeBuilder.special(RecipeSerializer.SHULKER_BOX_COLORING).save(param0, "shulker_box_coloring");
        SpecialRecipeBuilder.special(RecipeSerializer.TIPPED_ARROW).save(param0, "tipped_arrow");
        SpecialRecipeBuilder.special(RecipeSerializer.SUSPICIOUS_STEW).save(param0, "suspicious_stew");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.POTATO), Items.BAKED_POTATO, 0.35F, 200)
            .unlockedBy("has_potato", has(Items.POTATO))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CLAY_BALL), Items.BRICK, 0.3F, 200)
            .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.LOGS_THAT_BURN), Items.CHARCOAL, 0.15F, 200)
            .unlockedBy("has_log", has(ItemTags.LOGS_THAT_BURN))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHORUS_FRUIT), Items.POPPED_CHORUS_FRUIT, 0.1F, 200)
            .unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.COAL_ORES), Items.COAL, 0.1F, 200)
            .unlockedBy("has_coal_ore", has(ItemTags.COAL_ORES))
            .save(param0, "coal_from_smelting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.BEEF), Items.COOKED_BEEF, 0.35F, 200).unlockedBy("has_beef", has(Items.BEEF)).save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.CHICKEN), Items.COOKED_CHICKEN, 0.35F, 200)
            .unlockedBy("has_chicken", has(Items.CHICKEN))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.COD), Items.COOKED_COD, 0.35F, 200).unlockedBy("has_cod", has(Items.COD)).save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.KELP), Items.DRIED_KELP, 0.1F, 200)
            .unlockedBy("has_kelp", has(Blocks.KELP))
            .save(param0, "dried_kelp_from_smelting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.SALMON), Items.COOKED_SALMON, 0.35F, 200)
            .unlockedBy("has_salmon", has(Items.SALMON))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.MUTTON), Items.COOKED_MUTTON, 0.35F, 200)
            .unlockedBy("has_mutton", has(Items.MUTTON))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.PORKCHOP), Items.COOKED_PORKCHOP, 0.35F, 200)
            .unlockedBy("has_porkchop", has(Items.PORKCHOP))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.RABBIT), Items.COOKED_RABBIT, 0.35F, 200)
            .unlockedBy("has_rabbit", has(Items.RABBIT))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.DIAMOND_ORES), Items.DIAMOND, 1.0F, 200)
            .unlockedBy("has_diamond_ore", has(ItemTags.DIAMOND_ORES))
            .save(param0, "diamond_from_smelting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.LAPIS_ORES), Items.LAPIS_LAZULI, 0.2F, 200)
            .unlockedBy("has_lapis_ore", has(ItemTags.LAPIS_ORES))
            .save(param0, "lapis_from_smelting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.EMERALD_ORES), Items.EMERALD, 1.0F, 200)
            .unlockedBy("has_emerald_ore", has(ItemTags.EMERALD_ORES))
            .save(param0, "emerald_from_smelting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.SAND), Blocks.GLASS.asItem(), 0.1F, 200)
            .unlockedBy("has_sand", has(ItemTags.SAND))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.GOLD_ORES), Items.GOLD_INGOT, 1.0F, 200)
            .unlockedBy("has_gold_ore", has(ItemTags.GOLD_ORES))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SEA_PICKLE.asItem()), Items.LIME_DYE, 0.1F, 200)
            .unlockedBy("has_sea_pickle", has(Blocks.SEA_PICKLE))
            .save(param0, "lime_dye_from_smelting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CACTUS.asItem()), Items.GREEN_DYE, 1.0F, 200)
            .unlockedBy("has_cactus", has(Blocks.CACTUS))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(
                Ingredient.of(
                    Items.GOLDEN_PICKAXE,
                    Items.GOLDEN_SHOVEL,
                    Items.GOLDEN_AXE,
                    Items.GOLDEN_HOE,
                    Items.GOLDEN_SWORD,
                    Items.GOLDEN_HELMET,
                    Items.GOLDEN_CHESTPLATE,
                    Items.GOLDEN_LEGGINGS,
                    Items.GOLDEN_BOOTS,
                    Items.GOLDEN_HORSE_ARMOR
                ),
                Items.GOLD_NUGGET,
                0.1F,
                200
            )
            .unlockedBy("has_golden_pickaxe", has(Items.GOLDEN_PICKAXE))
            .unlockedBy("has_golden_shovel", has(Items.GOLDEN_SHOVEL))
            .unlockedBy("has_golden_axe", has(Items.GOLDEN_AXE))
            .unlockedBy("has_golden_hoe", has(Items.GOLDEN_HOE))
            .unlockedBy("has_golden_sword", has(Items.GOLDEN_SWORD))
            .unlockedBy("has_golden_helmet", has(Items.GOLDEN_HELMET))
            .unlockedBy("has_golden_chestplate", has(Items.GOLDEN_CHESTPLATE))
            .unlockedBy("has_golden_leggings", has(Items.GOLDEN_LEGGINGS))
            .unlockedBy("has_golden_boots", has(Items.GOLDEN_BOOTS))
            .unlockedBy("has_golden_horse_armor", has(Items.GOLDEN_HORSE_ARMOR))
            .save(param0, "gold_nugget_from_smelting");
        SimpleCookingRecipeBuilder.smelting(
                Ingredient.of(
                    Items.IRON_PICKAXE,
                    Items.IRON_SHOVEL,
                    Items.IRON_AXE,
                    Items.IRON_HOE,
                    Items.IRON_SWORD,
                    Items.IRON_HELMET,
                    Items.IRON_CHESTPLATE,
                    Items.IRON_LEGGINGS,
                    Items.IRON_BOOTS,
                    Items.IRON_HORSE_ARMOR,
                    Items.CHAINMAIL_HELMET,
                    Items.CHAINMAIL_CHESTPLATE,
                    Items.CHAINMAIL_LEGGINGS,
                    Items.CHAINMAIL_BOOTS
                ),
                Items.IRON_NUGGET,
                0.1F,
                200
            )
            .unlockedBy("has_iron_pickaxe", has(Items.IRON_PICKAXE))
            .unlockedBy("has_iron_shovel", has(Items.IRON_SHOVEL))
            .unlockedBy("has_iron_axe", has(Items.IRON_AXE))
            .unlockedBy("has_iron_hoe", has(Items.IRON_HOE))
            .unlockedBy("has_iron_sword", has(Items.IRON_SWORD))
            .unlockedBy("has_iron_helmet", has(Items.IRON_HELMET))
            .unlockedBy("has_iron_chestplate", has(Items.IRON_CHESTPLATE))
            .unlockedBy("has_iron_leggings", has(Items.IRON_LEGGINGS))
            .unlockedBy("has_iron_boots", has(Items.IRON_BOOTS))
            .unlockedBy("has_iron_horse_armor", has(Items.IRON_HORSE_ARMOR))
            .unlockedBy("has_chainmail_helmet", has(Items.CHAINMAIL_HELMET))
            .unlockedBy("has_chainmail_chestplate", has(Items.CHAINMAIL_CHESTPLATE))
            .unlockedBy("has_chainmail_leggings", has(Items.CHAINMAIL_LEGGINGS))
            .unlockedBy("has_chainmail_boots", has(Items.CHAINMAIL_BOOTS))
            .save(param0, "iron_nugget_from_smelting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.IRON_ORES), Items.IRON_INGOT, 0.7F, 200)
            .unlockedBy("has_iron_ore", has(ItemTags.IRON_ORES))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.COPPER_ORES), Items.COPPER_INGOT, 0.7F, 200)
            .unlockedBy("has_copper_ore", has(ItemTags.COPPER_ORES))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CLAY), Blocks.TERRACOTTA.asItem(), 0.35F, 200)
            .unlockedBy("has_clay_block", has(Blocks.CLAY))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHERRACK), Items.NETHER_BRICK, 0.1F, 200)
            .unlockedBy("has_netherrack", has(Blocks.NETHERRACK))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), Items.QUARTZ, 0.2F, 200)
            .unlockedBy("has_nether_quartz_ore", has(Blocks.NETHER_QUARTZ_ORE))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.REDSTONE_ORES), Items.REDSTONE, 0.7F, 200)
            .unlockedBy("has_redstone_ore", has(ItemTags.REDSTONE_ORES))
            .save(param0, "redstone_from_smelting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WET_SPONGE), Blocks.SPONGE.asItem(), 0.15F, 200)
            .unlockedBy("has_wet_sponge", has(Blocks.WET_SPONGE))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLESTONE), Blocks.STONE.asItem(), 0.1F, 200)
            .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE), Blocks.SMOOTH_STONE.asItem(), 0.1F, 200)
            .unlockedBy("has_stone", has(Blocks.STONE))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.SANDSTONE), Blocks.SMOOTH_SANDSTONE.asItem(), 0.1F, 200)
            .unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE.asItem(), 0.1F, 200)
            .unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.SMOOTH_QUARTZ.asItem(), 0.1F, 200)
            .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.CRACKED_STONE_BRICKS.asItem(), 0.1F, 200)
            .unlockedBy("has_stone_bricks", has(Blocks.STONE_BRICKS))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLACK_TERRACOTTA), Blocks.BLACK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_black_terracotta", has(Blocks.BLACK_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BLUE_TERRACOTTA), Blocks.BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_blue_terracotta", has(Blocks.BLUE_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BROWN_TERRACOTTA), Blocks.BROWN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_brown_terracotta", has(Blocks.BROWN_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.CYAN_TERRACOTTA), Blocks.CYAN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_cyan_terracotta", has(Blocks.CYAN_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GRAY_TERRACOTTA), Blocks.GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_gray_terracotta", has(Blocks.GRAY_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.GREEN_TERRACOTTA), Blocks.GREEN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_green_terracotta", has(Blocks.GREEN_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIGHT_BLUE_TERRACOTTA), Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_light_blue_terracotta", has(Blocks.LIGHT_BLUE_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIGHT_GRAY_TERRACOTTA), Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_light_gray_terracotta", has(Blocks.LIGHT_GRAY_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.LIME_TERRACOTTA), Blocks.LIME_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_lime_terracotta", has(Blocks.LIME_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.MAGENTA_TERRACOTTA), Blocks.MAGENTA_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_magenta_terracotta", has(Blocks.MAGENTA_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ORANGE_TERRACOTTA), Blocks.ORANGE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_orange_terracotta", has(Blocks.ORANGE_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PINK_TERRACOTTA), Blocks.PINK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_pink_terracotta", has(Blocks.PINK_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.PURPLE_TERRACOTTA), Blocks.PURPLE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_purple_terracotta", has(Blocks.PURPLE_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.RED_TERRACOTTA), Blocks.RED_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_red_terracotta", has(Blocks.RED_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.WHITE_TERRACOTTA), Blocks.WHITE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_white_terracotta", has(Blocks.WHITE_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.YELLOW_TERRACOTTA), Blocks.YELLOW_GLAZED_TERRACOTTA.asItem(), 0.1F, 200)
            .unlockedBy("has_yellow_terracotta", has(Blocks.YELLOW_TERRACOTTA))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.ANCIENT_DEBRIS), Items.NETHERITE_SCRAP, 2.0F, 200)
            .unlockedBy("has_ancient_debris", has(Blocks.ANCIENT_DEBRIS))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.BASALT), Blocks.SMOOTH_BASALT, 0.1F, 200)
            .unlockedBy("has_basalt", has(Blocks.BASALT))
            .save(param0);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Blocks.COBBLED_DEEPSLATE), Blocks.DEEPSLATE, 0.1F, 200)
            .unlockedBy("has_cobbled_deepslate", has(Blocks.COBBLED_DEEPSLATE))
            .save(param0);
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ItemTags.IRON_ORES), Items.IRON_INGOT, 0.7F, 100)
            .unlockedBy("has_iron_ore", has(ItemTags.IRON_ORES))
            .save(param0, "iron_ingot_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ItemTags.COPPER_ORES), Items.COPPER_INGOT, 0.7F, 100)
            .unlockedBy("has_copper_ore", has(ItemTags.COPPER_ORES))
            .save(param0, "copper_ingot_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ItemTags.GOLD_ORES), Items.GOLD_INGOT, 1.0F, 100)
            .unlockedBy("has_gold_ore", has(ItemTags.GOLD_ORES))
            .save(param0, "gold_ingot_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ItemTags.DIAMOND_ORES), Items.DIAMOND, 1.0F, 100)
            .unlockedBy("has_diamond_ore", has(ItemTags.DIAMOND_ORES))
            .save(param0, "diamond_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ItemTags.LAPIS_ORES), Items.LAPIS_LAZULI, 0.2F, 100)
            .unlockedBy("has_lapis_ore", has(ItemTags.LAPIS_ORES))
            .save(param0, "lapis_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ItemTags.REDSTONE_ORES), Items.REDSTONE, 0.7F, 100)
            .unlockedBy("has_redstone_ore", has(ItemTags.REDSTONE_ORES))
            .save(param0, "redstone_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ItemTags.COAL_ORES), Items.COAL, 0.1F, 100)
            .unlockedBy("has_coal_ore", has(ItemTags.COAL_ORES))
            .save(param0, "coal_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ItemTags.EMERALD_ORES), Items.EMERALD, 1.0F, 100)
            .unlockedBy("has_emerald_ore", has(ItemTags.EMERALD_ORES))
            .save(param0, "emerald_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.NETHER_QUARTZ_ORE), Items.QUARTZ, 0.2F, 100)
            .unlockedBy("has_nether_quartz_ore", has(Blocks.NETHER_QUARTZ_ORE))
            .save(param0, "quartz_from_blasting");
        SimpleCookingRecipeBuilder.blasting(
                Ingredient.of(
                    Items.GOLDEN_PICKAXE,
                    Items.GOLDEN_SHOVEL,
                    Items.GOLDEN_AXE,
                    Items.GOLDEN_HOE,
                    Items.GOLDEN_SWORD,
                    Items.GOLDEN_HELMET,
                    Items.GOLDEN_CHESTPLATE,
                    Items.GOLDEN_LEGGINGS,
                    Items.GOLDEN_BOOTS,
                    Items.GOLDEN_HORSE_ARMOR
                ),
                Items.GOLD_NUGGET,
                0.1F,
                100
            )
            .unlockedBy("has_golden_pickaxe", has(Items.GOLDEN_PICKAXE))
            .unlockedBy("has_golden_shovel", has(Items.GOLDEN_SHOVEL))
            .unlockedBy("has_golden_axe", has(Items.GOLDEN_AXE))
            .unlockedBy("has_golden_hoe", has(Items.GOLDEN_HOE))
            .unlockedBy("has_golden_sword", has(Items.GOLDEN_SWORD))
            .unlockedBy("has_golden_helmet", has(Items.GOLDEN_HELMET))
            .unlockedBy("has_golden_chestplate", has(Items.GOLDEN_CHESTPLATE))
            .unlockedBy("has_golden_leggings", has(Items.GOLDEN_LEGGINGS))
            .unlockedBy("has_golden_boots", has(Items.GOLDEN_BOOTS))
            .unlockedBy("has_golden_horse_armor", has(Items.GOLDEN_HORSE_ARMOR))
            .save(param0, "gold_nugget_from_blasting");
        SimpleCookingRecipeBuilder.blasting(
                Ingredient.of(
                    Items.IRON_PICKAXE,
                    Items.IRON_SHOVEL,
                    Items.IRON_AXE,
                    Items.IRON_HOE,
                    Items.IRON_SWORD,
                    Items.IRON_HELMET,
                    Items.IRON_CHESTPLATE,
                    Items.IRON_LEGGINGS,
                    Items.IRON_BOOTS,
                    Items.IRON_HORSE_ARMOR,
                    Items.CHAINMAIL_HELMET,
                    Items.CHAINMAIL_CHESTPLATE,
                    Items.CHAINMAIL_LEGGINGS,
                    Items.CHAINMAIL_BOOTS
                ),
                Items.IRON_NUGGET,
                0.1F,
                100
            )
            .unlockedBy("has_iron_pickaxe", has(Items.IRON_PICKAXE))
            .unlockedBy("has_iron_shovel", has(Items.IRON_SHOVEL))
            .unlockedBy("has_iron_axe", has(Items.IRON_AXE))
            .unlockedBy("has_iron_hoe", has(Items.IRON_HOE))
            .unlockedBy("has_iron_sword", has(Items.IRON_SWORD))
            .unlockedBy("has_iron_helmet", has(Items.IRON_HELMET))
            .unlockedBy("has_iron_chestplate", has(Items.IRON_CHESTPLATE))
            .unlockedBy("has_iron_leggings", has(Items.IRON_LEGGINGS))
            .unlockedBy("has_iron_boots", has(Items.IRON_BOOTS))
            .unlockedBy("has_iron_horse_armor", has(Items.IRON_HORSE_ARMOR))
            .unlockedBy("has_chainmail_helmet", has(Items.CHAINMAIL_HELMET))
            .unlockedBy("has_chainmail_chestplate", has(Items.CHAINMAIL_CHESTPLATE))
            .unlockedBy("has_chainmail_leggings", has(Items.CHAINMAIL_LEGGINGS))
            .unlockedBy("has_chainmail_boots", has(Items.CHAINMAIL_BOOTS))
            .save(param0, "iron_nugget_from_blasting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Blocks.ANCIENT_DEBRIS), Items.NETHERITE_SCRAP, 2.0F, 100)
            .unlockedBy("has_ancient_debris", has(Blocks.ANCIENT_DEBRIS))
            .save(param0, "netherite_scrap_from_blasting");
        cookRecipes(param0, "smoking", RecipeSerializer.SMOKING_RECIPE, 100);
        cookRecipes(param0, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, 600);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_SLAB, 2)
            .unlocks("has_stone", has(Blocks.STONE))
            .save(param0, "stone_slab_from_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_STAIRS)
            .unlocks("has_stone", has(Blocks.STONE))
            .save(param0, "stone_stairs_from_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICKS)
            .unlocks("has_stone", has(Blocks.STONE))
            .save(param0, "stone_bricks_from_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICK_SLAB, 2)
            .unlocks("has_stone", has(Blocks.STONE))
            .save(param0, "stone_brick_slab_from_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICK_STAIRS)
            .unlocks("has_stone", has(Blocks.STONE))
            .save(param0, "stone_brick_stairs_from_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.CHISELED_STONE_BRICKS)
            .unlocks("has_stone", has(Blocks.STONE))
            .save(param0, "chiseled_stone_bricks_stone_from_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE), Blocks.STONE_BRICK_WALL)
            .unlocks("has_stone", has(Blocks.STONE))
            .save(param0, "stone_brick_walls_from_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.CUT_SANDSTONE)
            .unlocks("has_sandstone", has(Blocks.SANDSTONE))
            .save(param0, "cut_sandstone_from_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.SANDSTONE_SLAB, 2)
            .unlocks("has_sandstone", has(Blocks.SANDSTONE))
            .save(param0, "sandstone_slab_from_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.CUT_SANDSTONE_SLAB, 2)
            .unlocks("has_sandstone", has(Blocks.SANDSTONE))
            .save(param0, "cut_sandstone_slab_from_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.CUT_SANDSTONE), Blocks.CUT_SANDSTONE_SLAB, 2)
            .unlocks("has_cut_sandstone", has(Blocks.SANDSTONE))
            .save(param0, "cut_sandstone_slab_from_cut_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.SANDSTONE_STAIRS)
            .unlocks("has_sandstone", has(Blocks.SANDSTONE))
            .save(param0, "sandstone_stairs_from_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.SANDSTONE_WALL)
            .unlocks("has_sandstone", has(Blocks.SANDSTONE))
            .save(param0, "sandstone_wall_from_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SANDSTONE), Blocks.CHISELED_SANDSTONE)
            .unlocks("has_sandstone", has(Blocks.SANDSTONE))
            .save(param0, "chiseled_sandstone_from_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE)
            .unlocks("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .save(param0, "cut_red_sandstone_from_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_SLAB, 2)
            .unlocks("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .save(param0, "red_sandstone_slab_from_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE_SLAB, 2)
            .unlocks("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .save(param0, "cut_red_sandstone_slab_from_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.CUT_RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE_SLAB, 2)
            .unlocks("has_cut_red_sandstone", has(Blocks.RED_SANDSTONE))
            .save(param0, "cut_red_sandstone_slab_from_cut_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_STAIRS)
            .unlocks("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .save(param0, "red_sandstone_stairs_from_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_WALL)
            .unlocks("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .save(param0, "red_sandstone_wall_from_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_SANDSTONE), Blocks.CHISELED_RED_SANDSTONE)
            .unlocks("has_red_sandstone", has(Blocks.RED_SANDSTONE))
            .save(param0, "chiseled_red_sandstone_from_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_SLAB, 2)
            .unlocks("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .save(param0, "quartz_slab_from_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_STAIRS)
            .unlocks("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .save(param0, "quartz_stairs_from_quartz_block_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_PILLAR)
            .unlocks("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .save(param0, "quartz_pillar_from_quartz_block_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.CHISELED_QUARTZ_BLOCK)
            .unlocks("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .save(param0, "chiseled_quartz_block_from_quartz_block_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_BRICKS)
            .unlocks("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
            .save(param0, "quartz_bricks_from_quartz_block_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.COBBLESTONE), Blocks.COBBLESTONE_STAIRS)
            .unlocks("has_cobblestone", has(Blocks.COBBLESTONE))
            .save(param0, "cobblestone_stairs_from_cobblestone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.COBBLESTONE), Blocks.COBBLESTONE_SLAB, 2)
            .unlocks("has_cobblestone", has(Blocks.COBBLESTONE))
            .save(param0, "cobblestone_slab_from_cobblestone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.COBBLESTONE), Blocks.COBBLESTONE_WALL)
            .unlocks("has_cobblestone", has(Blocks.COBBLESTONE))
            .save(param0, "cobblestone_wall_from_cobblestone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_SLAB, 2)
            .unlocks("has_stone_bricks", has(Blocks.STONE_BRICKS))
            .save(param0, "stone_brick_slab_from_stone_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_STAIRS)
            .unlocks("has_stone_bricks", has(Blocks.STONE_BRICKS))
            .save(param0, "stone_brick_stairs_from_stone_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_WALL)
            .unlocks("has_stone_bricks", has(Blocks.STONE_BRICKS))
            .save(param0, "stone_brick_wall_from_stone_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.STONE_BRICKS), Blocks.CHISELED_STONE_BRICKS)
            .unlocks("has_stone_bricks", has(Blocks.STONE_BRICKS))
            .save(param0, "chiseled_stone_bricks_from_stone_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BRICKS), Blocks.BRICK_SLAB, 2)
            .unlocks("has_bricks", has(Blocks.BRICKS))
            .save(param0, "brick_slab_from_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BRICKS), Blocks.BRICK_STAIRS)
            .unlocks("has_bricks", has(Blocks.BRICKS))
            .save(param0, "brick_stairs_from_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BRICKS), Blocks.BRICK_WALL)
            .unlocks("has_bricks", has(Blocks.BRICKS))
            .save(param0, "brick_wall_from_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_SLAB, 2)
            .unlocks("has_nether_bricks", has(Blocks.NETHER_BRICKS))
            .save(param0, "nether_brick_slab_from_nether_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_STAIRS)
            .unlocks("has_nether_bricks", has(Blocks.NETHER_BRICKS))
            .save(param0, "nether_brick_stairs_from_nether_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_WALL)
            .unlocks("has_nether_bricks", has(Blocks.NETHER_BRICKS))
            .save(param0, "nether_brick_wall_from_nether_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.NETHER_BRICKS), Blocks.CHISELED_NETHER_BRICKS)
            .unlocks("has_nether_bricks", has(Blocks.NETHER_BRICKS))
            .save(param0, "chiseled_nether_bricks_from_nether_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_SLAB, 2)
            .unlocks("has_nether_bricks", has(Blocks.RED_NETHER_BRICKS))
            .save(param0, "red_nether_brick_slab_from_red_nether_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_STAIRS)
            .unlocks("has_nether_bricks", has(Blocks.RED_NETHER_BRICKS))
            .save(param0, "red_nether_brick_stairs_from_red_nether_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_WALL)
            .unlocks("has_nether_bricks", has(Blocks.RED_NETHER_BRICKS))
            .save(param0, "red_nether_brick_wall_from_red_nether_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PURPUR_BLOCK), Blocks.PURPUR_SLAB, 2)
            .unlocks("has_purpur_block", has(Blocks.PURPUR_BLOCK))
            .save(param0, "purpur_slab_from_purpur_block_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PURPUR_BLOCK), Blocks.PURPUR_STAIRS)
            .unlocks("has_purpur_block", has(Blocks.PURPUR_BLOCK))
            .save(param0, "purpur_stairs_from_purpur_block_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PURPUR_BLOCK), Blocks.PURPUR_PILLAR)
            .unlocks("has_purpur_block", has(Blocks.PURPUR_BLOCK))
            .save(param0, "purpur_pillar_from_purpur_block_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE), Blocks.PRISMARINE_SLAB, 2)
            .unlocks("has_prismarine", has(Blocks.PRISMARINE))
            .save(param0, "prismarine_slab_from_prismarine_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE), Blocks.PRISMARINE_STAIRS)
            .unlocks("has_prismarine", has(Blocks.PRISMARINE))
            .save(param0, "prismarine_stairs_from_prismarine_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE), Blocks.PRISMARINE_WALL)
            .unlocks("has_prismarine", has(Blocks.PRISMARINE))
            .save(param0, "prismarine_wall_from_prismarine_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), Blocks.PRISMARINE_BRICK_SLAB, 2)
            .unlocks("has_prismarine_brick", has(Blocks.PRISMARINE_BRICKS))
            .save(param0, "prismarine_brick_slab_from_prismarine_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.PRISMARINE_BRICKS), Blocks.PRISMARINE_BRICK_STAIRS)
            .unlocks("has_prismarine_brick", has(Blocks.PRISMARINE_BRICKS))
            .save(param0, "prismarine_brick_stairs_from_prismarine_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DARK_PRISMARINE), Blocks.DARK_PRISMARINE_SLAB, 2)
            .unlocks("has_dark_prismarine", has(Blocks.DARK_PRISMARINE))
            .save(param0, "dark_prismarine_slab_from_dark_prismarine_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DARK_PRISMARINE), Blocks.DARK_PRISMARINE_STAIRS)
            .unlocks("has_dark_prismarine", has(Blocks.DARK_PRISMARINE))
            .save(param0, "dark_prismarine_stairs_from_dark_prismarine_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.ANDESITE_SLAB, 2)
            .unlocks("has_andesite", has(Blocks.ANDESITE))
            .save(param0, "andesite_slab_from_andesite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.ANDESITE_STAIRS)
            .unlocks("has_andesite", has(Blocks.ANDESITE))
            .save(param0, "andesite_stairs_from_andesite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.ANDESITE_WALL)
            .unlocks("has_andesite", has(Blocks.ANDESITE))
            .save(param0, "andesite_wall_from_andesite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE)
            .unlocks("has_andesite", has(Blocks.ANDESITE))
            .save(param0, "polished_andesite_from_andesite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE_SLAB, 2)
            .unlocks("has_andesite", has(Blocks.ANDESITE))
            .save(param0, "polished_andesite_slab_from_andesite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE_STAIRS)
            .unlocks("has_andesite", has(Blocks.ANDESITE))
            .save(param0, "polished_andesite_stairs_from_andesite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_ANDESITE), Blocks.POLISHED_ANDESITE_SLAB, 2)
            .unlocks("has_polished_andesite", has(Blocks.POLISHED_ANDESITE))
            .save(param0, "polished_andesite_slab_from_polished_andesite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_ANDESITE), Blocks.POLISHED_ANDESITE_STAIRS)
            .unlocks("has_polished_andesite", has(Blocks.POLISHED_ANDESITE))
            .save(param0, "polished_andesite_stairs_from_polished_andesite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BASALT), Blocks.POLISHED_BASALT)
            .unlocks("has_basalt", has(Blocks.BASALT))
            .save(param0, "polished_basalt_from_basalt_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.GRANITE_SLAB, 2)
            .unlocks("has_granite", has(Blocks.GRANITE))
            .save(param0, "granite_slab_from_granite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.GRANITE_STAIRS)
            .unlocks("has_granite", has(Blocks.GRANITE))
            .save(param0, "granite_stairs_from_granite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.GRANITE_WALL)
            .unlocks("has_granite", has(Blocks.GRANITE))
            .save(param0, "granite_wall_from_granite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.POLISHED_GRANITE)
            .unlocks("has_granite", has(Blocks.GRANITE))
            .save(param0, "polished_granite_from_granite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.POLISHED_GRANITE_SLAB, 2)
            .unlocks("has_granite", has(Blocks.GRANITE))
            .save(param0, "polished_granite_slab_from_granite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.GRANITE), Blocks.POLISHED_GRANITE_STAIRS)
            .unlocks("has_granite", has(Blocks.GRANITE))
            .save(param0, "polished_granite_stairs_from_granite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_GRANITE), Blocks.POLISHED_GRANITE_SLAB, 2)
            .unlocks("has_polished_granite", has(Blocks.POLISHED_GRANITE))
            .save(param0, "polished_granite_slab_from_polished_granite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_GRANITE), Blocks.POLISHED_GRANITE_STAIRS)
            .unlocks("has_polished_granite", has(Blocks.POLISHED_GRANITE))
            .save(param0, "polished_granite_stairs_from_polished_granite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.DIORITE_SLAB, 2)
            .unlocks("has_diorite", has(Blocks.DIORITE))
            .save(param0, "diorite_slab_from_diorite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.DIORITE_STAIRS)
            .unlocks("has_diorite", has(Blocks.DIORITE))
            .save(param0, "diorite_stairs_from_diorite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.DIORITE_WALL)
            .unlocks("has_diorite", has(Blocks.DIORITE))
            .save(param0, "diorite_wall_from_diorite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.POLISHED_DIORITE)
            .unlocks("has_diorite", has(Blocks.DIORITE))
            .save(param0, "polished_diorite_from_diorite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.POLISHED_DIORITE_SLAB, 2)
            .unlocks("has_diorite", has(Blocks.POLISHED_DIORITE))
            .save(param0, "polished_diorite_slab_from_diorite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.DIORITE), Blocks.POLISHED_DIORITE_STAIRS)
            .unlocks("has_diorite", has(Blocks.POLISHED_DIORITE))
            .save(param0, "polished_diorite_stairs_from_diorite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_DIORITE), Blocks.POLISHED_DIORITE_SLAB, 2)
            .unlocks("has_polished_diorite", has(Blocks.POLISHED_DIORITE))
            .save(param0, "polished_diorite_slab_from_polished_diorite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_DIORITE), Blocks.POLISHED_DIORITE_STAIRS)
            .unlocks("has_polished_diorite", has(Blocks.POLISHED_DIORITE))
            .save(param0, "polished_diorite_stairs_from_polished_diorite_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_SLAB, 2)
            .unlocks("has_mossy_stone_bricks", has(Blocks.MOSSY_STONE_BRICKS))
            .save(param0, "mossy_stone_brick_slab_from_mossy_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_STAIRS)
            .unlocks("has_mossy_stone_bricks", has(Blocks.MOSSY_STONE_BRICKS))
            .save(param0, "mossy_stone_brick_stairs_from_mossy_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_WALL)
            .unlocks("has_mossy_stone_bricks", has(Blocks.MOSSY_STONE_BRICKS))
            .save(param0, "mossy_stone_brick_wall_from_mossy_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_SLAB, 2)
            .unlocks("has_mossy_cobblestone", has(Blocks.MOSSY_COBBLESTONE))
            .save(param0, "mossy_cobblestone_slab_from_mossy_cobblestone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_STAIRS)
            .unlocks("has_mossy_cobblestone", has(Blocks.MOSSY_COBBLESTONE))
            .save(param0, "mossy_cobblestone_stairs_from_mossy_cobblestone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_WALL)
            .unlocks("has_mossy_cobblestone", has(Blocks.MOSSY_COBBLESTONE))
            .save(param0, "mossy_cobblestone_wall_from_mossy_cobblestone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_SANDSTONE), Blocks.SMOOTH_SANDSTONE_SLAB, 2)
            .unlocks("has_smooth_sandstone", has(Blocks.SMOOTH_SANDSTONE))
            .save(param0, "smooth_sandstone_slab_from_smooth_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_SANDSTONE), Blocks.SMOOTH_SANDSTONE_STAIRS)
            .unlocks("has_mossy_cobblestone", has(Blocks.SMOOTH_SANDSTONE))
            .save(param0, "smooth_sandstone_stairs_from_smooth_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE_SLAB, 2)
            .unlocks("has_smooth_red_sandstone", has(Blocks.SMOOTH_RED_SANDSTONE))
            .save(param0, "smooth_red_sandstone_slab_from_smooth_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE_STAIRS)
            .unlocks("has_smooth_red_sandstone", has(Blocks.SMOOTH_RED_SANDSTONE))
            .save(param0, "smooth_red_sandstone_stairs_from_smooth_red_sandstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_QUARTZ), Blocks.SMOOTH_QUARTZ_SLAB, 2)
            .unlocks("has_smooth_quartz", has(Blocks.SMOOTH_QUARTZ))
            .save(param0, "smooth_quartz_slab_from_smooth_quartz_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_QUARTZ), Blocks.SMOOTH_QUARTZ_STAIRS)
            .unlocks("has_smooth_quartz", has(Blocks.SMOOTH_QUARTZ))
            .save(param0, "smooth_quartz_stairs_from_smooth_quartz_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_SLAB, 2)
            .unlocks("has_end_stone_brick", has(Blocks.END_STONE_BRICKS))
            .save(param0, "end_stone_brick_slab_from_end_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_STAIRS)
            .unlocks("has_end_stone_brick", has(Blocks.END_STONE_BRICKS))
            .save(param0, "end_stone_brick_stairs_from_end_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_WALL)
            .unlocks("has_end_stone_brick", has(Blocks.END_STONE_BRICKS))
            .save(param0, "end_stone_brick_wall_from_end_stone_brick_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE), Blocks.END_STONE_BRICKS)
            .unlocks("has_end_stone", has(Blocks.END_STONE))
            .save(param0, "end_stone_bricks_from_end_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE), Blocks.END_STONE_BRICK_SLAB, 2)
            .unlocks("has_end_stone", has(Blocks.END_STONE))
            .save(param0, "end_stone_brick_slab_from_end_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE), Blocks.END_STONE_BRICK_STAIRS)
            .unlocks("has_end_stone", has(Blocks.END_STONE))
            .save(param0, "end_stone_brick_stairs_from_end_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.END_STONE), Blocks.END_STONE_BRICK_WALL)
            .unlocks("has_end_stone", has(Blocks.END_STONE))
            .save(param0, "end_stone_brick_wall_from_end_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.SMOOTH_STONE), Blocks.SMOOTH_STONE_SLAB, 2)
            .unlocks("has_smooth_stone", has(Blocks.SMOOTH_STONE))
            .save(param0, "smooth_stone_slab_from_smooth_stone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.BLACKSTONE_SLAB, 2)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "blackstone_slab_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.BLACKSTONE_STAIRS)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "blackstone_stairs_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.BLACKSTONE_WALL)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "blackstone_wall_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "polished_blackstone_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_WALL)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "polished_blackstone_wall_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_SLAB, 2)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "polished_blackstone_slab_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_STAIRS)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "polished_blackstone_stairs_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.CHISELED_POLISHED_BLACKSTONE)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "chiseled_polished_blackstone_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICKS)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "polished_blackstone_bricks_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, 2)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "polished_blackstone_brick_slab_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "polished_blackstone_brick_stairs_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_WALL)
            .unlocks("has_blackstone", has(Blocks.BLACKSTONE))
            .save(param0, "polished_blackstone_brick_wall_from_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_SLAB, 2)
            .unlocks("has_polished_blackstone", has(Blocks.POLISHED_BLACKSTONE))
            .save(param0, "polished_blackstone_slab_from_polished_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_STAIRS)
            .unlocks("has_polished_blackstone", has(Blocks.POLISHED_BLACKSTONE))
            .save(param0, "polished_blackstone_stairs_from_polished_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICKS)
            .unlocks("has_polished_blackstone", has(Blocks.POLISHED_BLACKSTONE))
            .save(param0, "polished_blackstone_bricks_from_polished_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_WALL)
            .unlocks("has_polished_blackstone", has(Blocks.POLISHED_BLACKSTONE))
            .save(param0, "polished_blackstone_wall_from_polished_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, 2)
            .unlocks("has_polished_blackstone", has(Blocks.POLISHED_BLACKSTONE))
            .save(param0, "polished_blackstone_brick_slab_from_polished_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
            .unlocks("has_polished_blackstone", has(Blocks.POLISHED_BLACKSTONE))
            .save(param0, "polished_blackstone_brick_stairs_from_polished_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_WALL)
            .unlocks("has_polished_blackstone", has(Blocks.POLISHED_BLACKSTONE))
            .save(param0, "polished_blackstone_brick_wall_from_polished_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE), Blocks.CHISELED_POLISHED_BLACKSTONE)
            .unlocks("has_polished_blackstone", has(Blocks.POLISHED_BLACKSTONE))
            .save(param0, "chiseled_polished_blackstone_from_polished_blackstone_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE_BRICKS), Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, 2)
            .unlocks("has_polished_blackstone_bricks", has(Blocks.POLISHED_BLACKSTONE_BRICKS))
            .save(param0, "polished_blackstone_brick_slab_from_polished_blackstone_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE_BRICKS), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
            .unlocks("has_polished_blackstone_bricks", has(Blocks.POLISHED_BLACKSTONE_BRICKS))
            .save(param0, "polished_blackstone_brick_stairs_from_polished_blackstone_bricks_stonecutting");
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.POLISHED_BLACKSTONE_BRICKS), Blocks.POLISHED_BLACKSTONE_BRICK_WALL)
            .unlocks("has_polished_blackstone_bricks", has(Blocks.POLISHED_BLACKSTONE_BRICKS))
            .save(param0, "polished_blackstone_brick_wall_from_polished_blackstone_bricks_stonecutting");
        stonecutterResultFromBase(param0, Blocks.CUT_COPPER_SLAB, Blocks.CUT_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.CUT_COPPER_STAIRS, Blocks.CUT_COPPER);
        stonecutterResultFromBase(param0, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER);
        stonecutterResultFromBase(param0, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER);
        stonecutterResultFromBase(param0, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER);
        stonecutterResultFromBase(param0, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER);
        stonecutterResultFromBase(param0, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER);
        stonecutterResultFromBase(param0, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER);
        stonecutterResultFromBase(param0, Blocks.CUT_COPPER, Blocks.COPPER_BLOCK);
        stonecutterResultFromBase(param0, Blocks.CUT_COPPER_STAIRS, Blocks.COPPER_BLOCK);
        stonecutterResultFromBase(param0, Blocks.CUT_COPPER_SLAB, Blocks.COPPER_BLOCK, 2);
        stonecutterResultFromBase(param0, Blocks.EXPOSED_CUT_COPPER, Blocks.EXPOSED_COPPER);
        stonecutterResultFromBase(param0, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.EXPOSED_COPPER);
        stonecutterResultFromBase(param0, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.WEATHERED_CUT_COPPER, Blocks.WEATHERED_COPPER);
        stonecutterResultFromBase(param0, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WEATHERED_COPPER);
        stonecutterResultFromBase(param0, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.OXIDIZED_CUT_COPPER, Blocks.OXIDIZED_COPPER);
        stonecutterResultFromBase(param0, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_COPPER);
        stonecutterResultFromBase(param0, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.WAXED_CUT_COPPER, Blocks.WAXED_COPPER_BLOCK);
        stonecutterResultFromBase(param0, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_COPPER_BLOCK);
        stonecutterResultFromBase(param0, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_COPPER_BLOCK, 2);
        stonecutterResultFromBase(param0, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_COPPER);
        stonecutterResultFromBase(param0, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_COPPER);
        stonecutterResultFromBase(param0, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_COPPER);
        stonecutterResultFromBase(param0, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_COPPER);
        stonecutterResultFromBase(param0, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_COPPER, 2);
        stonecutterResultFromBase(param0, Blocks.COBBLED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
        stonecutterResultFromBase(param0, Blocks.COBBLED_DEEPSLATE_STAIRS, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.COBBLED_DEEPSLATE_WALL, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.CHISELED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.POLISHED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
        stonecutterResultFromBase(param0, Blocks.POLISHED_DEEPSLATE_STAIRS, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICKS, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_WALL, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILES, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_SLAB, Blocks.COBBLED_DEEPSLATE, 2);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_WALL, Blocks.COBBLED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
        stonecutterResultFromBase(param0, Blocks.POLISHED_DEEPSLATE_STAIRS, Blocks.POLISHED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.POLISHED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.POLISHED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_WALL, Blocks.POLISHED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_SLAB, Blocks.POLISHED_DEEPSLATE, 2);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.POLISHED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_WALL, Blocks.POLISHED_DEEPSLATE);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICKS, 2);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.DEEPSLATE_BRICKS);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_BRICK_WALL, Blocks.DEEPSLATE_BRICKS);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_BRICKS);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_BRICKS, 2);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_BRICKS);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_BRICKS);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES, 2);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILES);
        stonecutterResultFromBase(param0, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_TILES);
        netheriteSmithing(param0, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        netheriteSmithing(param0, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        netheriteSmithing(param0, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET);
        netheriteSmithing(param0, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
        netheriteSmithing(param0, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
        netheriteSmithing(param0, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        netheriteSmithing(param0, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        netheriteSmithing(param0, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
        netheriteSmithing(param0, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
    }

    private static void netheriteSmithing(Consumer<FinishedRecipe> param0, Item param1, Item param2) {
        UpgradeRecipeBuilder.smithing(Ingredient.of(param1), Ingredient.of(Items.NETHERITE_INGOT), param2)
            .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
            .save(param0, Registry.ITEM.getKey(param2.asItem()).getPath() + "_smithing");
    }

    private static void planksFromLog(Consumer<FinishedRecipe> param0, ItemLike param1, Tag<Item> param2) {
        ShapelessRecipeBuilder.shapeless(param1, 4).requires(param2).group("planks").unlockedBy("has_log", has(param2)).save(param0);
    }

    private static void planksFromLogs(Consumer<FinishedRecipe> param0, ItemLike param1, Tag<Item> param2) {
        ShapelessRecipeBuilder.shapeless(param1, 4).requires(param2).group("planks").unlockedBy("has_logs", has(param2)).save(param0);
    }

    private static void woodFromLogs(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1, 3).define('#', param2).pattern("##").pattern("##").group("bark").unlockedBy("has_log", has(param2)).save(param0);
    }

    private static void woodenBoat(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1)
            .define('#', param2)
            .pattern("# #")
            .pattern("###")
            .group("boat")
            .unlockedBy("in_water", insideOf(Blocks.WATER))
            .save(param0);
    }

    private static RecipeBuilder buttonBuilder(ItemLike param0, Ingredient param1) {
        return ShapelessRecipeBuilder.shapeless(param0).requires(param1);
    }

    private static RecipeBuilder doorBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0, 3).define('#', param1).pattern("##").pattern("##").pattern("##");
    }

    private static RecipeBuilder fenceBuilder(ItemLike param0, Ingredient param1) {
        int var0 = param0 == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item var1 = param0 == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return ShapedRecipeBuilder.shaped(param0, var0).define('W', param1).define('#', var1).pattern("W#W").pattern("W#W");
    }

    private static RecipeBuilder fenceGateBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0).define('#', Items.STICK).define('W', param1).pattern("#W#").pattern("#W#");
    }

    private static void pressurePlate(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        pressurePlateBuilder(param1, Ingredient.of(param2)).unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    private static RecipeBuilder pressurePlateBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0).define('#', param1).pattern("##");
    }

    private static void slab(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        slabBuilder(param1, Ingredient.of(param2)).unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    private static RecipeBuilder slabBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0, 6).define('#', param1).pattern("###");
    }

    private static RecipeBuilder stairBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0, 4).define('#', param1).pattern("#  ").pattern("## ").pattern("###");
    }

    private static RecipeBuilder trapdoorBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0, 2).define('#', param1).pattern("###").pattern("###");
    }

    private static RecipeBuilder signBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0, 3).group("sign").define('#', param1).define('X', Items.STICK).pattern("###").pattern("###").pattern(" X ");
    }

    private static void coloredWoolFromWhiteWoolAndDye(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapelessRecipeBuilder.shapeless(param1)
            .requires(param2)
            .requires(Blocks.WHITE_WOOL)
            .group("wool")
            .unlockedBy("has_white_wool", has(Blocks.WHITE_WOOL))
            .save(param0);
    }

    private static void carpet(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1, 3).define('#', param2).pattern("##").group("carpet").unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    private static void coloredCarpetFromWhiteCarpetAndDye(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        String var0 = Registry.ITEM.getKey(param1.asItem()).getPath();
        ShapedRecipeBuilder.shaped(param1, 8)
            .define('#', Blocks.WHITE_CARPET)
            .define('$', param2)
            .pattern("###")
            .pattern("#$#")
            .pattern("###")
            .group("carpet")
            .unlockedBy("has_white_carpet", has(Blocks.WHITE_CARPET))
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0, var0 + "_from_white_carpet");
    }

    private static void bedFromPlanksAndWool(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1)
            .define('#', param2)
            .define('X', ItemTags.PLANKS)
            .pattern("###")
            .pattern("XXX")
            .group("bed")
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0);
    }

    private static void bedFromWhiteBedAndDye(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        String var0 = Registry.ITEM.getKey(param1.asItem()).getPath();
        ShapelessRecipeBuilder.shapeless(param1)
            .requires(Items.WHITE_BED)
            .requires(param2)
            .group("dyed_bed")
            .unlockedBy("has_bed", has(Items.WHITE_BED))
            .save(param0, var0 + "_from_white_bed");
    }

    private static void banner(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1)
            .define('#', param2)
            .define('|', Items.STICK)
            .pattern("###")
            .pattern("###")
            .pattern(" | ")
            .group("banner")
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0);
    }

    private static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1, 8)
            .define('#', Blocks.GLASS)
            .define('X', param2)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .group("stained_glass")
            .unlockedBy("has_glass", has(Blocks.GLASS))
            .save(param0);
    }

    private static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1, 16)
            .define('#', param2)
            .pattern("###")
            .pattern("###")
            .group("stained_glass_pane")
            .unlockedBy("has_glass", has(param2))
            .save(param0);
    }

    private static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        String var0 = Registry.ITEM.getKey(param1.asItem()).getPath();
        ShapedRecipeBuilder.shaped(param1, 8)
            .define('#', Blocks.GLASS_PANE)
            .define('$', param2)
            .pattern("###")
            .pattern("#$#")
            .pattern("###")
            .group("stained_glass_pane")
            .unlockedBy("has_glass_pane", has(Blocks.GLASS_PANE))
            .unlockedBy(getHasName(param2), has(param2))
            .save(param0, var0 + "_from_glass_pane");
    }

    private static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1, 8)
            .define('#', Blocks.TERRACOTTA)
            .define('X', param2)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .group("stained_terracotta")
            .unlockedBy("has_terracotta", has(Blocks.TERRACOTTA))
            .save(param0);
    }

    private static void concretePowder(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapelessRecipeBuilder.shapeless(param1, 8)
            .requires(param2)
            .requires(Blocks.SAND, 4)
            .requires(Blocks.GRAVEL, 4)
            .group("concrete_powder")
            .unlockedBy("has_sand", has(Blocks.SAND))
            .unlockedBy("has_gravel", has(Blocks.GRAVEL))
            .save(param0);
    }

    public static void candle(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapelessRecipeBuilder.shapeless(param1).requires(Blocks.CANDLE).requires(param2).unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    public static void wall(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        wallBuilder(param1, Ingredient.of(param2)).unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    public static RecipeBuilder wallBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0, 6).define('#', param1).pattern("###").pattern("###");
    }

    public static void polished(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        polishedBuilder(param1, Ingredient.of(param2)).unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    public static RecipeBuilder polishedBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0, 4).define('S', param1).pattern("SS").pattern("SS");
    }

    public static void cut(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        ShapedRecipeBuilder.shaped(param1, 4).define('#', param2).pattern("##").pattern("##").unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    public static void chiseled(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        chiseledBuilder(param1, Ingredient.of(param2)).unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    public static ShapedRecipeBuilder chiseledBuilder(ItemLike param0, Ingredient param1) {
        return ShapedRecipeBuilder.shaped(param0).define('#', param1).pattern("#").pattern("#");
    }

    private static void stonecutterResultFromBase(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        stonecutterResultFromBase(param0, param1, param2, 1);
    }

    private static void stonecutterResultFromBase(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2, int param3) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(param2), param1, param3)
            .unlocks(getHasName(param2), has(param2))
            .save(param0, getFromName(param1, param2) + "_stonecutting");
    }

    private static void smeltingResultFromBase(Consumer<FinishedRecipe> param0, ItemLike param1, ItemLike param2) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(param2), param1, 0.1F, 200).unlockedBy(getHasName(param2), has(param2)).save(param0);
    }

    private static void cookRecipes(Consumer<FinishedRecipe> param0, String param1, SimpleCookingSerializer<?> param2, int param3) {
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.BEEF), Items.COOKED_BEEF, 0.35F, param3, param2)
            .unlockedBy("has_beef", has(Items.BEEF))
            .save(param0, "cooked_beef_from_" + param1);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.CHICKEN), Items.COOKED_CHICKEN, 0.35F, param3, param2)
            .unlockedBy("has_chicken", has(Items.CHICKEN))
            .save(param0, "cooked_chicken_from_" + param1);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.COD), Items.COOKED_COD, 0.35F, param3, param2)
            .unlockedBy("has_cod", has(Items.COD))
            .save(param0, "cooked_cod_from_" + param1);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Blocks.KELP), Items.DRIED_KELP, 0.1F, param3, param2)
            .unlockedBy("has_kelp", has(Blocks.KELP))
            .save(param0, "dried_kelp_from_" + param1);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.SALMON), Items.COOKED_SALMON, 0.35F, param3, param2)
            .unlockedBy("has_salmon", has(Items.SALMON))
            .save(param0, "cooked_salmon_from_" + param1);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.MUTTON), Items.COOKED_MUTTON, 0.35F, param3, param2)
            .unlockedBy("has_mutton", has(Items.MUTTON))
            .save(param0, "cooked_mutton_from_" + param1);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.PORKCHOP), Items.COOKED_PORKCHOP, 0.35F, param3, param2)
            .unlockedBy("has_porkchop", has(Items.PORKCHOP))
            .save(param0, "cooked_porkchop_from_" + param1);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.POTATO), Items.BAKED_POTATO, 0.35F, param3, param2)
            .unlockedBy("has_potato", has(Items.POTATO))
            .save(param0, "baked_potato_from_" + param1);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(Items.RABBIT), Items.COOKED_RABBIT, 0.35F, param3, param2)
            .unlockedBy("has_rabbit", has(Items.RABBIT))
            .save(param0, "cooked_rabbit_from_" + param1);
    }

    private static void waxRecipes(Consumer<FinishedRecipe> param0) {
        HoneycombItem.WAXABLES
            .get()
            .forEach(
                (param1, param2) -> ShapelessRecipeBuilder.shapeless(param2)
                        .requires(param1)
                        .requires(Items.HONEYCOMB)
                        .unlockedBy(getHasName(param1), has(param1))
                        .save(param0, getFromName(param2, Items.HONEYCOMB))
            );
    }

    private static void generateRecipes(Consumer<FinishedRecipe> param0, BlockFamily param1) {
        param1.getVariants().forEach((param2, param3) -> {
            BiFunction<ItemLike, ItemLike, RecipeBuilder> var0x = shapeBuilders.get(param2);
            ItemLike var1x = getBaseBlock(param1, param2);
            if (var0x != null) {
                RecipeBuilder var2 = (RecipeBuilder)var0x.apply(param3, var1x);
                param1.getRecipeGroupPrefix().ifPresent(param2x -> var2.group(param2x + "_" + param2.getName()));
                var2.unlockedBy(param1.getRecipeUnlockedBy().orElseGet(() -> getHasName(var1x)), has(var1x));
                var2.save(param0);
            }

            if (param2 == BlockFamily.Variant.CRACKED) {
                smeltingResultFromBase(param0, param3, var1x);
            }

        });
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

    private static EnterBlockTrigger.TriggerInstance insideOf(Block param0) {
        return new EnterBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0, StatePropertiesPredicate.ANY);
    }

    private static InventoryChangeTrigger.TriggerInstance has(ItemLike param0) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(param0).build());
    }

    private static InventoryChangeTrigger.TriggerInstance has(Tag<Item> param0) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(param0).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... param0) {
        return new InventoryChangeTrigger.TriggerInstance(
            EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, param0
        );
    }

    private static String getHasName(ItemLike param0) {
        return "has_" + getBlockName(param0);
    }

    private static String getFromName(ItemLike param0, ItemLike param1) {
        return getBlockName(param0) + "_from_" + getBlockName(param1);
    }

    private static String getBlockName(ItemLike param0) {
        return Registry.ITEM.getKey(param0.asItem()).getPath();
    }

    @Override
    public String getName() {
        return "Recipes";
    }
}
