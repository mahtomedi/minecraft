package net.minecraft.world.item;

import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class CreativeModeTabs {
    private static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS = createKey("building_blocks");
    private static final ResourceKey<CreativeModeTab> COLORED_BLOCKS = createKey("colored_blocks");
    private static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS = createKey("natural_blocks");
    private static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = createKey("functional_blocks");
    private static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS = createKey("redstone_blocks");
    private static final ResourceKey<CreativeModeTab> HOTBAR = createKey("hotbar");
    private static final ResourceKey<CreativeModeTab> SEARCH = createKey("search");
    private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = createKey("tools_and_utilities");
    private static final ResourceKey<CreativeModeTab> COMBAT = createKey("combat");
    private static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = createKey("food_and_drinks");
    private static final ResourceKey<CreativeModeTab> INGREDIENTS = createKey("ingredients");
    private static final ResourceKey<CreativeModeTab> SPAWN_EGGS = createKey("spawn_eggs");
    private static final ResourceKey<CreativeModeTab> OP_BLOCKS = createKey("op_blocks");
    private static final ResourceKey<CreativeModeTab> INVENTORY = createKey("inventory");
    private static final Comparator<Holder<PaintingVariant>> PAINTING_COMPARATOR = Comparator.comparing(
        Holder::value, Comparator.<PaintingVariant>comparingInt(param0 -> param0.getHeight() * param0.getWidth()).thenComparing(PaintingVariant::getWidth)
    );
    @Nullable
    private static CreativeModeTab.ItemDisplayParameters CACHED_PARAMETERS;

    private static ResourceKey<CreativeModeTab> createKey(String param0) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(param0));
    }

    public static CreativeModeTab bootstrap(Registry<CreativeModeTab> param0) {
        Registry.register(
            param0,
            BUILDING_BLOCKS,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.buildingBlocks"))
                .icon(() -> new ItemStack(Blocks.BRICKS))
                .displayItems((param0x, param1) -> {
                    param1.accept(Items.OAK_LOG);
                    param1.accept(Items.OAK_WOOD);
                    param1.accept(Items.STRIPPED_OAK_LOG);
                    param1.accept(Items.STRIPPED_OAK_WOOD);
                    param1.accept(Items.OAK_PLANKS);
                    param1.accept(Items.OAK_STAIRS);
                    param1.accept(Items.OAK_SLAB);
                    param1.accept(Items.OAK_FENCE);
                    param1.accept(Items.OAK_FENCE_GATE);
                    param1.accept(Items.OAK_DOOR);
                    param1.accept(Items.OAK_TRAPDOOR);
                    param1.accept(Items.OAK_PRESSURE_PLATE);
                    param1.accept(Items.OAK_BUTTON);
                    param1.accept(Items.SPRUCE_LOG);
                    param1.accept(Items.SPRUCE_WOOD);
                    param1.accept(Items.STRIPPED_SPRUCE_LOG);
                    param1.accept(Items.STRIPPED_SPRUCE_WOOD);
                    param1.accept(Items.SPRUCE_PLANKS);
                    param1.accept(Items.SPRUCE_STAIRS);
                    param1.accept(Items.SPRUCE_SLAB);
                    param1.accept(Items.SPRUCE_FENCE);
                    param1.accept(Items.SPRUCE_FENCE_GATE);
                    param1.accept(Items.SPRUCE_DOOR);
                    param1.accept(Items.SPRUCE_TRAPDOOR);
                    param1.accept(Items.SPRUCE_PRESSURE_PLATE);
                    param1.accept(Items.SPRUCE_BUTTON);
                    param1.accept(Items.BIRCH_LOG);
                    param1.accept(Items.BIRCH_WOOD);
                    param1.accept(Items.STRIPPED_BIRCH_LOG);
                    param1.accept(Items.STRIPPED_BIRCH_WOOD);
                    param1.accept(Items.BIRCH_PLANKS);
                    param1.accept(Items.BIRCH_STAIRS);
                    param1.accept(Items.BIRCH_SLAB);
                    param1.accept(Items.BIRCH_FENCE);
                    param1.accept(Items.BIRCH_FENCE_GATE);
                    param1.accept(Items.BIRCH_DOOR);
                    param1.accept(Items.BIRCH_TRAPDOOR);
                    param1.accept(Items.BIRCH_PRESSURE_PLATE);
                    param1.accept(Items.BIRCH_BUTTON);
                    param1.accept(Items.JUNGLE_LOG);
                    param1.accept(Items.JUNGLE_WOOD);
                    param1.accept(Items.STRIPPED_JUNGLE_LOG);
                    param1.accept(Items.STRIPPED_JUNGLE_WOOD);
                    param1.accept(Items.JUNGLE_PLANKS);
                    param1.accept(Items.JUNGLE_STAIRS);
                    param1.accept(Items.JUNGLE_SLAB);
                    param1.accept(Items.JUNGLE_FENCE);
                    param1.accept(Items.JUNGLE_FENCE_GATE);
                    param1.accept(Items.JUNGLE_DOOR);
                    param1.accept(Items.JUNGLE_TRAPDOOR);
                    param1.accept(Items.JUNGLE_PRESSURE_PLATE);
                    param1.accept(Items.JUNGLE_BUTTON);
                    param1.accept(Items.ACACIA_LOG);
                    param1.accept(Items.ACACIA_WOOD);
                    param1.accept(Items.STRIPPED_ACACIA_LOG);
                    param1.accept(Items.STRIPPED_ACACIA_WOOD);
                    param1.accept(Items.ACACIA_PLANKS);
                    param1.accept(Items.ACACIA_STAIRS);
                    param1.accept(Items.ACACIA_SLAB);
                    param1.accept(Items.ACACIA_FENCE);
                    param1.accept(Items.ACACIA_FENCE_GATE);
                    param1.accept(Items.ACACIA_DOOR);
                    param1.accept(Items.ACACIA_TRAPDOOR);
                    param1.accept(Items.ACACIA_PRESSURE_PLATE);
                    param1.accept(Items.ACACIA_BUTTON);
                    param1.accept(Items.DARK_OAK_LOG);
                    param1.accept(Items.DARK_OAK_WOOD);
                    param1.accept(Items.STRIPPED_DARK_OAK_LOG);
                    param1.accept(Items.STRIPPED_DARK_OAK_WOOD);
                    param1.accept(Items.DARK_OAK_PLANKS);
                    param1.accept(Items.DARK_OAK_STAIRS);
                    param1.accept(Items.DARK_OAK_SLAB);
                    param1.accept(Items.DARK_OAK_FENCE);
                    param1.accept(Items.DARK_OAK_FENCE_GATE);
                    param1.accept(Items.DARK_OAK_DOOR);
                    param1.accept(Items.DARK_OAK_TRAPDOOR);
                    param1.accept(Items.DARK_OAK_PRESSURE_PLATE);
                    param1.accept(Items.DARK_OAK_BUTTON);
                    param1.accept(Items.MANGROVE_LOG);
                    param1.accept(Items.MANGROVE_WOOD);
                    param1.accept(Items.STRIPPED_MANGROVE_LOG);
                    param1.accept(Items.STRIPPED_MANGROVE_WOOD);
                    param1.accept(Items.MANGROVE_PLANKS);
                    param1.accept(Items.MANGROVE_STAIRS);
                    param1.accept(Items.MANGROVE_SLAB);
                    param1.accept(Items.MANGROVE_FENCE);
                    param1.accept(Items.MANGROVE_FENCE_GATE);
                    param1.accept(Items.MANGROVE_DOOR);
                    param1.accept(Items.MANGROVE_TRAPDOOR);
                    param1.accept(Items.MANGROVE_PRESSURE_PLATE);
                    param1.accept(Items.MANGROVE_BUTTON);
                    param1.accept(Items.CHERRY_LOG);
                    param1.accept(Items.CHERRY_WOOD);
                    param1.accept(Items.STRIPPED_CHERRY_LOG);
                    param1.accept(Items.STRIPPED_CHERRY_WOOD);
                    param1.accept(Items.CHERRY_PLANKS);
                    param1.accept(Items.CHERRY_STAIRS);
                    param1.accept(Items.CHERRY_SLAB);
                    param1.accept(Items.CHERRY_FENCE);
                    param1.accept(Items.CHERRY_FENCE_GATE);
                    param1.accept(Items.CHERRY_DOOR);
                    param1.accept(Items.CHERRY_TRAPDOOR);
                    param1.accept(Items.CHERRY_PRESSURE_PLATE);
                    param1.accept(Items.CHERRY_BUTTON);
                    param1.accept(Items.BAMBOO_BLOCK);
                    param1.accept(Items.STRIPPED_BAMBOO_BLOCK);
                    param1.accept(Items.BAMBOO_PLANKS);
                    param1.accept(Items.BAMBOO_MOSAIC);
                    param1.accept(Items.BAMBOO_STAIRS);
                    param1.accept(Items.BAMBOO_MOSAIC_STAIRS);
                    param1.accept(Items.BAMBOO_SLAB);
                    param1.accept(Items.BAMBOO_MOSAIC_SLAB);
                    param1.accept(Items.BAMBOO_FENCE);
                    param1.accept(Items.BAMBOO_FENCE_GATE);
                    param1.accept(Items.BAMBOO_DOOR);
                    param1.accept(Items.BAMBOO_TRAPDOOR);
                    param1.accept(Items.BAMBOO_PRESSURE_PLATE);
                    param1.accept(Items.BAMBOO_BUTTON);
                    param1.accept(Items.CRIMSON_STEM);
                    param1.accept(Items.CRIMSON_HYPHAE);
                    param1.accept(Items.STRIPPED_CRIMSON_STEM);
                    param1.accept(Items.STRIPPED_CRIMSON_HYPHAE);
                    param1.accept(Items.CRIMSON_PLANKS);
                    param1.accept(Items.CRIMSON_STAIRS);
                    param1.accept(Items.CRIMSON_SLAB);
                    param1.accept(Items.CRIMSON_FENCE);
                    param1.accept(Items.CRIMSON_FENCE_GATE);
                    param1.accept(Items.CRIMSON_DOOR);
                    param1.accept(Items.CRIMSON_TRAPDOOR);
                    param1.accept(Items.CRIMSON_PRESSURE_PLATE);
                    param1.accept(Items.CRIMSON_BUTTON);
                    param1.accept(Items.WARPED_STEM);
                    param1.accept(Items.WARPED_HYPHAE);
                    param1.accept(Items.STRIPPED_WARPED_STEM);
                    param1.accept(Items.STRIPPED_WARPED_HYPHAE);
                    param1.accept(Items.WARPED_PLANKS);
                    param1.accept(Items.WARPED_STAIRS);
                    param1.accept(Items.WARPED_SLAB);
                    param1.accept(Items.WARPED_FENCE);
                    param1.accept(Items.WARPED_FENCE_GATE);
                    param1.accept(Items.WARPED_DOOR);
                    param1.accept(Items.WARPED_TRAPDOOR);
                    param1.accept(Items.WARPED_PRESSURE_PLATE);
                    param1.accept(Items.WARPED_BUTTON);
                    param1.accept(Items.STONE);
                    param1.accept(Items.STONE_STAIRS);
                    param1.accept(Items.STONE_SLAB);
                    param1.accept(Items.STONE_PRESSURE_PLATE);
                    param1.accept(Items.STONE_BUTTON);
                    param1.accept(Items.COBBLESTONE);
                    param1.accept(Items.COBBLESTONE_STAIRS);
                    param1.accept(Items.COBBLESTONE_SLAB);
                    param1.accept(Items.COBBLESTONE_WALL);
                    param1.accept(Items.MOSSY_COBBLESTONE);
                    param1.accept(Items.MOSSY_COBBLESTONE_STAIRS);
                    param1.accept(Items.MOSSY_COBBLESTONE_SLAB);
                    param1.accept(Items.MOSSY_COBBLESTONE_WALL);
                    param1.accept(Items.SMOOTH_STONE);
                    param1.accept(Items.SMOOTH_STONE_SLAB);
                    param1.accept(Items.STONE_BRICKS);
                    param1.accept(Items.CRACKED_STONE_BRICKS);
                    param1.accept(Items.STONE_BRICK_STAIRS);
                    param1.accept(Items.STONE_BRICK_SLAB);
                    param1.accept(Items.STONE_BRICK_WALL);
                    param1.accept(Items.CHISELED_STONE_BRICKS);
                    param1.accept(Items.MOSSY_STONE_BRICKS);
                    param1.accept(Items.MOSSY_STONE_BRICK_STAIRS);
                    param1.accept(Items.MOSSY_STONE_BRICK_SLAB);
                    param1.accept(Items.MOSSY_STONE_BRICK_WALL);
                    param1.accept(Items.GRANITE);
                    param1.accept(Items.GRANITE_STAIRS);
                    param1.accept(Items.GRANITE_SLAB);
                    param1.accept(Items.GRANITE_WALL);
                    param1.accept(Items.POLISHED_GRANITE);
                    param1.accept(Items.POLISHED_GRANITE_STAIRS);
                    param1.accept(Items.POLISHED_GRANITE_SLAB);
                    param1.accept(Items.DIORITE);
                    param1.accept(Items.DIORITE_STAIRS);
                    param1.accept(Items.DIORITE_SLAB);
                    param1.accept(Items.DIORITE_WALL);
                    param1.accept(Items.POLISHED_DIORITE);
                    param1.accept(Items.POLISHED_DIORITE_STAIRS);
                    param1.accept(Items.POLISHED_DIORITE_SLAB);
                    param1.accept(Items.ANDESITE);
                    param1.accept(Items.ANDESITE_STAIRS);
                    param1.accept(Items.ANDESITE_SLAB);
                    param1.accept(Items.ANDESITE_WALL);
                    param1.accept(Items.POLISHED_ANDESITE);
                    param1.accept(Items.POLISHED_ANDESITE_STAIRS);
                    param1.accept(Items.POLISHED_ANDESITE_SLAB);
                    param1.accept(Items.DEEPSLATE);
                    param1.accept(Items.COBBLED_DEEPSLATE);
                    param1.accept(Items.COBBLED_DEEPSLATE_STAIRS);
                    param1.accept(Items.COBBLED_DEEPSLATE_SLAB);
                    param1.accept(Items.COBBLED_DEEPSLATE_WALL);
                    param1.accept(Items.CHISELED_DEEPSLATE);
                    param1.accept(Items.POLISHED_DEEPSLATE);
                    param1.accept(Items.POLISHED_DEEPSLATE_STAIRS);
                    param1.accept(Items.POLISHED_DEEPSLATE_SLAB);
                    param1.accept(Items.POLISHED_DEEPSLATE_WALL);
                    param1.accept(Items.DEEPSLATE_BRICKS);
                    param1.accept(Items.CRACKED_DEEPSLATE_BRICKS);
                    param1.accept(Items.DEEPSLATE_BRICK_STAIRS);
                    param1.accept(Items.DEEPSLATE_BRICK_SLAB);
                    param1.accept(Items.DEEPSLATE_BRICK_WALL);
                    param1.accept(Items.DEEPSLATE_TILES);
                    param1.accept(Items.CRACKED_DEEPSLATE_TILES);
                    param1.accept(Items.DEEPSLATE_TILE_STAIRS);
                    param1.accept(Items.DEEPSLATE_TILE_SLAB);
                    param1.accept(Items.DEEPSLATE_TILE_WALL);
                    param1.accept(Items.REINFORCED_DEEPSLATE);
                    param1.accept(Items.BRICKS);
                    param1.accept(Items.BRICK_STAIRS);
                    param1.accept(Items.BRICK_SLAB);
                    param1.accept(Items.BRICK_WALL);
                    param1.accept(Items.PACKED_MUD);
                    param1.accept(Items.MUD_BRICKS);
                    param1.accept(Items.MUD_BRICK_STAIRS);
                    param1.accept(Items.MUD_BRICK_SLAB);
                    param1.accept(Items.MUD_BRICK_WALL);
                    param1.accept(Items.SANDSTONE);
                    param1.accept(Items.SANDSTONE_STAIRS);
                    param1.accept(Items.SANDSTONE_SLAB);
                    param1.accept(Items.SANDSTONE_WALL);
                    param1.accept(Items.CHISELED_SANDSTONE);
                    param1.accept(Items.SMOOTH_SANDSTONE);
                    param1.accept(Items.SMOOTH_SANDSTONE_STAIRS);
                    param1.accept(Items.SMOOTH_SANDSTONE_SLAB);
                    param1.accept(Items.CUT_SANDSTONE);
                    param1.accept(Items.CUT_STANDSTONE_SLAB);
                    param1.accept(Items.RED_SANDSTONE);
                    param1.accept(Items.RED_SANDSTONE_STAIRS);
                    param1.accept(Items.RED_SANDSTONE_SLAB);
                    param1.accept(Items.RED_SANDSTONE_WALL);
                    param1.accept(Items.CHISELED_RED_SANDSTONE);
                    param1.accept(Items.SMOOTH_RED_SANDSTONE);
                    param1.accept(Items.SMOOTH_RED_SANDSTONE_STAIRS);
                    param1.accept(Items.SMOOTH_RED_SANDSTONE_SLAB);
                    param1.accept(Items.CUT_RED_SANDSTONE);
                    param1.accept(Items.CUT_RED_SANDSTONE_SLAB);
                    param1.accept(Items.SEA_LANTERN);
                    param1.accept(Items.PRISMARINE);
                    param1.accept(Items.PRISMARINE_STAIRS);
                    param1.accept(Items.PRISMARINE_SLAB);
                    param1.accept(Items.PRISMARINE_WALL);
                    param1.accept(Items.PRISMARINE_BRICKS);
                    param1.accept(Items.PRISMARINE_BRICK_STAIRS);
                    param1.accept(Items.PRISMARINE_BRICK_SLAB);
                    param1.accept(Items.DARK_PRISMARINE);
                    param1.accept(Items.DARK_PRISMARINE_STAIRS);
                    param1.accept(Items.DARK_PRISMARINE_SLAB);
                    param1.accept(Items.NETHERRACK);
                    param1.accept(Items.NETHER_BRICKS);
                    param1.accept(Items.CRACKED_NETHER_BRICKS);
                    param1.accept(Items.NETHER_BRICK_STAIRS);
                    param1.accept(Items.NETHER_BRICK_SLAB);
                    param1.accept(Items.NETHER_BRICK_WALL);
                    param1.accept(Items.NETHER_BRICK_FENCE);
                    param1.accept(Items.CHISELED_NETHER_BRICKS);
                    param1.accept(Items.RED_NETHER_BRICKS);
                    param1.accept(Items.RED_NETHER_BRICK_STAIRS);
                    param1.accept(Items.RED_NETHER_BRICK_SLAB);
                    param1.accept(Items.RED_NETHER_BRICK_WALL);
                    param1.accept(Items.BASALT);
                    param1.accept(Items.SMOOTH_BASALT);
                    param1.accept(Items.POLISHED_BASALT);
                    param1.accept(Items.BLACKSTONE);
                    param1.accept(Items.GILDED_BLACKSTONE);
                    param1.accept(Items.BLACKSTONE_STAIRS);
                    param1.accept(Items.BLACKSTONE_SLAB);
                    param1.accept(Items.BLACKSTONE_WALL);
                    param1.accept(Items.CHISELED_POLISHED_BLACKSTONE);
                    param1.accept(Items.POLISHED_BLACKSTONE);
                    param1.accept(Items.POLISHED_BLACKSTONE_STAIRS);
                    param1.accept(Items.POLISHED_BLACKSTONE_SLAB);
                    param1.accept(Items.POLISHED_BLACKSTONE_WALL);
                    param1.accept(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE);
                    param1.accept(Items.POLISHED_BLACKSTONE_BUTTON);
                    param1.accept(Items.POLISHED_BLACKSTONE_BRICKS);
                    param1.accept(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS);
                    param1.accept(Items.POLISHED_BLACKSTONE_BRICK_STAIRS);
                    param1.accept(Items.POLISHED_BLACKSTONE_BRICK_SLAB);
                    param1.accept(Items.POLISHED_BLACKSTONE_BRICK_WALL);
                    param1.accept(Items.END_STONE);
                    param1.accept(Items.END_STONE_BRICKS);
                    param1.accept(Items.END_STONE_BRICK_STAIRS);
                    param1.accept(Items.END_STONE_BRICK_SLAB);
                    param1.accept(Items.END_STONE_BRICK_WALL);
                    param1.accept(Items.PURPUR_BLOCK);
                    param1.accept(Items.PURPUR_PILLAR);
                    param1.accept(Items.PURPUR_STAIRS);
                    param1.accept(Items.PURPUR_SLAB);
                    param1.accept(Items.COAL_BLOCK);
                    param1.accept(Items.IRON_BLOCK);
                    param1.accept(Items.IRON_BARS);
                    param1.accept(Items.IRON_DOOR);
                    param1.accept(Items.IRON_TRAPDOOR);
                    param1.accept(Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
                    param1.accept(Items.CHAIN);
                    param1.accept(Items.GOLD_BLOCK);
                    param1.accept(Items.LIGHT_WEIGHTED_PRESSURE_PLATE);
                    param1.accept(Items.REDSTONE_BLOCK);
                    param1.accept(Items.EMERALD_BLOCK);
                    param1.accept(Items.LAPIS_BLOCK);
                    param1.accept(Items.DIAMOND_BLOCK);
                    param1.accept(Items.NETHERITE_BLOCK);
                    param1.accept(Items.QUARTZ_BLOCK);
                    param1.accept(Items.QUARTZ_STAIRS);
                    param1.accept(Items.QUARTZ_SLAB);
                    param1.accept(Items.CHISELED_QUARTZ_BLOCK);
                    param1.accept(Items.QUARTZ_BRICKS);
                    param1.accept(Items.QUARTZ_PILLAR);
                    param1.accept(Items.SMOOTH_QUARTZ);
                    param1.accept(Items.SMOOTH_QUARTZ_STAIRS);
                    param1.accept(Items.SMOOTH_QUARTZ_SLAB);
                    param1.accept(Items.AMETHYST_BLOCK);
                    param1.accept(Items.COPPER_BLOCK);
                    param1.accept(Items.CUT_COPPER);
                    param1.accept(Items.CUT_COPPER_STAIRS);
                    param1.accept(Items.CUT_COPPER_SLAB);
                    param1.accept(Items.EXPOSED_COPPER);
                    param1.accept(Items.EXPOSED_CUT_COPPER);
                    param1.accept(Items.EXPOSED_CUT_COPPER_STAIRS);
                    param1.accept(Items.EXPOSED_CUT_COPPER_SLAB);
                    param1.accept(Items.WEATHERED_COPPER);
                    param1.accept(Items.WEATHERED_CUT_COPPER);
                    param1.accept(Items.WEATHERED_CUT_COPPER_STAIRS);
                    param1.accept(Items.WEATHERED_CUT_COPPER_SLAB);
                    param1.accept(Items.OXIDIZED_COPPER);
                    param1.accept(Items.OXIDIZED_CUT_COPPER);
                    param1.accept(Items.OXIDIZED_CUT_COPPER_STAIRS);
                    param1.accept(Items.OXIDIZED_CUT_COPPER_SLAB);
                    param1.accept(Items.WAXED_COPPER_BLOCK);
                    param1.accept(Items.WAXED_CUT_COPPER);
                    param1.accept(Items.WAXED_CUT_COPPER_STAIRS);
                    param1.accept(Items.WAXED_CUT_COPPER_SLAB);
                    param1.accept(Items.WAXED_EXPOSED_COPPER);
                    param1.accept(Items.WAXED_EXPOSED_CUT_COPPER);
                    param1.accept(Items.WAXED_EXPOSED_CUT_COPPER_STAIRS);
                    param1.accept(Items.WAXED_EXPOSED_CUT_COPPER_SLAB);
                    param1.accept(Items.WAXED_WEATHERED_COPPER);
                    param1.accept(Items.WAXED_WEATHERED_CUT_COPPER);
                    param1.accept(Items.WAXED_WEATHERED_CUT_COPPER_STAIRS);
                    param1.accept(Items.WAXED_WEATHERED_CUT_COPPER_SLAB);
                    param1.accept(Items.WAXED_OXIDIZED_COPPER);
                    param1.accept(Items.WAXED_OXIDIZED_CUT_COPPER);
                    param1.accept(Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
                    param1.accept(Items.WAXED_OXIDIZED_CUT_COPPER_SLAB);
                })
                .build()
        );
        Registry.register(
            param0,
            COLORED_BLOCKS,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                .title(Component.translatable("itemGroup.coloredBlocks"))
                .icon(() -> new ItemStack(Blocks.CYAN_WOOL))
                .displayItems((param0x, param1) -> {
                    param1.accept(Items.WHITE_WOOL);
                    param1.accept(Items.LIGHT_GRAY_WOOL);
                    param1.accept(Items.GRAY_WOOL);
                    param1.accept(Items.BLACK_WOOL);
                    param1.accept(Items.BROWN_WOOL);
                    param1.accept(Items.RED_WOOL);
                    param1.accept(Items.ORANGE_WOOL);
                    param1.accept(Items.YELLOW_WOOL);
                    param1.accept(Items.LIME_WOOL);
                    param1.accept(Items.GREEN_WOOL);
                    param1.accept(Items.CYAN_WOOL);
                    param1.accept(Items.LIGHT_BLUE_WOOL);
                    param1.accept(Items.BLUE_WOOL);
                    param1.accept(Items.PURPLE_WOOL);
                    param1.accept(Items.MAGENTA_WOOL);
                    param1.accept(Items.PINK_WOOL);
                    param1.accept(Items.WHITE_CARPET);
                    param1.accept(Items.LIGHT_GRAY_CARPET);
                    param1.accept(Items.GRAY_CARPET);
                    param1.accept(Items.BLACK_CARPET);
                    param1.accept(Items.BROWN_CARPET);
                    param1.accept(Items.RED_CARPET);
                    param1.accept(Items.ORANGE_CARPET);
                    param1.accept(Items.YELLOW_CARPET);
                    param1.accept(Items.LIME_CARPET);
                    param1.accept(Items.GREEN_CARPET);
                    param1.accept(Items.CYAN_CARPET);
                    param1.accept(Items.LIGHT_BLUE_CARPET);
                    param1.accept(Items.BLUE_CARPET);
                    param1.accept(Items.PURPLE_CARPET);
                    param1.accept(Items.MAGENTA_CARPET);
                    param1.accept(Items.PINK_CARPET);
                    param1.accept(Items.TERRACOTTA);
                    param1.accept(Items.WHITE_TERRACOTTA);
                    param1.accept(Items.LIGHT_GRAY_TERRACOTTA);
                    param1.accept(Items.GRAY_TERRACOTTA);
                    param1.accept(Items.BLACK_TERRACOTTA);
                    param1.accept(Items.BROWN_TERRACOTTA);
                    param1.accept(Items.RED_TERRACOTTA);
                    param1.accept(Items.ORANGE_TERRACOTTA);
                    param1.accept(Items.YELLOW_TERRACOTTA);
                    param1.accept(Items.LIME_TERRACOTTA);
                    param1.accept(Items.GREEN_TERRACOTTA);
                    param1.accept(Items.CYAN_TERRACOTTA);
                    param1.accept(Items.LIGHT_BLUE_TERRACOTTA);
                    param1.accept(Items.BLUE_TERRACOTTA);
                    param1.accept(Items.PURPLE_TERRACOTTA);
                    param1.accept(Items.MAGENTA_TERRACOTTA);
                    param1.accept(Items.PINK_TERRACOTTA);
                    param1.accept(Items.WHITE_CONCRETE);
                    param1.accept(Items.LIGHT_GRAY_CONCRETE);
                    param1.accept(Items.GRAY_CONCRETE);
                    param1.accept(Items.BLACK_CONCRETE);
                    param1.accept(Items.BROWN_CONCRETE);
                    param1.accept(Items.RED_CONCRETE);
                    param1.accept(Items.ORANGE_CONCRETE);
                    param1.accept(Items.YELLOW_CONCRETE);
                    param1.accept(Items.LIME_CONCRETE);
                    param1.accept(Items.GREEN_CONCRETE);
                    param1.accept(Items.CYAN_CONCRETE);
                    param1.accept(Items.LIGHT_BLUE_CONCRETE);
                    param1.accept(Items.BLUE_CONCRETE);
                    param1.accept(Items.PURPLE_CONCRETE);
                    param1.accept(Items.MAGENTA_CONCRETE);
                    param1.accept(Items.PINK_CONCRETE);
                    param1.accept(Items.WHITE_CONCRETE_POWDER);
                    param1.accept(Items.LIGHT_GRAY_CONCRETE_POWDER);
                    param1.accept(Items.GRAY_CONCRETE_POWDER);
                    param1.accept(Items.BLACK_CONCRETE_POWDER);
                    param1.accept(Items.BROWN_CONCRETE_POWDER);
                    param1.accept(Items.RED_CONCRETE_POWDER);
                    param1.accept(Items.ORANGE_CONCRETE_POWDER);
                    param1.accept(Items.YELLOW_CONCRETE_POWDER);
                    param1.accept(Items.LIME_CONCRETE_POWDER);
                    param1.accept(Items.GREEN_CONCRETE_POWDER);
                    param1.accept(Items.CYAN_CONCRETE_POWDER);
                    param1.accept(Items.LIGHT_BLUE_CONCRETE_POWDER);
                    param1.accept(Items.BLUE_CONCRETE_POWDER);
                    param1.accept(Items.PURPLE_CONCRETE_POWDER);
                    param1.accept(Items.MAGENTA_CONCRETE_POWDER);
                    param1.accept(Items.PINK_CONCRETE_POWDER);
                    param1.accept(Items.WHITE_GLAZED_TERRACOTTA);
                    param1.accept(Items.LIGHT_GRAY_GLAZED_TERRACOTTA);
                    param1.accept(Items.GRAY_GLAZED_TERRACOTTA);
                    param1.accept(Items.BLACK_GLAZED_TERRACOTTA);
                    param1.accept(Items.BROWN_GLAZED_TERRACOTTA);
                    param1.accept(Items.RED_GLAZED_TERRACOTTA);
                    param1.accept(Items.ORANGE_GLAZED_TERRACOTTA);
                    param1.accept(Items.YELLOW_GLAZED_TERRACOTTA);
                    param1.accept(Items.LIME_GLAZED_TERRACOTTA);
                    param1.accept(Items.GREEN_GLAZED_TERRACOTTA);
                    param1.accept(Items.CYAN_GLAZED_TERRACOTTA);
                    param1.accept(Items.LIGHT_BLUE_GLAZED_TERRACOTTA);
                    param1.accept(Items.BLUE_GLAZED_TERRACOTTA);
                    param1.accept(Items.PURPLE_GLAZED_TERRACOTTA);
                    param1.accept(Items.MAGENTA_GLAZED_TERRACOTTA);
                    param1.accept(Items.PINK_GLAZED_TERRACOTTA);
                    param1.accept(Items.GLASS);
                    param1.accept(Items.TINTED_GLASS);
                    param1.accept(Items.WHITE_STAINED_GLASS);
                    param1.accept(Items.LIGHT_GRAY_STAINED_GLASS);
                    param1.accept(Items.GRAY_STAINED_GLASS);
                    param1.accept(Items.BLACK_STAINED_GLASS);
                    param1.accept(Items.BROWN_STAINED_GLASS);
                    param1.accept(Items.RED_STAINED_GLASS);
                    param1.accept(Items.ORANGE_STAINED_GLASS);
                    param1.accept(Items.YELLOW_STAINED_GLASS);
                    param1.accept(Items.LIME_STAINED_GLASS);
                    param1.accept(Items.GREEN_STAINED_GLASS);
                    param1.accept(Items.CYAN_STAINED_GLASS);
                    param1.accept(Items.LIGHT_BLUE_STAINED_GLASS);
                    param1.accept(Items.BLUE_STAINED_GLASS);
                    param1.accept(Items.PURPLE_STAINED_GLASS);
                    param1.accept(Items.MAGENTA_STAINED_GLASS);
                    param1.accept(Items.PINK_STAINED_GLASS);
                    param1.accept(Items.GLASS_PANE);
                    param1.accept(Items.WHITE_STAINED_GLASS_PANE);
                    param1.accept(Items.LIGHT_GRAY_STAINED_GLASS_PANE);
                    param1.accept(Items.GRAY_STAINED_GLASS_PANE);
                    param1.accept(Items.BLACK_STAINED_GLASS_PANE);
                    param1.accept(Items.BROWN_STAINED_GLASS_PANE);
                    param1.accept(Items.RED_STAINED_GLASS_PANE);
                    param1.accept(Items.ORANGE_STAINED_GLASS_PANE);
                    param1.accept(Items.YELLOW_STAINED_GLASS_PANE);
                    param1.accept(Items.LIME_STAINED_GLASS_PANE);
                    param1.accept(Items.GREEN_STAINED_GLASS_PANE);
                    param1.accept(Items.CYAN_STAINED_GLASS_PANE);
                    param1.accept(Items.LIGHT_BLUE_STAINED_GLASS_PANE);
                    param1.accept(Items.BLUE_STAINED_GLASS_PANE);
                    param1.accept(Items.PURPLE_STAINED_GLASS_PANE);
                    param1.accept(Items.MAGENTA_STAINED_GLASS_PANE);
                    param1.accept(Items.PINK_STAINED_GLASS_PANE);
                    param1.accept(Items.SHULKER_BOX);
                    param1.accept(Items.WHITE_SHULKER_BOX);
                    param1.accept(Items.LIGHT_GRAY_SHULKER_BOX);
                    param1.accept(Items.GRAY_SHULKER_BOX);
                    param1.accept(Items.BLACK_SHULKER_BOX);
                    param1.accept(Items.BROWN_SHULKER_BOX);
                    param1.accept(Items.RED_SHULKER_BOX);
                    param1.accept(Items.ORANGE_SHULKER_BOX);
                    param1.accept(Items.YELLOW_SHULKER_BOX);
                    param1.accept(Items.LIME_SHULKER_BOX);
                    param1.accept(Items.GREEN_SHULKER_BOX);
                    param1.accept(Items.CYAN_SHULKER_BOX);
                    param1.accept(Items.LIGHT_BLUE_SHULKER_BOX);
                    param1.accept(Items.BLUE_SHULKER_BOX);
                    param1.accept(Items.PURPLE_SHULKER_BOX);
                    param1.accept(Items.MAGENTA_SHULKER_BOX);
                    param1.accept(Items.PINK_SHULKER_BOX);
                    param1.accept(Items.WHITE_BED);
                    param1.accept(Items.LIGHT_GRAY_BED);
                    param1.accept(Items.GRAY_BED);
                    param1.accept(Items.BLACK_BED);
                    param1.accept(Items.BROWN_BED);
                    param1.accept(Items.RED_BED);
                    param1.accept(Items.ORANGE_BED);
                    param1.accept(Items.YELLOW_BED);
                    param1.accept(Items.LIME_BED);
                    param1.accept(Items.GREEN_BED);
                    param1.accept(Items.CYAN_BED);
                    param1.accept(Items.LIGHT_BLUE_BED);
                    param1.accept(Items.BLUE_BED);
                    param1.accept(Items.PURPLE_BED);
                    param1.accept(Items.MAGENTA_BED);
                    param1.accept(Items.PINK_BED);
                    param1.accept(Items.CANDLE);
                    param1.accept(Items.WHITE_CANDLE);
                    param1.accept(Items.LIGHT_GRAY_CANDLE);
                    param1.accept(Items.GRAY_CANDLE);
                    param1.accept(Items.BLACK_CANDLE);
                    param1.accept(Items.BROWN_CANDLE);
                    param1.accept(Items.RED_CANDLE);
                    param1.accept(Items.ORANGE_CANDLE);
                    param1.accept(Items.YELLOW_CANDLE);
                    param1.accept(Items.LIME_CANDLE);
                    param1.accept(Items.GREEN_CANDLE);
                    param1.accept(Items.CYAN_CANDLE);
                    param1.accept(Items.LIGHT_BLUE_CANDLE);
                    param1.accept(Items.BLUE_CANDLE);
                    param1.accept(Items.PURPLE_CANDLE);
                    param1.accept(Items.MAGENTA_CANDLE);
                    param1.accept(Items.PINK_CANDLE);
                    param1.accept(Items.WHITE_BANNER);
                    param1.accept(Items.LIGHT_GRAY_BANNER);
                    param1.accept(Items.GRAY_BANNER);
                    param1.accept(Items.BLACK_BANNER);
                    param1.accept(Items.BROWN_BANNER);
                    param1.accept(Items.RED_BANNER);
                    param1.accept(Items.ORANGE_BANNER);
                    param1.accept(Items.YELLOW_BANNER);
                    param1.accept(Items.LIME_BANNER);
                    param1.accept(Items.GREEN_BANNER);
                    param1.accept(Items.CYAN_BANNER);
                    param1.accept(Items.LIGHT_BLUE_BANNER);
                    param1.accept(Items.BLUE_BANNER);
                    param1.accept(Items.PURPLE_BANNER);
                    param1.accept(Items.MAGENTA_BANNER);
                    param1.accept(Items.PINK_BANNER);
                })
                .build()
        );
        Registry.register(
            param0,
            NATURAL_BLOCKS,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
                .title(Component.translatable("itemGroup.natural"))
                .icon(() -> new ItemStack(Blocks.GRASS_BLOCK))
                .displayItems((param0x, param1) -> {
                    param1.accept(Items.GRASS_BLOCK);
                    param1.accept(Items.PODZOL);
                    param1.accept(Items.MYCELIUM);
                    param1.accept(Items.DIRT_PATH);
                    param1.accept(Items.DIRT);
                    param1.accept(Items.COARSE_DIRT);
                    param1.accept(Items.ROOTED_DIRT);
                    param1.accept(Items.FARMLAND);
                    param1.accept(Items.MUD);
                    param1.accept(Items.CLAY);
                    param1.accept(Items.GRAVEL);
                    param1.accept(Items.SAND);
                    param1.accept(Items.SANDSTONE);
                    param1.accept(Items.RED_SAND);
                    param1.accept(Items.RED_SANDSTONE);
                    param1.accept(Items.ICE);
                    param1.accept(Items.PACKED_ICE);
                    param1.accept(Items.BLUE_ICE);
                    param1.accept(Items.SNOW_BLOCK);
                    param1.accept(Items.SNOW);
                    param1.accept(Items.MOSS_BLOCK);
                    param1.accept(Items.MOSS_CARPET);
                    param1.accept(Items.STONE);
                    param1.accept(Items.DEEPSLATE);
                    param1.accept(Items.GRANITE);
                    param1.accept(Items.DIORITE);
                    param1.accept(Items.ANDESITE);
                    param1.accept(Items.CALCITE);
                    param1.accept(Items.TUFF);
                    param1.accept(Items.DRIPSTONE_BLOCK);
                    param1.accept(Items.POINTED_DRIPSTONE);
                    param1.accept(Items.PRISMARINE);
                    param1.accept(Items.MAGMA_BLOCK);
                    param1.accept(Items.OBSIDIAN);
                    param1.accept(Items.CRYING_OBSIDIAN);
                    param1.accept(Items.NETHERRACK);
                    param1.accept(Items.CRIMSON_NYLIUM);
                    param1.accept(Items.WARPED_NYLIUM);
                    param1.accept(Items.SOUL_SAND);
                    param1.accept(Items.SOUL_SOIL);
                    param1.accept(Items.BONE_BLOCK);
                    param1.accept(Items.BLACKSTONE);
                    param1.accept(Items.BASALT);
                    param1.accept(Items.SMOOTH_BASALT);
                    param1.accept(Items.END_STONE);
                    param1.accept(Items.COAL_ORE);
                    param1.accept(Items.DEEPSLATE_COAL_ORE);
                    param1.accept(Items.IRON_ORE);
                    param1.accept(Items.DEEPSLATE_IRON_ORE);
                    param1.accept(Items.COPPER_ORE);
                    param1.accept(Items.DEEPSLATE_COPPER_ORE);
                    param1.accept(Items.GOLD_ORE);
                    param1.accept(Items.DEEPSLATE_GOLD_ORE);
                    param1.accept(Items.REDSTONE_ORE);
                    param1.accept(Items.DEEPSLATE_REDSTONE_ORE);
                    param1.accept(Items.EMERALD_ORE);
                    param1.accept(Items.DEEPSLATE_EMERALD_ORE);
                    param1.accept(Items.LAPIS_ORE);
                    param1.accept(Items.DEEPSLATE_LAPIS_ORE);
                    param1.accept(Items.DIAMOND_ORE);
                    param1.accept(Items.DEEPSLATE_DIAMOND_ORE);
                    param1.accept(Items.NETHER_GOLD_ORE);
                    param1.accept(Items.NETHER_QUARTZ_ORE);
                    param1.accept(Items.ANCIENT_DEBRIS);
                    param1.accept(Items.RAW_IRON_BLOCK);
                    param1.accept(Items.RAW_COPPER_BLOCK);
                    param1.accept(Items.RAW_GOLD_BLOCK);
                    param1.accept(Items.GLOWSTONE);
                    param1.accept(Items.AMETHYST_BLOCK);
                    param1.accept(Items.BUDDING_AMETHYST);
                    param1.accept(Items.SMALL_AMETHYST_BUD);
                    param1.accept(Items.MEDIUM_AMETHYST_BUD);
                    param1.accept(Items.LARGE_AMETHYST_BUD);
                    param1.accept(Items.AMETHYST_CLUSTER);
                    param1.accept(Items.OAK_LOG);
                    param1.accept(Items.SPRUCE_LOG);
                    param1.accept(Items.BIRCH_LOG);
                    param1.accept(Items.JUNGLE_LOG);
                    param1.accept(Items.ACACIA_LOG);
                    param1.accept(Items.DARK_OAK_LOG);
                    param1.accept(Items.MANGROVE_LOG);
                    param1.accept(Items.CHERRY_LOG);
                    param1.accept(Items.MUSHROOM_STEM);
                    param1.accept(Items.CRIMSON_STEM);
                    param1.accept(Items.WARPED_STEM);
                    param1.accept(Items.OAK_LEAVES);
                    param1.accept(Items.SPRUCE_LEAVES);
                    param1.accept(Items.BIRCH_LEAVES);
                    param1.accept(Items.JUNGLE_LEAVES);
                    param1.accept(Items.ACACIA_LEAVES);
                    param1.accept(Items.DARK_OAK_LEAVES);
                    param1.accept(Items.MANGROVE_LEAVES);
                    param1.accept(Items.MANGROVE_ROOTS);
                    param1.accept(Items.MUDDY_MANGROVE_ROOTS);
                    param1.accept(Items.CHERRY_LEAVES);
                    param1.accept(Items.AZALEA_LEAVES);
                    param1.accept(Items.FLOWERING_AZALEA_LEAVES);
                    param1.accept(Items.BROWN_MUSHROOM_BLOCK);
                    param1.accept(Items.RED_MUSHROOM_BLOCK);
                    param1.accept(Items.NETHER_WART_BLOCK);
                    param1.accept(Items.WARPED_WART_BLOCK);
                    param1.accept(Items.SHROOMLIGHT);
                    param1.accept(Items.OAK_SAPLING);
                    param1.accept(Items.SPRUCE_SAPLING);
                    param1.accept(Items.BIRCH_SAPLING);
                    param1.accept(Items.JUNGLE_SAPLING);
                    param1.accept(Items.ACACIA_SAPLING);
                    param1.accept(Items.DARK_OAK_SAPLING);
                    param1.accept(Items.MANGROVE_PROPAGULE);
                    param1.accept(Items.CHERRY_SAPLING);
                    param1.accept(Items.AZALEA);
                    param1.accept(Items.FLOWERING_AZALEA);
                    param1.accept(Items.BROWN_MUSHROOM);
                    param1.accept(Items.RED_MUSHROOM);
                    param1.accept(Items.CRIMSON_FUNGUS);
                    param1.accept(Items.WARPED_FUNGUS);
                    param1.accept(Items.GRASS);
                    param1.accept(Items.FERN);
                    param1.accept(Items.DEAD_BUSH);
                    param1.accept(Items.DANDELION);
                    param1.accept(Items.POPPY);
                    param1.accept(Items.BLUE_ORCHID);
                    param1.accept(Items.ALLIUM);
                    param1.accept(Items.AZURE_BLUET);
                    param1.accept(Items.RED_TULIP);
                    param1.accept(Items.ORANGE_TULIP);
                    param1.accept(Items.WHITE_TULIP);
                    param1.accept(Items.PINK_TULIP);
                    param1.accept(Items.OXEYE_DAISY);
                    param1.accept(Items.CORNFLOWER);
                    param1.accept(Items.LILY_OF_THE_VALLEY);
                    param1.accept(Items.TORCHFLOWER);
                    param1.accept(Items.PINK_PETALS);
                    param1.accept(Items.SPORE_BLOSSOM);
                    param1.accept(Items.BAMBOO);
                    param1.accept(Items.SUGAR_CANE);
                    param1.accept(Items.CACTUS);
                    param1.accept(Items.WITHER_ROSE);
                    param1.accept(Items.CRIMSON_ROOTS);
                    param1.accept(Items.WARPED_ROOTS);
                    param1.accept(Items.NETHER_SPROUTS);
                    param1.accept(Items.WEEPING_VINES);
                    param1.accept(Items.TWISTING_VINES);
                    param1.accept(Items.VINE);
                    param1.accept(Items.TALL_GRASS);
                    param1.accept(Items.LARGE_FERN);
                    param1.accept(Items.SUNFLOWER);
                    param1.accept(Items.LILAC);
                    param1.accept(Items.ROSE_BUSH);
                    param1.accept(Items.PEONY);
                    param1.accept(Items.PITCHER_PLANT);
                    param1.accept(Items.BIG_DRIPLEAF);
                    param1.accept(Items.SMALL_DRIPLEAF);
                    param1.accept(Items.CHORUS_PLANT);
                    param1.accept(Items.CHORUS_FLOWER);
                    param1.accept(Items.GLOW_LICHEN);
                    param1.accept(Items.HANGING_ROOTS);
                    param1.accept(Items.FROGSPAWN);
                    param1.accept(Items.TURTLE_EGG);
                    param1.accept(Items.SNIFFER_EGG);
                    param1.accept(Items.WHEAT_SEEDS);
                    param1.accept(Items.COCOA_BEANS);
                    param1.accept(Items.PUMPKIN_SEEDS);
                    param1.accept(Items.MELON_SEEDS);
                    param1.accept(Items.BEETROOT_SEEDS);
                    param1.accept(Items.TORCHFLOWER_SEEDS);
                    param1.accept(Items.PITCHER_POD);
                    param1.accept(Items.GLOW_BERRIES);
                    param1.accept(Items.SWEET_BERRIES);
                    param1.accept(Items.NETHER_WART);
                    param1.accept(Items.LILY_PAD);
                    param1.accept(Items.SEAGRASS);
                    param1.accept(Items.SEA_PICKLE);
                    param1.accept(Items.KELP);
                    param1.accept(Items.DRIED_KELP_BLOCK);
                    param1.accept(Items.TUBE_CORAL_BLOCK);
                    param1.accept(Items.BRAIN_CORAL_BLOCK);
                    param1.accept(Items.BUBBLE_CORAL_BLOCK);
                    param1.accept(Items.FIRE_CORAL_BLOCK);
                    param1.accept(Items.HORN_CORAL_BLOCK);
                    param1.accept(Items.DEAD_TUBE_CORAL_BLOCK);
                    param1.accept(Items.DEAD_BRAIN_CORAL_BLOCK);
                    param1.accept(Items.DEAD_BUBBLE_CORAL_BLOCK);
                    param1.accept(Items.DEAD_FIRE_CORAL_BLOCK);
                    param1.accept(Items.DEAD_HORN_CORAL_BLOCK);
                    param1.accept(Items.TUBE_CORAL);
                    param1.accept(Items.BRAIN_CORAL);
                    param1.accept(Items.BUBBLE_CORAL);
                    param1.accept(Items.FIRE_CORAL);
                    param1.accept(Items.HORN_CORAL);
                    param1.accept(Items.DEAD_BRAIN_CORAL);
                    param1.accept(Items.DEAD_BUBBLE_CORAL);
                    param1.accept(Items.DEAD_FIRE_CORAL);
                    param1.accept(Items.DEAD_HORN_CORAL);
                    param1.accept(Items.DEAD_TUBE_CORAL);
                    param1.accept(Items.TUBE_CORAL_FAN);
                    param1.accept(Items.BRAIN_CORAL_FAN);
                    param1.accept(Items.BUBBLE_CORAL_FAN);
                    param1.accept(Items.FIRE_CORAL_FAN);
                    param1.accept(Items.HORN_CORAL_FAN);
                    param1.accept(Items.DEAD_TUBE_CORAL_FAN);
                    param1.accept(Items.DEAD_BRAIN_CORAL_FAN);
                    param1.accept(Items.DEAD_BUBBLE_CORAL_FAN);
                    param1.accept(Items.DEAD_FIRE_CORAL_FAN);
                    param1.accept(Items.DEAD_HORN_CORAL_FAN);
                    param1.accept(Items.SPONGE);
                    param1.accept(Items.WET_SPONGE);
                    param1.accept(Items.MELON);
                    param1.accept(Items.PUMPKIN);
                    param1.accept(Items.CARVED_PUMPKIN);
                    param1.accept(Items.JACK_O_LANTERN);
                    param1.accept(Items.HAY_BLOCK);
                    param1.accept(Items.BEE_NEST);
                    param1.accept(Items.HONEYCOMB_BLOCK);
                    param1.accept(Items.SLIME_BLOCK);
                    param1.accept(Items.HONEY_BLOCK);
                    param1.accept(Items.OCHRE_FROGLIGHT);
                    param1.accept(Items.VERDANT_FROGLIGHT);
                    param1.accept(Items.PEARLESCENT_FROGLIGHT);
                    param1.accept(Items.SCULK);
                    param1.accept(Items.SCULK_VEIN);
                    param1.accept(Items.SCULK_CATALYST);
                    param1.accept(Items.SCULK_SHRIEKER);
                    param1.accept(Items.SCULK_SENSOR);
                    param1.accept(Items.COBWEB);
                    param1.accept(Items.BEDROCK);
                })
                .build()
        );
        Registry.register(
            param0,
            FUNCTIONAL_BLOCKS,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3)
                .title(Component.translatable("itemGroup.functional"))
                .icon(() -> new ItemStack(Items.OAK_SIGN))
                .displayItems(
                    (param0x, param1) -> {
                        param1.accept(Items.TORCH);
                        param1.accept(Items.SOUL_TORCH);
                        param1.accept(Items.REDSTONE_TORCH);
                        param1.accept(Items.LANTERN);
                        param1.accept(Items.SOUL_LANTERN);
                        param1.accept(Items.CHAIN);
                        param1.accept(Items.END_ROD);
                        param1.accept(Items.SEA_LANTERN);
                        param1.accept(Items.REDSTONE_LAMP);
                        param1.accept(Items.GLOWSTONE);
                        param1.accept(Items.SHROOMLIGHT);
                        param1.accept(Items.OCHRE_FROGLIGHT);
                        param1.accept(Items.VERDANT_FROGLIGHT);
                        param1.accept(Items.PEARLESCENT_FROGLIGHT);
                        param1.accept(Items.CRYING_OBSIDIAN);
                        param1.accept(Items.GLOW_LICHEN);
                        param1.accept(Items.MAGMA_BLOCK);
                        param1.accept(Items.CRAFTING_TABLE);
                        param1.accept(Items.STONECUTTER);
                        param1.accept(Items.CARTOGRAPHY_TABLE);
                        param1.accept(Items.FLETCHING_TABLE);
                        param1.accept(Items.SMITHING_TABLE);
                        param1.accept(Items.GRINDSTONE);
                        param1.accept(Items.LOOM);
                        param1.accept(Items.FURNACE);
                        param1.accept(Items.SMOKER);
                        param1.accept(Items.BLAST_FURNACE);
                        param1.accept(Items.CAMPFIRE);
                        param1.accept(Items.SOUL_CAMPFIRE);
                        param1.accept(Items.ANVIL);
                        param1.accept(Items.CHIPPED_ANVIL);
                        param1.accept(Items.DAMAGED_ANVIL);
                        param1.accept(Items.COMPOSTER);
                        param1.accept(Items.NOTE_BLOCK);
                        param1.accept(Items.JUKEBOX);
                        param1.accept(Items.ENCHANTING_TABLE);
                        param1.accept(Items.END_CRYSTAL);
                        param1.accept(Items.BREWING_STAND);
                        param1.accept(Items.CAULDRON);
                        param1.accept(Items.BELL);
                        param1.accept(Items.BEACON);
                        param1.accept(Items.CONDUIT);
                        param1.accept(Items.LODESTONE);
                        param1.accept(Items.LADDER);
                        param1.accept(Items.SCAFFOLDING);
                        param1.accept(Items.BEE_NEST);
                        param1.accept(Items.BEEHIVE);
                        param1.accept(Items.SUSPICIOUS_SAND);
                        param1.accept(Items.SUSPICIOUS_GRAVEL);
                        param1.accept(Items.LIGHTNING_ROD);
                        param1.accept(Items.FLOWER_POT);
                        param1.accept(Items.DECORATED_POT);
                        param1.accept(Items.ARMOR_STAND);
                        param1.accept(Items.ITEM_FRAME);
                        param1.accept(Items.GLOW_ITEM_FRAME);
                        param1.accept(Items.PAINTING);
                        param0x.holders()
                            .lookup(Registries.PAINTING_VARIANT)
                            .ifPresent(
                                param1x -> generatePresetPaintings(
                                        param1,
                                        param1x,
                                        param0xxx -> param0xxx.is(PaintingVariantTags.PLACEABLE),
                                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                                    )
                            );
                        param1.accept(Items.BOOKSHELF);
                        param1.accept(Items.CHISELED_BOOKSHELF);
                        param1.accept(Items.LECTERN);
                        param1.accept(Items.TINTED_GLASS);
                        param1.accept(Items.OAK_SIGN);
                        param1.accept(Items.OAK_HANGING_SIGN);
                        param1.accept(Items.SPRUCE_SIGN);
                        param1.accept(Items.SPRUCE_HANGING_SIGN);
                        param1.accept(Items.BIRCH_SIGN);
                        param1.accept(Items.BIRCH_HANGING_SIGN);
                        param1.accept(Items.JUNGLE_SIGN);
                        param1.accept(Items.JUNGLE_HANGING_SIGN);
                        param1.accept(Items.ACACIA_SIGN);
                        param1.accept(Items.ACACIA_HANGING_SIGN);
                        param1.accept(Items.DARK_OAK_SIGN);
                        param1.accept(Items.DARK_OAK_HANGING_SIGN);
                        param1.accept(Items.MANGROVE_SIGN);
                        param1.accept(Items.MANGROVE_HANGING_SIGN);
                        param1.accept(Items.CHERRY_SIGN);
                        param1.accept(Items.CHERRY_HANGING_SIGN);
                        param1.accept(Items.BAMBOO_SIGN);
                        param1.accept(Items.BAMBOO_HANGING_SIGN);
                        param1.accept(Items.CRIMSON_SIGN);
                        param1.accept(Items.CRIMSON_HANGING_SIGN);
                        param1.accept(Items.WARPED_SIGN);
                        param1.accept(Items.WARPED_HANGING_SIGN);
                        param1.accept(Items.CHEST);
                        param1.accept(Items.BARREL);
                        param1.accept(Items.ENDER_CHEST);
                        param1.accept(Items.SHULKER_BOX);
                        param1.accept(Items.WHITE_SHULKER_BOX);
                        param1.accept(Items.LIGHT_GRAY_SHULKER_BOX);
                        param1.accept(Items.GRAY_SHULKER_BOX);
                        param1.accept(Items.BLACK_SHULKER_BOX);
                        param1.accept(Items.BROWN_SHULKER_BOX);
                        param1.accept(Items.RED_SHULKER_BOX);
                        param1.accept(Items.ORANGE_SHULKER_BOX);
                        param1.accept(Items.YELLOW_SHULKER_BOX);
                        param1.accept(Items.LIME_SHULKER_BOX);
                        param1.accept(Items.GREEN_SHULKER_BOX);
                        param1.accept(Items.CYAN_SHULKER_BOX);
                        param1.accept(Items.LIGHT_BLUE_SHULKER_BOX);
                        param1.accept(Items.BLUE_SHULKER_BOX);
                        param1.accept(Items.PURPLE_SHULKER_BOX);
                        param1.accept(Items.MAGENTA_SHULKER_BOX);
                        param1.accept(Items.PINK_SHULKER_BOX);
                        param1.accept(Items.RESPAWN_ANCHOR);
                        param1.accept(Items.WHITE_BED);
                        param1.accept(Items.LIGHT_GRAY_BED);
                        param1.accept(Items.GRAY_BED);
                        param1.accept(Items.BLACK_BED);
                        param1.accept(Items.BROWN_BED);
                        param1.accept(Items.RED_BED);
                        param1.accept(Items.ORANGE_BED);
                        param1.accept(Items.YELLOW_BED);
                        param1.accept(Items.LIME_BED);
                        param1.accept(Items.GREEN_BED);
                        param1.accept(Items.CYAN_BED);
                        param1.accept(Items.LIGHT_BLUE_BED);
                        param1.accept(Items.BLUE_BED);
                        param1.accept(Items.PURPLE_BED);
                        param1.accept(Items.MAGENTA_BED);
                        param1.accept(Items.PINK_BED);
                        param1.accept(Items.CANDLE);
                        param1.accept(Items.WHITE_CANDLE);
                        param1.accept(Items.LIGHT_GRAY_CANDLE);
                        param1.accept(Items.GRAY_CANDLE);
                        param1.accept(Items.BLACK_CANDLE);
                        param1.accept(Items.BROWN_CANDLE);
                        param1.accept(Items.RED_CANDLE);
                        param1.accept(Items.ORANGE_CANDLE);
                        param1.accept(Items.YELLOW_CANDLE);
                        param1.accept(Items.LIME_CANDLE);
                        param1.accept(Items.GREEN_CANDLE);
                        param1.accept(Items.CYAN_CANDLE);
                        param1.accept(Items.LIGHT_BLUE_CANDLE);
                        param1.accept(Items.BLUE_CANDLE);
                        param1.accept(Items.PURPLE_CANDLE);
                        param1.accept(Items.MAGENTA_CANDLE);
                        param1.accept(Items.PINK_CANDLE);
                        param1.accept(Items.WHITE_BANNER);
                        param1.accept(Items.LIGHT_GRAY_BANNER);
                        param1.accept(Items.GRAY_BANNER);
                        param1.accept(Items.BLACK_BANNER);
                        param1.accept(Items.BROWN_BANNER);
                        param1.accept(Items.RED_BANNER);
                        param1.accept(Items.ORANGE_BANNER);
                        param1.accept(Items.YELLOW_BANNER);
                        param1.accept(Items.LIME_BANNER);
                        param1.accept(Items.GREEN_BANNER);
                        param1.accept(Items.CYAN_BANNER);
                        param1.accept(Items.LIGHT_BLUE_BANNER);
                        param1.accept(Items.BLUE_BANNER);
                        param1.accept(Items.PURPLE_BANNER);
                        param1.accept(Items.MAGENTA_BANNER);
                        param1.accept(Items.PINK_BANNER);
                        param1.accept(Raid.getLeaderBannerInstance());
                        param1.accept(Items.SKELETON_SKULL);
                        param1.accept(Items.WITHER_SKELETON_SKULL);
                        param1.accept(Items.PLAYER_HEAD);
                        param1.accept(Items.ZOMBIE_HEAD);
                        param1.accept(Items.CREEPER_HEAD);
                        param1.accept(Items.PIGLIN_HEAD);
                        param1.accept(Items.DRAGON_HEAD);
                        param1.accept(Items.DRAGON_EGG);
                        param1.accept(Items.END_PORTAL_FRAME);
                        param1.accept(Items.ENDER_EYE);
                        param1.accept(Items.INFESTED_STONE);
                        param1.accept(Items.INFESTED_COBBLESTONE);
                        param1.accept(Items.INFESTED_STONE_BRICKS);
                        param1.accept(Items.INFESTED_MOSSY_STONE_BRICKS);
                        param1.accept(Items.INFESTED_CRACKED_STONE_BRICKS);
                        param1.accept(Items.INFESTED_CHISELED_STONE_BRICKS);
                        param1.accept(Items.INFESTED_DEEPSLATE);
                    }
                )
                .build()
        );
        Registry.register(
            param0,
            REDSTONE_BLOCKS,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4)
                .title(Component.translatable("itemGroup.redstone"))
                .icon(() -> new ItemStack(Items.REDSTONE))
                .displayItems((param0x, param1) -> {
                    param1.accept(Items.REDSTONE);
                    param1.accept(Items.REDSTONE_TORCH);
                    param1.accept(Items.REDSTONE_BLOCK);
                    param1.accept(Items.REPEATER);
                    param1.accept(Items.COMPARATOR);
                    param1.accept(Items.TARGET);
                    param1.accept(Items.LEVER);
                    param1.accept(Items.OAK_BUTTON);
                    param1.accept(Items.STONE_BUTTON);
                    param1.accept(Items.OAK_PRESSURE_PLATE);
                    param1.accept(Items.STONE_PRESSURE_PLATE);
                    param1.accept(Items.LIGHT_WEIGHTED_PRESSURE_PLATE);
                    param1.accept(Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
                    param1.accept(Items.SCULK_SENSOR);
                    param1.accept(Items.CALIBRATED_SCULK_SENSOR);
                    param1.accept(Items.SCULK_SHRIEKER);
                    param1.accept(Items.AMETHYST_BLOCK);
                    param1.accept(Items.WHITE_WOOL);
                    param1.accept(Items.TRIPWIRE_HOOK);
                    param1.accept(Items.STRING);
                    param1.accept(Items.LECTERN);
                    param1.accept(Items.DAYLIGHT_DETECTOR);
                    param1.accept(Items.LIGHTNING_ROD);
                    param1.accept(Items.PISTON);
                    param1.accept(Items.STICKY_PISTON);
                    param1.accept(Items.SLIME_BLOCK);
                    param1.accept(Items.HONEY_BLOCK);
                    param1.accept(Items.DISPENSER);
                    param1.accept(Items.DROPPER);
                    param1.accept(Items.HOPPER);
                    param1.accept(Items.CHEST);
                    param1.accept(Items.BARREL);
                    param1.accept(Items.CHISELED_BOOKSHELF);
                    param1.accept(Items.FURNACE);
                    param1.accept(Items.TRAPPED_CHEST);
                    param1.accept(Items.JUKEBOX);
                    param1.accept(Items.OBSERVER);
                    param1.accept(Items.NOTE_BLOCK);
                    param1.accept(Items.COMPOSTER);
                    param1.accept(Items.CAULDRON);
                    param1.accept(Items.RAIL);
                    param1.accept(Items.POWERED_RAIL);
                    param1.accept(Items.DETECTOR_RAIL);
                    param1.accept(Items.ACTIVATOR_RAIL);
                    param1.accept(Items.MINECART);
                    param1.accept(Items.HOPPER_MINECART);
                    param1.accept(Items.CHEST_MINECART);
                    param1.accept(Items.FURNACE_MINECART);
                    param1.accept(Items.TNT_MINECART);
                    param1.accept(Items.OAK_CHEST_BOAT);
                    param1.accept(Items.BAMBOO_CHEST_RAFT);
                    param1.accept(Items.OAK_DOOR);
                    param1.accept(Items.IRON_DOOR);
                    param1.accept(Items.OAK_FENCE_GATE);
                    param1.accept(Items.OAK_TRAPDOOR);
                    param1.accept(Items.IRON_TRAPDOOR);
                    param1.accept(Items.TNT);
                    param1.accept(Items.REDSTONE_LAMP);
                    param1.accept(Items.BELL);
                    param1.accept(Items.BIG_DRIPLEAF);
                    param1.accept(Items.ARMOR_STAND);
                    param1.accept(Items.REDSTONE_ORE);
                })
                .build()
        );
        Registry.register(
            param0,
            HOTBAR,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 5)
                .title(Component.translatable("itemGroup.hotbar"))
                .icon(() -> new ItemStack(Blocks.BOOKSHELF))
                .alignedRight()
                .type(CreativeModeTab.Type.HOTBAR)
                .build()
        );
        Registry.register(
            param0,
            SEARCH,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 6)
                .title(Component.translatable("itemGroup.search"))
                .icon(() -> new ItemStack(Items.COMPASS))
                .displayItems((param1, param2) -> {
                    Set<ItemStack> var0x = ItemStackLinkedSet.createTypeAndTagSet();
        
                    for(CreativeModeTab var1 : param0) {
                        if (var1.getType() != CreativeModeTab.Type.SEARCH) {
                            var0x.addAll(var1.getSearchTabDisplayItems());
                        }
                    }
        
                    param2.acceptAll(var0x);
                })
                .backgroundSuffix("item_search.png")
                .alignedRight()
                .type(CreativeModeTab.Type.SEARCH)
                .build()
        );
        Registry.register(
            param0,
            TOOLS_AND_UTILITIES,
            CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0)
                .title(Component.translatable("itemGroup.tools"))
                .icon(() -> new ItemStack(Items.DIAMOND_PICKAXE))
                .displayItems(
                    (param0x, param1) -> {
                        param1.accept(Items.WOODEN_SHOVEL);
                        param1.accept(Items.WOODEN_PICKAXE);
                        param1.accept(Items.WOODEN_AXE);
                        param1.accept(Items.WOODEN_HOE);
                        param1.accept(Items.STONE_SHOVEL);
                        param1.accept(Items.STONE_PICKAXE);
                        param1.accept(Items.STONE_AXE);
                        param1.accept(Items.STONE_HOE);
                        param1.accept(Items.IRON_SHOVEL);
                        param1.accept(Items.IRON_PICKAXE);
                        param1.accept(Items.IRON_AXE);
                        param1.accept(Items.IRON_HOE);
                        param1.accept(Items.GOLDEN_SHOVEL);
                        param1.accept(Items.GOLDEN_PICKAXE);
                        param1.accept(Items.GOLDEN_AXE);
                        param1.accept(Items.GOLDEN_HOE);
                        param1.accept(Items.DIAMOND_SHOVEL);
                        param1.accept(Items.DIAMOND_PICKAXE);
                        param1.accept(Items.DIAMOND_AXE);
                        param1.accept(Items.DIAMOND_HOE);
                        param1.accept(Items.NETHERITE_SHOVEL);
                        param1.accept(Items.NETHERITE_PICKAXE);
                        param1.accept(Items.NETHERITE_AXE);
                        param1.accept(Items.NETHERITE_HOE);
                        param1.accept(Items.BUCKET);
                        param1.accept(Items.WATER_BUCKET);
                        param1.accept(Items.PUFFERFISH_BUCKET);
                        param1.accept(Items.SALMON_BUCKET);
                        param1.accept(Items.COD_BUCKET);
                        param1.accept(Items.TROPICAL_FISH_BUCKET);
                        param1.accept(Items.AXOLOTL_BUCKET);
                        param1.accept(Items.TADPOLE_BUCKET);
                        param1.accept(Items.LAVA_BUCKET);
                        param1.accept(Items.POWDER_SNOW_BUCKET);
                        param1.accept(Items.MILK_BUCKET);
                        param1.accept(Items.FISHING_ROD);
                        param1.accept(Items.FLINT_AND_STEEL);
                        param1.accept(Items.FIRE_CHARGE);
                        param1.accept(Items.BONE_MEAL);
                        param1.accept(Items.SHEARS);
                        param1.accept(Items.BRUSH);
                        param1.accept(Items.NAME_TAG);
                        param1.accept(Items.LEAD);
                        if (param0x.enabledFeatures().contains(FeatureFlags.BUNDLE)) {
                            param1.accept(Items.BUNDLE);
                        }
            
                        param1.accept(Items.COMPASS);
                        param1.accept(Items.RECOVERY_COMPASS);
                        param1.accept(Items.CLOCK);
                        param1.accept(Items.SPYGLASS);
                        param1.accept(Items.MAP);
                        param1.accept(Items.WRITABLE_BOOK);
                        param1.accept(Items.ENDER_PEARL);
                        param1.accept(Items.ENDER_EYE);
                        param1.accept(Items.ELYTRA);
                        generateFireworksAllDurations(param1, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        param1.accept(Items.SADDLE);
                        param1.accept(Items.CARROT_ON_A_STICK);
                        param1.accept(Items.WARPED_FUNGUS_ON_A_STICK);
                        param1.accept(Items.OAK_BOAT);
                        param1.accept(Items.OAK_CHEST_BOAT);
                        param1.accept(Items.SPRUCE_BOAT);
                        param1.accept(Items.SPRUCE_CHEST_BOAT);
                        param1.accept(Items.BIRCH_BOAT);
                        param1.accept(Items.BIRCH_CHEST_BOAT);
                        param1.accept(Items.JUNGLE_BOAT);
                        param1.accept(Items.JUNGLE_CHEST_BOAT);
                        param1.accept(Items.ACACIA_BOAT);
                        param1.accept(Items.ACACIA_CHEST_BOAT);
                        param1.accept(Items.DARK_OAK_BOAT);
                        param1.accept(Items.DARK_OAK_CHEST_BOAT);
                        param1.accept(Items.MANGROVE_BOAT);
                        param1.accept(Items.MANGROVE_CHEST_BOAT);
                        param1.accept(Items.CHERRY_BOAT);
                        param1.accept(Items.CHERRY_CHEST_BOAT);
                        param1.accept(Items.BAMBOO_RAFT);
                        param1.accept(Items.BAMBOO_CHEST_RAFT);
                        param1.accept(Items.RAIL);
                        param1.accept(Items.POWERED_RAIL);
                        param1.accept(Items.DETECTOR_RAIL);
                        param1.accept(Items.ACTIVATOR_RAIL);
                        param1.accept(Items.MINECART);
                        param1.accept(Items.HOPPER_MINECART);
                        param1.accept(Items.CHEST_MINECART);
                        param1.accept(Items.FURNACE_MINECART);
                        param1.accept(Items.TNT_MINECART);
                        param0x.holders()
                            .lookup(Registries.INSTRUMENT)
                            .ifPresent(
                                param1x -> generateInstrumentTypes(
                                        param1, param1x, Items.GOAT_HORN, InstrumentTags.GOAT_HORNS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                                    )
                            );
                        param1.accept(Items.MUSIC_DISC_13);
                        param1.accept(Items.MUSIC_DISC_CAT);
                        param1.accept(Items.MUSIC_DISC_BLOCKS);
                        param1.accept(Items.MUSIC_DISC_CHIRP);
                        param1.accept(Items.MUSIC_DISC_FAR);
                        param1.accept(Items.MUSIC_DISC_MALL);
                        param1.accept(Items.MUSIC_DISC_MELLOHI);
                        param1.accept(Items.MUSIC_DISC_STAL);
                        param1.accept(Items.MUSIC_DISC_STRAD);
                        param1.accept(Items.MUSIC_DISC_WARD);
                        param1.accept(Items.MUSIC_DISC_11);
                        param1.accept(Items.MUSIC_DISC_WAIT);
                        param1.accept(Items.MUSIC_DISC_OTHERSIDE);
                        param1.accept(Items.MUSIC_DISC_5);
                        param1.accept(Items.MUSIC_DISC_PIGSTEP);
                        param1.accept(Items.MUSIC_DISC_RELIC);
                    }
                )
                .build()
        );
        Registry.register(
            param0,
            COMBAT,
            CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 1)
                .title(Component.translatable("itemGroup.combat"))
                .icon(() -> new ItemStack(Items.NETHERITE_SWORD))
                .displayItems(
                    (param0x, param1) -> {
                        param1.accept(Items.WOODEN_SWORD);
                        param1.accept(Items.STONE_SWORD);
                        param1.accept(Items.IRON_SWORD);
                        param1.accept(Items.GOLDEN_SWORD);
                        param1.accept(Items.DIAMOND_SWORD);
                        param1.accept(Items.NETHERITE_SWORD);
                        param1.accept(Items.WOODEN_AXE);
                        param1.accept(Items.STONE_AXE);
                        param1.accept(Items.IRON_AXE);
                        param1.accept(Items.GOLDEN_AXE);
                        param1.accept(Items.DIAMOND_AXE);
                        param1.accept(Items.NETHERITE_AXE);
                        param1.accept(Items.TRIDENT);
                        param1.accept(Items.SHIELD);
                        param1.accept(Items.LEATHER_HELMET);
                        param1.accept(Items.LEATHER_CHESTPLATE);
                        param1.accept(Items.LEATHER_LEGGINGS);
                        param1.accept(Items.LEATHER_BOOTS);
                        param1.accept(Items.CHAINMAIL_HELMET);
                        param1.accept(Items.CHAINMAIL_CHESTPLATE);
                        param1.accept(Items.CHAINMAIL_LEGGINGS);
                        param1.accept(Items.CHAINMAIL_BOOTS);
                        param1.accept(Items.IRON_HELMET);
                        param1.accept(Items.IRON_CHESTPLATE);
                        param1.accept(Items.IRON_LEGGINGS);
                        param1.accept(Items.IRON_BOOTS);
                        param1.accept(Items.GOLDEN_HELMET);
                        param1.accept(Items.GOLDEN_CHESTPLATE);
                        param1.accept(Items.GOLDEN_LEGGINGS);
                        param1.accept(Items.GOLDEN_BOOTS);
                        param1.accept(Items.DIAMOND_HELMET);
                        param1.accept(Items.DIAMOND_CHESTPLATE);
                        param1.accept(Items.DIAMOND_LEGGINGS);
                        param1.accept(Items.DIAMOND_BOOTS);
                        param1.accept(Items.NETHERITE_HELMET);
                        param1.accept(Items.NETHERITE_CHESTPLATE);
                        param1.accept(Items.NETHERITE_LEGGINGS);
                        param1.accept(Items.NETHERITE_BOOTS);
                        param1.accept(Items.TURTLE_HELMET);
                        param1.accept(Items.LEATHER_HORSE_ARMOR);
                        param1.accept(Items.IRON_HORSE_ARMOR);
                        param1.accept(Items.GOLDEN_HORSE_ARMOR);
                        param1.accept(Items.DIAMOND_HORSE_ARMOR);
                        param1.accept(Items.TOTEM_OF_UNDYING);
                        param1.accept(Items.TNT);
                        param1.accept(Items.END_CRYSTAL);
                        param1.accept(Items.SNOWBALL);
                        param1.accept(Items.EGG);
                        param1.accept(Items.BOW);
                        param1.accept(Items.CROSSBOW);
                        generateFireworksAllDurations(param1, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        param1.accept(Items.ARROW);
                        param1.accept(Items.SPECTRAL_ARROW);
                        param0x.holders()
                            .lookup(Registries.POTION)
                            .ifPresent(
                                param1x -> generatePotionEffectTypes(param1, param1x, Items.TIPPED_ARROW, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)
                            );
                    }
                )
                .build()
        );
        Registry.register(
            param0,
            FOOD_AND_DRINKS,
            CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 2)
                .title(Component.translatable("itemGroup.foodAndDrink"))
                .icon(() -> new ItemStack(Items.GOLDEN_APPLE))
                .displayItems((param0x, param1) -> {
                    param1.accept(Items.APPLE);
                    param1.accept(Items.GOLDEN_APPLE);
                    param1.accept(Items.ENCHANTED_GOLDEN_APPLE);
                    param1.accept(Items.MELON_SLICE);
                    param1.accept(Items.SWEET_BERRIES);
                    param1.accept(Items.GLOW_BERRIES);
                    param1.accept(Items.CHORUS_FRUIT);
                    param1.accept(Items.CARROT);
                    param1.accept(Items.GOLDEN_CARROT);
                    param1.accept(Items.POTATO);
                    param1.accept(Items.BAKED_POTATO);
                    param1.accept(Items.POISONOUS_POTATO);
                    param1.accept(Items.BEETROOT);
                    param1.accept(Items.DRIED_KELP);
                    param1.accept(Items.BEEF);
                    param1.accept(Items.COOKED_BEEF);
                    param1.accept(Items.PORKCHOP);
                    param1.accept(Items.COOKED_PORKCHOP);
                    param1.accept(Items.MUTTON);
                    param1.accept(Items.COOKED_MUTTON);
                    param1.accept(Items.CHICKEN);
                    param1.accept(Items.COOKED_CHICKEN);
                    param1.accept(Items.RABBIT);
                    param1.accept(Items.COOKED_RABBIT);
                    param1.accept(Items.COD);
                    param1.accept(Items.COOKED_COD);
                    param1.accept(Items.SALMON);
                    param1.accept(Items.COOKED_SALMON);
                    param1.accept(Items.TROPICAL_FISH);
                    param1.accept(Items.PUFFERFISH);
                    param1.accept(Items.BREAD);
                    param1.accept(Items.COOKIE);
                    param1.accept(Items.CAKE);
                    param1.accept(Items.PUMPKIN_PIE);
                    param1.accept(Items.ROTTEN_FLESH);
                    param1.accept(Items.SPIDER_EYE);
                    param1.accept(Items.MUSHROOM_STEW);
                    param1.accept(Items.BEETROOT_SOUP);
                    param1.accept(Items.RABBIT_STEW);
                    generateSuspiciousStews(param1, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                    param1.accept(Items.MILK_BUCKET);
                    param1.accept(Items.HONEY_BOTTLE);
                    param0x.holders().lookup(Registries.POTION).ifPresent(param1x -> {
                        generatePotionEffectTypes(param1, param1x, Items.POTION, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        generatePotionEffectTypes(param1, param1x, Items.SPLASH_POTION, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        generatePotionEffectTypes(param1, param1x, Items.LINGERING_POTION, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                    });
                })
                .build()
        );
        Registry.register(
            param0,
            INGREDIENTS,
            CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 3)
                .title(Component.translatable("itemGroup.ingredients"))
                .icon(() -> new ItemStack(Items.IRON_INGOT))
                .displayItems((param0x, param1) -> {
                    param1.accept(Items.COAL);
                    param1.accept(Items.CHARCOAL);
                    param1.accept(Items.RAW_IRON);
                    param1.accept(Items.RAW_COPPER);
                    param1.accept(Items.RAW_GOLD);
                    param1.accept(Items.EMERALD);
                    param1.accept(Items.LAPIS_LAZULI);
                    param1.accept(Items.DIAMOND);
                    param1.accept(Items.ANCIENT_DEBRIS);
                    param1.accept(Items.QUARTZ);
                    param1.accept(Items.AMETHYST_SHARD);
                    param1.accept(Items.IRON_NUGGET);
                    param1.accept(Items.GOLD_NUGGET);
                    param1.accept(Items.IRON_INGOT);
                    param1.accept(Items.COPPER_INGOT);
                    param1.accept(Items.GOLD_INGOT);
                    param1.accept(Items.NETHERITE_SCRAP);
                    param1.accept(Items.NETHERITE_INGOT);
                    param1.accept(Items.STICK);
                    param1.accept(Items.FLINT);
                    param1.accept(Items.WHEAT);
                    param1.accept(Items.BONE);
                    param1.accept(Items.BONE_MEAL);
                    param1.accept(Items.STRING);
                    param1.accept(Items.FEATHER);
                    param1.accept(Items.SNOWBALL);
                    param1.accept(Items.EGG);
                    param1.accept(Items.LEATHER);
                    param1.accept(Items.RABBIT_HIDE);
                    param1.accept(Items.HONEYCOMB);
                    param1.accept(Items.INK_SAC);
                    param1.accept(Items.GLOW_INK_SAC);
                    param1.accept(Items.SCUTE);
                    param1.accept(Items.SLIME_BALL);
                    param1.accept(Items.CLAY_BALL);
                    param1.accept(Items.PRISMARINE_SHARD);
                    param1.accept(Items.PRISMARINE_CRYSTALS);
                    param1.accept(Items.NAUTILUS_SHELL);
                    param1.accept(Items.HEART_OF_THE_SEA);
                    param1.accept(Items.FIRE_CHARGE);
                    param1.accept(Items.BLAZE_ROD);
                    param1.accept(Items.NETHER_STAR);
                    param1.accept(Items.ENDER_PEARL);
                    param1.accept(Items.ENDER_EYE);
                    param1.accept(Items.SHULKER_SHELL);
                    param1.accept(Items.POPPED_CHORUS_FRUIT);
                    param1.accept(Items.ECHO_SHARD);
                    param1.accept(Items.DISC_FRAGMENT_5);
                    param1.accept(Items.WHITE_DYE);
                    param1.accept(Items.LIGHT_GRAY_DYE);
                    param1.accept(Items.GRAY_DYE);
                    param1.accept(Items.BLACK_DYE);
                    param1.accept(Items.BROWN_DYE);
                    param1.accept(Items.RED_DYE);
                    param1.accept(Items.ORANGE_DYE);
                    param1.accept(Items.YELLOW_DYE);
                    param1.accept(Items.LIME_DYE);
                    param1.accept(Items.GREEN_DYE);
                    param1.accept(Items.CYAN_DYE);
                    param1.accept(Items.LIGHT_BLUE_DYE);
                    param1.accept(Items.BLUE_DYE);
                    param1.accept(Items.PURPLE_DYE);
                    param1.accept(Items.MAGENTA_DYE);
                    param1.accept(Items.PINK_DYE);
                    param1.accept(Items.BOWL);
                    param1.accept(Items.BRICK);
                    param1.accept(Items.NETHER_BRICK);
                    param1.accept(Items.PAPER);
                    param1.accept(Items.BOOK);
                    param1.accept(Items.FIREWORK_STAR);
                    param1.accept(Items.GLASS_BOTTLE);
                    param1.accept(Items.NETHER_WART);
                    param1.accept(Items.REDSTONE);
                    param1.accept(Items.GLOWSTONE_DUST);
                    param1.accept(Items.GUNPOWDER);
                    param1.accept(Items.DRAGON_BREATH);
                    param1.accept(Items.FERMENTED_SPIDER_EYE);
                    param1.accept(Items.BLAZE_POWDER);
                    param1.accept(Items.SUGAR);
                    param1.accept(Items.RABBIT_FOOT);
                    param1.accept(Items.GLISTERING_MELON_SLICE);
                    param1.accept(Items.SPIDER_EYE);
                    param1.accept(Items.PUFFERFISH);
                    param1.accept(Items.MAGMA_CREAM);
                    param1.accept(Items.GOLDEN_CARROT);
                    param1.accept(Items.GHAST_TEAR);
                    param1.accept(Items.TURTLE_HELMET);
                    param1.accept(Items.PHANTOM_MEMBRANE);
                    param1.accept(Items.FLOWER_BANNER_PATTERN);
                    param1.accept(Items.CREEPER_BANNER_PATTERN);
                    param1.accept(Items.SKULL_BANNER_PATTERN);
                    param1.accept(Items.MOJANG_BANNER_PATTERN);
                    param1.accept(Items.GLOBE_BANNER_PATTERN);
                    param1.accept(Items.PIGLIN_BANNER_PATTERN);
                    param1.accept(Items.ANGLER_POTTERY_SHERD);
                    param1.accept(Items.ARCHER_POTTERY_SHERD);
                    param1.accept(Items.ARMS_UP_POTTERY_SHERD);
                    param1.accept(Items.BLADE_POTTERY_SHERD);
                    param1.accept(Items.BREWER_POTTERY_SHERD);
                    param1.accept(Items.BURN_POTTERY_SHERD);
                    param1.accept(Items.DANGER_POTTERY_SHERD);
                    param1.accept(Items.EXPLORER_POTTERY_SHERD);
                    param1.accept(Items.FRIEND_POTTERY_SHERD);
                    param1.accept(Items.HEART_POTTERY_SHERD);
                    param1.accept(Items.HEARTBREAK_POTTERY_SHERD);
                    param1.accept(Items.HOWL_POTTERY_SHERD);
                    param1.accept(Items.MINER_POTTERY_SHERD);
                    param1.accept(Items.MOURNER_POTTERY_SHERD);
                    param1.accept(Items.PLENTY_POTTERY_SHERD);
                    param1.accept(Items.PRIZE_POTTERY_SHERD);
                    param1.accept(Items.SHEAF_POTTERY_SHERD);
                    param1.accept(Items.SHELTER_POTTERY_SHERD);
                    param1.accept(Items.SKULL_POTTERY_SHERD);
                    param1.accept(Items.SNORT_POTTERY_SHERD);
                    param1.accept(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
                    param1.accept(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);
                    param1.accept(Items.EXPERIENCE_BOTTLE);
                    Set<EnchantmentCategory> var0x = EnumSet.allOf(EnchantmentCategory.class);
                    param0x.holders().lookup(Registries.ENCHANTMENT).ifPresent(param2 -> {
                        generateEnchantmentBookTypesOnlyMaxLevel(param1, param2, var0x, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                        generateEnchantmentBookTypesAllLevels(param1, param2, var0x, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                    });
                })
                .build()
        );
        Registry.register(
            param0,
            SPAWN_EGGS,
            CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 4)
                .title(Component.translatable("itemGroup.spawnEggs"))
                .icon(() -> new ItemStack(Items.PIG_SPAWN_EGG))
                .displayItems((param0x, param1) -> {
                    param1.accept(Items.SPAWNER);
                    param1.accept(Items.ALLAY_SPAWN_EGG);
                    param1.accept(Items.AXOLOTL_SPAWN_EGG);
                    param1.accept(Items.BAT_SPAWN_EGG);
                    param1.accept(Items.BEE_SPAWN_EGG);
                    param1.accept(Items.BLAZE_SPAWN_EGG);
                    param1.accept(Items.CAMEL_SPAWN_EGG);
                    param1.accept(Items.CAT_SPAWN_EGG);
                    param1.accept(Items.CAVE_SPIDER_SPAWN_EGG);
                    param1.accept(Items.CHICKEN_SPAWN_EGG);
                    param1.accept(Items.COD_SPAWN_EGG);
                    param1.accept(Items.COW_SPAWN_EGG);
                    param1.accept(Items.CREEPER_SPAWN_EGG);
                    param1.accept(Items.DOLPHIN_SPAWN_EGG);
                    param1.accept(Items.DONKEY_SPAWN_EGG);
                    param1.accept(Items.DROWNED_SPAWN_EGG);
                    param1.accept(Items.ELDER_GUARDIAN_SPAWN_EGG);
                    param1.accept(Items.ENDERMAN_SPAWN_EGG);
                    param1.accept(Items.ENDERMITE_SPAWN_EGG);
                    param1.accept(Items.EVOKER_SPAWN_EGG);
                    param1.accept(Items.FOX_SPAWN_EGG);
                    param1.accept(Items.FROG_SPAWN_EGG);
                    param1.accept(Items.GHAST_SPAWN_EGG);
                    param1.accept(Items.GLOW_SQUID_SPAWN_EGG);
                    param1.accept(Items.GOAT_SPAWN_EGG);
                    param1.accept(Items.GUARDIAN_SPAWN_EGG);
                    param1.accept(Items.HOGLIN_SPAWN_EGG);
                    param1.accept(Items.HORSE_SPAWN_EGG);
                    param1.accept(Items.HUSK_SPAWN_EGG);
                    param1.accept(Items.IRON_GOLEM_SPAWN_EGG);
                    param1.accept(Items.LLAMA_SPAWN_EGG);
                    param1.accept(Items.MAGMA_CUBE_SPAWN_EGG);
                    param1.accept(Items.MOOSHROOM_SPAWN_EGG);
                    param1.accept(Items.MULE_SPAWN_EGG);
                    param1.accept(Items.OCELOT_SPAWN_EGG);
                    param1.accept(Items.PANDA_SPAWN_EGG);
                    param1.accept(Items.PARROT_SPAWN_EGG);
                    param1.accept(Items.PHANTOM_SPAWN_EGG);
                    param1.accept(Items.PIG_SPAWN_EGG);
                    param1.accept(Items.PIGLIN_SPAWN_EGG);
                    param1.accept(Items.PIGLIN_BRUTE_SPAWN_EGG);
                    param1.accept(Items.PILLAGER_SPAWN_EGG);
                    param1.accept(Items.POLAR_BEAR_SPAWN_EGG);
                    param1.accept(Items.PUFFERFISH_SPAWN_EGG);
                    param1.accept(Items.RABBIT_SPAWN_EGG);
                    param1.accept(Items.RAVAGER_SPAWN_EGG);
                    param1.accept(Items.SALMON_SPAWN_EGG);
                    param1.accept(Items.SHEEP_SPAWN_EGG);
                    param1.accept(Items.SHULKER_SPAWN_EGG);
                    param1.accept(Items.SILVERFISH_SPAWN_EGG);
                    param1.accept(Items.SKELETON_SPAWN_EGG);
                    param1.accept(Items.SKELETON_HORSE_SPAWN_EGG);
                    param1.accept(Items.SLIME_SPAWN_EGG);
                    param1.accept(Items.SNIFFER_SPAWN_EGG);
                    param1.accept(Items.SNOW_GOLEM_SPAWN_EGG);
                    param1.accept(Items.SPIDER_SPAWN_EGG);
                    param1.accept(Items.SQUID_SPAWN_EGG);
                    param1.accept(Items.STRAY_SPAWN_EGG);
                    param1.accept(Items.STRIDER_SPAWN_EGG);
                    param1.accept(Items.TADPOLE_SPAWN_EGG);
                    param1.accept(Items.TRADER_LLAMA_SPAWN_EGG);
                    param1.accept(Items.TROPICAL_FISH_SPAWN_EGG);
                    param1.accept(Items.TURTLE_SPAWN_EGG);
                    param1.accept(Items.VEX_SPAWN_EGG);
                    param1.accept(Items.VILLAGER_SPAWN_EGG);
                    param1.accept(Items.VINDICATOR_SPAWN_EGG);
                    param1.accept(Items.WANDERING_TRADER_SPAWN_EGG);
                    param1.accept(Items.WARDEN_SPAWN_EGG);
                    param1.accept(Items.WITCH_SPAWN_EGG);
                    param1.accept(Items.WITHER_SKELETON_SPAWN_EGG);
                    param1.accept(Items.WOLF_SPAWN_EGG);
                    param1.accept(Items.ZOGLIN_SPAWN_EGG);
                    param1.accept(Items.ZOMBIE_SPAWN_EGG);
                    param1.accept(Items.ZOMBIE_HORSE_SPAWN_EGG);
                    param1.accept(Items.ZOMBIE_VILLAGER_SPAWN_EGG);
                    param1.accept(Items.ZOMBIFIED_PIGLIN_SPAWN_EGG);
                })
                .build()
        );
        Registry.register(
            param0,
            OP_BLOCKS,
            CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 5)
                .title(Component.translatable("itemGroup.op"))
                .icon(() -> new ItemStack(Items.COMMAND_BLOCK))
                .alignedRight()
                .displayItems(
                    (param0x, param1) -> {
                        if (param0x.hasPermissions()) {
                            param1.accept(Items.COMMAND_BLOCK);
                            param1.accept(Items.CHAIN_COMMAND_BLOCK);
                            param1.accept(Items.REPEATING_COMMAND_BLOCK);
                            param1.accept(Items.COMMAND_BLOCK_MINECART);
                            param1.accept(Items.JIGSAW);
                            param1.accept(Items.STRUCTURE_BLOCK);
                            param1.accept(Items.STRUCTURE_VOID);
                            param1.accept(Items.BARRIER);
                            param1.accept(Items.DEBUG_STICK);
            
                            for(int var0x = 15; var0x >= 0; --var0x) {
                                param1.accept(LightBlock.setLightOnStack(new ItemStack(Items.LIGHT), var0x));
                            }
            
                            param0x.holders()
                                .lookup(Registries.PAINTING_VARIANT)
                                .ifPresent(
                                    param1x -> generatePresetPaintings(
                                            param1,
                                            param1x,
                                            param0xxx -> !param0xxx.is(PaintingVariantTags.PLACEABLE),
                                            CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                                        )
                                );
                        }
            
                    }
                )
                .build()
        );
        return Registry.register(
            param0,
            INVENTORY,
            CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 6)
                .title(Component.translatable("itemGroup.inventory"))
                .icon(() -> new ItemStack(Blocks.CHEST))
                .backgroundSuffix("inventory.png")
                .hideTitle()
                .alignedRight()
                .type(CreativeModeTab.Type.INVENTORY)
                .noScrollBar()
                .build()
        );
    }

    public static void validate() {
        Map<Pair<CreativeModeTab.Row, Integer>, String> var0 = new HashMap<>();

        for(ResourceKey<CreativeModeTab> var1 : BuiltInRegistries.CREATIVE_MODE_TAB.registryKeySet()) {
            CreativeModeTab var2 = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(var1);
            String var3 = var2.getDisplayName().getString();
            String var4 = var0.put(Pair.of(var2.row(), var2.column()), var3);
            if (var4 != null) {
                throw new IllegalArgumentException("Duplicate position: " + var3 + " vs. " + var4);
            }
        }

    }

    public static CreativeModeTab getDefaultTab() {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(BUILDING_BLOCKS);
    }

    private static void generatePotionEffectTypes(CreativeModeTab.Output param0, HolderLookup<Potion> param1, Item param2, CreativeModeTab.TabVisibility param3) {
        param1.listElements()
            .filter(param0x -> !param0x.is(Potions.EMPTY_ID))
            .map(param1x -> PotionUtils.setPotion(new ItemStack(param2), param1x.value()))
            .forEach(param2x -> param0.accept(param2x, param3));
    }

    private static void generateEnchantmentBookTypesOnlyMaxLevel(
        CreativeModeTab.Output param0, HolderLookup<Enchantment> param1, Set<EnchantmentCategory> param2, CreativeModeTab.TabVisibility param3
    ) {
        param1.listElements()
            .map(Holder::value)
            .filter(param1x -> param2.contains(param1x.category))
            .map(param0x -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(param0x, param0x.getMaxLevel())))
            .forEach(param2x -> param0.accept(param2x, param3));
    }

    private static void generateEnchantmentBookTypesAllLevels(
        CreativeModeTab.Output param0, HolderLookup<Enchantment> param1, Set<EnchantmentCategory> param2, CreativeModeTab.TabVisibility param3
    ) {
        param1.listElements()
            .map(Holder::value)
            .filter(param1x -> param2.contains(param1x.category))
            .flatMap(
                param0x -> IntStream.rangeClosed(param0x.getMinLevel(), param0x.getMaxLevel())
                        .mapToObj(param1x -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(param0x, param1x)))
            )
            .forEach(param2x -> param0.accept(param2x, param3));
    }

    private static void generateInstrumentTypes(
        CreativeModeTab.Output param0, HolderLookup<Instrument> param1, Item param2, TagKey<Instrument> param3, CreativeModeTab.TabVisibility param4
    ) {
        param1.get(param3)
            .ifPresent(param3x -> param3x.stream().map(param1x -> InstrumentItem.create(param2, param1x)).forEach(param2x -> param0.accept(param2x, param4)));
    }

    private static void generateSuspiciousStews(CreativeModeTab.Output param0, CreativeModeTab.TabVisibility param1) {
        List<SuspiciousEffectHolder> var0 = SuspiciousEffectHolder.getAllEffectHolders();
        Set<ItemStack> var1 = ItemStackLinkedSet.createTypeAndTagSet();

        for(SuspiciousEffectHolder var2 : var0) {
            ItemStack var3 = new ItemStack(Items.SUSPICIOUS_STEW);
            SuspiciousStewItem.saveMobEffect(var3, var2.getSuspiciousEffect(), var2.getEffectDuration());
            var1.add(var3);
        }

        param0.acceptAll(var1, param1);
    }

    private static void generateFireworksAllDurations(CreativeModeTab.Output param0, CreativeModeTab.TabVisibility param1) {
        for(byte var0 : FireworkRocketItem.CRAFTABLE_DURATIONS) {
            ItemStack var1 = new ItemStack(Items.FIREWORK_ROCKET);
            FireworkRocketItem.setDuration(var1, var0);
            param0.accept(var1, param1);
        }

    }

    private static void generatePresetPaintings(
        CreativeModeTab.Output param0,
        HolderLookup.RegistryLookup<PaintingVariant> param1,
        Predicate<Holder<PaintingVariant>> param2,
        CreativeModeTab.TabVisibility param3
    ) {
        param1.listElements().filter(param2).sorted(PAINTING_COMPARATOR).forEach(param2x -> {
            ItemStack var0x = new ItemStack(Items.PAINTING);
            CompoundTag var1x = var0x.getOrCreateTagElement("EntityTag");
            Painting.storeVariant(var1x, param2x);
            param0.accept(var0x, param3);
        });
    }

    public static List<CreativeModeTab> tabs() {
        return streamAllTabs().filter(CreativeModeTab::shouldDisplay).toList();
    }

    public static List<CreativeModeTab> allTabs() {
        return streamAllTabs().toList();
    }

    private static Stream<CreativeModeTab> streamAllTabs() {
        return BuiltInRegistries.CREATIVE_MODE_TAB.stream();
    }

    public static CreativeModeTab searchTab() {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(SEARCH);
    }

    private static void buildAllTabContents(CreativeModeTab.ItemDisplayParameters param0) {
        streamAllTabs().filter(param0x -> param0x.getType() == CreativeModeTab.Type.CATEGORY).forEach(param1 -> param1.buildContents(param0));
        streamAllTabs().filter(param0x -> param0x.getType() != CreativeModeTab.Type.CATEGORY).forEach(param1 -> param1.buildContents(param0));
    }

    public static boolean tryRebuildTabContents(FeatureFlagSet param0, boolean param1, HolderLookup.Provider param2) {
        if (CACHED_PARAMETERS != null && !CACHED_PARAMETERS.needsUpdate(param0, param1, param2)) {
            return false;
        } else {
            CACHED_PARAMETERS = new CreativeModeTab.ItemDisplayParameters(param0, param1, param2);
            buildAllTabContents(CACHED_PARAMETERS);
            return true;
        }
    }
}
