package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderStateShard {
    protected final String name;
    private final Runnable setupState;
    private final Runnable clearState;
    protected static final RenderStateShard.TransparencyStateShard NO_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
        "no_transparency", () -> RenderSystem.disableBlend(), () -> {
        }
    );
    protected static final RenderStateShard.TransparencyStateShard FORCED_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
        "forced_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 0.15F);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.depthMask(false);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
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
        "glint_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        }, () -> {
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
        "translucent_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }, () -> RenderSystem.disableBlend()
    );
    protected static final RenderStateShard.AlphaStateShard NO_ALPHA = new RenderStateShard.AlphaStateShard(0.0F);
    protected static final RenderStateShard.AlphaStateShard DEFAULT_ALPHA = new RenderStateShard.AlphaStateShard(0.003921569F);
    protected static final RenderStateShard.AlphaStateShard MIDWAY_ALPHA = new RenderStateShard.AlphaStateShard(0.5F);
    protected static final RenderStateShard.ShadeModelStateShard FLAT_SHADE = new RenderStateShard.ShadeModelStateShard(false);
    protected static final RenderStateShard.ShadeModelStateShard SMOOTH_SHADE = new RenderStateShard.ShadeModelStateShard(true);
    protected static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = new RenderStateShard.TextureStateShard(
        TextureAtlas.LOCATION_BLOCKS, false, true
    );
    protected static final RenderStateShard.TextureStateShard BLOCK_SHEET = new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false);
    protected static final RenderStateShard.TextureStateShard NO_TEXTURE = new RenderStateShard.TextureStateShard();
    protected static final RenderStateShard.TexturingStateShard DEFAULT_TEXTURING = new RenderStateShard.TexturingStateShard("default_texturing", () -> {
    }, () -> {
    });
    protected static final RenderStateShard.TexturingStateShard OUTLINE_TEXTURING = new RenderStateShard.TexturingStateShard(
        "outline_texturing", () -> RenderSystem.setupOutline(), () -> RenderSystem.teardownOutline()
    );
    protected static final RenderStateShard.TexturingStateShard GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
        "glint_texturing", () -> setupGlintTexturing(8.0F), () -> {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
        }
    );
    protected static final RenderStateShard.TexturingStateShard ENTITY_GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
        "entity_glint_texturing", () -> setupGlintTexturing(0.16F), () -> {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
        }
    );
    protected static final RenderStateShard.LightmapStateShard LIGHTMAP = new RenderStateShard.LightmapStateShard(true);
    protected static final RenderStateShard.LightmapStateShard NO_LIGHTMAP = new RenderStateShard.LightmapStateShard(false);
    protected static final RenderStateShard.OverlayStateShard OVERLAY = new RenderStateShard.OverlayStateShard(true);
    protected static final RenderStateShard.OverlayStateShard NO_OVERLAY = new RenderStateShard.OverlayStateShard(false);
    protected static final RenderStateShard.DiffuseLightingStateShard DIFFUSE_LIGHTING = new RenderStateShard.DiffuseLightingStateShard(true);
    protected static final RenderStateShard.DiffuseLightingStateShard NO_DIFFUSE_LIGHTING = new RenderStateShard.DiffuseLightingStateShard(false);
    protected static final RenderStateShard.CullStateShard CULL = new RenderStateShard.CullStateShard(true);
    protected static final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
    protected static final RenderStateShard.DepthTestStateShard NO_DEPTH_TEST = new RenderStateShard.DepthTestStateShard(519);
    protected static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST = new RenderStateShard.DepthTestStateShard(514);
    protected static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST = new RenderStateShard.DepthTestStateShard(515);
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
    protected static final RenderStateShard.LayeringStateShard PROJECTION_LAYERING = new RenderStateShard.LayeringStateShard("projection_layering", () -> {
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.0F, 1.0F, 0.999F);
        RenderSystem.matrixMode(5888);
    }, () -> {
        RenderSystem.matrixMode(5889);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
    });
    protected static final RenderStateShard.FogStateShard NO_FOG = new RenderStateShard.FogStateShard("no_fog", () -> {
    }, () -> {
    });
    protected static final RenderStateShard.FogStateShard FOG = new RenderStateShard.FogStateShard("fog", () -> {
        FogRenderer.levelFogColor();
        RenderSystem.enableFog();
    }, () -> RenderSystem.disableFog());
    protected static final RenderStateShard.FogStateShard BLACK_FOG = new RenderStateShard.FogStateShard("black_fog", () -> {
        RenderSystem.fog(2918, 0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.enableFog();
    }, () -> {
        FogRenderer.levelFogColor();
        RenderSystem.disableFog();
    });
    protected static final RenderStateShard.OutputStateShard MAIN_TARGET = new RenderStateShard.OutputStateShard("main_target", () -> {
    }, () -> {
    });
    protected static final RenderStateShard.OutputStateShard OUTLINE_TARGET = new RenderStateShard.OutputStateShard(
        "outline_target",
        () -> Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false),
        () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false)
    );
    protected static final RenderStateShard.LineStateShard DEFAULT_LINE = new RenderStateShard.LineStateShard(1.0F);

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
    public boolean equals(@Nullable Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            RenderStateShard var0 = (RenderStateShard)param0;
            return this.name.equals(var0.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    private static void setupGlintTexturing(float param0) {
        RenderSystem.matrixMode(5890);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        long var0 = Util.getMillis() * 8L;
        float var1 = (float)(var0 % 110000L) / 110000.0F;
        float var2 = (float)(var0 % 30000L) / 30000.0F;
        RenderSystem.translatef(-var1, var2, 0.0F);
        RenderSystem.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.scalef(param0, param0, param0);
        RenderSystem.matrixMode(5888);
    }

    @OnlyIn(Dist.CLIENT)
    public static class AlphaStateShard extends RenderStateShard {
        private final float cutoff;

        public AlphaStateShard(float param0) {
            super("alpha", () -> {
                if (param0 > 0.0F) {
                    RenderSystem.enableAlphaTest();
                    RenderSystem.alphaFunc(516, param0);
                } else {
                    RenderSystem.disableAlphaTest();
                }

            }, () -> {
                RenderSystem.disableAlphaTest();
                RenderSystem.defaultAlphaFunc();
            });
            this.cutoff = param0;
        }

        @Override
        public boolean equals(@Nullable Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 == null || this.getClass() != param0.getClass()) {
                return false;
            } else if (!super.equals(param0)) {
                return false;
            } else {
                return this.cutoff == ((RenderStateShard.AlphaStateShard)param0).cutoff;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.cutoff);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class BooleanStateShard extends RenderStateShard {
        private final boolean enabled;

        public BooleanStateShard(String param0, Runnable param1, Runnable param2, boolean param3) {
            super(param0, param1, param2);
            this.enabled = param3;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderStateShard.BooleanStateShard var0 = (RenderStateShard.BooleanStateShard)param0;
                return this.enabled == var0.enabled;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(this.enabled);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CullStateShard extends RenderStateShard.BooleanStateShard {
        public CullStateShard(boolean param0) {
            super("cull", () -> {
                if (param0) {
                    RenderSystem.enableCull();
                }

            }, () -> {
                if (param0) {
                    RenderSystem.disableCull();
                }

            }, param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class DepthTestStateShard extends RenderStateShard {
        private final int function;

        public DepthTestStateShard(int param0) {
            super("depth_test", () -> {
                if (param0 != 519) {
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(param0);
                }

            }, () -> {
                if (param0 != 519) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthFunc(515);
                }

            });
            this.function = param0;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderStateShard.DepthTestStateShard var0 = (RenderStateShard.DepthTestStateShard)param0;
                return this.function == var0.function;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(this.function);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class DiffuseLightingStateShard extends RenderStateShard.BooleanStateShard {
        public DiffuseLightingStateShard(boolean param0) {
            super("diffuse_lighting", () -> {
                if (param0) {
                    Lighting.turnBackOn();
                }

            }, () -> {
                if (param0) {
                    Lighting.turnOff();
                }

            }, param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class FogStateShard extends RenderStateShard {
        public FogStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LayeringStateShard extends RenderStateShard {
        public LayeringStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LightmapStateShard extends RenderStateShard.BooleanStateShard {
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
    public static class LineStateShard extends RenderStateShard {
        private final float width;

        public LineStateShard(float param0) {
            super("alpha", () -> {
                if (param0 != 1.0F) {
                    RenderSystem.lineWidth(param0);
                }

            }, () -> {
                if (param0 != 1.0F) {
                    RenderSystem.lineWidth(1.0F);
                }

            });
            this.width = param0;
        }

        @Override
        public boolean equals(@Nullable Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 == null || this.getClass() != param0.getClass()) {
                return false;
            } else if (!super.equals(param0)) {
                return false;
            } else {
                return this.width == ((RenderStateShard.LineStateShard)param0).width;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.width);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class OffsetTexturingStateShard extends RenderStateShard.TexturingStateShard {
        private final float uOffset;
        private final float vOffset;

        public OffsetTexturingStateShard(float param0, float param1) {
            super("offset_texturing", () -> {
                RenderSystem.matrixMode(5890);
                RenderSystem.pushMatrix();
                RenderSystem.loadIdentity();
                RenderSystem.translatef(param0, param1, 0.0F);
                RenderSystem.matrixMode(5888);
            }, () -> {
                RenderSystem.matrixMode(5890);
                RenderSystem.popMatrix();
                RenderSystem.matrixMode(5888);
            });
            this.uOffset = param0;
            this.vOffset = param1;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderStateShard.OffsetTexturingStateShard var0 = (RenderStateShard.OffsetTexturingStateShard)param0;
                return Float.compare(var0.uOffset, this.uOffset) == 0 && Float.compare(var0.vOffset, this.vOffset) == 0;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.uOffset, this.vOffset);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class OutputStateShard extends RenderStateShard {
        public OutputStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class OverlayStateShard extends RenderStateShard.BooleanStateShard {
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
    public static final class PortalTexturingStateShard extends RenderStateShard.TexturingStateShard {
        private final int iteration;

        public PortalTexturingStateShard(int param0) {
            super("portal_texturing", () -> {
                RenderSystem.matrixMode(5890);
                RenderSystem.pushMatrix();
                RenderSystem.loadIdentity();
                RenderSystem.translatef(0.5F, 0.5F, 0.0F);
                RenderSystem.scalef(0.5F, 0.5F, 1.0F);
                RenderSystem.translatef(17.0F / (float)param0, (2.0F + (float)param0 / 1.5F) * ((float)(Util.getMillis() % 800000L) / 800000.0F), 0.0F);
                RenderSystem.rotatef(((float)(param0 * param0) * 4321.0F + (float)param0 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.scalef(4.5F - (float)param0 / 4.0F, 4.5F - (float)param0 / 4.0F, 1.0F);
                RenderSystem.mulTextureByProjModelView();
                RenderSystem.matrixMode(5888);
                RenderSystem.setupEndPortalTexGen();
            }, () -> {
                RenderSystem.matrixMode(5890);
                RenderSystem.popMatrix();
                RenderSystem.matrixMode(5888);
                RenderSystem.clearTexGen();
            });
            this.iteration = param0;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderStateShard.PortalTexturingStateShard var0 = (RenderStateShard.PortalTexturingStateShard)param0;
                return this.iteration == var0.iteration;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(this.iteration);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ShadeModelStateShard extends RenderStateShard {
        private final boolean smooth;

        public ShadeModelStateShard(boolean param0) {
            super("shade_model", () -> RenderSystem.shadeModel(param0 ? 7425 : 7424), () -> RenderSystem.shadeModel(7424));
            this.smooth = param0;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderStateShard.ShadeModelStateShard var0 = (RenderStateShard.ShadeModelStateShard)param0;
                return this.smooth == var0.smooth;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(this.smooth);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TextureStateShard extends RenderStateShard {
        private final Optional<ResourceLocation> texture;
        private final boolean blur;
        private final boolean mipmap;

        public TextureStateShard(ResourceLocation param0, boolean param1, boolean param2) {
            super("texture", () -> {
                RenderSystem.enableTexture();
                TextureManager var0 = Minecraft.getInstance().getTextureManager();
                var0.bind(param0);
                var0.getTexture(param0).setFilter(param1, param2);
            }, () -> {
            });
            this.texture = Optional.of(param0);
            this.blur = param1;
            this.mipmap = param2;
        }

        public TextureStateShard() {
            super("texture", () -> RenderSystem.disableTexture(), () -> RenderSystem.enableTexture());
            this.texture = Optional.empty();
            this.blur = false;
            this.mipmap = false;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderStateShard.TextureStateShard var0 = (RenderStateShard.TextureStateShard)param0;
                return this.texture.equals(var0.texture) && this.blur == var0.blur && this.mipmap == var0.mipmap;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.texture.hashCode();
        }

        protected Optional<ResourceLocation> texture() {
            return this.texture;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TexturingStateShard extends RenderStateShard {
        public TexturingStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TransparencyStateShard extends RenderStateShard {
        public TransparencyStateShard(String param0, Runnable param1, Runnable param2) {
            super(param0, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WriteMaskStateShard extends RenderStateShard {
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
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                RenderStateShard.WriteMaskStateShard var0 = (RenderStateShard.WriteMaskStateShard)param0;
                return this.writeColor == var0.writeColor && this.writeDepth == var0.writeDepth;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.writeColor, this.writeDepth);
        }
    }
}
