package net.minecraft.data;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockFamilies {
    private static final Map<Block, BlockFamily> MAP = Maps.newHashMap();
    public static final BlockFamily ACACIA_PLANKS = familyBuilder(Blocks.ACACIA_PLANKS)
        .button(Blocks.ACACIA_BUTTON)
        .fence(Blocks.ACACIA_FENCE)
        .fenceGate(Blocks.ACACIA_FENCE_GATE)
        .pressurePlate(Blocks.ACACIA_PRESSURE_PLATE)
        .sign(Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN)
        .slab(Blocks.ACACIA_SLAB)
        .stairs(Blocks.ACACIA_STAIRS)
        .door(Blocks.ACACIA_DOOR)
        .trapdoor(Blocks.ACACIA_TRAPDOOR)
        .recipeGroupPrefix("wooden")
        .recipeUnlockedBy("has_planks")
        .getFamily();
    public static final BlockFamily BIRCH_PLANKS = familyBuilder(Blocks.BIRCH_PLANKS)
        .button(Blocks.BIRCH_BUTTON)
        .fence(Blocks.BIRCH_FENCE)
        .fenceGate(Blocks.BIRCH_FENCE_GATE)
        .pressurePlate(Blocks.BIRCH_PRESSURE_PLATE)
        .sign(Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN)
        .slab(Blocks.BIRCH_SLAB)
        .stairs(Blocks.BIRCH_STAIRS)
        .door(Blocks.BIRCH_DOOR)
        .trapdoor(Blocks.BIRCH_TRAPDOOR)
        .recipeGroupPrefix("wooden")
        .recipeUnlockedBy("has_planks")
        .getFamily();
    public static final BlockFamily CRIMSON_PLANKS = familyBuilder(Blocks.CRIMSON_PLANKS)
        .button(Blocks.CRIMSON_BUTTON)
        .fence(Blocks.CRIMSON_FENCE)
        .fenceGate(Blocks.CRIMSON_FENCE_GATE)
        .pressurePlate(Blocks.CRIMSON_PRESSURE_PLATE)
        .sign(Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN)
        .slab(Blocks.CRIMSON_SLAB)
        .stairs(Blocks.CRIMSON_STAIRS)
        .door(Blocks.CRIMSON_DOOR)
        .trapdoor(Blocks.CRIMSON_TRAPDOOR)
        .recipeGroupPrefix("wooden")
        .recipeUnlockedBy("has_planks")
        .getFamily();
    public static final BlockFamily JUNGLE_PLANKS = familyBuilder(Blocks.JUNGLE_PLANKS)
        .button(Blocks.JUNGLE_BUTTON)
        .fence(Blocks.JUNGLE_FENCE)
        .fenceGate(Blocks.JUNGLE_FENCE_GATE)
        .pressurePlate(Blocks.JUNGLE_PRESSURE_PLATE)
        .sign(Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN)
        .slab(Blocks.JUNGLE_SLAB)
        .stairs(Blocks.JUNGLE_STAIRS)
        .door(Blocks.JUNGLE_DOOR)
        .trapdoor(Blocks.JUNGLE_TRAPDOOR)
        .recipeGroupPrefix("wooden")
        .recipeUnlockedBy("has_planks")
        .getFamily();
    public static final BlockFamily OAK_PLANKS = familyBuilder(Blocks.OAK_PLANKS)
        .button(Blocks.OAK_BUTTON)
        .fence(Blocks.OAK_FENCE)
        .fenceGate(Blocks.OAK_FENCE_GATE)
        .pressurePlate(Blocks.OAK_PRESSURE_PLATE)
        .sign(Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN)
        .slab(Blocks.OAK_SLAB)
        .stairs(Blocks.OAK_STAIRS)
        .door(Blocks.OAK_DOOR)
        .trapdoor(Blocks.OAK_TRAPDOOR)
        .recipeGroupPrefix("wooden")
        .recipeUnlockedBy("has_planks")
        .getFamily();
    public static final BlockFamily DARK_OAK_PLANKS = familyBuilder(Blocks.DARK_OAK_PLANKS)
        .button(Blocks.DARK_OAK_BUTTON)
        .fence(Blocks.DARK_OAK_FENCE)
        .fenceGate(Blocks.DARK_OAK_FENCE_GATE)
        .pressurePlate(Blocks.DARK_OAK_PRESSURE_PLATE)
        .sign(Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN)
        .slab(Blocks.DARK_OAK_SLAB)
        .stairs(Blocks.DARK_OAK_STAIRS)
        .door(Blocks.DARK_OAK_DOOR)
        .trapdoor(Blocks.DARK_OAK_TRAPDOOR)
        .recipeGroupPrefix("wooden")
        .recipeUnlockedBy("has_planks")
        .getFamily();
    public static final BlockFamily SPRUCE_PLANKS = familyBuilder(Blocks.SPRUCE_PLANKS)
        .button(Blocks.SPRUCE_BUTTON)
        .fence(Blocks.SPRUCE_FENCE)
        .fenceGate(Blocks.SPRUCE_FENCE_GATE)
        .pressurePlate(Blocks.SPRUCE_PRESSURE_PLATE)
        .sign(Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN)
        .slab(Blocks.SPRUCE_SLAB)
        .stairs(Blocks.SPRUCE_STAIRS)
        .door(Blocks.SPRUCE_DOOR)
        .trapdoor(Blocks.SPRUCE_TRAPDOOR)
        .recipeGroupPrefix("wooden")
        .recipeUnlockedBy("has_planks")
        .getFamily();
    public static final BlockFamily WARPED_PLANKS = familyBuilder(Blocks.WARPED_PLANKS)
        .button(Blocks.WARPED_BUTTON)
        .fence(Blocks.WARPED_FENCE)
        .fenceGate(Blocks.WARPED_FENCE_GATE)
        .pressurePlate(Blocks.WARPED_PRESSURE_PLATE)
        .sign(Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN)
        .slab(Blocks.WARPED_SLAB)
        .stairs(Blocks.WARPED_STAIRS)
        .door(Blocks.WARPED_DOOR)
        .trapdoor(Blocks.WARPED_TRAPDOOR)
        .recipeGroupPrefix("wooden")
        .recipeUnlockedBy("has_planks")
        .getFamily();
    public static final BlockFamily ANDESITE = familyBuilder(Blocks.ANDESITE)
        .wall(Blocks.ANDESITE_WALL)
        .stairs(Blocks.ANDESITE_STAIRS)
        .slab(Blocks.ANDESITE_SLAB)
        .polished(Blocks.POLISHED_ANDESITE)
        .getFamily();
    public static final BlockFamily POLISHED_ANDESITE = familyBuilder(Blocks.POLISHED_ANDESITE)
        .stairs(Blocks.POLISHED_ANDESITE_STAIRS)
        .slab(Blocks.POLISHED_ANDESITE_SLAB)
        .getFamily();
    public static final BlockFamily BLACKSTONE = familyBuilder(Blocks.BLACKSTONE)
        .wall(Blocks.BLACKSTONE_WALL)
        .stairs(Blocks.BLACKSTONE_STAIRS)
        .slab(Blocks.BLACKSTONE_SLAB)
        .polished(Blocks.POLISHED_BLACKSTONE)
        .getFamily();
    public static final BlockFamily POLISHED_BLACKSTONE = familyBuilder(Blocks.POLISHED_BLACKSTONE)
        .wall(Blocks.POLISHED_BLACKSTONE_WALL)
        .pressurePlate(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE)
        .button(Blocks.POLISHED_BLACKSTONE_BUTTON)
        .stairs(Blocks.POLISHED_BLACKSTONE_STAIRS)
        .slab(Blocks.POLISHED_BLACKSTONE_SLAB)
        .polished(Blocks.POLISHED_BLACKSTONE_BRICKS)
        .chiseled(Blocks.CHISELED_POLISHED_BLACKSTONE)
        .getFamily();
    public static final BlockFamily POLISHED_BLACKSTONE_BRICKS = familyBuilder(Blocks.POLISHED_BLACKSTONE_BRICKS)
        .wall(Blocks.POLISHED_BLACKSTONE_BRICK_WALL)
        .stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
        .slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB)
        .getFamily();
    public static final BlockFamily BRICKS = familyBuilder(Blocks.BRICKS)
        .wall(Blocks.BRICK_WALL)
        .stairs(Blocks.BRICK_STAIRS)
        .slab(Blocks.BRICK_SLAB)
        .getFamily();
    public static final BlockFamily END_STONE_BRICKS = familyBuilder(Blocks.END_STONE_BRICKS)
        .wall(Blocks.END_STONE_BRICK_WALL)
        .stairs(Blocks.END_STONE_BRICK_STAIRS)
        .slab(Blocks.END_STONE_BRICK_SLAB)
        .getFamily();
    public static final BlockFamily MOSSY_STONE_BRICKS = familyBuilder(Blocks.MOSSY_STONE_BRICKS)
        .wall(Blocks.MOSSY_STONE_BRICK_WALL)
        .stairs(Blocks.MOSSY_STONE_BRICK_STAIRS)
        .slab(Blocks.MOSSY_STONE_BRICK_SLAB)
        .getFamily();
    public static final BlockFamily CUT_COPPER = familyBuilder(Blocks.CUT_COPPER)
        .slab(Blocks.CUT_COPPER_SLAB)
        .stairs(Blocks.CUT_COPPER_STAIRS)
        .dontGenerateModel()
        .getFamily();
    public static final BlockFamily WAXED_CUT_COPPER = familyBuilder(Blocks.WAXED_CUT_COPPER)
        .slab(Blocks.WAXED_CUT_COPPER_SLAB)
        .stairs(Blocks.WAXED_CUT_COPPER_STAIRS)
        .dontGenerateModel()
        .getFamily();
    public static final BlockFamily EXPOSED_CUT_COPPER = familyBuilder(Blocks.EXPOSED_CUT_COPPER)
        .slab(Blocks.EXPOSED_CUT_COPPER_SLAB)
        .stairs(Blocks.EXPOSED_CUT_COPPER_STAIRS)
        .dontGenerateModel()
        .getFamily();
    public static final BlockFamily WAXED_EXPOSED_CUT_COPPER = familyBuilder(Blocks.WAXED_EXPOSED_CUT_COPPER)
        .slab(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB)
        .stairs(Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS)
        .dontGenerateModel()
        .getFamily();
    public static final BlockFamily WEATHERED_CUT_COPPER = familyBuilder(Blocks.WEATHERED_CUT_COPPER)
        .slab(Blocks.WEATHERED_CUT_COPPER_SLAB)
        .stairs(Blocks.WEATHERED_CUT_COPPER_STAIRS)
        .dontGenerateModel()
        .getFamily();
    public static final BlockFamily WAXED_WEATHERED_CUT_COPPER = familyBuilder(Blocks.WAXED_WEATHERED_CUT_COPPER)
        .slab(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB)
        .stairs(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS)
        .dontGenerateModel()
        .getFamily();
    public static final BlockFamily OXIDIZED_CUT_COPPER = familyBuilder(Blocks.OXIDIZED_CUT_COPPER)
        .slab(Blocks.OXIDIZED_CUT_COPPER_SLAB)
        .stairs(Blocks.OXIDIZED_CUT_COPPER_STAIRS)
        .getFamily();
    public static final BlockFamily COBBLESTONE = familyBuilder(Blocks.COBBLESTONE)
        .wall(Blocks.COBBLESTONE_WALL)
        .stairs(Blocks.COBBLESTONE_STAIRS)
        .slab(Blocks.COBBLESTONE_SLAB)
        .getFamily();
    public static final BlockFamily MOSSY_COBBLESTONE = familyBuilder(Blocks.MOSSY_COBBLESTONE)
        .wall(Blocks.MOSSY_COBBLESTONE_WALL)
        .stairs(Blocks.MOSSY_COBBLESTONE_STAIRS)
        .slab(Blocks.MOSSY_COBBLESTONE_SLAB)
        .getFamily();
    public static final BlockFamily DIORITE = familyBuilder(Blocks.DIORITE)
        .wall(Blocks.DIORITE_WALL)
        .stairs(Blocks.DIORITE_STAIRS)
        .slab(Blocks.DIORITE_SLAB)
        .polished(Blocks.POLISHED_DIORITE)
        .getFamily();
    public static final BlockFamily POLISHED_DIORITE = familyBuilder(Blocks.POLISHED_DIORITE)
        .stairs(Blocks.POLISHED_DIORITE_STAIRS)
        .slab(Blocks.POLISHED_DIORITE_SLAB)
        .getFamily();
    public static final BlockFamily GRANITE = familyBuilder(Blocks.GRANITE)
        .wall(Blocks.GRANITE_WALL)
        .stairs(Blocks.GRANITE_STAIRS)
        .slab(Blocks.GRANITE_SLAB)
        .polished(Blocks.POLISHED_GRANITE)
        .getFamily();
    public static final BlockFamily POLISHED_GRANITE = familyBuilder(Blocks.POLISHED_GRANITE)
        .stairs(Blocks.POLISHED_GRANITE_STAIRS)
        .slab(Blocks.POLISHED_GRANITE_SLAB)
        .getFamily();
    public static final BlockFamily NETHER_BRICKS = familyBuilder(Blocks.NETHER_BRICKS)
        .fence(Blocks.NETHER_BRICK_FENCE)
        .wall(Blocks.NETHER_BRICK_WALL)
        .stairs(Blocks.NETHER_BRICK_STAIRS)
        .slab(Blocks.NETHER_BRICK_SLAB)
        .chiseled(Blocks.CHISELED_NETHER_BRICKS)
        .getFamily();
    public static final BlockFamily RED_NETHER_BRICKS = familyBuilder(Blocks.RED_NETHER_BRICKS)
        .slab(Blocks.RED_NETHER_BRICK_SLAB)
        .stairs(Blocks.RED_NETHER_BRICK_STAIRS)
        .wall(Blocks.RED_NETHER_BRICK_WALL)
        .getFamily();
    public static final BlockFamily PRISMARINE = familyBuilder(Blocks.PRISMARINE)
        .wall(Blocks.PRISMARINE_WALL)
        .stairs(Blocks.PRISMARINE_STAIRS)
        .slab(Blocks.PRISMARINE_SLAB)
        .getFamily();
    public static final BlockFamily PURPUR = familyBuilder(Blocks.PURPUR_BLOCK)
        .stairs(Blocks.PURPUR_STAIRS)
        .slab(Blocks.PURPUR_SLAB)
        .dontGenerateRecipe()
        .getFamily();
    public static final BlockFamily PRISMARINE_BRICKS = familyBuilder(Blocks.PRISMARINE_BRICKS)
        .stairs(Blocks.PRISMARINE_BRICK_STAIRS)
        .slab(Blocks.PRISMARINE_BRICK_SLAB)
        .getFamily();
    public static final BlockFamily DARK_PRISMARINE = familyBuilder(Blocks.DARK_PRISMARINE)
        .stairs(Blocks.DARK_PRISMARINE_STAIRS)
        .slab(Blocks.DARK_PRISMARINE_SLAB)
        .getFamily();
    public static final BlockFamily QUARTZ = familyBuilder(Blocks.QUARTZ_BLOCK)
        .stairs(Blocks.QUARTZ_STAIRS)
        .slab(Blocks.QUARTZ_SLAB)
        .chiseled(Blocks.CHISELED_QUARTZ_BLOCK)
        .dontGenerateRecipe()
        .getFamily();
    public static final BlockFamily SMOOTH_QUARTZ = familyBuilder(Blocks.SMOOTH_QUARTZ)
        .stairs(Blocks.SMOOTH_QUARTZ_STAIRS)
        .slab(Blocks.SMOOTH_QUARTZ_SLAB)
        .getFamily();
    public static final BlockFamily SANDSTONE = familyBuilder(Blocks.SANDSTONE)
        .wall(Blocks.SANDSTONE_WALL)
        .stairs(Blocks.SANDSTONE_STAIRS)
        .slab(Blocks.SANDSTONE_SLAB)
        .chiseled(Blocks.CHISELED_SANDSTONE)
        .dontGenerateRecipe()
        .getFamily();
    public static final BlockFamily CUT_SANDSTONE = familyBuilder(Blocks.CUT_SANDSTONE).slab(Blocks.CUT_SANDSTONE_SLAB).getFamily();
    public static final BlockFamily SMOOTH_SANDSTONE = familyBuilder(Blocks.SMOOTH_SANDSTONE)
        .slab(Blocks.SMOOTH_SANDSTONE_SLAB)
        .stairs(Blocks.SMOOTH_SANDSTONE_STAIRS)
        .getFamily();
    public static final BlockFamily RED_SANDSTONE = familyBuilder(Blocks.RED_SANDSTONE)
        .wall(Blocks.RED_SANDSTONE_WALL)
        .stairs(Blocks.RED_SANDSTONE_STAIRS)
        .slab(Blocks.RED_SANDSTONE_SLAB)
        .chiseled(Blocks.CHISELED_RED_SANDSTONE)
        .dontGenerateRecipe()
        .getFamily();
    public static final BlockFamily CUT_RED_SANDSTONE = familyBuilder(Blocks.CUT_RED_SANDSTONE).slab(Blocks.CUT_RED_SANDSTONE_SLAB).getFamily();
    public static final BlockFamily SMOOTH_RED_SANDSTONE = familyBuilder(Blocks.SMOOTH_RED_SANDSTONE)
        .slab(Blocks.SMOOTH_RED_SANDSTONE_SLAB)
        .stairs(Blocks.SMOOTH_RED_SANDSTONE_STAIRS)
        .getFamily();
    public static final BlockFamily STONE = familyBuilder(Blocks.STONE)
        .slab(Blocks.STONE_SLAB)
        .pressurePlate(Blocks.STONE_PRESSURE_PLATE)
        .button(Blocks.STONE_BUTTON)
        .stairs(Blocks.STONE_STAIRS)
        .getFamily();
    public static final BlockFamily STONE_BRICK = familyBuilder(Blocks.STONE_BRICKS)
        .wall(Blocks.STONE_BRICK_WALL)
        .stairs(Blocks.STONE_BRICK_STAIRS)
        .slab(Blocks.STONE_BRICK_SLAB)
        .chiseled(Blocks.CHISELED_STONE_BRICKS)
        .dontGenerateRecipe()
        .getFamily();
    public static final BlockFamily GRIMSTONE = familyBuilder(Blocks.GRIMSTONE)
        .slab(Blocks.GRIMSTONE_SLAB)
        .stairs(Blocks.GRIMSTONE_STAIRS)
        .chiseled(Blocks.CHISELED_GRIMSTONE)
        .polished(Blocks.POLISHED_GRIMSTONE)
        .wall(Blocks.GRIMSTONE_WALL)
        .getFamily();
    public static final BlockFamily POLISHED_GRIMSTONE = familyBuilder(Blocks.POLISHED_GRIMSTONE)
        .slab(Blocks.POLISHED_GRIMSTONE_SLAB)
        .stairs(Blocks.POLISHED_GRIMSTONE_STAIRS)
        .wall(Blocks.POLISHED_GRIMSTONE_WALL)
        .getFamily();
    public static final BlockFamily GRIMSTONE_TILES = familyBuilder(Blocks.GRIMSTONE_TILES)
        .slab(Blocks.GRIMSTONE_TILE_SLAB)
        .stairs(Blocks.GRIMSTONE_TILE_STAIRS)
        .wall(Blocks.GRIMSTONE_TILE_WALL)
        .getFamily();
    public static final BlockFamily GRIMSTONE_BRICKS = familyBuilder(Blocks.GRIMSTONE_BRICKS)
        .slab(Blocks.GRIMSTONE_BRICK_SLAB)
        .stairs(Blocks.GRIMSTONE_BRICK_STAIRS)
        .wall(Blocks.GRIMSTONE_BRICK_WALL)
        .getFamily();

    private static BlockFamily.Builder familyBuilder(Block param0) {
        BlockFamily.Builder var0 = new BlockFamily.Builder(param0);
        BlockFamily var1 = MAP.put(param0, var0.getFamily());
        if (var1 != null) {
            throw new IllegalStateException("Duplicate family definition for " + Registry.BLOCK.getKey(param0));
        } else {
            return var0;
        }
    }

    public static Stream<BlockFamily> getAllFamilies() {
        return MAP.values().stream();
    }
}
