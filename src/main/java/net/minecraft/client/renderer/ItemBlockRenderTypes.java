package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemBlockRenderTypes {
    private static final Map<Block, RenderType> TYPE_BY_BLOCK = Util.make(Maps.newHashMap(), param0 -> {
        RenderType var0 = RenderType.tripwire();
        param0.put(Blocks.TRIPWIRE, var0);
        RenderType var1 = RenderType.cutoutMipped();
        param0.put(Blocks.GRASS_BLOCK, var1);
        param0.put(Blocks.IRON_BARS, var1);
        param0.put(Blocks.GLASS_PANE, var1);
        param0.put(Blocks.TRIPWIRE_HOOK, var1);
        param0.put(Blocks.HOPPER, var1);
        param0.put(Blocks.CHAIN, var1);
        param0.put(Blocks.JUNGLE_LEAVES, var1);
        param0.put(Blocks.OAK_LEAVES, var1);
        param0.put(Blocks.SPRUCE_LEAVES, var1);
        param0.put(Blocks.ACACIA_LEAVES, var1);
        param0.put(Blocks.BIRCH_LEAVES, var1);
        param0.put(Blocks.DARK_OAK_LEAVES, var1);
        param0.put(Blocks.AZALEA_LEAVES, var1);
        param0.put(Blocks.AZALEA_LEAVES_FLOWERS, var1);
        RenderType var2 = RenderType.cutout();
        param0.put(Blocks.OAK_SAPLING, var2);
        param0.put(Blocks.SPRUCE_SAPLING, var2);
        param0.put(Blocks.BIRCH_SAPLING, var2);
        param0.put(Blocks.JUNGLE_SAPLING, var2);
        param0.put(Blocks.ACACIA_SAPLING, var2);
        param0.put(Blocks.DARK_OAK_SAPLING, var2);
        param0.put(Blocks.GLASS, var2);
        param0.put(Blocks.WHITE_BED, var2);
        param0.put(Blocks.ORANGE_BED, var2);
        param0.put(Blocks.MAGENTA_BED, var2);
        param0.put(Blocks.LIGHT_BLUE_BED, var2);
        param0.put(Blocks.YELLOW_BED, var2);
        param0.put(Blocks.LIME_BED, var2);
        param0.put(Blocks.PINK_BED, var2);
        param0.put(Blocks.GRAY_BED, var2);
        param0.put(Blocks.LIGHT_GRAY_BED, var2);
        param0.put(Blocks.CYAN_BED, var2);
        param0.put(Blocks.PURPLE_BED, var2);
        param0.put(Blocks.BLUE_BED, var2);
        param0.put(Blocks.BROWN_BED, var2);
        param0.put(Blocks.GREEN_BED, var2);
        param0.put(Blocks.RED_BED, var2);
        param0.put(Blocks.BLACK_BED, var2);
        param0.put(Blocks.POWERED_RAIL, var2);
        param0.put(Blocks.DETECTOR_RAIL, var2);
        param0.put(Blocks.COBWEB, var2);
        param0.put(Blocks.GRASS, var2);
        param0.put(Blocks.FERN, var2);
        param0.put(Blocks.DEAD_BUSH, var2);
        param0.put(Blocks.SEAGRASS, var2);
        param0.put(Blocks.TALL_SEAGRASS, var2);
        param0.put(Blocks.DANDELION, var2);
        param0.put(Blocks.POPPY, var2);
        param0.put(Blocks.BLUE_ORCHID, var2);
        param0.put(Blocks.ALLIUM, var2);
        param0.put(Blocks.AZURE_BLUET, var2);
        param0.put(Blocks.RED_TULIP, var2);
        param0.put(Blocks.ORANGE_TULIP, var2);
        param0.put(Blocks.WHITE_TULIP, var2);
        param0.put(Blocks.PINK_TULIP, var2);
        param0.put(Blocks.OXEYE_DAISY, var2);
        param0.put(Blocks.CORNFLOWER, var2);
        param0.put(Blocks.WITHER_ROSE, var2);
        param0.put(Blocks.LILY_OF_THE_VALLEY, var2);
        param0.put(Blocks.BROWN_MUSHROOM, var2);
        param0.put(Blocks.RED_MUSHROOM, var2);
        param0.put(Blocks.TORCH, var2);
        param0.put(Blocks.WALL_TORCH, var2);
        param0.put(Blocks.SOUL_TORCH, var2);
        param0.put(Blocks.SOUL_WALL_TORCH, var2);
        param0.put(Blocks.FIRE, var2);
        param0.put(Blocks.SOUL_FIRE, var2);
        param0.put(Blocks.SPAWNER, var2);
        param0.put(Blocks.REDSTONE_WIRE, var2);
        param0.put(Blocks.WHEAT, var2);
        param0.put(Blocks.OAK_DOOR, var2);
        param0.put(Blocks.LADDER, var2);
        param0.put(Blocks.RAIL, var2);
        param0.put(Blocks.IRON_DOOR, var2);
        param0.put(Blocks.REDSTONE_TORCH, var2);
        param0.put(Blocks.REDSTONE_WALL_TORCH, var2);
        param0.put(Blocks.CACTUS, var2);
        param0.put(Blocks.SUGAR_CANE, var2);
        param0.put(Blocks.REPEATER, var2);
        param0.put(Blocks.OAK_TRAPDOOR, var2);
        param0.put(Blocks.SPRUCE_TRAPDOOR, var2);
        param0.put(Blocks.BIRCH_TRAPDOOR, var2);
        param0.put(Blocks.JUNGLE_TRAPDOOR, var2);
        param0.put(Blocks.ACACIA_TRAPDOOR, var2);
        param0.put(Blocks.DARK_OAK_TRAPDOOR, var2);
        param0.put(Blocks.CRIMSON_TRAPDOOR, var2);
        param0.put(Blocks.WARPED_TRAPDOOR, var2);
        param0.put(Blocks.ATTACHED_PUMPKIN_STEM, var2);
        param0.put(Blocks.ATTACHED_MELON_STEM, var2);
        param0.put(Blocks.PUMPKIN_STEM, var2);
        param0.put(Blocks.MELON_STEM, var2);
        param0.put(Blocks.VINE, var2);
        param0.put(Blocks.GLOW_LICHEN, var2);
        param0.put(Blocks.LILY_PAD, var2);
        param0.put(Blocks.NETHER_WART, var2);
        param0.put(Blocks.BREWING_STAND, var2);
        param0.put(Blocks.COCOA, var2);
        param0.put(Blocks.BEACON, var2);
        param0.put(Blocks.FLOWER_POT, var2);
        param0.put(Blocks.POTTED_OAK_SAPLING, var2);
        param0.put(Blocks.POTTED_SPRUCE_SAPLING, var2);
        param0.put(Blocks.POTTED_BIRCH_SAPLING, var2);
        param0.put(Blocks.POTTED_JUNGLE_SAPLING, var2);
        param0.put(Blocks.POTTED_ACACIA_SAPLING, var2);
        param0.put(Blocks.POTTED_DARK_OAK_SAPLING, var2);
        param0.put(Blocks.POTTED_FERN, var2);
        param0.put(Blocks.POTTED_DANDELION, var2);
        param0.put(Blocks.POTTED_POPPY, var2);
        param0.put(Blocks.POTTED_BLUE_ORCHID, var2);
        param0.put(Blocks.POTTED_ALLIUM, var2);
        param0.put(Blocks.POTTED_AZURE_BLUET, var2);
        param0.put(Blocks.POTTED_RED_TULIP, var2);
        param0.put(Blocks.POTTED_ORANGE_TULIP, var2);
        param0.put(Blocks.POTTED_WHITE_TULIP, var2);
        param0.put(Blocks.POTTED_PINK_TULIP, var2);
        param0.put(Blocks.POTTED_OXEYE_DAISY, var2);
        param0.put(Blocks.POTTED_CORNFLOWER, var2);
        param0.put(Blocks.POTTED_LILY_OF_THE_VALLEY, var2);
        param0.put(Blocks.POTTED_WITHER_ROSE, var2);
        param0.put(Blocks.POTTED_RED_MUSHROOM, var2);
        param0.put(Blocks.POTTED_BROWN_MUSHROOM, var2);
        param0.put(Blocks.POTTED_DEAD_BUSH, var2);
        param0.put(Blocks.POTTED_CACTUS, var2);
        param0.put(Blocks.CARROTS, var2);
        param0.put(Blocks.POTATOES, var2);
        param0.put(Blocks.COMPARATOR, var2);
        param0.put(Blocks.ACTIVATOR_RAIL, var2);
        param0.put(Blocks.IRON_TRAPDOOR, var2);
        param0.put(Blocks.SUNFLOWER, var2);
        param0.put(Blocks.LILAC, var2);
        param0.put(Blocks.ROSE_BUSH, var2);
        param0.put(Blocks.PEONY, var2);
        param0.put(Blocks.TALL_GRASS, var2);
        param0.put(Blocks.LARGE_FERN, var2);
        param0.put(Blocks.SPRUCE_DOOR, var2);
        param0.put(Blocks.BIRCH_DOOR, var2);
        param0.put(Blocks.JUNGLE_DOOR, var2);
        param0.put(Blocks.ACACIA_DOOR, var2);
        param0.put(Blocks.DARK_OAK_DOOR, var2);
        param0.put(Blocks.END_ROD, var2);
        param0.put(Blocks.CHORUS_PLANT, var2);
        param0.put(Blocks.CHORUS_FLOWER, var2);
        param0.put(Blocks.BEETROOTS, var2);
        param0.put(Blocks.KELP, var2);
        param0.put(Blocks.KELP_PLANT, var2);
        param0.put(Blocks.TURTLE_EGG, var2);
        param0.put(Blocks.DEAD_TUBE_CORAL, var2);
        param0.put(Blocks.DEAD_BRAIN_CORAL, var2);
        param0.put(Blocks.DEAD_BUBBLE_CORAL, var2);
        param0.put(Blocks.DEAD_FIRE_CORAL, var2);
        param0.put(Blocks.DEAD_HORN_CORAL, var2);
        param0.put(Blocks.TUBE_CORAL, var2);
        param0.put(Blocks.BRAIN_CORAL, var2);
        param0.put(Blocks.BUBBLE_CORAL, var2);
        param0.put(Blocks.FIRE_CORAL, var2);
        param0.put(Blocks.HORN_CORAL, var2);
        param0.put(Blocks.DEAD_TUBE_CORAL_FAN, var2);
        param0.put(Blocks.DEAD_BRAIN_CORAL_FAN, var2);
        param0.put(Blocks.DEAD_BUBBLE_CORAL_FAN, var2);
        param0.put(Blocks.DEAD_FIRE_CORAL_FAN, var2);
        param0.put(Blocks.DEAD_HORN_CORAL_FAN, var2);
        param0.put(Blocks.TUBE_CORAL_FAN, var2);
        param0.put(Blocks.BRAIN_CORAL_FAN, var2);
        param0.put(Blocks.BUBBLE_CORAL_FAN, var2);
        param0.put(Blocks.FIRE_CORAL_FAN, var2);
        param0.put(Blocks.HORN_CORAL_FAN, var2);
        param0.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, var2);
        param0.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, var2);
        param0.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, var2);
        param0.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, var2);
        param0.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, var2);
        param0.put(Blocks.TUBE_CORAL_WALL_FAN, var2);
        param0.put(Blocks.BRAIN_CORAL_WALL_FAN, var2);
        param0.put(Blocks.BUBBLE_CORAL_WALL_FAN, var2);
        param0.put(Blocks.FIRE_CORAL_WALL_FAN, var2);
        param0.put(Blocks.HORN_CORAL_WALL_FAN, var2);
        param0.put(Blocks.SEA_PICKLE, var2);
        param0.put(Blocks.CONDUIT, var2);
        param0.put(Blocks.BAMBOO_SAPLING, var2);
        param0.put(Blocks.BAMBOO, var2);
        param0.put(Blocks.POTTED_BAMBOO, var2);
        param0.put(Blocks.SCAFFOLDING, var2);
        param0.put(Blocks.STONECUTTER, var2);
        param0.put(Blocks.LANTERN, var2);
        param0.put(Blocks.SOUL_LANTERN, var2);
        param0.put(Blocks.CAMPFIRE, var2);
        param0.put(Blocks.SOUL_CAMPFIRE, var2);
        param0.put(Blocks.SWEET_BERRY_BUSH, var2);
        param0.put(Blocks.WEEPING_VINES, var2);
        param0.put(Blocks.WEEPING_VINES_PLANT, var2);
        param0.put(Blocks.TWISTING_VINES, var2);
        param0.put(Blocks.TWISTING_VINES_PLANT, var2);
        param0.put(Blocks.NETHER_SPROUTS, var2);
        param0.put(Blocks.CRIMSON_FUNGUS, var2);
        param0.put(Blocks.WARPED_FUNGUS, var2);
        param0.put(Blocks.CRIMSON_ROOTS, var2);
        param0.put(Blocks.WARPED_ROOTS, var2);
        param0.put(Blocks.POTTED_CRIMSON_FUNGUS, var2);
        param0.put(Blocks.POTTED_WARPED_FUNGUS, var2);
        param0.put(Blocks.POTTED_CRIMSON_ROOTS, var2);
        param0.put(Blocks.POTTED_WARPED_ROOTS, var2);
        param0.put(Blocks.CRIMSON_DOOR, var2);
        param0.put(Blocks.WARPED_DOOR, var2);
        param0.put(Blocks.POINTED_DRIPSTONE, var2);
        param0.put(Blocks.SMALL_AMETHYST_BUD, var2);
        param0.put(Blocks.MEDIUM_AMETHYST_BUD, var2);
        param0.put(Blocks.LARGE_AMETHYST_BUD, var2);
        param0.put(Blocks.AMETHYST_CLUSTER, var2);
        param0.put(Blocks.LIGHTNING_ROD, var2);
        param0.put(Blocks.CAVE_VINES_HEAD, var2);
        param0.put(Blocks.CAVE_VINES_BODY, var2);
        param0.put(Blocks.SPORE_BLOSSOM, var2);
        param0.put(Blocks.FLOWERING_AZALEA, var2);
        param0.put(Blocks.AZALEA, var2);
        param0.put(Blocks.MOSS_CARPET, var2);
        param0.put(Blocks.BIG_DRIPLEAF, var2);
        param0.put(Blocks.BIG_DRIPLEAF_STEM, var2);
        param0.put(Blocks.SMALL_DRIPLEAF, var2);
        param0.put(Blocks.HANGING_ROOTS, var2);
        param0.put(Blocks.SCULK_SENSOR, var2);
        RenderType var3 = RenderType.translucent();
        param0.put(Blocks.ICE, var3);
        param0.put(Blocks.NETHER_PORTAL, var3);
        param0.put(Blocks.WHITE_STAINED_GLASS, var3);
        param0.put(Blocks.ORANGE_STAINED_GLASS, var3);
        param0.put(Blocks.MAGENTA_STAINED_GLASS, var3);
        param0.put(Blocks.LIGHT_BLUE_STAINED_GLASS, var3);
        param0.put(Blocks.YELLOW_STAINED_GLASS, var3);
        param0.put(Blocks.LIME_STAINED_GLASS, var3);
        param0.put(Blocks.PINK_STAINED_GLASS, var3);
        param0.put(Blocks.GRAY_STAINED_GLASS, var3);
        param0.put(Blocks.LIGHT_GRAY_STAINED_GLASS, var3);
        param0.put(Blocks.CYAN_STAINED_GLASS, var3);
        param0.put(Blocks.PURPLE_STAINED_GLASS, var3);
        param0.put(Blocks.BLUE_STAINED_GLASS, var3);
        param0.put(Blocks.BROWN_STAINED_GLASS, var3);
        param0.put(Blocks.GREEN_STAINED_GLASS, var3);
        param0.put(Blocks.RED_STAINED_GLASS, var3);
        param0.put(Blocks.BLACK_STAINED_GLASS, var3);
        param0.put(Blocks.WHITE_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.ORANGE_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.MAGENTA_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.YELLOW_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.LIME_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.PINK_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.GRAY_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.CYAN_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.PURPLE_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.BLUE_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.BROWN_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.GREEN_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.RED_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.BLACK_STAINED_GLASS_PANE, var3);
        param0.put(Blocks.SLIME_BLOCK, var3);
        param0.put(Blocks.HONEY_BLOCK, var3);
        param0.put(Blocks.FROSTED_ICE, var3);
        param0.put(Blocks.BUBBLE_COLUMN, var3);
        param0.put(Blocks.TINTED_GLASS, var3);
    });
    private static final Map<Fluid, RenderType> TYPE_BY_FLUID = Util.make(Maps.newHashMap(), param0 -> {
        RenderType var0 = RenderType.translucent();
        param0.put(Fluids.FLOWING_WATER, var0);
        param0.put(Fluids.WATER, var0);
    });
    private static boolean renderCutout;

    public static RenderType getChunkRenderType(BlockState param0) {
        Block var0 = param0.getBlock();
        if (var0 instanceof LeavesBlock) {
            return renderCutout ? RenderType.cutoutMipped() : RenderType.solid();
        } else {
            RenderType var1 = TYPE_BY_BLOCK.get(var0);
            return var1 != null ? var1 : RenderType.solid();
        }
    }

    public static RenderType getMovingBlockRenderType(BlockState param0) {
        Block var0 = param0.getBlock();
        if (var0 instanceof LeavesBlock) {
            return renderCutout ? RenderType.cutoutMipped() : RenderType.solid();
        } else {
            RenderType var1 = TYPE_BY_BLOCK.get(var0);
            if (var1 != null) {
                return var1 == RenderType.translucent() ? RenderType.translucentMovingBlock() : var1;
            } else {
                return RenderType.solid();
            }
        }
    }

    public static RenderType getRenderType(BlockState param0, boolean param1) {
        RenderType var0 = getChunkRenderType(param0);
        if (var0 == RenderType.translucent()) {
            if (!Minecraft.useShaderTransparency()) {
                return Sheets.translucentCullBlockSheet();
            } else {
                return param1 ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
            }
        } else {
            return Sheets.cutoutBlockSheet();
        }
    }

    public static RenderType getRenderType(ItemStack param0, boolean param1) {
        Item var0 = param0.getItem();
        if (var0 instanceof BlockItem) {
            Block var1 = ((BlockItem)var0).getBlock();
            return getRenderType(var1.defaultBlockState(), param1);
        } else {
            return param1 ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
        }
    }

    public static RenderType getRenderLayer(FluidState param0) {
        RenderType var0 = TYPE_BY_FLUID.get(param0.getType());
        return var0 != null ? var0 : RenderType.solid();
    }

    public static void setFancy(boolean param0) {
        renderCutout = param0;
    }
}
