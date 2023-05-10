package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public abstract class RenderStateShard {
    private static final float VIEW_SCALE_Z_EPSILON = 0.99975586F;
    public static final double MAX_ENCHANTMENT_GLINT_SPEED_MILLIS = 8.0;
    protected final String name;
    private final Runnable setupState;
    private final Runnable clearState;
    protected static final RenderStateShard.TransparencyStateShard NO_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
        "no_transparency", () -> RenderSystem.disableBlend(), () -> {
        }
    );
    protected static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
        "additive_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    );
    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
        "lightning_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    );
    protected static final RenderStateShard.TransparencyStateShard GLINT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
        "glint_transparency",
        () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
            );
        },
        () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    );
    protected static final RenderStateShard.TransparencyStateShard CRUMBLING_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
        "crumbling_transparency",
        () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
        },
        () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    );
    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
        "translucent_transparency",
        () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
        },
        () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    );
    protected static final RenderStateShard.ShaderStateShard NO_SHADER = new RenderStateShard.ShaderStateShard();
    protected static final RenderStateShard.ShaderStateShard POSITION_COLOR_LIGHTMAP_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getPositionColorLightmapShader
    );
    protected static final RenderStateShard.ShaderStateShard POSITION_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionShader);
    protected static final RenderStateShard.ShaderStateShard POSITION_COLOR_TEX_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getPositionColorTexShader
    );
    protected static final RenderStateShard.ShaderStateShard POSITION_TEX_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexShader);
    protected static final RenderStateShard.ShaderStateShard POSITION_COLOR_TEX_LIGHTMAP_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getPositionColorTexLightmapShader
    );
    protected static final RenderStateShard.ShaderStateShard POSITION_COLOR_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader);
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_SOLID_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeSolidShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_CUTOUT_MIPPED_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeCutoutMippedShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_CUTOUT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeCutoutShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TRANSLUCENT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTranslucentShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TRANSLUCENT_MOVING_BLOCK_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTranslucentMovingBlockShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TRANSLUCENT_NO_CRUMBLING_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTranslucentNoCrumblingShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeArmorCutoutNoCullShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_SOLID_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntitySolidShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityCutoutShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityCutoutNoCullShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityCutoutNoCullZOffsetShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeItemEntityTranslucentCullShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityTranslucentCullShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityTranslucentShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityTranslucentEmissiveShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntitySmoothCutoutShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_BEACON_BEAM_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeBeaconBeamShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_DECAL_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityDecalShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_NO_OUTLINE_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityNoOutlineShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_SHADOW_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityShadowShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_ALPHA_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityAlphaShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_EYES_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEyesShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENERGY_SWIRL_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEnergySwirlShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_LEASH_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeLeashShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_WATER_MASK_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeWaterMaskShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_OUTLINE_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeOutlineShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ARMOR_GLINT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeArmorGlintShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeArmorEntityGlintShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_GLINT_TRANSLUCENT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeGlintTranslucentShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_GLINT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeGlintShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_GLINT_DIRECT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeGlintDirectShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_GLINT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityGlintShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityGlintDirectShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_CRUMBLING_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeCrumblingShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TEXT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTextShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TEXT_BACKGROUND_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTextBackgroundShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TEXT_INTENSITY_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTextIntensityShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TEXT_SEE_THROUGH_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTextSeeThroughShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTextBackgroundSeeThroughShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTextIntensitySeeThroughShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_LIGHTNING_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeLightningShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_TRIPWIRE_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeTripwireShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_END_PORTAL_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEndPortalShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_END_GATEWAY_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEndGatewayShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_LINES_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeLinesShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_GUI_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeGuiShader);
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_GUI_OVERLAY_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeGuiOverlayShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_GUI_TEXT_HIGHLIGHT_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeGuiTextHighlightShader
    );
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_GUI_GHOST_RECIPE_OVERLAY_SHADER = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeGuiGhostRecipeOverlayShader
    );
    protected static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = new RenderStateShard.TextureStateShard(
        TextureAtlas.LOCATION_BLOCKS, false, true
    );
    protected static final RenderStateShard.TextureStateShard BLOCK_SHEET = new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false);
    protected static final RenderStateShard.EmptyTextureStateShard NO_TEXTURE = new RenderStateShard.EmptyTextureStateShard();
    protected static final RenderStateShard.TexturingStateShard DEFAULT_TEXTURING = new RenderStateShard.TexturingStateShard("default_texturing", () -> {
    }, () -> {
    });
    protected static final RenderStateShard.TexturingStateShard GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
        "glint_texturing", () -> setupGlintTexturing(8.0F), () -> RenderSystem.resetTextureMatrix()
    );
    protected static final RenderStateShard.TexturingStateShard ENTITY_GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
        "entity_glint_texturing", () -> setupGlintTexturing(0.16F), () -> RenderSystem.resetTextureMatrix()
    );
    protected static final RenderStateShard.LightmapStateShard LIGHTMAP = new RenderStateShard.LightmapStateShard(true);
    protected static final RenderStateShard.LightmapStateShard NO_LIGHTMAP = new RenderStateShard.LightmapStateShard(false);
    protected static final RenderStateShard.OverlayStateShard OVERLAY = new RenderStateShard.OverlayStateShard(true);
    protected static final RenderStateShard.OverlayStateShard NO_OVERLAY = new RenderStateShard.OverlayStateShard(false);
    protected static final RenderStateShard.CullStateShard CULL = new RenderStateShard.CullStateShard(true);
    protected static final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
    protected static final RenderStateShard.DepthTestStateShard NO_DEPTH_TEST = new RenderStateShard.DepthTestStateShard("always", 519);
    protected static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST = new RenderStateShard.DepthTestStateShard("==", 514);
    protected static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST = new RenderStateShard.DepthTestStateShard("<=", 515);
    protected static final RenderStateShard.DepthTestStateShard GREATER_DEPTH_TEST = new RenderStateShard.DepthTestStateShard(">", 516);
    protected static final RenderStateShard.WriteMaskStateShard COLOR_DEPTH_WRITE = new RenderStateShard.WriteMaskStateShard(true, true);
    protected static final RenderStateShard.WriteMaskStateShard COLOR_WRITE = new RenderStateShard.WriteMaskStateShard(true, false);
    protected static final RenderStateShard.WriteMaskStateShard DEPTH_WRITE = new RenderStateShard.WriteMaskStateShard(false, true);
    protected static final RenderStateShard.LayeringStateShard NO_LAYERING = new RenderStateShard.LayeringStateShard("no_layering", () -> {
    }, () -> {
    });
    protected static final RenderStateShard.LayeringStateShard POLYGON_OFFSET_LAYERING = new RenderStateShard.LayeringStateShard(
        "polygon_offset_layering", () -> {
            RenderSystem.polygonOffset(-1.0F, -10.0F);
            RenderSystem.enablePolygonOffset();
        }, () -> {
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
        }
    );
    protected static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING = new RenderStateShard.LayeringStateShard(
        "view_offset_z_layering", () -> {
            PoseStack var0 = RenderSystem.getModelViewStack();
            var0.pushPose();
            var0.scale(0.99975586F, 0.99975586F, 0.99975586F);
            RenderSystem.applyModelViewMatrix();
        }, () -> {
            PoseStack var0 = RenderSystem.getModelViewStack();
            var0.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    );
    protected static final RenderStateShard.OutputStateShard MAIN_TARGET = new RenderStateShard.OutputStateShard("main_target", () -> {
    }, () -> {
    });
    protected static final RenderStateShard.OutputStateShard OUTLINE_TARGET = new RenderStateShard.OutputStateShard(
        "outline_target",
        () -> Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false),
        () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false)
    );
    protected static final RenderStateShard.OutputStateShard TRANSLUCENT_TARGET = new RenderStateShard.OutputStateShard("translucent_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getTranslucentTarget().bindWrite(false);
        }

    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

    });
    protected static final RenderStateShard.OutputStateShard PARTICLES_TARGET = new RenderStateShard.OutputStateShard("particles_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getParticlesTarget().bindWrite(false);
        }

    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

    });
    protected static final RenderStateShard.OutputStateShard WEATHER_TARGET = new RenderStateShard.OutputStateShard("weather_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getWeatherTarget().bindWrite(false);
        }

    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

    });
    protected static final RenderStateShard.OutputStateShard CLOUDS_TARGET = new RenderStateShard.OutputStateShard("clouds_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getCloudsTarget().bindWrite(false);
        }

    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

    });
    protected static final RenderStateShard.OutputStateShard ITEM_ENTITY_TARGET = new RenderStateShard.OutputStateShard("item_entity_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getItemEntityTarget().bindWrite(false);
        }

    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

    });
    protected static final RenderStateShard.LineStateShard DEFAULT_LINE = new RenderStateShard.LineStateShard(OptionalDouble.of(1.0));
    protected static final RenderStateShard.ColorLogicStateShard NO_COLOR_LOGIC = new RenderStateShard.ColorLogicStateShard(
        "no_color_logic", () -> RenderSystem.disableColorLogicOp(), () -> {
        }
    );
    protected static final RenderStateShard.ColorLogicStateShard OR_REVERSE_COLOR_LOGIC = new RenderStateShard.ColorLogicStateShard("or_reverse", () -> {
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
    }, () -> RenderSystem.disableColorLogicOp());

    public RenderStateShard(String param0, Runnable param1, Runnable param2) {
        this.name = param0;
        this.setupState = param1;
        this.clearState = param2;
    }

    public void setupRenderState() {
        this.setupState.run();
    }

    public void clearRenderState() {
        this.clearState.run();
    }

    @Override
    public String toString() {
        return this.name;
    }

    private static void setupGlintTexturing(float param0) {
        long var0 = (long)((double)Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * 8.0);
        float var1 = (float)(var0 % 110000L) / 110000.0F;
        float var2 = (float)(var0 % 30000L) / 30000.0F;
        Matrix4f var3 = new Matrix4f().translation(-var1, var2, 0.0F);
        var3.rotateZ((float) (Math.PI / 18)).scale(param0);
        RenderSystem.setTextureMatrix(var3);
    }

    @OnlyIn(Dist.CLIENT)
    static class BooleanStateShard extends RenderStateShard {
        private final boolean enabled;

        public BooleanStateShard(String param0, Runnable param1, Runnable param2, boolean param3) {
            super(param0, param1, param2);
            this.enabled = param3;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.enabled + "]";
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class ColorLogicStateShard extends RenderStateShard {
        public ColorLogicStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class CullStateShard extends RenderStateShard.BooleanStateShard {
        public CullStateShard(boolean param0) {
            super("cull", () -> {
                if (!param0) {
                    RenderSystem.disableCull();
                }

            }, () -> {
                if (!param0) {
                    RenderSystem.enableCull();
                }

            }, param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class DepthTestStateShard extends RenderStateShard {
        private final String functionName;

        public DepthTestStateShard(String param0, int param1) {
            super("depth_test", () -> {
                if (param1 != 519) {
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(param1);
                }

            }, () -> {
                if (param1 != 519) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthFunc(515);
                }

            });
            this.functionName = param0;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.functionName + "]";
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class EmptyTextureStateShard extends RenderStateShard {
        public EmptyTextureStateShard(Runnable param0, Runnable param1) {
            super("texture", param0, param1);
        }

        EmptyTextureStateShard() {
            super("texture", () -> {
            }, () -> {
            });
        }

        protected Optional<ResourceLocation> cutoutTexture() {
            return Optional.empty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class LayeringStateShard extends RenderStateShard {
        public LayeringStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class LightmapStateShard extends RenderStateShard.BooleanStateShard {
        public LightmapStateShard(boolean param0) {
            super("lightmap", () -> {
                if (param0) {
                    Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                }

            }, () -> {
                if (param0) {
                    Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
                }

            }, param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class LineStateShard extends RenderStateShard {
        private final OptionalDouble width;

        public LineStateShard(OptionalDouble param0) {
            super("line_width", () -> {
                if (!Objects.equals(param0, OptionalDouble.of(1.0))) {
                    if (param0.isPresent()) {
                        RenderSystem.lineWidth((float)param0.getAsDouble());
                    } else {
                        RenderSystem.lineWidth(Math.max(2.5F, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F));
                    }
                }

            }, () -> {
                if (!Objects.equals(param0, OptionalDouble.of(1.0))) {
                    RenderSystem.lineWidth(1.0F);
                }

            });
            this.width = param0;
        }

        @Override
        public String toString() {
            return this.name + "[" + (this.width.isPresent() ? this.width.getAsDouble() : "window_scale") + "]";
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class MultiTextureStateShard extends RenderStateShard.EmptyTextureStateShard {
        private final Optional<ResourceLocation> cutoutTexture;

        MultiTextureStateShard(ImmutableList<Triple<ResourceLocation, Boolean, Boolean>> param0) {
            super(() -> {
                int var0 = 0;

                for(Triple<ResourceLocation, Boolean, Boolean> var1x : param0) {
                    TextureManager var2 = Minecraft.getInstance().getTextureManager();
                    var2.getTexture((ResourceLocation)var1x.getLeft()).setFilter(var1x.getMiddle(), var1x.getRight());
                    RenderSystem.setShaderTexture(var0++, (ResourceLocation)var1x.getLeft());
                }

            }, () -> {
            });
            this.cutoutTexture = param0.stream().findFirst().map(Triple::getLeft);
        }

        @Override
        protected Optional<ResourceLocation> cutoutTexture() {
            return this.cutoutTexture;
        }

        public static RenderStateShard.MultiTextureStateShard.Builder builder() {
            return new RenderStateShard.MultiTextureStateShard.Builder();
        }

        @OnlyIn(Dist.CLIENT)
        public static final class Builder {
            private final ImmutableList.Builder<Triple<ResourceLocation, Boolean, Boolean>> builder = new ImmutableList.Builder<>();

            public RenderStateShard.MultiTextureStateShard.Builder add(ResourceLocation param0, boolean param1, boolean param2) {
                this.builder.add(Triple.of(param0, param1, param2));
                return this;
            }

            public RenderStateShard.MultiTextureStateShard build() {
                return new RenderStateShard.MultiTextureStateShard(this.builder.build());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static final class OffsetTexturingStateShard extends RenderStateShard.TexturingStateShard {
        public OffsetTexturingStateShard(float param0, float param1) {
            super(
                "offset_texturing",
                () -> RenderSystem.setTextureMatrix(new Matrix4f().translation(param0, param1, 0.0F)),
                () -> RenderSystem.resetTextureMatrix()
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class OutputStateShard extends RenderStateShard {
        public OutputStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class OverlayStateShard extends RenderStateShard.BooleanStateShard {
        public OverlayStateShard(boolean param0) {
            super("overlay", () -> {
                if (param0) {
                    Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
                }

            }, () -> {
                if (param0) {
                    Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
                }

            }, param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class ShaderStateShard extends RenderStateShard {
        private final Optional<Supplier<ShaderInstance>> shader;

        public ShaderStateShard(Supplier<ShaderInstance> param0) {
            super("shader", () -> RenderSystem.setShader(param0), () -> {
            });
            this.shader = Optional.of(param0);
        }

        public ShaderStateShard() {
            super("shader", () -> RenderSystem.setShader(() -> null), () -> {
            });
            this.shader = Optional.empty();
        }

        @Override
        public String toString() {
            return this.name + "[" + this.shader + "]";
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class TextureStateShard extends RenderStateShard.EmptyTextureStateShard {
        private final Optional<ResourceLocation> texture;
        private final boolean blur;
        private final boolean mipmap;

        public TextureStateShard(ResourceLocation param0, boolean param1, boolean param2) {
            super(() -> {
                TextureManager var0 = Minecraft.getInstance().getTextureManager();
                var0.getTexture(param0).setFilter(param1, param2);
                RenderSystem.setShaderTexture(0, param0);
            }, () -> {
            });
            this.texture = Optional.of(param0);
            this.blur = param1;
            this.mipmap = param2;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.texture + "(blur=" + this.blur + ", mipmap=" + this.mipmap + ")]";
        }

        @Override
        protected Optional<ResourceLocation> cutoutTexture() {
            return this.texture;
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class TexturingStateShard extends RenderStateShard {
        public TexturingStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class TransparencyStateShard extends RenderStateShard {
        public TransparencyStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class WriteMaskStateShard extends RenderStateShard {
        private final boolean writeColor;
        private final boolean writeDepth;

        public WriteMaskStateShard(boolean param0, boolean param1) {
            super("write_mask_state", () -> {
                if (!param1) {
                    RenderSystem.depthMask(param1);
                }

                if (!param0) {
                    RenderSystem.colorMask(param0, param0, param0, param0);
                }

            }, () -> {
                if (!param1) {
                    RenderSystem.depthMask(true);
                }

                if (!param0) {
                    RenderSystem.colorMask(true, true, true, true);
                }

            });
            this.writeColor = param0;
            this.writeDepth = param1;
        }

        @Override
        public String toString() {
            return this.name + "[writeColor=" + this.writeColor + ", writeDepth=" + this.writeDepth + "]";
        }
    }
}
