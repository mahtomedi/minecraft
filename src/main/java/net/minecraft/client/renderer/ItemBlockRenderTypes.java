package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        RenderType var0 = RenderType.cutoutMipped();
        param0.put(Blocks.GRASS_BLOCK, var0);
        param0.put(Blocks.IRON_BARS, var0);
        param0.put(Blocks.GLASS_PANE, var0);
        param0.put(Blocks.TRIPWIRE_HOOK, var0);
        param0.put(Blocks.HOPPER, var0);
        param0.put(Blocks.JUNGLE_LEAVES, var0);
        param0.put(Blocks.OAK_LEAVES, var0);
        param0.put(Blocks.SPRUCE_LEAVES, var0);
        param0.put(Blocks.ACACIA_LEAVES, var0);
        param0.put(Blocks.BIRCH_LEAVES, var0);
        param0.put(Blocks.DARK_OAK_LEAVES, var0);
        RenderType var1 = RenderType.cutout();
        param0.put(Blocks.OAK_SAPLING, var1);
        param0.put(Blocks.SPRUCE_SAPLING, var1);
        param0.put(Blocks.BIRCH_SAPLING, var1);
        param0.put(Blocks.JUNGLE_SAPLING, var1);
        param0.put(Blocks.ACACIA_SAPLING, var1);
        param0.put(Blocks.DARK_OAK_SAPLING, var1);
        param0.put(Blocks.GLASS, var1);
        param0.put(Blocks.WHITE_BED, var1);
        param0.put(Blocks.ORANGE_BED, var1);
        param0.put(Blocks.MAGENTA_BED, var1);
        param0.put(Blocks.LIGHT_BLUE_BED, var1);
        param0.put(Blocks.YELLOW_BED, var1);
        param0.put(Blocks.LIME_BED, var1);
        param0.put(Blocks.PINK_BED, var1);
        param0.put(Blocks.GRAY_BED, var1);
        param0.put(Blocks.LIGHT_GRAY_BED, var1);
        param0.put(Blocks.CYAN_BED, var1);
        param0.put(Blocks.PURPLE_BED, var1);
        param0.put(Blocks.BLUE_BED, var1);
        param0.put(Blocks.BROWN_BED, var1);
        param0.put(Blocks.GREEN_BED, var1);
        param0.put(Blocks.RED_BED, var1);
        param0.put(Blocks.BLACK_BED, var1);
        param0.put(Blocks.POWERED_RAIL, var1);
        param0.put(Blocks.DETECTOR_RAIL, var1);
        param0.put(Blocks.COBWEB, var1);
        param0.put(Blocks.GRASS, var1);
        param0.put(Blocks.FERN, var1);
        param0.put(Blocks.DEAD_BUSH, var1);
        param0.put(Blocks.SEAGRASS, var1);
        param0.put(Blocks.TALL_SEAGRASS, var1);
        param0.put(Blocks.DANDELION, var1);
        param0.put(Blocks.POPPY, var1);
        param0.put(Blocks.BLUE_ORCHID, var1);
        param0.put(Blocks.ALLIUM, var1);
        param0.put(Blocks.AZURE_BLUET, var1);
        param0.put(Blocks.RED_TULIP, var1);
        param0.put(Blocks.ORANGE_TULIP, var1);
        param0.put(Blocks.WHITE_TULIP, var1);
        param0.put(Blocks.PINK_TULIP, var1);
        param0.put(Blocks.OXEYE_DAISY, var1);
        param0.put(Blocks.CORNFLOWER, var1);
        param0.put(Blocks.WITHER_ROSE, var1);
        param0.put(Blocks.LILY_OF_THE_VALLEY, var1);
        param0.put(Blocks.BROWN_MUSHROOM, var1);
        param0.put(Blocks.RED_MUSHROOM, var1);
        param0.put(Blocks.TORCH, var1);
        param0.put(Blocks.WALL_TORCH, var1);
        param0.put(Blocks.FIRE, var1);
        param0.put(Blocks.SPAWNER, var1);
        param0.put(Blocks.REDSTONE_WIRE, var1);
        param0.put(Blocks.WHEAT, var1);
        param0.put(Blocks.OAK_DOOR, var1);
        param0.put(Blocks.LADDER, var1);
        param0.put(Blocks.RAIL, var1);
        param0.put(Blocks.IRON_DOOR, var1);
        param0.put(Blocks.REDSTONE_TORCH, var1);
        param0.put(Blocks.REDSTONE_WALL_TORCH, var1);
        param0.put(Blocks.CACTUS, var1);
        param0.put(Blocks.SUGAR_CANE, var1);
        param0.put(Blocks.REPEATER, var1);
        param0.put(Blocks.OAK_TRAPDOOR, var1);
        param0.put(Blocks.SPRUCE_TRAPDOOR, var1);
        param0.put(Blocks.BIRCH_TRAPDOOR, var1);
        param0.put(Blocks.JUNGLE_TRAPDOOR, var1);
        param0.put(Blocks.ACACIA_TRAPDOOR, var1);
        param0.put(Blocks.DARK_OAK_TRAPDOOR, var1);
        param0.put(Blocks.ATTACHED_PUMPKIN_STEM, var1);
        param0.put(Blocks.ATTACHED_MELON_STEM, var1);
        param0.put(Blocks.PUMPKIN_STEM, var1);
        param0.put(Blocks.MELON_STEM, var1);
        param0.put(Blocks.VINE, var1);
        param0.put(Blocks.LILY_PAD, var1);
        param0.put(Blocks.NETHER_WART, var1);
        param0.put(Blocks.BREWING_STAND, var1);
        param0.put(Blocks.COCOA, var1);
        param0.put(Blocks.BEACON, var1);
        param0.put(Blocks.FLOWER_POT, var1);
        param0.put(Blocks.POTTED_OAK_SAPLING, var1);
        param0.put(Blocks.POTTED_SPRUCE_SAPLING, var1);
        param0.put(Blocks.POTTED_BIRCH_SAPLING, var1);
        param0.put(Blocks.POTTED_JUNGLE_SAPLING, var1);
        param0.put(Blocks.POTTED_ACACIA_SAPLING, var1);
        param0.put(Blocks.POTTED_DARK_OAK_SAPLING, var1);
        param0.put(Blocks.POTTED_FERN, var1);
        param0.put(Blocks.POTTED_DANDELION, var1);
        param0.put(Blocks.POTTED_POPPY, var1);
        param0.put(Blocks.POTTED_BLUE_ORCHID, var1);
        param0.put(Blocks.POTTED_ALLIUM, var1);
        param0.put(Blocks.POTTED_AZURE_BLUET, var1);
        param0.put(Blocks.POTTED_RED_TULIP, var1);
        param0.put(Blocks.POTTED_ORANGE_TULIP, var1);
        param0.put(Blocks.POTTED_WHITE_TULIP, var1);
        param0.put(Blocks.POTTED_PINK_TULIP, var1);
        param0.put(Blocks.POTTED_OXEYE_DAISY, var1);
        param0.put(Blocks.POTTED_CORNFLOWER, var1);
        param0.put(Blocks.POTTED_LILY_OF_THE_VALLEY, var1);
        param0.put(Blocks.POTTED_WITHER_ROSE, var1);
        param0.put(Blocks.POTTED_RED_MUSHROOM, var1);
        param0.put(Blocks.POTTED_BROWN_MUSHROOM, var1);
        param0.put(Blocks.POTTED_DEAD_BUSH, var1);
        param0.put(Blocks.POTTED_CACTUS, var1);
        param0.put(Blocks.CARROTS, var1);
        param0.put(Blocks.POTATOES, var1);
        param0.put(Blocks.COMPARATOR, var1);
        param0.put(Blocks.ACTIVATOR_RAIL, var1);
        param0.put(Blocks.IRON_TRAPDOOR, var1);
        param0.put(Blocks.SUNFLOWER, var1);
        param0.put(Blocks.LILAC, var1);
        param0.put(Blocks.ROSE_BUSH, var1);
        param0.put(Blocks.PEONY, var1);
        param0.put(Blocks.TALL_GRASS, var1);
        param0.put(Blocks.LARGE_FERN, var1);
        param0.put(Blocks.SPRUCE_DOOR, var1);
        param0.put(Blocks.BIRCH_DOOR, var1);
        param0.put(Blocks.JUNGLE_DOOR, var1);
        param0.put(Blocks.ACACIA_DOOR, var1);
        param0.put(Blocks.DARK_OAK_DOOR, var1);
        param0.put(Blocks.END_ROD, var1);
        param0.put(Blocks.CHORUS_PLANT, var1);
        param0.put(Blocks.CHORUS_FLOWER, var1);
        param0.put(Blocks.BEETROOTS, var1);
        param0.put(Blocks.KELP, var1);
        param0.put(Blocks.KELP_PLANT, var1);
        param0.put(Blocks.TURTLE_EGG, var1);
        param0.put(Blocks.DEAD_TUBE_CORAL, var1);
        param0.put(Blocks.DEAD_BRAIN_CORAL, var1);
        param0.put(Blocks.DEAD_BUBBLE_CORAL, var1);
        param0.put(Blocks.DEAD_FIRE_CORAL, var1);
        param0.put(Blocks.DEAD_HORN_CORAL, var1);
        param0.put(Blocks.TUBE_CORAL, var1);
        param0.put(Blocks.BRAIN_CORAL, var1);
        param0.put(Blocks.BUBBLE_CORAL, var1);
        param0.put(Blocks.FIRE_CORAL, var1);
        param0.put(Blocks.HORN_CORAL, var1);
        param0.put(Blocks.DEAD_TUBE_CORAL_FAN, var1);
        param0.put(Blocks.DEAD_BRAIN_CORAL_FAN, var1);
        param0.put(Blocks.DEAD_BUBBLE_CORAL_FAN, var1);
        param0.put(Blocks.DEAD_FIRE_CORAL_FAN, var1);
        param0.put(Blocks.DEAD_HORN_CORAL_FAN, var1);
        param0.put(Blocks.TUBE_CORAL_FAN, var1);
        param0.put(Blocks.BRAIN_CORAL_FAN, var1);
        param0.put(Blocks.BUBBLE_CORAL_FAN, var1);
        param0.put(Blocks.FIRE_CORAL_FAN, var1);
        param0.put(Blocks.HORN_CORAL_FAN, var1);
        param0.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, var1);
        param0.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, var1);
        param0.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, var1);
        param0.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, var1);
        param0.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, var1);
        param0.put(Blocks.TUBE_CORAL_WALL_FAN, var1);
        param0.put(Blocks.BRAIN_CORAL_WALL_FAN, var1);
        param0.put(Blocks.BUBBLE_CORAL_WALL_FAN, var1);
        param0.put(Blocks.FIRE_CORAL_WALL_FAN, var1);
        param0.put(Blocks.HORN_CORAL_WALL_FAN, var1);
        param0.put(Blocks.SEA_PICKLE, var1);
        param0.put(Blocks.CONDUIT, var1);
        param0.put(Blocks.BAMBOO_SAPLING, var1);
        param0.put(Blocks.BAMBOO, var1);
        param0.put(Blocks.POTTED_BAMBOO, var1);
        param0.put(Blocks.SCAFFOLDING, var1);
        param0.put(Blocks.STONECUTTER, var1);
        param0.put(Blocks.LANTERN, var1);
        param0.put(Blocks.CAMPFIRE, var1);
        param0.put(Blocks.SWEET_BERRY_BUSH, var1);
        RenderType var2 = RenderType.translucent();
        param0.put(Blocks.ICE, var2);
        param0.put(Blocks.NETHER_PORTAL, var2);
        param0.put(Blocks.WHITE_STAINED_GLASS, var2);
        param0.put(Blocks.ORANGE_STAINED_GLASS, var2);
        param0.put(Blocks.MAGENTA_STAINED_GLASS, var2);
        param0.put(Blocks.LIGHT_BLUE_STAINED_GLASS, var2);
        param0.put(Blocks.YELLOW_STAINED_GLASS, var2);
        param0.put(Blocks.LIME_STAINED_GLASS, var2);
        param0.put(Blocks.PINK_STAINED_GLASS, var2);
        param0.put(Blocks.GRAY_STAINED_GLASS, var2);
        param0.put(Blocks.LIGHT_GRAY_STAINED_GLASS, var2);
        param0.put(Blocks.CYAN_STAINED_GLASS, var2);
        param0.put(Blocks.PURPLE_STAINED_GLASS, var2);
        param0.put(Blocks.BLUE_STAINED_GLASS, var2);
        param0.put(Blocks.BROWN_STAINED_GLASS, var2);
        param0.put(Blocks.GREEN_STAINED_GLASS, var2);
        param0.put(Blocks.RED_STAINED_GLASS, var2);
        param0.put(Blocks.BLACK_STAINED_GLASS, var2);
        param0.put(Blocks.TRIPWIRE, var2);
        param0.put(Blocks.WHITE_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.ORANGE_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.MAGENTA_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.YELLOW_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.LIME_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.PINK_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.GRAY_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.CYAN_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.PURPLE_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.BLUE_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.BROWN_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.GREEN_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.RED_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.BLACK_STAINED_GLASS_PANE, var2);
        param0.put(Blocks.SLIME_BLOCK, var2);
        param0.put(Blocks.HONEY_BLOCK, var2);
        param0.put(Blocks.FROSTED_ICE, var2);
        param0.put(Blocks.BUBBLE_COLUMN, var2);
    });
    private static final Map<Item, RenderType> TYPE_BY_ITEM = Util.make(Maps.newHashMap(), param0 -> {
        RenderType var0 = RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
        param0.put(Items.LEVER, var0);
        param0.put(Items.OAK_SIGN, var0);
        param0.put(Items.DARK_OAK_SIGN, var0);
        param0.put(Items.ACACIA_SIGN, var0);
        param0.put(Items.BIRCH_SIGN, var0);
        param0.put(Items.JUNGLE_SIGN, var0);
        param0.put(Items.SPRUCE_SIGN, var0);
        param0.put(Items.CAKE, var0);
        param0.put(Items.CAULDRON, var0);
        param0.put(Items.BELL, var0);
        param0.put(Items.BARRIER, var0);
        param0.put(Items.STRUCTURE_VOID, var0);
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

    public static RenderType getRenderType(BlockState param0) {
        RenderType var0 = getChunkRenderType(param0);
        if (var0 == RenderType.translucent()) {
            return RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
        } else {
            return var0 != RenderType.cutout() && var0 != RenderType.cutoutMipped()
                ? RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)
                : RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
        }
    }

    public static RenderType getRenderType(ItemStack param0) {
        Item var0 = param0.getItem();
        RenderType var1 = TYPE_BY_ITEM.get(var0);
        if (var1 != null) {
            return var1;
        } else if (var0 instanceof BlockItem) {
            Block var2 = ((BlockItem)var0).getBlock();
            return getRenderType(var2.defaultBlockState());
        } else {
            return RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
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
