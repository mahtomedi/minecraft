package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.TextureObject;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
public class RenderType {
    private static final Set<RenderType> LAYERS = Sets.newHashSet();
    public static final RenderType SOLID = register(new RenderType("solid", 2097152, () -> {
    }, () -> {
    }));
    public static final RenderType CUTOUT_MIPPED = register(new RenderType("cutout_mipped", 131072, () -> {
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(516, 0.5F);
    }, () -> {
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultAlphaFunc();
    }));
    public static final RenderType CUTOUT = register(new RenderType("cutout", 131072, () -> {
        TextureObject var0 = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        var0.bind();
        var0.pushFilter(false, false);
        CUTOUT_MIPPED.setupRenderState();
    }, () -> {
        TextureObject var0 = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        var0.bind();
        var0.popFilter();
        CUTOUT_MIPPED.clearRenderState();
    }));
    public static final RenderType TRANSLUCENT = register(new RenderType("translucent", 262144, () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }));
    public static final RenderType ENTITY = register(new RenderType("entity", 262144, () -> {
        TextureObject var0 = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        var0.bind();
        Lighting.turnOff();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.enableBlend();
        if (Minecraft.useAmbientOcclusion()) {
            RenderSystem.shadeModel(7425);
        } else {
            RenderSystem.shadeModel(7424);
        }

    }, () -> Lighting.turnOn()));
    public static final RenderType CRUMBLING = register(
        new RenderType(
            "crumbling",
            262144,
            () -> {
                RenderSystem.polygonOffset(-1.0F, -10.0F);
                RenderSystem.enablePolygonOffset();
                RenderSystem.defaultAlphaFunc();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
                );
            },
            () -> {
                RenderSystem.disableAlphaTest();
                RenderSystem.polygonOffset(0.0F, 0.0F);
                RenderSystem.disablePolygonOffset();
                RenderSystem.enableAlphaTest();
                RenderSystem.disableBlend();
            }
        )
    );
    private static boolean renderCutout;
    private static final Map<Block, RenderType> TYPE_BY_BLOCK = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(Blocks.GRASS_BLOCK, CUTOUT_MIPPED);
        param0.put(Blocks.IRON_BARS, CUTOUT_MIPPED);
        param0.put(Blocks.GLASS_PANE, CUTOUT_MIPPED);
        param0.put(Blocks.TRIPWIRE_HOOK, CUTOUT_MIPPED);
        param0.put(Blocks.HOPPER, CUTOUT_MIPPED);
        param0.put(Blocks.JUNGLE_LEAVES, CUTOUT_MIPPED);
        param0.put(Blocks.OAK_LEAVES, CUTOUT_MIPPED);
        param0.put(Blocks.SPRUCE_LEAVES, CUTOUT_MIPPED);
        param0.put(Blocks.ACACIA_LEAVES, CUTOUT_MIPPED);
        param0.put(Blocks.BIRCH_LEAVES, CUTOUT_MIPPED);
        param0.put(Blocks.DARK_OAK_LEAVES, CUTOUT_MIPPED);
        param0.put(Blocks.OAK_SAPLING, CUTOUT);
        param0.put(Blocks.SPRUCE_SAPLING, CUTOUT);
        param0.put(Blocks.BIRCH_SAPLING, CUTOUT);
        param0.put(Blocks.JUNGLE_SAPLING, CUTOUT);
        param0.put(Blocks.ACACIA_SAPLING, CUTOUT);
        param0.put(Blocks.DARK_OAK_SAPLING, CUTOUT);
        param0.put(Blocks.GLASS, CUTOUT);
        param0.put(Blocks.WHITE_BED, CUTOUT);
        param0.put(Blocks.ORANGE_BED, CUTOUT);
        param0.put(Blocks.MAGENTA_BED, CUTOUT);
        param0.put(Blocks.LIGHT_BLUE_BED, CUTOUT);
        param0.put(Blocks.YELLOW_BED, CUTOUT);
        param0.put(Blocks.LIME_BED, CUTOUT);
        param0.put(Blocks.PINK_BED, CUTOUT);
        param0.put(Blocks.GRAY_BED, CUTOUT);
        param0.put(Blocks.LIGHT_GRAY_BED, CUTOUT);
        param0.put(Blocks.CYAN_BED, CUTOUT);
        param0.put(Blocks.PURPLE_BED, CUTOUT);
        param0.put(Blocks.BLUE_BED, CUTOUT);
        param0.put(Blocks.BROWN_BED, CUTOUT);
        param0.put(Blocks.GREEN_BED, CUTOUT);
        param0.put(Blocks.RED_BED, CUTOUT);
        param0.put(Blocks.BLACK_BED, CUTOUT);
        param0.put(Blocks.POWERED_RAIL, CUTOUT);
        param0.put(Blocks.DETECTOR_RAIL, CUTOUT);
        param0.put(Blocks.COBWEB, CUTOUT);
        param0.put(Blocks.GRASS, CUTOUT);
        param0.put(Blocks.FERN, CUTOUT);
        param0.put(Blocks.DEAD_BUSH, CUTOUT);
        param0.put(Blocks.SEAGRASS, CUTOUT);
        param0.put(Blocks.TALL_SEAGRASS, CUTOUT);
        param0.put(Blocks.DANDELION, CUTOUT);
        param0.put(Blocks.POPPY, CUTOUT);
        param0.put(Blocks.BLUE_ORCHID, CUTOUT);
        param0.put(Blocks.ALLIUM, CUTOUT);
        param0.put(Blocks.AZURE_BLUET, CUTOUT);
        param0.put(Blocks.RED_TULIP, CUTOUT);
        param0.put(Blocks.ORANGE_TULIP, CUTOUT);
        param0.put(Blocks.WHITE_TULIP, CUTOUT);
        param0.put(Blocks.PINK_TULIP, CUTOUT);
        param0.put(Blocks.OXEYE_DAISY, CUTOUT);
        param0.put(Blocks.CORNFLOWER, CUTOUT);
        param0.put(Blocks.WITHER_ROSE, CUTOUT);
        param0.put(Blocks.LILY_OF_THE_VALLEY, CUTOUT);
        param0.put(Blocks.BROWN_MUSHROOM, CUTOUT);
        param0.put(Blocks.RED_MUSHROOM, CUTOUT);
        param0.put(Blocks.TORCH, CUTOUT);
        param0.put(Blocks.WALL_TORCH, CUTOUT);
        param0.put(Blocks.FIRE, CUTOUT);
        param0.put(Blocks.SPAWNER, CUTOUT);
        param0.put(Blocks.REDSTONE_WIRE, CUTOUT);
        param0.put(Blocks.WHEAT, CUTOUT);
        param0.put(Blocks.OAK_DOOR, CUTOUT);
        param0.put(Blocks.LADDER, CUTOUT);
        param0.put(Blocks.RAIL, CUTOUT);
        param0.put(Blocks.IRON_DOOR, CUTOUT);
        param0.put(Blocks.REDSTONE_TORCH, CUTOUT);
        param0.put(Blocks.REDSTONE_WALL_TORCH, CUTOUT);
        param0.put(Blocks.CACTUS, CUTOUT);
        param0.put(Blocks.SUGAR_CANE, CUTOUT);
        param0.put(Blocks.REPEATER, CUTOUT);
        param0.put(Blocks.OAK_TRAPDOOR, CUTOUT);
        param0.put(Blocks.SPRUCE_TRAPDOOR, CUTOUT);
        param0.put(Blocks.BIRCH_TRAPDOOR, CUTOUT);
        param0.put(Blocks.JUNGLE_TRAPDOOR, CUTOUT);
        param0.put(Blocks.ACACIA_TRAPDOOR, CUTOUT);
        param0.put(Blocks.DARK_OAK_TRAPDOOR, CUTOUT);
        param0.put(Blocks.ATTACHED_PUMPKIN_STEM, CUTOUT);
        param0.put(Blocks.ATTACHED_MELON_STEM, CUTOUT);
        param0.put(Blocks.PUMPKIN_STEM, CUTOUT);
        param0.put(Blocks.MELON_STEM, CUTOUT);
        param0.put(Blocks.VINE, CUTOUT);
        param0.put(Blocks.LILY_PAD, CUTOUT);
        param0.put(Blocks.NETHER_WART, CUTOUT);
        param0.put(Blocks.BREWING_STAND, CUTOUT);
        param0.put(Blocks.COCOA, CUTOUT);
        param0.put(Blocks.BEACON, CUTOUT);
        param0.put(Blocks.FLOWER_POT, CUTOUT);
        param0.put(Blocks.POTTED_OAK_SAPLING, CUTOUT);
        param0.put(Blocks.POTTED_SPRUCE_SAPLING, CUTOUT);
        param0.put(Blocks.POTTED_BIRCH_SAPLING, CUTOUT);
        param0.put(Blocks.POTTED_JUNGLE_SAPLING, CUTOUT);
        param0.put(Blocks.POTTED_ACACIA_SAPLING, CUTOUT);
        param0.put(Blocks.POTTED_DARK_OAK_SAPLING, CUTOUT);
        param0.put(Blocks.POTTED_FERN, CUTOUT);
        param0.put(Blocks.POTTED_DANDELION, CUTOUT);
        param0.put(Blocks.POTTED_POPPY, CUTOUT);
        param0.put(Blocks.POTTED_BLUE_ORCHID, CUTOUT);
        param0.put(Blocks.POTTED_ALLIUM, CUTOUT);
        param0.put(Blocks.POTTED_AZURE_BLUET, CUTOUT);
        param0.put(Blocks.POTTED_RED_TULIP, CUTOUT);
        param0.put(Blocks.POTTED_ORANGE_TULIP, CUTOUT);
        param0.put(Blocks.POTTED_WHITE_TULIP, CUTOUT);
        param0.put(Blocks.POTTED_PINK_TULIP, CUTOUT);
        param0.put(Blocks.POTTED_OXEYE_DAISY, CUTOUT);
        param0.put(Blocks.POTTED_CORNFLOWER, CUTOUT);
        param0.put(Blocks.POTTED_LILY_OF_THE_VALLEY, CUTOUT);
        param0.put(Blocks.POTTED_WITHER_ROSE, CUTOUT);
        param0.put(Blocks.POTTED_RED_MUSHROOM, CUTOUT);
        param0.put(Blocks.POTTED_BROWN_MUSHROOM, CUTOUT);
        param0.put(Blocks.POTTED_DEAD_BUSH, CUTOUT);
        param0.put(Blocks.POTTED_CACTUS, CUTOUT);
        param0.put(Blocks.CARROTS, CUTOUT);
        param0.put(Blocks.POTATOES, CUTOUT);
        param0.put(Blocks.COMPARATOR, CUTOUT);
        param0.put(Blocks.ACTIVATOR_RAIL, CUTOUT);
        param0.put(Blocks.IRON_TRAPDOOR, CUTOUT);
        param0.put(Blocks.SUNFLOWER, CUTOUT);
        param0.put(Blocks.LILAC, CUTOUT);
        param0.put(Blocks.ROSE_BUSH, CUTOUT);
        param0.put(Blocks.PEONY, CUTOUT);
        param0.put(Blocks.TALL_GRASS, CUTOUT);
        param0.put(Blocks.LARGE_FERN, CUTOUT);
        param0.put(Blocks.SPRUCE_DOOR, CUTOUT);
        param0.put(Blocks.BIRCH_DOOR, CUTOUT);
        param0.put(Blocks.JUNGLE_DOOR, CUTOUT);
        param0.put(Blocks.ACACIA_DOOR, CUTOUT);
        param0.put(Blocks.DARK_OAK_DOOR, CUTOUT);
        param0.put(Blocks.END_ROD, CUTOUT);
        param0.put(Blocks.CHORUS_PLANT, CUTOUT);
        param0.put(Blocks.CHORUS_FLOWER, CUTOUT);
        param0.put(Blocks.BEETROOTS, CUTOUT);
        param0.put(Blocks.KELP, CUTOUT);
        param0.put(Blocks.KELP_PLANT, CUTOUT);
        param0.put(Blocks.TURTLE_EGG, CUTOUT);
        param0.put(Blocks.DEAD_TUBE_CORAL, CUTOUT);
        param0.put(Blocks.DEAD_BRAIN_CORAL, CUTOUT);
        param0.put(Blocks.DEAD_BUBBLE_CORAL, CUTOUT);
        param0.put(Blocks.DEAD_FIRE_CORAL, CUTOUT);
        param0.put(Blocks.DEAD_HORN_CORAL, CUTOUT);
        param0.put(Blocks.TUBE_CORAL, CUTOUT);
        param0.put(Blocks.BRAIN_CORAL, CUTOUT);
        param0.put(Blocks.BUBBLE_CORAL, CUTOUT);
        param0.put(Blocks.FIRE_CORAL, CUTOUT);
        param0.put(Blocks.HORN_CORAL, CUTOUT);
        param0.put(Blocks.DEAD_TUBE_CORAL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_BRAIN_CORAL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_BUBBLE_CORAL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_FIRE_CORAL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_HORN_CORAL_FAN, CUTOUT);
        param0.put(Blocks.TUBE_CORAL_FAN, CUTOUT);
        param0.put(Blocks.BRAIN_CORAL_FAN, CUTOUT);
        param0.put(Blocks.BUBBLE_CORAL_FAN, CUTOUT);
        param0.put(Blocks.FIRE_CORAL_FAN, CUTOUT);
        param0.put(Blocks.HORN_CORAL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.TUBE_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.BRAIN_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.BUBBLE_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.FIRE_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.HORN_CORAL_WALL_FAN, CUTOUT);
        param0.put(Blocks.SEA_PICKLE, CUTOUT);
        param0.put(Blocks.CONDUIT, CUTOUT);
        param0.put(Blocks.BAMBOO_SAPLING, CUTOUT);
        param0.put(Blocks.BAMBOO, CUTOUT);
        param0.put(Blocks.POTTED_BAMBOO, CUTOUT);
        param0.put(Blocks.SCAFFOLDING, CUTOUT);
        param0.put(Blocks.STONECUTTER, CUTOUT);
        param0.put(Blocks.LANTERN, CUTOUT);
        param0.put(Blocks.CAMPFIRE, CUTOUT);
        param0.put(Blocks.SWEET_BERRY_BUSH, CUTOUT);
        param0.put(Blocks.ICE, TRANSLUCENT);
        param0.put(Blocks.NETHER_PORTAL, TRANSLUCENT);
        param0.put(Blocks.WHITE_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.ORANGE_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.MAGENTA_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.LIGHT_BLUE_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.YELLOW_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.LIME_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.PINK_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.GRAY_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.LIGHT_GRAY_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.CYAN_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.PURPLE_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.BLUE_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.BROWN_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.GREEN_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.RED_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.BLACK_STAINED_GLASS, TRANSLUCENT);
        param0.put(Blocks.TRIPWIRE, TRANSLUCENT);
        param0.put(Blocks.WHITE_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.ORANGE_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.MAGENTA_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.YELLOW_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.LIME_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.PINK_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.GRAY_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.CYAN_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.PURPLE_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.BLUE_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.BROWN_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.GREEN_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.RED_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.BLACK_STAINED_GLASS_PANE, TRANSLUCENT);
        param0.put(Blocks.SLIME_BLOCK, TRANSLUCENT);
        param0.put(Blocks.FROSTED_ICE, TRANSLUCENT);
        param0.put(Blocks.BUBBLE_COLUMN, TRANSLUCENT);
    });
    private static final Map<Fluid, RenderType> TYPE_BY_FLUID = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(Fluids.FLOWING_WATER, TRANSLUCENT);
        param0.put(Fluids.WATER, TRANSLUCENT);
    });
    private final String name;
    private final int bufferSize;
    private final Runnable setupState;
    private final Runnable clearState;

    private static RenderType register(RenderType param0) {
        LAYERS.add(param0);
        return param0;
    }

    RenderType(String param0, int param1, Runnable param2, Runnable param3) {
        this.name = param0;
        this.bufferSize = param1;
        this.setupState = param2;
        this.clearState = param3;
    }

    public static void setFancy(boolean param0) {
        renderCutout = param0;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static RenderType getRenderLayer(BlockState param0) {
        Block var0 = param0.getBlock();
        if (var0 instanceof LeavesBlock) {
            return renderCutout ? CUTOUT_MIPPED : SOLID;
        } else {
            RenderType var1 = TYPE_BY_BLOCK.get(var0);
            return var1 != null ? var1 : SOLID;
        }
    }

    public static RenderType getRenderLayer(FluidState param0) {
        RenderType var0 = TYPE_BY_FLUID.get(param0.getType());
        return var0 != null ? var0 : SOLID;
    }

    public static Set<RenderType> chunkBufferLayers() {
        return ImmutableSet.of(SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public void setupRenderState() {
        this.setupState.run();
    }

    public void clearRenderState() {
        this.clearState.run();
    }
}
