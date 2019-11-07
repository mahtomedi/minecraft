package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderType extends RenderStateShard {
    private static final RenderType SOLID = new RenderType.CompositeRenderType(
        "solid",
        DefaultVertexFormat.BLOCK,
        7,
        2097152,
        true,
        false,
        RenderType.CompositeState.builder()
            .setShadeModelState(SMOOTH_SHADE)
            .setLightmapState(LIGHTMAP)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .createCompositeState(true)
    );
    private static final RenderType CUTOUT_MIPPED = new RenderType.CompositeRenderType(
        "cutout_mipped",
        DefaultVertexFormat.BLOCK,
        7,
        131072,
        true,
        false,
        RenderType.CompositeState.builder()
            .setShadeModelState(SMOOTH_SHADE)
            .setLightmapState(LIGHTMAP)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .setAlphaState(MIDWAY_ALPHA)
            .createCompositeState(true)
    );
    private static final RenderType CUTOUT = new RenderType.CompositeRenderType(
        "cutout",
        DefaultVertexFormat.BLOCK,
        7,
        131072,
        true,
        false,
        RenderType.CompositeState.builder()
            .setShadeModelState(SMOOTH_SHADE)
            .setLightmapState(LIGHTMAP)
            .setTextureState(BLOCK_SHEET)
            .setAlphaState(MIDWAY_ALPHA)
            .createCompositeState(true)
    );
    private static final RenderType TRANSLUCENT = new RenderType.CompositeRenderType(
        "translucent",
        DefaultVertexFormat.BLOCK,
        7,
        262144,
        true,
        true,
        RenderType.CompositeState.builder()
            .setShadeModelState(SMOOTH_SHADE)
            .setLightmapState(LIGHTMAP)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(true)
    );
    private static final RenderType TRANSLUCENT_NO_CRUMBLING = new RenderType(
        "translucent_no_crumbling", DefaultVertexFormat.BLOCK, 7, 256, false, true, TRANSLUCENT::setupRenderState, TRANSLUCENT::clearRenderState
    );
    private static final RenderType LEASH = new RenderType.CompositeRenderType(
        "leash",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        7,
        256,
        RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).setCullState(NO_CULL).setLightmapState(LIGHTMAP).createCompositeState(false)
    );
    private static final RenderType WATER_MASK = new RenderType.CompositeRenderType(
        "water_mask",
        DefaultVertexFormat.POSITION,
        7,
        256,
        RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).setWriteMaskState(DEPTH_WRITE).createCompositeState(false)
    );
    private static final RenderType GLINT = new RenderType.CompositeRenderType(
        "glint",
        DefaultVertexFormat.POSITION_TEX,
        7,
        256,
        RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, false, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false)
    );
    private static final RenderType ENTITY_GLINT = new RenderType.CompositeRenderType(
        "entity_glint",
        DefaultVertexFormat.POSITION_TEX,
        7,
        256,
        RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, false, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false)
    );
    private static final RenderType LIGHTNING = new RenderType.CompositeRenderType(
        "lightning",
        DefaultVertexFormat.POSITION_COLOR,
        7,
        256,
        false,
        true,
        RenderType.CompositeState.builder()
            .setWriteMaskState(COLOR_WRITE)
            .setTransparencyState(LIGHTNING_TRANSPARENCY)
            .setShadeModelState(SMOOTH_SHADE)
            .createCompositeState(false)
    );
    private final VertexFormat format;
    private final int mode;
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;

    public static RenderType solid() {
        return SOLID;
    }

    public static RenderType cutoutMipped() {
        return CUTOUT_MIPPED;
    }

    public static RenderType cutout() {
        return CUTOUT;
    }

    public static RenderType translucent() {
        return TRANSLUCENT;
    }

    public static RenderType translucentNoCrumbling() {
        return TRANSLUCENT_NO_CRUMBLING;
    }

    public static RenderType entitySolid(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setTransparencyState(NO_TRANSPARENCY)
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(true);
        return new RenderType.CompositeRenderType("entity_solid", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, var0);
    }

    public static RenderType entityCutout(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setTransparencyState(NO_TRANSPARENCY)
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setAlphaState(DEFAULT_ALPHA)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(true);
        return new RenderType.CompositeRenderType("entity_cutout", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, var0);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setTransparencyState(NO_TRANSPARENCY)
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setAlphaState(DEFAULT_ALPHA)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(true);
        return new RenderType.CompositeRenderType("entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, var0);
    }

    public static RenderType entityTranslucentCull(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setAlphaState(DEFAULT_ALPHA)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(true);
        return new RenderType.CompositeRenderType("entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, var0);
    }

    public static RenderType entityTranslucent(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setAlphaState(DEFAULT_ALPHA)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(true);
        return new RenderType.CompositeRenderType("entity_translucent", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, var0);
    }

    public static RenderType entityForceTranslucent(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setTransparencyState(FORCED_TRANSPARENCY)
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(true);
        return new RenderType.CompositeRenderType("entity_force_translucent", DefaultVertexFormat.NEW_ENTITY, 7, 256, false, true, var0);
    }

    public static RenderType entitySmoothCutout(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setAlphaState(MIDWAY_ALPHA)
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setShadeModelState(SMOOTH_SHADE)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .createCompositeState(true);
        return new RenderType.CompositeRenderType("entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY, 7, 256, var0);
    }

    public static RenderType beaconBeam(ResourceLocation param0, boolean param1) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setTransparencyState(param1 ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
            .setWriteMaskState(param1 ? COLOR_WRITE : COLOR_DEPTH_WRITE)
            .setFogState(NO_FOG)
            .createCompositeState(false);
        return new RenderType.CompositeRenderType("beacon_beam", DefaultVertexFormat.BLOCK, 7, 256, false, true, var0);
    }

    public static RenderType entityDecal(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setAlphaState(DEFAULT_ALPHA)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .createCompositeState(false);
        return new RenderType.CompositeRenderType("entity_decal", DefaultVertexFormat.NEW_ENTITY, 7, 256, var0);
    }

    public static RenderType entityNoOutline(ResourceLocation param0) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDiffuseLightingState(DIFFUSE_LIGHTING)
            .setAlphaState(DEFAULT_ALPHA)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false);
        return new RenderType.CompositeRenderType("entity_no_outline", DefaultVertexFormat.NEW_ENTITY, 7, 256, false, true, var0);
    }

    public static RenderType entityAlpha(ResourceLocation param0, float param1) {
        RenderType.CompositeState var0 = RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
            .setAlphaState(new RenderStateShard.AlphaStateShard(param1))
            .setCullState(NO_CULL)
            .createCompositeState(true);
        return new RenderType.CompositeRenderType("entity_alpha", DefaultVertexFormat.NEW_ENTITY, 7, 256, var0);
    }

    public static RenderType eyes(ResourceLocation param0) {
        RenderStateShard.TextureStateShard var0 = new RenderStateShard.TextureStateShard(param0, false, false);
        return new RenderType.CompositeRenderType(
            "eyes",
            DefaultVertexFormat.NEW_ENTITY,
            7,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                .setTextureState(var0)
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setFogState(BLACK_FOG)
                .createCompositeState(false)
        );
    }

    public static RenderType energySwirl(ResourceLocation param0, float param1, float param2) {
        return new RenderType.CompositeRenderType(
            "energy_swirl",
            DefaultVertexFormat.NEW_ENTITY,
            7,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(param1, param2))
                .setFogState(BLACK_FOG)
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setDiffuseLightingState(DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
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
        return new RenderType.CompositeRenderType(
            "outline",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            7,
            256,
            RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setCullState(NO_CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .setAlphaState(DEFAULT_ALPHA)
                .setTexturingState(OUTLINE_TEXTURING)
                .setFogState(NO_FOG)
                .setOutputState(OUTLINE_TARGET)
                .createCompositeState(false)
        );
    }

    public static RenderType glint() {
        return GLINT;
    }

    public static RenderType entityGlint() {
        return ENTITY_GLINT;
    }

    public static RenderType crumbling(int param0) {
        RenderStateShard.TextureStateShard var0 = new RenderStateShard.TextureStateShard(ModelBakery.BREAKING_LOCATIONS.get(param0), false, false);
        return new RenderType.CompositeRenderType(
            "crumbling",
            DefaultVertexFormat.BLOCK,
            7,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                .setTextureState(var0)
                .setAlphaState(DEFAULT_ALPHA)
                .setTransparencyState(CRUMBLING_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setLayeringState(POLYGON_OFFSET_LAYERING)
                .createCompositeState(false)
        );
    }

    public static RenderType text(ResourceLocation param0) {
        return new RenderType.CompositeRenderType(
            "text",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            7,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setAlphaState(DEFAULT_ALPHA)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false)
        );
    }

    public static RenderType textSeeThrough(ResourceLocation param0) {
        return new RenderType.CompositeRenderType(
            "text_see_through",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            7,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(param0, false, false))
                .setAlphaState(DEFAULT_ALPHA)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setDepthTestState(NO_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)
        );
    }

    public static RenderType lightning() {
        return LIGHTNING;
    }

    public static RenderType endPortal(int param0) {
        RenderStateShard.TransparencyStateShard var0;
        RenderStateShard.TextureStateShard var1;
        if (param0 <= 1) {
            var0 = TRANSLUCENT_TRANSPARENCY;
            var1 = new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_SKY_LOCATION, false, false);
        } else {
            var0 = ADDITIVE_TRANSPARENCY;
            var1 = new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false);
        }

        return new RenderType.CompositeRenderType(
            "end_portal",
            DefaultVertexFormat.POSITION_COLOR,
            7,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                .setTransparencyState(var0)
                .setTextureState(var1)
                .setTexturingState(new RenderStateShard.PortalTexturingStateShard(param0))
                .setFogState(BLACK_FOG)
                .createCompositeState(false)
        );
    }

    public static RenderType lines() {
        return new RenderType.CompositeRenderType(
            "lines",
            DefaultVertexFormat.POSITION_COLOR,
            1,
            256,
            RenderType.CompositeState.builder()
                .setLineState(new RenderStateShard.LineStateShard(Math.max(2.5F, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F)))
                .setLayeringState(PROJECTION_LAYERING)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .createCompositeState(false)
        );
    }

    public RenderType(String param0, VertexFormat param1, int param2, int param3, boolean param4, boolean param5, Runnable param6, Runnable param7) {
        super(param0, param6, param7);
        this.format = param1;
        this.mode = param2;
        this.bufferSize = param3;
        this.affectsCrumbling = param4;
        this.sortOnUpload = param5;
    }

    public void end(BufferBuilder param0, int param1, int param2, int param3) {
        if (param0.building()) {
            if (this.sortOnUpload) {
                param0.sortQuads((float)param1, (float)param2, (float)param3);
            }

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

    public static List<RenderType> chunkBufferLayers() {
        return ImmutableList.of(solid(), cutoutMipped(), cutout(), translucent());
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public VertexFormat format() {
        return this.format;
    }

    public int mode() {
        return this.mode;
    }

    public Optional<ResourceLocation> outlineTexture() {
        return Optional.empty();
    }

    public boolean affectsCrumbling() {
        return this.affectsCrumbling;
    }

    @OnlyIn(Dist.CLIENT)
    static class CompositeRenderType extends RenderType {
        private final RenderType.CompositeState state;
        private int hashCode;
        private boolean hashed = false;

        public CompositeRenderType(String param0, VertexFormat param1, int param2, int param3, RenderType.CompositeState param4) {
            this(param0, param1, param2, param3, false, false, param4);
        }

        public CompositeRenderType(String param0, VertexFormat param1, int param2, int param3, boolean param4, boolean param5, RenderType.CompositeState param6) {
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
        }

        @Override
        public Optional<ResourceLocation> outlineTexture() {
            return this.state().affectsOutline ? this.state().textureState.texture() : Optional.empty();
        }

        protected final RenderType.CompositeState state() {
            return this.state;
        }

        @Override
        public boolean equals(@Nullable Object param0) {
            if (!super.equals(param0)) {
                return false;
            } else if (this.getClass() != param0.getClass()) {
                return false;
            } else {
                RenderType.CompositeRenderType var0 = (RenderType.CompositeRenderType)param0;
                return this.state.equals(var0.state);
            }
        }

        @Override
        public int hashCode() {
            if (!this.hashed) {
                this.hashed = true;
                this.hashCode = Objects.hash(super.hashCode(), this.state);
            }

            return this.hashCode;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class CompositeState {
        private final RenderStateShard.TextureStateShard textureState;
        private final RenderStateShard.TransparencyStateShard transparencyState;
        private final RenderStateShard.DiffuseLightingStateShard diffuseLightingState;
        private final RenderStateShard.ShadeModelStateShard shadeModelState;
        private final RenderStateShard.AlphaStateShard alphaState;
        private final RenderStateShard.DepthTestStateShard depthTestState;
        private final RenderStateShard.CullStateShard cullState;
        private final RenderStateShard.LightmapStateShard lightmapState;
        private final RenderStateShard.OverlayStateShard overlayState;
        private final RenderStateShard.FogStateShard fogState;
        private final RenderStateShard.LayeringStateShard layeringState;
        private final RenderStateShard.OutputStateShard outputState;
        private final RenderStateShard.TexturingStateShard texturingState;
        private final RenderStateShard.WriteMaskStateShard writeMaskState;
        private final RenderStateShard.LineStateShard lineState;
        private final boolean affectsOutline;
        private final ImmutableList<RenderStateShard> states;

        private CompositeState(
            RenderStateShard.TextureStateShard param0,
            RenderStateShard.TransparencyStateShard param1,
            RenderStateShard.DiffuseLightingStateShard param2,
            RenderStateShard.ShadeModelStateShard param3,
            RenderStateShard.AlphaStateShard param4,
            RenderStateShard.DepthTestStateShard param5,
            RenderStateShard.CullStateShard param6,
            RenderStateShard.LightmapStateShard param7,
            RenderStateShard.OverlayStateShard param8,
            RenderStateShard.FogStateShard param9,
            RenderStateShard.LayeringStateShard param10,
            RenderStateShard.OutputStateShard param11,
            RenderStateShard.TexturingStateShard param12,
            RenderStateShard.WriteMaskStateShard param13,
            RenderStateShard.LineStateShard param14,
            boolean param15
        ) {
            this.textureState = param0;
            this.transparencyState = param1;
            this.diffuseLightingState = param2;
            this.shadeModelState = param3;
            this.alphaState = param4;
            this.depthTestState = param5;
            this.cullState = param6;
            this.lightmapState = param7;
            this.overlayState = param8;
            this.fogState = param9;
            this.layeringState = param10;
            this.outputState = param11;
            this.texturingState = param12;
            this.writeMaskState = param13;
            this.lineState = param14;
            this.affectsOutline = param15;
            this.states = ImmutableList.of(
                this.textureState,
                this.transparencyState,
                this.diffuseLightingState,
                this.shadeModelState,
                this.alphaState,
                this.depthTestState,
                this.cullState,
                this.lightmapState,
                this.overlayState,
                this.fogState,
                this.layeringState,
                this.outputState,
                this.texturingState,
                this.writeMaskState,
                this.lineState
            );
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderType.CompositeState var0 = (RenderType.CompositeState)param0;
                return this.affectsOutline == var0.affectsOutline && this.states.equals(var0.states);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.states, this.affectsOutline);
        }

        public static RenderType.CompositeState.CompositeStateBuilder builder() {
            return new RenderType.CompositeState.CompositeStateBuilder();
        }

        @OnlyIn(Dist.CLIENT)
        public static class CompositeStateBuilder {
            private RenderStateShard.TextureStateShard textureState = RenderStateShard.NO_TEXTURE;
            private RenderStateShard.TransparencyStateShard transparencyState = RenderStateShard.NO_TRANSPARENCY;
            private RenderStateShard.DiffuseLightingStateShard diffuseLightingState = RenderStateShard.NO_DIFFUSE_LIGHTING;
            private RenderStateShard.ShadeModelStateShard shadeModelState = RenderStateShard.FLAT_SHADE;
            private RenderStateShard.AlphaStateShard alphaState = RenderStateShard.NO_ALPHA;
            private RenderStateShard.DepthTestStateShard depthTestState = RenderStateShard.LEQUAL_DEPTH_TEST;
            private RenderStateShard.CullStateShard cullState = RenderStateShard.CULL;
            private RenderStateShard.LightmapStateShard lightmapState = RenderStateShard.NO_LIGHTMAP;
            private RenderStateShard.OverlayStateShard overlayState = RenderStateShard.NO_OVERLAY;
            private RenderStateShard.FogStateShard fogState = RenderStateShard.FOG;
            private RenderStateShard.LayeringStateShard layeringState = RenderStateShard.NO_LAYERING;
            private RenderStateShard.OutputStateShard outputState = RenderStateShard.MAIN_TARGET;
            private RenderStateShard.TexturingStateShard texturingState = RenderStateShard.DEFAULT_TEXTURING;
            private RenderStateShard.WriteMaskStateShard writeMaskState = RenderStateShard.COLOR_DEPTH_WRITE;
            private RenderStateShard.LineStateShard lineState = RenderStateShard.DEFAULT_LINE;

            private CompositeStateBuilder() {
            }

            public RenderType.CompositeState.CompositeStateBuilder setTextureState(RenderStateShard.TextureStateShard param0) {
                this.textureState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setTransparencyState(RenderStateShard.TransparencyStateShard param0) {
                this.transparencyState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setDiffuseLightingState(RenderStateShard.DiffuseLightingStateShard param0) {
                this.diffuseLightingState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setShadeModelState(RenderStateShard.ShadeModelStateShard param0) {
                this.shadeModelState = param0;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setAlphaState(RenderStateShard.AlphaStateShard param0) {
                this.alphaState = param0;
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

            public RenderType.CompositeState.CompositeStateBuilder setFogState(RenderStateShard.FogStateShard param0) {
                this.fogState = param0;
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

            public RenderType.CompositeState createCompositeState(boolean param0) {
                return new RenderType.CompositeState(
                    this.textureState,
                    this.transparencyState,
                    this.diffuseLightingState,
                    this.shadeModelState,
                    this.alphaState,
                    this.depthTestState,
                    this.cullState,
                    this.lightmapState,
                    this.overlayState,
                    this.fogState,
                    this.layeringState,
                    this.outputState,
                    this.texturingState,
                    this.writeMaskState,
                    this.lineState,
                    param0
                );
            }
        }
    }
}
