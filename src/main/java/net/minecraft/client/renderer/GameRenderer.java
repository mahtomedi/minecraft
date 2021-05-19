package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GameRenderer implements ResourceManagerReloadListener, AutoCloseable {
    private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean DEPTH_BUFFER_DEBUG = false;
    public static final float PROJECTION_Z_NEAR = 0.05F;
    private final Minecraft minecraft;
    private final ResourceManager resourceManager;
    private final Random random = new Random();
    private float renderDistance;
    public final ItemInHandRenderer itemInHandRenderer;
    private final MapRenderer mapRenderer;
    private final RenderBuffers renderBuffers;
    private int tick;
    private float fov;
    private float oldFov;
    private float darkenWorldAmount;
    private float darkenWorldAmountO;
    private boolean renderHand = true;
    private boolean renderBlockOutline = true;
    private long lastScreenshotAttempt;
    private long lastActiveTime = Util.getMillis();
    private final LightTexture lightTexture;
    private final OverlayTexture overlayTexture = new OverlayTexture();
    private boolean panoramicMode;
    private float zoom = 1.0F;
    private float zoomX;
    private float zoomY;
    public static final int ITEM_ACTIVATION_ANIMATION_LENGTH = 40;
    @Nullable
    private ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;
    @Nullable
    private PostChain postEffect;
    private static final ResourceLocation[] EFFECTS = new ResourceLocation[]{
        new ResourceLocation("shaders/post/notch.json"),
        new ResourceLocation("shaders/post/fxaa.json"),
        new ResourceLocation("shaders/post/art.json"),
        new ResourceLocation("shaders/post/bumpy.json"),
        new ResourceLocation("shaders/post/blobs2.json"),
        new ResourceLocation("shaders/post/pencil.json"),
        new ResourceLocation("shaders/post/color_convolve.json"),
        new ResourceLocation("shaders/post/deconverge.json"),
        new ResourceLocation("shaders/post/flip.json"),
        new ResourceLocation("shaders/post/invert.json"),
        new ResourceLocation("shaders/post/ntsc.json"),
        new ResourceLocation("shaders/post/outline.json"),
        new ResourceLocation("shaders/post/phosphor.json"),
        new ResourceLocation("shaders/post/scan_pincushion.json"),
        new ResourceLocation("shaders/post/sobel.json"),
        new ResourceLocation("shaders/post/bits.json"),
        new ResourceLocation("shaders/post/desaturate.json"),
        new ResourceLocation("shaders/post/green.json"),
        new ResourceLocation("shaders/post/blur.json"),
        new ResourceLocation("shaders/post/wobble.json"),
        new ResourceLocation("shaders/post/blobs.json"),
        new ResourceLocation("shaders/post/antialias.json"),
        new ResourceLocation("shaders/post/creeper.json"),
        new ResourceLocation("shaders/post/spider.json")
    };
    public static final int EFFECT_NONE = EFFECTS.length;
    private int effectIndex = EFFECT_NONE;
    private boolean effectActive;
    private final Camera mainCamera = new Camera();
    public ShaderInstance blitShader;
    private final Map<String, ShaderInstance> shaders = Maps.newHashMap();
    @Nullable
    private static ShaderInstance positionShader;
    @Nullable
    private static ShaderInstance positionColorShader;
    @Nullable
    private static ShaderInstance positionColorTexShader;
    @Nullable
    private static ShaderInstance positionTexShader;
    @Nullable
    private static ShaderInstance positionTexColorShader;
    @Nullable
    private static ShaderInstance blockShader;
    @Nullable
    private static ShaderInstance newEntityShader;
    @Nullable
    private static ShaderInstance particleShader;
    @Nullable
    private static ShaderInstance positionColorLightmapShader;
    @Nullable
    private static ShaderInstance positionColorTexLightmapShader;
    @Nullable
    private static ShaderInstance positionTexColorNormalShader;
    @Nullable
    private static ShaderInstance positionTexLightmapColorShader;
    @Nullable
    private static ShaderInstance rendertypeSolidShader;
    @Nullable
    private static ShaderInstance rendertypeCutoutMippedShader;
    @Nullable
    private static ShaderInstance rendertypeCutoutShader;
    @Nullable
    private static ShaderInstance rendertypeTranslucentShader;
    @Nullable
    private static ShaderInstance rendertypeTranslucentMovingBlockShader;
    @Nullable
    private static ShaderInstance rendertypeTranslucentNoCrumblingShader;
    @Nullable
    private static ShaderInstance rendertypeArmorCutoutNoCullShader;
    @Nullable
    private static ShaderInstance rendertypeEntitySolidShader;
    @Nullable
    private static ShaderInstance rendertypeEntityCutoutShader;
    @Nullable
    private static ShaderInstance rendertypeEntityCutoutNoCullShader;
    @Nullable
    private static ShaderInstance rendertypeEntityCutoutNoCullZOffsetShader;
    @Nullable
    private static ShaderInstance rendertypeItemEntityTranslucentCullShader;
    @Nullable
    private static ShaderInstance rendertypeEntityTranslucentCullShader;
    @Nullable
    private static ShaderInstance rendertypeEntityTranslucentShader;
    @Nullable
    private static ShaderInstance rendertypeEntitySmoothCutoutShader;
    @Nullable
    private static ShaderInstance rendertypeBeaconBeamShader;
    @Nullable
    private static ShaderInstance rendertypeEntityDecalShader;
    @Nullable
    private static ShaderInstance rendertypeEntityNoOutlineShader;
    @Nullable
    private static ShaderInstance rendertypeEntityShadowShader;
    @Nullable
    private static ShaderInstance rendertypeEntityAlphaShader;
    @Nullable
    private static ShaderInstance rendertypeEyesShader;
    @Nullable
    private static ShaderInstance rendertypeEnergySwirlShader;
    @Nullable
    private static ShaderInstance rendertypeLeashShader;
    @Nullable
    private static ShaderInstance rendertypeWaterMaskShader;
    @Nullable
    private static ShaderInstance rendertypeOutlineShader;
    @Nullable
    private static ShaderInstance rendertypeArmorGlintShader;
    @Nullable
    private static ShaderInstance rendertypeArmorEntityGlintShader;
    @Nullable
    private static ShaderInstance rendertypeGlintTranslucentShader;
    @Nullable
    private static ShaderInstance rendertypeGlintShader;
    @Nullable
    private static ShaderInstance rendertypeGlintDirectShader;
    @Nullable
    private static ShaderInstance rendertypeEntityGlintShader;
    @Nullable
    private static ShaderInstance rendertypeEntityGlintDirectShader;
    @Nullable
    private static ShaderInstance rendertypeTextShader;
    @Nullable
    private static ShaderInstance rendertypeTextIntensityShader;
    @Nullable
    private static ShaderInstance rendertypeTextSeeThroughShader;
    @Nullable
    private static ShaderInstance rendertypeTextIntensitySeeThroughShader;
    @Nullable
    private static ShaderInstance rendertypeLightningShader;
    @Nullable
    private static ShaderInstance rendertypeTripwireShader;
    @Nullable
    private static ShaderInstance rendertypeEndPortalShader;
    @Nullable
    private static ShaderInstance rendertypeEndGatewayShader;
    @Nullable
    private static ShaderInstance rendertypeLinesShader;
    @Nullable
    private static ShaderInstance rendertypeCrumblingShader;

    public GameRenderer(Minecraft param0, ResourceManager param1, RenderBuffers param2) {
        this.minecraft = param0;
        this.resourceManager = param1;
        this.itemInHandRenderer = param0.getItemInHandRenderer();
        this.mapRenderer = new MapRenderer(param0.getTextureManager());
        this.lightTexture = new LightTexture(this, param0);
        this.renderBuffers = param2;
        this.postEffect = null;
    }

    @Override
    public void close() {
        this.lightTexture.close();
        this.mapRenderer.close();
        this.overlayTexture.close();
        this.shutdownEffect();
        this.shutdownShaders();
        if (this.blitShader != null) {
            this.blitShader.close();
        }

    }

    public void setRenderHand(boolean param0) {
        this.renderHand = param0;
    }

    public void setRenderBlockOutline(boolean param0) {
        this.renderBlockOutline = param0;
    }

    public void setPanoramicMode(boolean param0) {
        this.panoramicMode = param0;
    }

    public boolean isPanoramicMode() {
        return this.panoramicMode;
    }

    public void shutdownEffect() {
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        this.postEffect = null;
        this.effectIndex = EFFECT_NONE;
    }

    public void togglePostEffect() {
        this.effectActive = !this.effectActive;
    }

    public void checkEntityPostEffect(@Nullable Entity param0) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        this.postEffect = null;
        if (param0 instanceof Creeper) {
            this.loadEffect(new ResourceLocation("shaders/post/creeper.json"));
        } else if (param0 instanceof Spider) {
            this.loadEffect(new ResourceLocation("shaders/post/spider.json"));
        } else if (param0 instanceof EnderMan) {
            this.loadEffect(new ResourceLocation("shaders/post/invert.json"));
        }

    }

    public void cycleEffect() {
        if (this.minecraft.getCameraEntity() instanceof Player) {
            if (this.postEffect != null) {
                this.postEffect.close();
            }

            this.effectIndex = (this.effectIndex + 1) % (EFFECTS.length + 1);
            if (this.effectIndex == EFFECT_NONE) {
                this.postEffect = null;
            } else {
                this.loadEffect(EFFECTS[this.effectIndex]);
            }

        }
    }

    private void loadEffect(ResourceLocation param0) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        try {
            this.postEffect = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), param0);
            this.postEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            this.effectActive = true;
        } catch (IOException var3) {
            LOGGER.warn("Failed to load shader: {}", param0, var3);
            this.effectIndex = EFFECT_NONE;
            this.effectActive = false;
        } catch (JsonSyntaxException var4) {
            LOGGER.warn("Failed to parse shader: {}", param0, var4);
            this.effectIndex = EFFECT_NONE;
            this.effectActive = false;
        }

    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.reloadShaders(param0);
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        this.postEffect = null;
        if (this.effectIndex == EFFECT_NONE) {
            this.checkEntityPostEffect(this.minecraft.getCameraEntity());
        } else {
            this.loadEffect(EFFECTS[this.effectIndex]);
        }

    }

    public void preloadUiShader(ResourceProvider param0) {
        if (this.blitShader != null) {
            throw new RuntimeException("Blit shader already preloaded");
        } else {
            try {
                this.blitShader = new ShaderInstance(param0, "blit_screen", DefaultVertexFormat.BLIT_SCREEN);
            } catch (IOException var3) {
                throw new RuntimeException("could not preload blit shader", var3);
            }

            positionShader = this.preloadShader(param0, "position", DefaultVertexFormat.POSITION);
            positionColorShader = this.preloadShader(param0, "position_color", DefaultVertexFormat.POSITION_COLOR);
            positionColorTexShader = this.preloadShader(param0, "position_color_tex", DefaultVertexFormat.POSITION_COLOR_TEX);
            positionTexShader = this.preloadShader(param0, "position_tex", DefaultVertexFormat.POSITION_TEX);
            positionTexColorShader = this.preloadShader(param0, "position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR);
            rendertypeTextShader = this.preloadShader(param0, "rendertype_text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }
    }

    private ShaderInstance preloadShader(ResourceProvider param0, String param1, VertexFormat param2) {
        try {
            ShaderInstance var0 = new ShaderInstance(param0, param1, param2);
            this.shaders.put(param1, var0);
            return var0;
        } catch (Exception var5) {
            throw new IllegalStateException("could not preload shader " + param1, var5);
        }
    }

    public void reloadShaders(ResourceManager param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        List<Pair<ShaderInstance, Consumer<ShaderInstance>>> var0 = Lists.newArrayListWithCapacity(this.shaders.size());

        try {
            var0.add(Pair.of(new ShaderInstance(param0, "block", DefaultVertexFormat.BLOCK), param0x -> blockShader = param0x));
            var0.add(Pair.of(new ShaderInstance(param0, "new_entity", DefaultVertexFormat.NEW_ENTITY), param0x -> newEntityShader = param0x));
            var0.add(Pair.of(new ShaderInstance(param0, "particle", DefaultVertexFormat.PARTICLE), param0x -> particleShader = param0x));
            var0.add(Pair.of(new ShaderInstance(param0, "position", DefaultVertexFormat.POSITION), param0x -> positionShader = param0x));
            var0.add(Pair.of(new ShaderInstance(param0, "position_color", DefaultVertexFormat.POSITION_COLOR), param0x -> positionColorShader = param0x));
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "position_color_lightmap", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP),
                    param0x -> positionColorLightmapShader = param0x
                )
            );
            var0.add(
                Pair.of(new ShaderInstance(param0, "position_color_tex", DefaultVertexFormat.POSITION_COLOR_TEX), param0x -> positionColorTexShader = param0x)
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                    param0x -> positionColorTexLightmapShader = param0x
                )
            );
            var0.add(Pair.of(new ShaderInstance(param0, "position_tex", DefaultVertexFormat.POSITION_TEX), param0x -> positionTexShader = param0x));
            var0.add(
                Pair.of(new ShaderInstance(param0, "position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR), param0x -> positionTexColorShader = param0x)
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "position_tex_color_normal", DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL),
                    param0x -> positionTexColorNormalShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "position_tex_lightmap_color", DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR),
                    param0x -> positionTexLightmapColorShader = param0x
                )
            );
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_solid", DefaultVertexFormat.BLOCK), param0x -> rendertypeSolidShader = param0x));
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_cutout_mipped", DefaultVertexFormat.BLOCK), param0x -> rendertypeCutoutMippedShader = param0x)
            );
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_cutout", DefaultVertexFormat.BLOCK), param0x -> rendertypeCutoutShader = param0x));
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_translucent", DefaultVertexFormat.BLOCK), param0x -> rendertypeTranslucentShader = param0x));
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_translucent_moving_block", DefaultVertexFormat.BLOCK),
                    param0x -> rendertypeTranslucentMovingBlockShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_translucent_no_crumbling", DefaultVertexFormat.BLOCK),
                    param0x -> rendertypeTranslucentNoCrumblingShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY),
                    param0x -> rendertypeArmorCutoutNoCullShader = param0x
                )
            );
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_entity_solid", DefaultVertexFormat.NEW_ENTITY), param0x -> rendertypeEntitySolidShader = param0x)
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_cutout", DefaultVertexFormat.NEW_ENTITY), param0x -> rendertypeEntityCutoutShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY),
                    param0x -> rendertypeEntityCutoutNoCullShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_cutout_no_cull_z_offset", DefaultVertexFormat.NEW_ENTITY),
                    param0x -> rendertypeEntityCutoutNoCullZOffsetShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY),
                    param0x -> rendertypeItemEntityTranslucentCullShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY),
                    param0x -> rendertypeEntityTranslucentCullShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_translucent", DefaultVertexFormat.NEW_ENTITY),
                    param0x -> rendertypeEntityTranslucentShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY),
                    param0x -> rendertypeEntitySmoothCutoutShader = param0x
                )
            );
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_beacon_beam", DefaultVertexFormat.BLOCK), param0x -> rendertypeBeaconBeamShader = param0x));
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_entity_decal", DefaultVertexFormat.NEW_ENTITY), param0x -> rendertypeEntityDecalShader = param0x)
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_no_outline", DefaultVertexFormat.NEW_ENTITY),
                    param0x -> rendertypeEntityNoOutlineShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_shadow", DefaultVertexFormat.NEW_ENTITY), param0x -> rendertypeEntityShadowShader = param0x
                )
            );
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_entity_alpha", DefaultVertexFormat.NEW_ENTITY), param0x -> rendertypeEntityAlphaShader = param0x)
            );
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_eyes", DefaultVertexFormat.NEW_ENTITY), param0x -> rendertypeEyesShader = param0x));
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_energy_swirl", DefaultVertexFormat.NEW_ENTITY), param0x -> rendertypeEnergySwirlShader = param0x)
            );
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_leash", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), param0x -> rendertypeLeashShader = param0x)
            );
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_water_mask", DefaultVertexFormat.POSITION), param0x -> rendertypeWaterMaskShader = param0x));
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_outline", DefaultVertexFormat.POSITION_COLOR_TEX), param0x -> rendertypeOutlineShader = param0x)
            );
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_armor_glint", DefaultVertexFormat.POSITION_TEX), param0x -> rendertypeArmorGlintShader = param0x)
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_armor_entity_glint", DefaultVertexFormat.POSITION_TEX),
                    param0x -> rendertypeArmorEntityGlintShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_glint_translucent", DefaultVertexFormat.POSITION_TEX),
                    param0x -> rendertypeGlintTranslucentShader = param0x
                )
            );
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_glint", DefaultVertexFormat.POSITION_TEX), param0x -> rendertypeGlintShader = param0x));
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_glint_direct", DefaultVertexFormat.POSITION_TEX), param0x -> rendertypeGlintDirectShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_glint", DefaultVertexFormat.POSITION_TEX), param0x -> rendertypeEntityGlintShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_entity_glint_direct", DefaultVertexFormat.POSITION_TEX),
                    param0x -> rendertypeEntityGlintDirectShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), param0x -> rendertypeTextShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_text_intensity", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                    param0x -> rendertypeTextIntensityShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_text_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                    param0x -> rendertypeTextSeeThroughShader = param0x
                )
            );
            var0.add(
                Pair.of(
                    new ShaderInstance(param0, "rendertype_text_intensity_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                    param0x -> rendertypeTextIntensitySeeThroughShader = param0x
                )
            );
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_lightning", DefaultVertexFormat.POSITION_COLOR), param0x -> rendertypeLightningShader = param0x)
            );
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_tripwire", DefaultVertexFormat.BLOCK), param0x -> rendertypeTripwireShader = param0x));
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_end_portal", DefaultVertexFormat.POSITION), param0x -> rendertypeEndPortalShader = param0x));
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_end_gateway", DefaultVertexFormat.POSITION), param0x -> rendertypeEndGatewayShader = param0x)
            );
            var0.add(
                Pair.of(new ShaderInstance(param0, "rendertype_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL), param0x -> rendertypeLinesShader = param0x)
            );
            var0.add(Pair.of(new ShaderInstance(param0, "rendertype_crumbling", DefaultVertexFormat.BLOCK), param0x -> rendertypeCrumblingShader = param0x));
        } catch (IOException var4) {
            var0.forEach(param0x -> param0x.getFirst().close());
            throw new RuntimeException("could not reload shaders", var4);
        }

        this.shutdownShaders();
        var0.forEach(param0x -> {
            ShaderInstance var0x = param0x.getFirst();
            this.shaders.put(var0x.getName(), var0x);
            param0x.getSecond().accept(var0x);
        });
    }

    private void shutdownShaders() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        this.shaders.values().forEach(ShaderInstance::close);
        this.shaders.clear();
    }

    @Nullable
    public ShaderInstance getShader(@Nullable String param0) {
        return param0 == null ? null : this.shaders.get(param0);
    }

    public void tick() {
        this.tickFov();
        this.lightTexture.tick();
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.mainCamera.tick();
        ++this.tick;
        this.itemInHandRenderer.tick();
        this.minecraft.levelRenderer.tickRain(this.mainCamera);
        this.darkenWorldAmountO = this.darkenWorldAmount;
        if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
            this.darkenWorldAmount += 0.05F;
            if (this.darkenWorldAmount > 1.0F) {
                this.darkenWorldAmount = 1.0F;
            }
        } else if (this.darkenWorldAmount > 0.0F) {
            this.darkenWorldAmount -= 0.0125F;
        }

        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }

    }

    @Nullable
    public PostChain currentEffect() {
        return this.postEffect;
    }

    public void resize(int param0, int param1) {
        if (this.postEffect != null) {
            this.postEffect.resize(param0, param1);
        }

        this.minecraft.levelRenderer.resize(param0, param1);
    }

    public void pick(float param0) {
        Entity var0 = this.minecraft.getCameraEntity();
        if (var0 != null) {
            if (this.minecraft.level != null) {
                this.minecraft.getProfiler().push("pick");
                this.minecraft.crosshairPickEntity = null;
                double var1 = (double)this.minecraft.gameMode.getPickRange();
                this.minecraft.hitResult = var0.pick(var1, param0, false);
                Vec3 var2 = var0.getEyePosition(param0);
                boolean var3 = false;
                int var4 = 3;
                double var5 = var1;
                if (this.minecraft.gameMode.hasFarPickRange()) {
                    var5 = 6.0;
                    var1 = var5;
                } else {
                    if (var1 > 3.0) {
                        var3 = true;
                    }

                    var1 = var1;
                }

                var5 *= var5;
                if (this.minecraft.hitResult != null) {
                    var5 = this.minecraft.hitResult.getLocation().distanceToSqr(var2);
                }

                Vec3 var6 = var0.getViewVector(1.0F);
                Vec3 var7 = var2.add(var6.x * var1, var6.y * var1, var6.z * var1);
                float var8 = 1.0F;
                AABB var9 = var0.getBoundingBox().expandTowards(var6.scale(var1)).inflate(1.0, 1.0, 1.0);
                EntityHitResult var10 = ProjectileUtil.getEntityHitResult(
                    var0, var2, var7, var9, param0x -> !param0x.isSpectator() && param0x.isPickable(), var5
                );
                if (var10 != null) {
                    Entity var11 = var10.getEntity();
                    Vec3 var12 = var10.getLocation();
                    double var13 = var2.distanceToSqr(var12);
                    if (var3 && var13 > 9.0) {
                        this.minecraft.hitResult = BlockHitResult.miss(var12, Direction.getNearest(var6.x, var6.y, var6.z), new BlockPos(var12));
                    } else if (var13 < var5 || this.minecraft.hitResult == null) {
                        this.minecraft.hitResult = var10;
                        if (var11 instanceof LivingEntity || var11 instanceof ItemFrame) {
                            this.minecraft.crosshairPickEntity = var11;
                        }
                    }
                }

                this.minecraft.getProfiler().pop();
            }
        }
    }

    private void tickFov() {
        float var0 = 1.0F;
        if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer var1) {
            var0 = var1.getFieldOfViewModifier();
        }

        this.oldFov = this.fov;
        this.fov += (var0 - this.fov) * 0.5F;
        if (this.fov > 1.5F) {
            this.fov = 1.5F;
        }

        if (this.fov < 0.1F) {
            this.fov = 0.1F;
        }

    }

    private double getFov(Camera param0, float param1, boolean param2) {
        if (this.panoramicMode) {
            return 90.0;
        } else {
            double var0 = 70.0;
            if (param2) {
                var0 = this.minecraft.options.fov;
                var0 *= (double)Mth.lerp(param1, this.oldFov, this.fov);
            }

            if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).isDeadOrDying()) {
                float var1 = Math.min((float)((LivingEntity)param0.getEntity()).deathTime + param1, 20.0F);
                var0 /= (double)((1.0F - 500.0F / (var1 + 500.0F)) * 2.0F + 1.0F);
            }

            FogType var2 = param0.getFluidInCamera();
            if (var2 == FogType.LAVA || var2 == FogType.WATER) {
                var0 *= (double)Mth.lerp(this.minecraft.options.fovEffectScale, 1.0F, 0.85714287F);
            }

            return var0;
        }
    }

    private void bobHurt(PoseStack param0, float param1) {
        if (this.minecraft.getCameraEntity() instanceof LivingEntity var0) {
            float var1 = (float)var0.hurtTime - param1;
            if (var0.isDeadOrDying()) {
                float var2 = Math.min((float)var0.deathTime + param1, 20.0F);
                param0.mulPose(Vector3f.ZP.rotationDegrees(40.0F - 8000.0F / (var2 + 200.0F)));
            }

            if (var1 < 0.0F) {
                return;
            }

            var1 /= (float)var0.hurtDuration;
            var1 = Mth.sin(var1 * var1 * var1 * var1 * (float) Math.PI);
            float var3 = var0.hurtDir;
            param0.mulPose(Vector3f.YP.rotationDegrees(-var3));
            param0.mulPose(Vector3f.ZP.rotationDegrees(-var1 * 14.0F));
            param0.mulPose(Vector3f.YP.rotationDegrees(var3));
        }

    }

    private void bobView(PoseStack param0, float param1) {
        if (this.minecraft.getCameraEntity() instanceof Player) {
            Player var0 = (Player)this.minecraft.getCameraEntity();
            float var1 = var0.walkDist - var0.walkDistO;
            float var2 = -(var0.walkDist + var1 * param1);
            float var3 = Mth.lerp(param1, var0.oBob, var0.bob);
            param0.translate((double)(Mth.sin(var2 * (float) Math.PI) * var3 * 0.5F), (double)(-Math.abs(Mth.cos(var2 * (float) Math.PI) * var3)), 0.0);
            param0.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(var2 * (float) Math.PI) * var3 * 3.0F));
            param0.mulPose(Vector3f.XP.rotationDegrees(Math.abs(Mth.cos(var2 * (float) Math.PI - 0.2F) * var3) * 5.0F));
        }
    }

    public void renderZoomed(float param0, float param1, float param2) {
        this.zoom = param0;
        this.zoomX = param1;
        this.zoomY = param2;
        this.setRenderBlockOutline(false);
        this.setRenderHand(false);
        this.renderLevel(1.0F, 0L, new PoseStack());
        this.zoom = 1.0F;
    }

    private void renderItemInHand(PoseStack param0, Camera param1, float param2) {
        if (!this.panoramicMode) {
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(param1, param2, false)));
            PoseStack.Pose var0 = param0.last();
            var0.pose().setIdentity();
            var0.normal().setIdentity();
            param0.pushPose();
            this.bobHurt(param0, param2);
            if (this.minecraft.options.bobView) {
                this.bobView(param0, param2);
            }

            boolean var1 = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
            if (this.minecraft.options.getCameraType().isFirstPerson()
                && !var1
                && !this.minecraft.options.hideGui
                && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.lightTexture.turnOnLightLayer();
                this.itemInHandRenderer
                    .renderHandsWithItems(
                        param2,
                        param0,
                        this.renderBuffers.bufferSource(),
                        this.minecraft.player,
                        this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, param2)
                    );
                this.lightTexture.turnOffLightLayer();
            }

            param0.popPose();
            if (this.minecraft.options.getCameraType().isFirstPerson() && !var1) {
                ScreenEffectRenderer.renderScreenEffect(this.minecraft, param0);
                this.bobHurt(param0, param2);
            }

            if (this.minecraft.options.bobView) {
                this.bobView(param0, param2);
            }

        }
    }

    public void resetProjectionMatrix(Matrix4f param0) {
        RenderSystem.setProjectionMatrix(param0);
    }

    public Matrix4f getProjectionMatrix(double param0) {
        PoseStack var0 = new PoseStack();
        var0.last().pose().setIdentity();
        if (this.zoom != 1.0F) {
            var0.translate((double)this.zoomX, (double)(-this.zoomY), 0.0);
            var0.scale(this.zoom, this.zoom, 1.0F);
        }

        var0.last()
            .pose()
            .multiply(
                Matrix4f.perspective(
                    param0, (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(), 0.05F, this.getDepthFar()
                )
            );
        return var0.last().pose();
    }

    public float getDepthFar() {
        return this.renderDistance * 4.0F;
    }

    public static float getNightVisionScale(LivingEntity param0, float param1) {
        int var0 = param0.getEffect(MobEffects.NIGHT_VISION).getDuration();
        return var0 > 200 ? 1.0F : 0.7F + Mth.sin(((float)var0 - param1) * (float) Math.PI * 0.2F) * 0.3F;
    }

    public void render(float param0, long param1, boolean param2) {
        if (!this.minecraft.isWindowActive()
            && this.minecraft.options.pauseOnLostFocus
            && (!this.minecraft.options.touchscreen || !this.minecraft.mouseHandler.isRightPressed())) {
            if (Util.getMillis() - this.lastActiveTime > 500L) {
                this.minecraft.pauseGame(false);
            }
        } else {
            this.lastActiveTime = Util.getMillis();
        }

        if (!this.minecraft.noRender) {
            int var0 = (int)(
                this.minecraft.mouseHandler.xpos()
                    * (double)this.minecraft.getWindow().getGuiScaledWidth()
                    / (double)this.minecraft.getWindow().getScreenWidth()
            );
            int var1 = (int)(
                this.minecraft.mouseHandler.ypos()
                    * (double)this.minecraft.getWindow().getGuiScaledHeight()
                    / (double)this.minecraft.getWindow().getScreenHeight()
            );
            RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            if (param2 && this.minecraft.level != null) {
                this.minecraft.getProfiler().push("level");
                this.renderLevel(param0, param1, new PoseStack());
                if (this.minecraft.hasSingleplayerServer() && this.lastScreenshotAttempt < Util.getMillis() - 1000L) {
                    this.lastScreenshotAttempt = Util.getMillis();
                    if (!this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
                        this.takeAutoScreenshot();
                    }
                }

                this.minecraft.levelRenderer.doEntityOutline();
                if (this.postEffect != null && this.effectActive) {
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableTexture();
                    RenderSystem.resetTextureMatrix();
                    this.postEffect.process(param0);
                }

                this.minecraft.getMainRenderTarget().bindWrite(true);
            }

            Window var2 = this.minecraft.getWindow();
            RenderSystem.clear(256, Minecraft.ON_OSX);
            Matrix4f var3 = Matrix4f.orthographic(
                0.0F, (float)((double)var2.getWidth() / var2.getGuiScale()), 0.0F, (float)((double)var2.getHeight() / var2.getGuiScale()), 1000.0F, 3000.0F
            );
            RenderSystem.setProjectionMatrix(var3);
            PoseStack var4 = RenderSystem.getModelViewStack();
            var4.setIdentity();
            var4.translate(0.0, 0.0, -2000.0);
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();
            PoseStack var5 = new PoseStack();
            if (param2 && this.minecraft.level != null) {
                this.minecraft.getProfiler().popPush("gui");
                if (this.minecraft.player != null) {
                    float var6 = Mth.lerp(param0, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
                    if (var6 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONFUSION) && this.minecraft.options.screenEffectScale < 1.0F) {
                        this.renderConfusionOverlay(var6 * (1.0F - this.minecraft.options.screenEffectScale));
                    }
                }

                if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
                    this.renderItemActivationAnimation(this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), param0);
                    this.minecraft.gui.render(var5, param0);
                    RenderSystem.clear(256, Minecraft.ON_OSX);
                }

                this.minecraft.getProfiler().pop();
            }

            if (this.minecraft.getOverlay() != null) {
                try {
                    this.minecraft.getOverlay().render(var5, var0, var1, this.minecraft.getDeltaFrameTime());
                } catch (Throwable var16) {
                    CrashReport var8 = CrashReport.forThrowable(var16, "Rendering overlay");
                    CrashReportCategory var9 = var8.addCategory("Overlay render details");
                    var9.setDetail("Overlay name", () -> this.minecraft.getOverlay().getClass().getCanonicalName());
                    throw new ReportedException(var8);
                }
            } else if (this.minecraft.screen != null) {
                try {
                    this.minecraft.screen.render(var5, var0, var1, this.minecraft.getDeltaFrameTime());
                } catch (Throwable var151) {
                    CrashReport var11 = CrashReport.forThrowable(var151, "Rendering screen");
                    CrashReportCategory var12 = var11.addCategory("Screen render details");
                    var12.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                    var12.setDetail(
                        "Mouse location",
                        () -> String.format(
                                Locale.ROOT,
                                "Scaled: (%d, %d). Absolute: (%f, %f)",
                                var0,
                                var1,
                                this.minecraft.mouseHandler.xpos(),
                                this.minecraft.mouseHandler.ypos()
                            )
                    );
                    var12.setDetail(
                        "Screen size",
                        () -> String.format(
                                Locale.ROOT,
                                "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f",
                                this.minecraft.getWindow().getGuiScaledWidth(),
                                this.minecraft.getWindow().getGuiScaledHeight(),
                                this.minecraft.getWindow().getWidth(),
                                this.minecraft.getWindow().getHeight(),
                                this.minecraft.getWindow().getGuiScale()
                            )
                    );
                    throw new ReportedException(var11);
                }

                try {
                    if (this.minecraft.screen != null) {
                        this.minecraft.screen.handleDelayedNarration();
                    }
                } catch (Throwable var141) {
                    CrashReport var14 = CrashReport.forThrowable(var141, "Narrating screen");
                    CrashReportCategory var15 = var14.addCategory("Screen details");
                    var15.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                    throw new ReportedException(var14);
                }
            }

        }
    }

    private void takeAutoScreenshot() {
        if (this.minecraft.levelRenderer.countRenderedChunks() > 10
            && this.minecraft.levelRenderer.hasRenderedAllChunks()
            && !this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
            NativeImage var0 = Screenshot.takeScreenshot(
                this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.minecraft.getMainRenderTarget()
            );
            Util.ioPool().execute(() -> {
                int var0x = var0.getWidth();
                int var1x = var0.getHeight();
                int var2 = 0;
                int var3 = 0;
                if (var0x > var1x) {
                    var2 = (var0x - var1x) / 2;
                    var0x = var1x;
                } else {
                    var3 = (var1x - var0x) / 2;
                    var1x = var0x;
                }

                try (NativeImage var4 = new NativeImage(64, 64, false)) {
                    var0.resizeSubRectTo(var2, var3, var0x, var1x, var4);
                    var4.writeToFile(this.minecraft.getSingleplayerServer().getWorldScreenshotFile());
                } catch (IOException var16) {
                    LOGGER.warn("Couldn't save auto screenshot", (Throwable)var16);
                } finally {
                    var0.close();
                }

            });
        }

    }

    private boolean shouldRenderBlockOutline() {
        if (!this.renderBlockOutline) {
            return false;
        } else {
            Entity var0 = this.minecraft.getCameraEntity();
            boolean var1 = var0 instanceof Player && !this.minecraft.options.hideGui;
            if (var1 && !((Player)var0).getAbilities().mayBuild) {
                ItemStack var2 = ((LivingEntity)var0).getMainHandItem();
                HitResult var3 = this.minecraft.hitResult;
                if (var3 != null && var3.getType() == HitResult.Type.BLOCK) {
                    BlockPos var4 = ((BlockHitResult)var3).getBlockPos();
                    BlockState var5 = this.minecraft.level.getBlockState(var4);
                    if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                        var1 = var5.getMenuProvider(this.minecraft.level, var4) != null;
                    } else {
                        BlockInWorld var6 = new BlockInWorld(this.minecraft.level, var4, false);
                        var1 = !var2.isEmpty()
                            && (
                                var2.hasAdventureModeBreakTagForBlock(this.minecraft.level.getTagManager(), var6)
                                    || var2.hasAdventureModePlaceTagForBlock(this.minecraft.level.getTagManager(), var6)
                            );
                    }
                }
            }

            return var1;
        }
    }

    public void renderLevel(float param0, long param1, PoseStack param2) {
        this.lightTexture.updateLightTexture(param0);
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.pick(param0);
        this.minecraft.getProfiler().push("center");
        boolean var0 = this.shouldRenderBlockOutline();
        this.minecraft.getProfiler().popPush("camera");
        Camera var1 = this.mainCamera;
        this.renderDistance = (float)(this.minecraft.options.renderDistance * 16);
        PoseStack var2 = new PoseStack();
        double var3 = this.getFov(var1, param0, true);
        var2.last().pose().multiply(this.getProjectionMatrix(var3));
        this.bobHurt(var2, param0);
        if (this.minecraft.options.bobView) {
            this.bobView(var2, param0);
        }

        float var4 = Mth.lerp(param0, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime)
            * this.minecraft.options.screenEffectScale
            * this.minecraft.options.screenEffectScale;
        if (var4 > 0.0F) {
            int var5 = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
            float var6 = 5.0F / (var4 * var4 + 5.0F) - var4 * 0.04F;
            var6 *= var6;
            Vector3f var7 = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
            var2.mulPose(var7.rotationDegrees(((float)this.tick + param0) * (float)var5));
            var2.scale(1.0F / var6, 1.0F, 1.0F);
            float var8 = -((float)this.tick + param0) * (float)var5;
            var2.mulPose(var7.rotationDegrees(var8));
        }

        Matrix4f var9 = var2.last().pose();
        this.resetProjectionMatrix(var9);
        var1.setup(
            this.minecraft.level,
            (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()),
            !this.minecraft.options.getCameraType().isFirstPerson(),
            this.minecraft.options.getCameraType().isMirrored(),
            param0
        );
        param2.mulPose(Vector3f.XP.rotationDegrees(var1.getXRot()));
        param2.mulPose(Vector3f.YP.rotationDegrees(var1.getYRot() + 180.0F));
        this.minecraft.levelRenderer.prepareCullFrustum(param2, var1.getPosition(), this.getProjectionMatrix(Math.max(var3, this.minecraft.options.fov)));
        this.minecraft.levelRenderer.renderLevel(param2, param0, param1, var0, var1, this, this.lightTexture, var9);
        this.minecraft.getProfiler().popPush("hand");
        if (this.renderHand) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            this.renderItemInHand(param2, var1, param0);
        }

        this.minecraft.getProfiler().pop();
    }

    public void resetData() {
        this.itemActivationItem = null;
        this.mapRenderer.resetData();
        this.mainCamera.reset();
    }

    public MapRenderer getMapRenderer() {
        return this.mapRenderer;
    }

    public void displayItemActivation(ItemStack param0) {
        this.itemActivationItem = param0;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = this.random.nextFloat() * 2.0F - 1.0F;
        this.itemActivationOffY = this.random.nextFloat() * 2.0F - 1.0F;
    }

    private void renderItemActivationAnimation(int param0, int param1, float param2) {
        if (this.itemActivationItem != null && this.itemActivationTicks > 0) {
            int var0 = 40 - this.itemActivationTicks;
            float var1 = ((float)var0 + param2) / 40.0F;
            float var2 = var1 * var1;
            float var3 = var1 * var2;
            float var4 = 10.25F * var3 * var2 - 24.95F * var2 * var2 + 25.5F * var3 - 13.8F * var2 + 4.0F * var1;
            float var5 = var4 * (float) Math.PI;
            float var6 = this.itemActivationOffX * (float)(param0 / 4);
            float var7 = this.itemActivationOffY * (float)(param1 / 4);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            PoseStack var8 = new PoseStack();
            var8.pushPose();
            var8.translate(
                (double)((float)(param0 / 2) + var6 * Mth.abs(Mth.sin(var5 * 2.0F))),
                (double)((float)(param1 / 2) + var7 * Mth.abs(Mth.sin(var5 * 2.0F))),
                -50.0
            );
            float var9 = 50.0F + 175.0F * Mth.sin(var5);
            var8.scale(var9, -var9, var9);
            var8.mulPose(Vector3f.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(var5))));
            var8.mulPose(Vector3f.XP.rotationDegrees(6.0F * Mth.cos(var1 * 8.0F)));
            var8.mulPose(Vector3f.ZP.rotationDegrees(6.0F * Mth.cos(var1 * 8.0F)));
            MultiBufferSource.BufferSource var10 = this.renderBuffers.bufferSource();
            this.minecraft
                .getItemRenderer()
                .renderStatic(this.itemActivationItem, ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, var8, var10, 0);
            var8.popPose();
            var10.endBatch();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
        }
    }

    private void renderConfusionOverlay(float param0) {
        int var0 = this.minecraft.getWindow().getGuiScaledWidth();
        int var1 = this.minecraft.getWindow().getGuiScaledHeight();
        double var2 = Mth.lerp((double)param0, 2.0, 1.0);
        float var3 = 0.2F * param0;
        float var4 = 0.4F * param0;
        float var5 = 0.2F * param0;
        double var6 = (double)var0 * var2;
        double var7 = (double)var1 * var2;
        double var8 = ((double)var0 - var6) / 2.0;
        double var9 = ((double)var1 - var7) / 2.0;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE
        );
        RenderSystem.setShaderColor(var3, var4, var5, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, NAUSEA_LOCATION);
        Tesselator var10 = Tesselator.getInstance();
        BufferBuilder var11 = var10.getBuilder();
        var11.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var11.vertex(var8, var9 + var7, -90.0).uv(0.0F, 1.0F).endVertex();
        var11.vertex(var8 + var6, var9 + var7, -90.0).uv(1.0F, 1.0F).endVertex();
        var11.vertex(var8 + var6, var9, -90.0).uv(1.0F, 0.0F).endVertex();
        var11.vertex(var8, var9, -90.0).uv(0.0F, 0.0F).endVertex();
        var10.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public float getDarkenWorldAmount(float param0) {
        return Mth.lerp(param0, this.darkenWorldAmountO, this.darkenWorldAmount);
    }

    public float getRenderDistance() {
        return this.renderDistance;
    }

    public Camera getMainCamera() {
        return this.mainCamera;
    }

    public LightTexture lightTexture() {
        return this.lightTexture;
    }

    public OverlayTexture overlayTexture() {
        return this.overlayTexture;
    }

    @Nullable
    public static ShaderInstance getPositionShader() {
        return positionShader;
    }

    @Nullable
    public static ShaderInstance getPositionColorShader() {
        return positionColorShader;
    }

    @Nullable
    public static ShaderInstance getPositionColorTexShader() {
        return positionColorTexShader;
    }

    @Nullable
    public static ShaderInstance getPositionTexShader() {
        return positionTexShader;
    }

    @Nullable
    public static ShaderInstance getPositionTexColorShader() {
        return positionTexColorShader;
    }

    @Nullable
    public static ShaderInstance getBlockShader() {
        return blockShader;
    }

    @Nullable
    public static ShaderInstance getNewEntityShader() {
        return newEntityShader;
    }

    @Nullable
    public static ShaderInstance getParticleShader() {
        return particleShader;
    }

    @Nullable
    public static ShaderInstance getPositionColorLightmapShader() {
        return positionColorLightmapShader;
    }

    @Nullable
    public static ShaderInstance getPositionColorTexLightmapShader() {
        return positionColorTexLightmapShader;
    }

    @Nullable
    public static ShaderInstance getPositionTexColorNormalShader() {
        return positionTexColorNormalShader;
    }

    @Nullable
    public static ShaderInstance getPositionTexLightmapColorShader() {
        return positionTexLightmapColorShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeSolidShader() {
        return rendertypeSolidShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeCutoutMippedShader() {
        return rendertypeCutoutMippedShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeCutoutShader() {
        return rendertypeCutoutShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTranslucentShader() {
        return rendertypeTranslucentShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTranslucentMovingBlockShader() {
        return rendertypeTranslucentMovingBlockShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTranslucentNoCrumblingShader() {
        return rendertypeTranslucentNoCrumblingShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeArmorCutoutNoCullShader() {
        return rendertypeArmorCutoutNoCullShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntitySolidShader() {
        return rendertypeEntitySolidShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityCutoutShader() {
        return rendertypeEntityCutoutShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityCutoutNoCullShader() {
        return rendertypeEntityCutoutNoCullShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityCutoutNoCullZOffsetShader() {
        return rendertypeEntityCutoutNoCullZOffsetShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeItemEntityTranslucentCullShader() {
        return rendertypeItemEntityTranslucentCullShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityTranslucentCullShader() {
        return rendertypeEntityTranslucentCullShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityTranslucentShader() {
        return rendertypeEntityTranslucentShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntitySmoothCutoutShader() {
        return rendertypeEntitySmoothCutoutShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeBeaconBeamShader() {
        return rendertypeBeaconBeamShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityDecalShader() {
        return rendertypeEntityDecalShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityNoOutlineShader() {
        return rendertypeEntityNoOutlineShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityShadowShader() {
        return rendertypeEntityShadowShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityAlphaShader() {
        return rendertypeEntityAlphaShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEyesShader() {
        return rendertypeEyesShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEnergySwirlShader() {
        return rendertypeEnergySwirlShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeLeashShader() {
        return rendertypeLeashShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeWaterMaskShader() {
        return rendertypeWaterMaskShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeOutlineShader() {
        return rendertypeOutlineShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeArmorGlintShader() {
        return rendertypeArmorGlintShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeArmorEntityGlintShader() {
        return rendertypeArmorEntityGlintShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGlintTranslucentShader() {
        return rendertypeGlintTranslucentShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGlintShader() {
        return rendertypeGlintShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGlintDirectShader() {
        return rendertypeGlintDirectShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityGlintShader() {
        return rendertypeEntityGlintShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityGlintDirectShader() {
        return rendertypeEntityGlintDirectShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextShader() {
        return rendertypeTextShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextIntensityShader() {
        return rendertypeTextIntensityShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextSeeThroughShader() {
        return rendertypeTextSeeThroughShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextIntensitySeeThroughShader() {
        return rendertypeTextIntensitySeeThroughShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeLightningShader() {
        return rendertypeLightningShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTripwireShader() {
        return rendertypeTripwireShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEndPortalShader() {
        return rendertypeEndPortalShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEndGatewayShader() {
        return rendertypeEndGatewayShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeLinesShader() {
        return rendertypeLinesShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeCrumblingShader() {
        return rendertypeCrumblingShader;
    }
}
