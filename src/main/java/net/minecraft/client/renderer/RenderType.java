package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
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
    public static final RenderType SOLID = new RenderType("solid", DefaultVertexFormat.BLOCK, 7, 2097152, false, true, () -> {
        RenderSystem.enableTexture();
        Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableCull();
        RenderSystem.shadeModel(7425);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthFunc(515);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
    }, () -> {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.shadeModel(7424);
    });
    public static final RenderType CUTOUT_MIPPED = new RenderType("cutout_mipped", DefaultVertexFormat.BLOCK, 7, 131072, false, true, () -> {
        Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(516, 0.5F);
        RenderSystem.shadeModel(7425);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
    }, () -> {
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.shadeModel(7424);
    });
    public static final RenderType CUTOUT = new RenderType("cutout", DefaultVertexFormat.BLOCK, 7, 131072, false, true, () -> {
        AbstractTexture var0 = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        var0.bind();
        var0.pushFilter(false, false);
        CUTOUT_MIPPED.setupRenderState();
    }, () -> {
        AbstractTexture var0 = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        var0.bind();
        var0.popFilter();
        CUTOUT_MIPPED.clearRenderState();
    });
    public static final RenderType TRANSLUCENT = new RenderType("translucent", DefaultVertexFormat.BLOCK, 7, 262144, false, true, () -> {
        Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
    }, () -> {
        RenderSystem.disableBlend();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.shadeModel(7424);
    });
    public static final RenderType TRANSLUCENT_NO_CRUMBLING = new RenderType(
        "translucent_no_crumbling", DefaultVertexFormat.BLOCK, 7, 256, false, false, TRANSLUCENT::setupRenderState, TRANSLUCENT::clearRenderState
    );
    public static final RenderType LEASH = new RenderType("leash", DefaultVertexFormat.POSITION_COLOR, 7, 256, false, false, () -> {
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
    }, () -> {
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
    });
    public static final RenderType WATER_MASK = new RenderType("water_mask", DefaultVertexFormat.BLOCK, 7, 256, false, false, () -> {
        RenderSystem.disableTexture();
        RenderSystem.colorMask(false, false, false, false);
    }, () -> {
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableTexture();
    });
    public static final RenderType OUTLINE = new RenderType("outline", DefaultVertexFormat.POSITION_COLOR, 7, 256, false, false, () -> {
        RenderSystem.depthFunc(519);
        RenderSystem.disableTexture();
        RenderSystem.disableFog();
        Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false);
    }, () -> {
        RenderSystem.depthFunc(515);
        RenderSystem.enableTexture();
        RenderSystem.enableFog();
    });
    public static final RenderType GLINT = new RenderType(
        "glint", DefaultVertexFormat.POSITION_TEX, 7, 256, false, false, () -> setupGlint(8.0F), () -> clearGlint()
    );
    public static final RenderType ENTITY_GLINT = new RenderType(
        "entity_glint", DefaultVertexFormat.POSITION_TEX, 7, 256, false, false, () -> setupGlint(0.16F), () -> clearGlint()
    );
    public static final RenderType BEACON_BEAM = new RenderType("beacon_beam", DefaultVertexFormat.BLOCK, 7, 256, false, false, () -> {
        RenderSystem.defaultAlphaFunc();
        Minecraft.getInstance().getTextureManager().bind(BeaconRenderer.BEAM_LOCATION);
        RenderSystem.texParameter(3553, 10242, 10497);
        RenderSystem.texParameter(3553, 10243, 10497);
        RenderSystem.disableFog();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
    }, () -> {
        RenderSystem.enableFog();
        RenderSystem.depthMask(true);
    });
    public static final RenderType LIGHTNING = new RenderType("lightning", DefaultVertexFormat.POSITION_COLOR, 7, 256, false, false, () -> {
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.shadeModel(7425);
        RenderSystem.disableAlphaTest();
    }, () -> {
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
    });
    public static final RenderType LINES = new RenderType("lines", DefaultVertexFormat.POSITION_COLOR, 1, 256, false, false, () -> {
        RenderSystem.disableAlphaTest();
        RenderSystem.lineWidth(Math.max(2.5F, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F));
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.0F, 1.0F, 0.999F);
        RenderSystem.matrixMode(5888);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, () -> {
        RenderSystem.matrixMode(5889);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    });
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
    private final VertexFormat format;
    private final int mode;
    private final int bufferSize;
    private final Runnable setupState;
    private final Runnable clearState;
    private final boolean affectsEntityOutline;
    private final boolean affectsCrumbling;

    public static RenderType NEW_ENTITY(ResourceLocation param0) {
        return NEW_ENTITY(param0, false, true, false);
    }

    public static RenderType NEW_ENTITY(ResourceLocation param0, boolean param1, boolean param2, boolean param3) {
        return NEW_ENTITY(param0, param1, param2, param3, 0.1F, false);
    }

    public static RenderType NEW_ENTITY(ResourceLocation param0, boolean param1, boolean param2, boolean param3, float param4, boolean param5) {
        return new RenderType.StatefullRenderType<>(
            "new_entity",
            DefaultVertexFormat.NEW_ENTITY,
            256,
            new RenderType.EntityState(param0, param1, param2, param3, param4, param5),
            true,
            false,
            param0x -> {
                RenderSystem.disableCull();
                RenderSystem.enableRescaleNormal();
                RenderSystem.shadeModel(param0x.smoothShading ? 7425 : 7424);
                Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
                Minecraft.getInstance().getTextureManager().bind(param0x.texture);
                RenderSystem.texParameter(3553, 10241, 9728);
                RenderSystem.texParameter(3553, 10240, 9728);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                if (param0x.forceTranslucent) {
                    RenderSystem.depthMask(false);
                    RenderSystem.enableBlend();
                    RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 0.15F);
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
                }
    
                if (param0x.alphaCutoff <= 0.0F) {
                    RenderSystem.disableAlphaTest();
                } else {
                    RenderSystem.enableAlphaTest();
                    RenderSystem.alphaFunc(516, param0x.alphaCutoff);
                }
    
                if (param0x.lighting) {
                    Lighting.turnBackOn();
                }
    
                if (param0x.equalDepth) {
                    RenderSystem.depthFunc(514);
                }
    
            },
            param0x -> {
                RenderSystem.shadeModel(7424);
                Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
                Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
                RenderSystem.enableCull();
                RenderSystem.cullFace(GlStateManager.CullFace.BACK);
                if (param0x.forceTranslucent) {
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.depthMask(true);
                }
    
                if (param0x.lighting) {
                    Lighting.turnOff();
                }
    
                if (param0x.equalDepth) {
                    RenderSystem.depthFunc(515);
                }
    
                RenderSystem.disableAlphaTest();
                RenderSystem.defaultAlphaFunc();
            }
        );
    }

    public static RenderType EYES(ResourceLocation param0) {
        return new RenderType.StatefullRenderType<>("eyes", DefaultVertexFormat.NEW_ENTITY, 256, param0, false, false, param0x -> {
            Minecraft.getInstance().getTextureManager().bind(param0x);
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            RenderSystem.depthMask(false);
            FogRenderer.resetFogColor(true);
            RenderSystem.enableDepthTest();
        }, param0x -> {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            FogRenderer.resetFogColor(false);
            RenderSystem.defaultBlendFunc();
        });
    }

    public static RenderType POWER_SWIRL(ResourceLocation param0, float param1, float param2) {
        RenderType var0 = NEW_ENTITY(param0);
        return new RenderType.StatefullRenderType<>(
            "power_swirl", DefaultVertexFormat.NEW_ENTITY, 256, new RenderType.SwirlState(param0, param1, param2), false, false, param1x -> {
                var0.setupRenderState();
                RenderSystem.matrixMode(5890);
                RenderSystem.pushMatrix();
                RenderSystem.loadIdentity();
                RenderSystem.translatef(param1x.uOffset, param1x.vOffset, 0.0F);
                RenderSystem.matrixMode(5888);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                FogRenderer.resetFogColor(true);
            }, param1x -> {
                var0.clearRenderState();
                FogRenderer.resetFogColor(false);
                RenderSystem.matrixMode(5890);
                RenderSystem.popMatrix();
                RenderSystem.matrixMode(5888);
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
            }
        );
    }

    public static RenderType CRUMBLING(int param0) {
        return new RenderType.StatefullRenderType<>(
            "crumbling",
            DefaultVertexFormat.BLOCK,
            256,
            param0,
            false,
            false,
            param0x -> {
                Minecraft.getInstance().getTextureManager().bind(ModelBakery.BREAKING_LOCATIONS.get(param0x));
                RenderSystem.polygonOffset(-1.0F, -10.0F);
                RenderSystem.enablePolygonOffset();
                RenderSystem.defaultAlphaFunc();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
                );
            },
            param0x -> {
                RenderSystem.disableAlphaTest();
                RenderSystem.polygonOffset(0.0F, 0.0F);
                RenderSystem.disablePolygonOffset();
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
            }
        );
    }

    public static RenderType TEXT(ResourceLocation param0) {
        return new RenderType.StatefullRenderType<>("text", DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, 256, param0, false, false, param0x -> {
            Minecraft.getInstance().getTextureManager().bind(param0x);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }, param0x -> {
        });
    }

    public static RenderType TEXT_SEE_THROUGH(ResourceLocation param0) {
        RenderType var0 = TEXT(param0);
        return new RenderType.StatefullRenderType<>("text_see_through", DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, 256, param0, false, false, param1 -> {
            var0.setupRenderState();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
        }, param1 -> {
            var0.clearRenderState();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
        });
    }

    public static RenderType PORTAL(int param0) {
        return new RenderType.StatefullRenderType<>(
            "portal",
            DefaultVertexFormat.POSITION_COLOR,
            256,
            param0,
            false,
            false,
            param0x -> {
                RenderSystem.enableBlend();
                if (param0x >= 2) {
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE.value, GlStateManager.DestFactor.ONE.value);
                    Minecraft.getInstance().getTextureManager().bind(TheEndPortalRenderer.END_PORTAL_LOCATION);
                    FogRenderer.resetFogColor(true);
                } else {
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value);
                    Minecraft.getInstance().getTextureManager().bind(TheEndPortalRenderer.END_SKY_LOCATION);
                }
    
                RenderSystem.matrixMode(5890);
                RenderSystem.pushMatrix();
                RenderSystem.loadIdentity();
                RenderSystem.translatef(0.5F, 0.5F, 0.0F);
                RenderSystem.scalef(0.5F, 0.5F, 1.0F);
                RenderSystem.translatef(
                    17.0F / (float)param0x.intValue(), (2.0F + (float)param0x.intValue() / 1.5F) * ((float)(Util.getMillis() % 800000L) / 800000.0F), 0.0F
                );
                RenderSystem.rotatef(((float)(param0x * param0x) * 4321.0F + (float)param0x.intValue() * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.scalef(4.5F - (float)param0x.intValue() / 4.0F, 4.5F - (float)param0x.intValue() / 4.0F, 1.0F);
                RenderSystem.mulTextureByProjModelView();
                RenderSystem.matrixMode(5888);
                RenderSystem.setupEndPortalTexGen();
            },
            param0x -> {
                RenderSystem.defaultBlendFunc();
                RenderSystem.matrixMode(5890);
                RenderSystem.popMatrix();
                RenderSystem.matrixMode(5888);
                RenderSystem.clearTexGen();
                FogRenderer.resetFogColor(false);
            }
        );
    }

    public RenderType(String param0, VertexFormat param1, int param2, int param3, boolean param4, boolean param5, Runnable param6, Runnable param7) {
        this.name = param0;
        this.format = param1;
        this.mode = param2;
        this.bufferSize = param3;
        this.setupState = param6;
        this.clearState = param7;
        this.affectsEntityOutline = param4;
        this.affectsCrumbling = param5;
    }

    public static void setFancy(boolean param0) {
        renderCutout = param0;
    }

    public void end(BufferBuilder param0) {
        if (param0.building()) {
            param0.end();
            this.setupRenderState();
            BufferUploader.end(param0);
            this.clearRenderState();
        }
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

    public static List<RenderType> chunkBufferLayers() {
        return ImmutableList.of(SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);
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

    public VertexFormat format() {
        return this.format;
    }

    public int mode() {
        return this.mode;
    }

    public boolean affectsEntityOutline() {
        return this.affectsEntityOutline;
    }

    public boolean affectsCrumbling() {
        return this.affectsCrumbling;
    }

    @Override
    public boolean equals(@Nullable Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            RenderType var0 = (RenderType)param0;
            return this.name.equals(var0.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    private static void setupGlint(float param0) {
        RenderSystem.enableTexture();
        TextureManager var0 = Minecraft.getInstance().getTextureManager();
        var0.bind(ItemRenderer.ENCHANT_GLINT_LOCATION);
        RenderSystem.texParameter(3553, 10241, 9728);
        RenderSystem.texParameter(3553, 10240, 9728);
        RenderSystem.texParameter(3553, 10242, 10497);
        RenderSystem.texParameter(3553, 10243, 10497);
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(514);
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        RenderSystem.matrixMode(5890);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        long var1 = Util.getMillis() * 8L;
        float var2 = (float)(var1 % 110000L) / 110000.0F;
        float var3 = (float)(var1 % 30000L) / 30000.0F;
        RenderSystem.translatef(-var2, var3, 0.0F);
        RenderSystem.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.scalef(param0, param0, param0);
    }

    private static void clearGlint() {
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class EntityState {
        private final ResourceLocation texture;
        private final boolean forceTranslucent;
        private final boolean lighting;
        private final boolean smoothShading;
        private final float alphaCutoff;
        private final boolean equalDepth;

        public EntityState(ResourceLocation param0, boolean param1, boolean param2, boolean param3, float param4, boolean param5) {
            this.texture = param0;
            this.forceTranslucent = param1;
            this.lighting = param2;
            this.smoothShading = param3;
            this.alphaCutoff = param4;
            this.equalDepth = param5;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderType.EntityState var0 = (RenderType.EntityState)param0;
                return this.forceTranslucent == var0.forceTranslucent && this.lighting == var0.lighting && this.texture.equals(var0.texture);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.texture, this.forceTranslucent, this.lighting);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class StatefullRenderType<S> extends RenderType {
        private final S state;

        public StatefullRenderType(
            String param0, VertexFormat param1, int param2, S param3, boolean param4, boolean param5, Consumer<S> param6, Consumer<S> param7
        ) {
            super(param0, param1, 7, param2, param4, param5, () -> param6.accept(param3), () -> param7.accept(param3));
            this.state = param3;
        }

        @Override
        public boolean equals(@Nullable Object param0) {
            if (!super.equals(param0)) {
                return false;
            } else if (this.getClass() != param0.getClass()) {
                return false;
            } else {
                RenderType.StatefullRenderType<?> var0 = (RenderType.StatefullRenderType)param0;
                return this.state.equals(var0.state);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.state);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class SwirlState {
        private final ResourceLocation texture;
        private final float uOffset;
        private final float vOffset;

        public SwirlState(ResourceLocation param0, float param1, float param2) {
            this.texture = param0;
            this.uOffset = param1;
            this.vOffset = param2;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderType.SwirlState var0 = (RenderType.SwirlState)param0;
                return Float.compare(var0.uOffset, this.uOffset) == 0 && Float.compare(var0.vOffset, this.vOffset) == 0 && this.texture.equals(var0.texture);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.texture, this.uOffset, this.vOffset);
        }
    }
}
