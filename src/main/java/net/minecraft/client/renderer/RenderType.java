package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RenderType extends RenderStateShard {
    private static final int MEGABYTE = 1048576;
    public static final int BIG_BUFFER_SIZE = 4194304;
    public static final int SMALL_BUFFER_SIZE = 786432;
    public static final int TRANSIENT_BUFFER_SIZE = 1536;
    private static final RenderType SOLID = create(
        "solid",
        DefaultVertexFormat.BLOCK,
        VertexFormat.Mode.QUADS,
        4194304,
        true,
        false,
        RenderType.CompositeState.builder()
            .setLightmapState(LIGHTMAP)
            .setShaderState(RENDERTYPE_SOLID_SHADER)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .createCompositeState(true)
    );
    private static final RenderType CUTOUT_MIPPED = create(
        "cutout_mipped",
        DefaultVertexFormat.BLOCK,
        VertexFormat.Mode.QUADS,
        4194304,
        true,
        false,
        RenderType.CompositeState.builder()
            .setLightmapState(LIGHTMAP)
            .setShaderState(RENDERTYPE_CUTOUT_MIPPED_SHADER)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .createCompositeState(true)
    );
    private static final RenderType CUTOUT = create(
        "cutout",
        DefaultVertexFormat.BLOCK,
        VertexFormat.Mode.QUADS,
        786432,
        true,
        false,
        RenderType.CompositeState.builder()
            .setLightmapState(LIGHTMAP)
            .setShaderState(RENDERTYPE_CUTOUT_SHADER)
            .setTextureState(BLOCK_SHEET)
            .createCompositeState(true)
    );
    private static final RenderType TRANSLUCENT = create(
        "translucent", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 786432, true, true, translucentState(RENDERTYPE_TRANSLUCENT_SHADER)
    );
    private static final RenderType TRANSLUCENT_MOVING_BLOCK = create(
        "translucent_moving_block", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 786432, false, true, translucentMovingBlockState()
    );
    private static final Function<ResourceLocation, RenderType> ARMOR_CUTOUT_NO_CULL = Util.memoize(
        param0 -> createArmorCutoutNoCull("armor_cutout_no_cull", param0, false)
    );
    private static final Function<ResourceLocation, RenderType> ENTITY_SOLID = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
            return create("entity_solid", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
            return create("entity_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false, var0);
        }
    );
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL = Util.memoize(
        (param0, param1) -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(param1);
            return create("entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false, var0);
        }
    );
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL_Z_OFFSET = Util.memoize(
        (param0, param1) -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(param1);
            return create("entity_cutout_no_cull_z_offset", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> ITEM_ENTITY_TRANSLUCENT_CULL = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(true);
            return create("item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
            return create("entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, var0);
        }
    );
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT = Util.memoize(
        (param0, param1) -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(param1);
            return create("entity_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, var0);
        }
    );
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_EMISSIVE = Util.memoize(
        (param0, param1) -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE)
                .setOverlayState(OVERLAY)
                .createCompositeState(param1);
            return create("entity_translucent_emissive", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> ENTITY_SMOOTH_CUTOUT = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(true);
            return create("entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, var0);
        }
    );
    private static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM = Util.memoize(
        (param0, param1) -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(param1 ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                .setWriteMaskState(param1 ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .createCompositeState(false);
            return create("beacon_beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 1536, false, true, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> ENTITY_DECAL = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_DECAL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setDepthTestState(EQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(false);
            return create("entity_decal", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> ENTITY_NO_OUTLINE = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_NO_OUTLINE_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
            return create("entity_no_outline", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, true, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> ENTITY_SHADOW = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_SHADOW_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(false);
            return create("entity_shadow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, false, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> DRAGON_EXPLOSION_ALPHA = Util.memoize(
        param0 -> {
            RenderType.CompositeState var0 = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_ALPHA_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setCullState(NO_CULL)
                .createCompositeState(true);
            return create("entity_alpha", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, var0);
        }
    );
    private static final Function<ResourceLocation, RenderType> EYES = Util.memoize(
        param0 -> {
            RenderStateShard.TextureStateShard var0 = new RenderStateShard.TextureStateShard(param0, false, false);
            return create(
                "eyes",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(var0)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
            );
        }
    );
    private static final RenderType LEASH = create(
        "leash",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        VertexFormat.Mode.TRIANGLE_STRIP,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_LEASH_SHADER)
            .setTextureState(NO_TEXTURE)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .createCompositeState(false)
    );
    private static final RenderType WATER_MASK = create(
        "water_mask",
        DefaultVertexFormat.POSITION,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_WATER_MASK_SHADER)
            .setTextureState(NO_TEXTURE)
            .setWriteMaskState(DEPTH_WRITE)
            .createCompositeState(false)
    );
    private static final RenderType ARMOR_GLINT = create(
        "armor_glint",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ARMOR_GLINT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ENTITY, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false)
    );
    private static final RenderType ARMOR_ENTITY_GLINT = create(
        "armor_entity_glint",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ENTITY, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false)
    );
    private static final RenderType GLINT_TRANSLUCENT = create(
        "glint_translucent",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .setOutputState(ITEM_ENTITY_TARGET)
            .createCompositeState(false)
    );
    private static final RenderType GLINT = create(
        "glint",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GLINT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false)
    );
    private static final RenderType GLINT_DIRECT = create(
        "glint_direct",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GLINT_DIRECT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false)
    );
    private static final RenderType ENTITY_GLINT = create(
        "entity_glint",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ENTITY, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false)
    );
    private static final RenderType ENTITY_GLINT_DIRECT = create(
        "entity_glint_direct",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ENTITY, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false)
    );
    private static final Function<ResourceLocation, RenderType> CRUMBLING = Util.memoize(
        param0 -> {
            RenderStateShard.TextureStateShard var0 = new RenderStateShard.TextureStateShard(param0, false, false);
            return create(
                "crumbling",
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_CRUMBLING_SHADER)
                    .setTextureState(var0)
                    .setTransparencyState(CRUMBLING_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
            );
        }
    );
    private static final Function<ResourceLocation, RenderType> TEXT = Util.memoize(
        param0 -> create(
                "text",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                786432,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
            )
    );
    private static final RenderType TEXT_BACKGROUND = create(
        "text_background",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_TEXT_BACKGROUND_SHADER)
            .setTextureState(NO_TEXTURE)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setLightmapState(LIGHTMAP)
            .createCompositeState(false)
    );
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY = Util.memoize(
        param0 -> create(
                "text_intensity",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                786432,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_INTENSITY_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
            )
    );
    private static final Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET = Util.memoize(
        param0 -> create(
                "text_polygon_offset",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
            )
    );
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize(
        param0 -> create(
                "text_intensity_polygon_offset",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_INTENSITY_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
            )
    );
    private static final Function<ResourceLocation, RenderType> TEXT_SEE_THROUGH = Util.memoize(
        param0 -> create(
                "text_see_through",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
            )
    );
    private static final RenderType TEXT_BACKGROUND_SEE_THROUGH = create(
        "text_background_see_through",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER)
            .setTextureState(NO_TEXTURE)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setLightmapState(LIGHTMAP)
            .setDepthTestState(NO_DEPTH_TEST)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false)
    );
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_SEE_THROUGH = Util.memoize(
        param0 -> create(
                "text_intensity_see_through",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
            )
    );
    private static final RenderType LIGHTNING = create(
        "lightning",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setTransparencyState(LIGHTNING_TRANSPARENCY)
            .setOutputState(WEATHER_TARGET)
            .createCompositeState(false)
    );
    private static final RenderType TRIPWIRE = create("tripwire", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 1536, true, true, tripwireState());
    private static final RenderType END_PORTAL = create(
        "end_portal",
        DefaultVertexFormat.POSITION,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_END_PORTAL_SHADER)
            .setTextureState(
                RenderStateShard.MultiTextureStateShard.builder()
                    .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                    .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                    .build()
            )
            .createCompositeState(false)
    );
    private static final RenderType END_GATEWAY = create(
        "end_gateway",
        DefaultVertexFormat.POSITION,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_END_GATEWAY_SHADER)
            .setTextureState(
                RenderStateShard.MultiTextureStateShard.builder()
                    .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                    .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                    .build()
            )
            .createCompositeState(false)
    );
    public static final RenderType.CompositeRenderType LINES = create(
        "lines",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_LINES_SHADER)
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setCullState(NO_CULL)
            .createCompositeState(false)
    );
    public static final RenderType.CompositeRenderType LINE_STRIP = create(
        "line_strip",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINE_STRIP,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_LINES_SHADER)
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setCullState(NO_CULL)
            .createCompositeState(false)
    );
    private static final Function<Double, RenderType.CompositeRenderType> DEBUG_LINE_STRIP = Util.memoize(
        param0 -> create(
                "debug_line_strip",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.DEBUG_LINE_STRIP,
                1536,
                RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(param0)))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .createCompositeState(false)
            )
    );
    private static final RenderType.CompositeRenderType DEBUG_FILLED_BOX = create(
        "debug_filled_box",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLE_STRIP,
        1536,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );
    private static final RenderType.CompositeRenderType DEBUG_QUADS = create(
        "debug_quads",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(NO_CULL)
            .createCompositeState(false)
    );
    private static final RenderType.CompositeRenderType DEBUG_SECTION_QUADS = create(
        "debug_section_quads",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(CULL)
            .createCompositeState(false)
    );
    private static final RenderType.CompositeRenderType GUI = create(
        "gui",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        786432,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GUI_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .createCompositeState(false)
    );
    private static final RenderType.CompositeRenderType GUI_OVERLAY = create(
        "gui_overlay",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GUI_OVERLAY_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(NO_DEPTH_TEST)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false)
    );
    private static final RenderType.CompositeRenderType GUI_TEXT_HIGHLIGHT = create(
        "gui_text_highlight",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GUI_TEXT_HIGHLIGHT_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(NO_DEPTH_TEST)
            .setColorLogicState(OR_REVERSE_COLOR_LOGIC)
            .createCompositeState(false)
    );
    private static final RenderType.CompositeRenderType GUI_GHOST_RECIPE_OVERLAY = create(
        "gui_ghost_recipe_overlay",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GUI_GHOST_RECIPE_OVERLAY_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(GREATER_DEPTH_TEST)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false)
    );
    private static final ImmutableList<RenderType> CHUNK_BUFFER_LAYERS = ImmutableList.of(solid(), cutoutMipped(), cutout(), translucent(), tripwire());
    private final VertexFormat format;
    private final VertexFormat.Mode mode;
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;
    private final Optional<RenderType> asOptional;

    public static RenderType solid() {
        return SOLID;
    }

    public static RenderType cutoutMipped() {
        return CUTOUT_MIPPED;
    }

    public static RenderType cutout() {
        return CUTOUT;
    }

    private static RenderType.CompositeState translucentState(RenderStateShard.ShaderStateShard param0) {
        return RenderType.CompositeState.builder()
            .setLightmapState(LIGHTMAP)
            .setShaderState(param0)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(TRANSLUCENT_TARGET)
            .createCompositeState(true);
    }

    public static RenderType translucent() {
        return TRANSLUCENT;
    }

    private static RenderType.CompositeState translucentMovingBlockState() {
        return RenderType.CompositeState.builder()
            .setLightmapState(LIGHTMAP)
            .setShaderState(RENDERTYPE_TRANSLUCENT_MOVING_BLOCK_SHADER)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .createCompositeState(true);
    }

    public static RenderType translucentMovingBlock() {
        return TRANSLUCENT_MOVING_BLOCK;
    }

    private static RenderType.CompositeRenderType createArmorCutoutNoCull(String param0, ResourceLocation param1, boolean param2) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(param1, false, false))
            .setTransparencyState(NO_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setDepthTestState(param2 ? EQUAL_DEPTH_TEST : LEQUAL_DEPTH_TEST)
            .createCompositeState(true);
        return create(param0, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false, var0);
    }

    public static RenderType armorCutoutNoCull(ResourceLocation param0) {
        return ARMOR_CUTOUT_NO_CULL.apply(param0);
    }

    public static RenderType createArmorDecalCutoutNoCull(ResourceLocation param0) {
        return createArmorCutoutNoCull("armor_decal_cutout_no_cull", param0, true);
    }

    public static RenderType entitySolid(ResourceLocation param0) {
        return ENTITY_SOLID.apply(param0);
    }

    public static RenderType entityCutout(ResourceLocation param0) {
        return ENTITY_CUTOUT.apply(param0);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation param0, boolean param1) {
        return ENTITY_CUTOUT_NO_CULL.apply(param0, param1);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation param0) {
        return entityCutoutNoCull(param0, true);
    }

    public static RenderType entityCutoutNoCullZOffset(ResourceLocation param0, boolean param1) {
        return ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(param0, param1);
    }

    public static RenderType entityCutoutNoCullZOffset(ResourceLocation param0) {
        return entityCutoutNoCullZOffset(param0, true);
    }

    public static RenderType itemEntityTranslucentCull(ResourceLocation param0) {
        return ITEM_ENTITY_TRANSLUCENT_CULL.apply(param0);
    }

    public static RenderType entityTranslucentCull(ResourceLocation param0) {
        return ENTITY_TRANSLUCENT_CULL.apply(param0);
    }

    public static RenderType entityTranslucent(ResourceLocation param0, boolean param1) {
        return ENTITY_TRANSLUCENT.apply(param0, param1);
    }

    public static RenderType entityTranslucent(ResourceLocation param0) {
        return entityTranslucent(param0, true);
    }

    public static RenderType entityTranslucentEmissive(ResourceLocation param0, boolean param1) {
        return ENTITY_TRANSLUCENT_EMISSIVE.apply(param0, param1);
    }

    public static RenderType entityTranslucentEmissive(ResourceLocation param0) {
        return entityTranslucentEmissive(param0, true);
    }

    public static RenderType entitySmoothCutout(ResourceLocation param0) {
        return ENTITY_SMOOTH_CUTOUT.apply(param0);
    }

    public static RenderType beaconBeam(ResourceLocation param0, boolean param1) {
        return BEACON_BEAM.apply(param0, param1);
    }

    public static RenderType entityDecal(ResourceLocation param0) {
        return ENTITY_DECAL.apply(param0);
    }

    public static RenderType entityNoOutline(ResourceLocation param0) {
        return ENTITY_NO_OUTLINE.apply(param0);
    }

    public static RenderType entityShadow(ResourceLocation param0) {
        return ENTITY_SHADOW.apply(param0);
    }

    public static RenderType dragonExplosionAlpha(ResourceLocation param0) {
        return DRAGON_EXPLOSION_ALPHA.apply(param0);
    }

    public static RenderType eyes(ResourceLocation param0) {
        return EYES.apply(param0);
    }

    public static RenderType energySwirl(ResourceLocation param0, float param1, float param2) {
        return create(
            "energy_swirl",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(param1, param2))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(false)
        );
    }

    public static RenderType leash() {
        return LEASH;
    }

    public static RenderType waterMask() {
        return WATER_MASK;
    }

    public static RenderType outline(ResourceLocation param0) {
        return RenderType.CompositeRenderType.OUTLINE.apply(param0, NO_CULL);
    }

    public static RenderType armorGlint() {
        return ARMOR_GLINT;
    }

    public static RenderType armorEntityGlint() {
        return ARMOR_ENTITY_GLINT;
    }

    public static RenderType glintTranslucent() {
        return GLINT_TRANSLUCENT;
    }

    public static RenderType glint() {
        return GLINT;
    }

    public static RenderType glintDirect() {
        return GLINT_DIRECT;
    }

    public static RenderType entityGlint() {
        return ENTITY_GLINT;
    }

    public static RenderType entityGlintDirect() {
        return ENTITY_GLINT_DIRECT;
    }

    public static RenderType crumbling(ResourceLocation param0) {
        return CRUMBLING.apply(param0);
    }

    public static RenderType text(ResourceLocation param0) {
        return TEXT.apply(param0);
    }

    public static RenderType textBackground() {
        return TEXT_BACKGROUND;
    }

    public static RenderType textIntensity(ResourceLocation param0) {
        return TEXT_INTENSITY.apply(param0);
    }

    public static RenderType textPolygonOffset(ResourceLocation param0) {
        return TEXT_POLYGON_OFFSET.apply(param0);
    }

    public static RenderType textIntensityPolygonOffset(ResourceLocation param0) {
        return TEXT_INTENSITY_POLYGON_OFFSET.apply(param0);
    }

    public static RenderType textSeeThrough(ResourceLocation param0) {
        return TEXT_SEE_THROUGH.apply(param0);
    }

    public static RenderType textBackgroundSeeThrough() {
        return TEXT_BACKGROUND_SEE_THROUGH;
    }

    public static RenderType textIntensitySeeThrough(ResourceLocation param0) {
        return TEXT_INTENSITY_SEE_THROUGH.apply(param0);
    }

    public static RenderType lightning() {
        return LIGHTNING;
    }

    private static RenderType.CompositeState tripwireState() {
        return RenderType.CompositeState.builder()
            .setLightmapState(LIGHTMAP)
            .setShaderState(RENDERTYPE_TRIPWIRE_SHADER)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(WEATHER_TARGET)
            .createCompositeState(true);
    }

    public static RenderType tripwire() {
        return TRIPWIRE;
    }

    public static RenderType endPortal() {
        return END_PORTAL;
    }

    public static RenderType endGateway() {
        return END_GATEWAY;
    }

    public static RenderType lines() {
        return LINES;
    }

    public static RenderType lineStrip() {
        return LINE_STRIP;
    }

    public static RenderType debugLineStrip(double param0) {
        return DEBUG_LINE_STRIP.apply(param0);
    }

    public static RenderType debugFilledBox() {
        return DEBUG_FILLED_BOX;
    }

    public static RenderType debugQuads() {
        return DEBUG_QUADS;
    }

    public static RenderType debugSectionQuads() {
        return DEBUG_SECTION_QUADS;
    }

    public static RenderType gui() {
        return GUI;
    }

    public static RenderType guiOverlay() {
        return GUI_OVERLAY;
    }

    public static RenderType guiTextHighlight() {
        return GUI_TEXT_HIGHLIGHT;
    }

    public static RenderType guiGhostRecipeOverlay() {
        return GUI_GHOST_RECIPE_OVERLAY;
    }

    public RenderType(
        String param0, VertexFormat param1, VertexFormat.Mode param2, int param3, boolean param4, boolean param5, Runnable param6, Runnable param7
    ) {
        super(param0, param6, param7);
        this.format = param1;
        this.mode = param2;
        this.bufferSize = param3;
        this.affectsCrumbling = param4;
        this.sortOnUpload = param5;
        this.asOptional = Optional.of(this);
    }

    static RenderType.CompositeRenderType create(String param0, VertexFormat param1, VertexFormat.Mode param2, int param3, RenderType.CompositeState param4) {
        return create(param0, param1, param2, param3, false, false, param4);
    }

    private static RenderType.CompositeRenderType create(
        String param0, VertexFormat param1, VertexFormat.Mode param2, int param3, boolean param4, boolean param5, RenderType.CompositeState param6
    ) {
        return new RenderType.CompositeRenderType(param0, param1, param2, param3, param4, param5, param6);
    }

    public void end(BufferBuilder param0, VertexSorting param1) {
        if (param0.building()) {
            if (this.sortOnUpload) {
                param0.setQuadSorting(param1);
            }

            BufferBuilder.RenderedBuffer var0 = param0.end();
            this.setupRenderState();
            BufferUploader.drawWithShader(var0);
            this.clearRenderState();
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static List<RenderType> chunkBufferLayers() {
        return CHUNK_BUFFER_LAYERS;
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public VertexFormat format() {
        return this.format;
    }

    public VertexFormat.Mode mode() {
        return this.mode;
    }

    public Optional<RenderType> outline() {
        return Optional.empty();
    }

    public boolean isOutline() {
        return false;
    }

    public boolean affectsCrumbling() {
        return this.affectsCrumbling;
    }

    public boolean canConsolidateConsecutiveGeometry() {
        return !this.mode.connectedPrimitives;
    }

    public Optional<RenderType> asOptional() {
        return this.asOptional;
    }

    @OnlyIn(Dist.CLIENT)
    static final class CompositeRenderType extends RenderType {
        static final BiFunction<ResourceLocation, RenderStateShard.CullStateShard, RenderType> OUTLINE = Util.memoize(
            (param0, param1) -> RenderType.create(
                    "outline",
                    DefaultVertexFormat.POSITION_COLOR_TEX,
                    VertexFormat.Mode.QUADS,
                    1536,
                    RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_OUTLINE_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                        .setCullState(param1)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setOutputState(OUTLINE_TARGET)
                        .createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
                )
        );
        private final RenderType.CompositeState state;
        private final Optional<RenderType> outline;
        private final boolean isOutline;

        CompositeRenderType(
            String param0, VertexFormat param1, VertexFormat.Mode param2, int param3, boolean param4, boolean param5, RenderType.CompositeState param6
        ) {
            super(
                param0,
                param1,
                param2,
                param3,
                param4,
                param5,
                () -> param6.states.forEach(RenderStateShard::setupRenderState),
                () -> param6.states.forEach(RenderStateShard::clearRenderState)
            );
            this.state = param6;
            this.outline = param6.outlineProperty == RenderType.OutlineProperty.AFFECTS_OUTLINE
                ? param6.textureState.cutoutTexture().map(param1x -> OUTLINE.apply(param1x, param6.cullState))
                : Optional.empty();
            this.isOutline = param6.outlineProperty == RenderType.OutlineProperty.IS_OUTLINE;
        }

        @Override
        public Optional<RenderType> outline() {
            return this.outline;
        }

        @Override
        public boolean isOutline() {
            return this.isOutline;
        }

        protected final RenderType.CompositeState state() {
            return this.state;
        }

        @Override
        public String toString() {
            return "RenderType[" + this.name + ":" + this.state + "]";
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static final class CompositeState {
        final RenderStateShard.EmptyTextureStateShard textureState;
        private final RenderStateShard.ShaderStateShard shaderState;
        private final RenderStateShard.TransparencyStateShard transparencyState;
        private final RenderStateShard.DepthTestStateShard depthTestState;
        final RenderStateShard.CullStateShard cullState;
        private final RenderStateShard.LightmapStateShard lightmapState;
        private final RenderStateShard.OverlayStateShard overlayState;
        private final RenderStateShard.LayeringStateShard layeringState;
        private final RenderStateShard.OutputStateShard outputState;
        private final RenderStateShard.TexturingStateShard texturingState;
        private final RenderStateShard.WriteMaskStateShard writeMaskState;
        private final RenderStateShard.LineStateShard lineState;
        private final RenderStateShard.ColorLogicStateShard colorLogicState;
        final RenderType.OutlineProperty outlineProperty;
        final ImmutableList<RenderStateShard> states;

        CompositeState(
            RenderStateShard.EmptyTextureStateShard param0,
            RenderStateShard.ShaderStateShard param1,
            RenderStateShard.TransparencyStateShard param2,
            RenderStateShard.DepthTestStateShard param3,
            RenderStateShard.CullStateShard param4,
            RenderStateShard.LightmapStateShard param5,
            RenderStateShard.OverlayStateShard param6,
            RenderStateShard.LayeringStateShard param7,
            RenderStateShard.OutputStateShard param8,
            RenderStateShard.TexturingStateShard param9,
            RenderStateShard.WriteMaskStateShard param10,
            RenderStateShard.LineStateShard param11,
            RenderStateShard.ColorLogicStateShard param12,
            RenderType.OutlineProperty param13
        ) {
            this.textureState = param0;
            this.shaderState = param1;
            this.transparencyState = param2;
            this.depthTestState = param3;
            this.cullState = param4;
            this.lightmapState = param5;
            this.overlayState = param6;
            this.layeringState = param7;
            this.outputState = param8;
            this.texturingState = param9;
            this.writeMaskState = param10;
            this.lineState = param11;
            this.colorLogicState = param12;
            this.outlineProperty = param13;
            this.states = ImmutableList.of(
                this.textureState,
                this.shaderState,
                this.transparencyState,
                this.depthTestState,
                this.cullState,
                this.lightmapState,
                this.overlayState,
                this.layeringState,
                this.outputState,
                this.texturingState,
                this.writeMaskState,
                this.colorLogicState,
                this.lineState
            );
        }

        @Override
        public String toString() {
            return "CompositeState[" + this.states + ", outlineProperty=" + this.outlineProperty + "]";
        }

        public static RenderType.CompositeState.CompositeStateBuilder builder() {
            return new RenderType.CompositeState.CompositeStateBuilder();
        }

        @OnlyIn(Dist.CLIENT)
        public static class CompositeStateBuilder {
            private RenderStateShard.EmptyTextureStateShard textureState = RenderStateShard.NO_TEXTURE;
            private RenderStateShard.ShaderStateShard shaderState = RenderStateShard.NO_SHADER;
            private RenderStateShard.TransparencyStateShard transparencyState = RenderStateShard.NO_TRANSPARENCY;
            private RenderStateShard.DepthTestStateShard depthTestState = RenderStateShard.LEQUAL_DEPTH_TEST;
            private RenderStateShard.CullStateShard cullState = RenderStateShard.CULL;
            private RenderStateShard.LightmapStateShard lightmapState = RenderStateShard.NO_LIGHTMAP;
            private RenderStateShard.OverlayStateShard overlayState = RenderStateShard.NO_OVERLAY;
            private RenderStateShard.LayeringStateShard layeringState = RenderStateShard.NO_LAYERING;
            private RenderStateShard.OutputStateShard outputState = RenderStateShard.MAIN_TARGET;
            private RenderStateShard.TexturingStateShard texturingState = RenderStateShard.DEFAULT_TEXTURING;
            private RenderStateShard.WriteMaskStateShard writeMaskState = RenderStateShard.COLOR_DEPTH_WRITE;
            private RenderStateShard.LineStateShard lineState = RenderStateShard.DEFAULT_LINE;
            private RenderStateShard.ColorLogicStateShard colorLogicState = RenderStateShard.NO_COLOR_LOGIC;

            CompositeStateBuilder() {
            }

            public RenderType.CompositeState.CompositeStateBuilder setTextureState(RenderStateShard.EmptyTextureStateShard param0) {
                this.textureState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setShaderState(RenderStateShard.ShaderStateShard param0) {
                this.shaderState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setTransparencyState(RenderStateShard.TransparencyStateShard param0) {
                this.transparencyState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setDepthTestState(RenderStateShard.DepthTestStateShard param0) {
                this.depthTestState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setCullState(RenderStateShard.CullStateShard param0) {
                this.cullState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setLightmapState(RenderStateShard.LightmapStateShard param0) {
                this.lightmapState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setOverlayState(RenderStateShard.OverlayStateShard param0) {
                this.overlayState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setLayeringState(RenderStateShard.LayeringStateShard param0) {
                this.layeringState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setOutputState(RenderStateShard.OutputStateShard param0) {
                this.outputState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setTexturingState(RenderStateShard.TexturingStateShard param0) {
                this.texturingState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setWriteMaskState(RenderStateShard.WriteMaskStateShard param0) {
                this.writeMaskState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setLineState(RenderStateShard.LineStateShard param0) {
                this.lineState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setColorLogicState(RenderStateShard.ColorLogicStateShard param0) {
                this.colorLogicState = param0;
                return this;
            }

            public RenderType.CompositeState createCompositeState(boolean param0) {
                return this.createCompositeState(param0 ? RenderType.OutlineProperty.AFFECTS_OUTLINE : RenderType.OutlineProperty.NONE);
            }

            public RenderType.CompositeState createCompositeState(RenderType.OutlineProperty param0) {
                return new RenderType.CompositeState(
                    this.textureState,
                    this.shaderState,
                    this.transparencyState,
                    this.depthTestState,
                    this.cullState,
                    this.lightmapState,
                    this.overlayState,
                    this.layeringState,
                    this.outputState,
                    this.texturingState,
                    this.writeMaskState,
                    this.lineState,
                    this.colorLogicState,
                    param0
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum OutlineProperty {
        NONE("none"),
        IS_OUTLINE("is_outline"),
        AFFECTS_OUTLINE("affects_outline");

        private final String name;

        private OutlineProperty(String param0) {
            this.name = param0;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
