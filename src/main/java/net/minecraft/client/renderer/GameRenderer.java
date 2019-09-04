package net.minecraft.client.renderer;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.FrustumCuller;
import net.minecraft.client.renderer.culling.FrustumData;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleResource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GameRenderer implements AutoCloseable, ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
    private final Minecraft minecraft;
    private final ResourceManager resourceManager;
    private final Random random = new Random();
    private float renderDistance;
    public final ItemInHandRenderer itemInHandRenderer;
    private final MapRenderer mapRenderer;
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
    private int rainSoundTime;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];
    private final FogRenderer fog;
    private boolean panoramicMode;
    private double zoom = 1.0;
    private double zoom_x;
    private double zoom_y;
    private ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;
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
    private int frameId;
    private final Camera mainCamera = new Camera();

    public GameRenderer(Minecraft param0, ResourceManager param1) {
        this.minecraft = param0;
        this.resourceManager = param1;
        this.itemInHandRenderer = param0.getItemInHandRenderer();
        this.mapRenderer = new MapRenderer(param0.getTextureManager());
        this.lightTexture = new LightTexture(this);
        this.fog = new FogRenderer(this);
        this.postEffect = null;

        for(int var0 = 0; var0 < 32; ++var0) {
            for(int var1 = 0; var1 < 32; ++var1) {
                float var2 = (float)(var1 - 16);
                float var3 = (float)(var0 - 16);
                float var4 = Mth.sqrt(var2 * var2 + var3 * var3);
                this.rainSizeX[var0 << 5 | var1] = -var3 / var4;
                this.rainSizeZ[var0 << 5 | var1] = var2 / var4;
            }
        }

    }

    @Override
    public void close() {
        this.lightTexture.close();
        this.mapRenderer.close();
        this.shutdownEffect();
    }

    public boolean postEffectActive() {
        return this.postEffect != null;
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

    private void loadEffect(ResourceLocation param0) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        try {
            this.postEffect = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), param0);
            this.postEffect.resize(this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
            this.effectActive = true;
        } catch (IOException var3) {
            LOGGER.warn("Failed to load shader: {}", param0, var3);
            this.effectIndex = EFFECT_NONE;
            this.effectActive = false;
        } catch (JsonSyntaxException var4) {
            LOGGER.warn("Failed to load shader: {}", param0, var4);
            this.effectIndex = EFFECT_NONE;
            this.effectActive = false;
        }

    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
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

    public void tick() {
        this.tickFov();
        this.lightTexture.tick();
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.mainCamera.tick();
        ++this.tick;
        this.itemInHandRenderer.tick();
        this.tickRain();
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
        if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer) {
            AbstractClientPlayer var1 = (AbstractClientPlayer)this.minecraft.getCameraEntity();
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

            if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).getHealth() <= 0.0F) {
                float var1 = Math.min((float)((LivingEntity)param0.getEntity()).deathTime + param1, 20.0F);
                var0 /= (double)((1.0F - 500.0F / (var1 + 500.0F)) * 2.0F + 1.0F);
            }

            FluidState var2 = param0.getFluidInCamera();
            if (!var2.isEmpty()) {
                var0 = var0 * 60.0 / 70.0;
            }

            return var0;
        }
    }

    private void bobHurt(float param0) {
        if (this.minecraft.getCameraEntity() instanceof LivingEntity) {
            LivingEntity var0 = (LivingEntity)this.minecraft.getCameraEntity();
            float var1 = (float)var0.hurtTime - param0;
            if (var0.getHealth() <= 0.0F) {
                float var2 = Math.min((float)var0.deathTime + param0, 20.0F);
                RenderSystem.rotatef(40.0F - 8000.0F / (var2 + 200.0F), 0.0F, 0.0F, 1.0F);
            }

            if (var1 < 0.0F) {
                return;
            }

            var1 /= (float)var0.hurtDuration;
            var1 = Mth.sin(var1 * var1 * var1 * var1 * (float) Math.PI);
            float var3 = var0.hurtDir;
            RenderSystem.rotatef(-var3, 0.0F, 1.0F, 0.0F);
            RenderSystem.rotatef(-var1 * 14.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.rotatef(var3, 0.0F, 1.0F, 0.0F);
        }

    }

    private void bobView(float param0) {
        if (this.minecraft.getCameraEntity() instanceof Player) {
            Player var0 = (Player)this.minecraft.getCameraEntity();
            float var1 = var0.walkDist - var0.walkDistO;
            float var2 = -(var0.walkDist + var1 * param0);
            float var3 = Mth.lerp(param0, var0.oBob, var0.bob);
            RenderSystem.translatef(Mth.sin(var2 * (float) Math.PI) * var3 * 0.5F, -Math.abs(Mth.cos(var2 * (float) Math.PI) * var3), 0.0F);
            RenderSystem.rotatef(Mth.sin(var2 * (float) Math.PI) * var3 * 3.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.rotatef(Math.abs(Mth.cos(var2 * (float) Math.PI - 0.2F) * var3) * 5.0F, 1.0F, 0.0F, 0.0F);
        }
    }

    private void setupCamera(float param0) {
        this.renderDistance = (float)(this.minecraft.options.renderDistance * 16);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        if (this.zoom != 1.0) {
            RenderSystem.translatef((float)this.zoom_x, (float)(-this.zoom_y), 0.0F);
            RenderSystem.scaled(this.zoom, this.zoom, 1.0);
        }

        RenderSystem.multMatrix(
            Matrix4f.perspective(
                this.getFov(this.mainCamera, param0, true),
                (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
                0.05F,
                this.renderDistance * Mth.SQRT_OF_TWO
            )
        );
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        this.bobHurt(param0);
        if (this.minecraft.options.bobView) {
            this.bobView(param0);
        }

        float var0 = Mth.lerp(param0, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
        if (var0 > 0.0F) {
            int var1 = 20;
            if (this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
                var1 = 7;
            }

            float var2 = 5.0F / (var0 * var0 + 5.0F) - var0 * 0.04F;
            var2 *= var2;
            RenderSystem.rotatef(((float)this.tick + param0) * (float)var1, 0.0F, 1.0F, 1.0F);
            RenderSystem.scalef(1.0F / var2, 1.0F, 1.0F);
            RenderSystem.rotatef(-((float)this.tick + param0) * (float)var1, 0.0F, 1.0F, 1.0F);
        }

    }

    private void renderItemInHand(Camera param0, float param1) {
        if (!this.panoramicMode) {
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(
                Matrix4f.perspective(
                    this.getFov(param0, param1, false),
                    (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
                    0.05F,
                    this.renderDistance * 2.0F
                )
            );
            RenderSystem.matrixMode(5888);
            RenderSystem.loadIdentity();
            RenderSystem.pushMatrix();
            this.bobHurt(param1);
            if (this.minecraft.options.bobView) {
                this.bobView(param1);
            }

            boolean var0 = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
            if (this.minecraft.options.thirdPersonView == 0
                && !var0
                && !this.minecraft.options.hideGui
                && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.turnOnLightLayer();
                this.itemInHandRenderer.render(param1);
                this.turnOffLightLayer();
            }

            RenderSystem.popMatrix();
            if (this.minecraft.options.thirdPersonView == 0 && !var0) {
                this.itemInHandRenderer.renderScreenEffect(param1);
                this.bobHurt(param1);
            }

            if (this.minecraft.options.bobView) {
                this.bobView(param1);
            }

        }
    }

    public void turnOffLightLayer() {
        this.lightTexture.turnOffLightLayer();
    }

    public void turnOnLightLayer() {
        this.lightTexture.turnOnLightLayer();
    }

    public float getNightVisionScale(LivingEntity param0, float param1) {
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
                this.minecraft.mouseHandler.xpos() * (double)this.minecraft.window.getGuiScaledWidth() / (double)this.minecraft.window.getScreenWidth()
            );
            int var1 = (int)(
                this.minecraft.mouseHandler.ypos() * (double)this.minecraft.window.getGuiScaledHeight() / (double)this.minecraft.window.getScreenHeight()
            );
            int var2 = this.minecraft.options.framerateLimit;
            if (param2 && this.minecraft.level != null) {
                this.minecraft.getProfiler().push("level");
                int var3 = Math.min(Minecraft.getAverageFps(), var2);
                var3 = Math.max(var3, 60);
                long var4 = Util.getNanos() - param1;
                long var5 = Math.max((long)(1000000000 / var3 / 4) - var4, 0L);
                this.renderLevel(param0, Util.getNanos() + var5);
                if (this.minecraft.hasSingleplayerServer() && this.lastScreenshotAttempt < Util.getMillis() - 1000L) {
                    this.lastScreenshotAttempt = Util.getMillis();
                    if (!this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
                        this.takeAutoScreenshot();
                    }
                }

                this.minecraft.levelRenderer.doEntityOutline();
                if (this.postEffect != null && this.effectActive) {
                    RenderSystem.matrixMode(5890);
                    RenderSystem.pushMatrix();
                    RenderSystem.loadIdentity();
                    this.postEffect.process(param0);
                    RenderSystem.popMatrix();
                }

                this.minecraft.getMainRenderTarget().bindWrite(true);
                this.minecraft.getProfiler().popPush("gui");
                if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
                    RenderSystem.alphaFunc(516, 0.1F);
                    this.minecraft.window.setupGuiState(Minecraft.ON_OSX);
                    this.renderItemActivationAnimation(this.minecraft.window.getGuiScaledWidth(), this.minecraft.window.getGuiScaledHeight(), param0);
                    this.minecraft.gui.render(param0);
                }

                this.minecraft.getProfiler().pop();
            } else {
                RenderSystem.viewport(0, 0, this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
                RenderSystem.matrixMode(5889);
                RenderSystem.loadIdentity();
                RenderSystem.matrixMode(5888);
                RenderSystem.loadIdentity();
                this.minecraft.window.setupGuiState(Minecraft.ON_OSX);
            }

            if (this.minecraft.overlay != null) {
                RenderSystem.clear(256, Minecraft.ON_OSX);

                try {
                    this.minecraft.overlay.render(var0, var1, this.minecraft.getDeltaFrameTime());
                } catch (Throwable var14) {
                    CrashReport var7 = CrashReport.forThrowable(var14, "Rendering overlay");
                    CrashReportCategory var8 = var7.addCategory("Overlay render details");
                    var8.setDetail("Overlay name", () -> this.minecraft.overlay.getClass().getCanonicalName());
                    throw new ReportedException(var7);
                }
            } else if (this.minecraft.screen != null) {
                RenderSystem.clear(256, Minecraft.ON_OSX);

                try {
                    this.minecraft.screen.render(var0, var1, this.minecraft.getDeltaFrameTime());
                } catch (Throwable var13) {
                    CrashReport var10 = CrashReport.forThrowable(var13, "Rendering screen");
                    CrashReportCategory var11 = var10.addCategory("Screen render details");
                    var11.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                    var11.setDetail(
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
                    var11.setDetail(
                        "Screen size",
                        () -> String.format(
                                Locale.ROOT,
                                "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f",
                                this.minecraft.window.getGuiScaledWidth(),
                                this.minecraft.window.getGuiScaledHeight(),
                                this.minecraft.window.getWidth(),
                                this.minecraft.window.getHeight(),
                                this.minecraft.window.getGuiScale()
                            )
                    );
                    throw new ReportedException(var10);
                }
            }

        }
    }

    private void takeAutoScreenshot() {
        if (this.minecraft.levelRenderer.countRenderedChunks() > 10
            && this.minecraft.levelRenderer.hasRenderedAllChunks()
            && !this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
            NativeImage var0 = Screenshot.takeScreenshot(
                this.minecraft.window.getWidth(), this.minecraft.window.getHeight(), this.minecraft.getMainRenderTarget()
            );
            SimpleResource.IO_EXECUTOR.execute(() -> {
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
                } catch (IOException var27) {
                    LOGGER.warn("Couldn't save auto screenshot", (Throwable)var27);
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
            if (var1 && !((Player)var0).abilities.mayBuild) {
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

    public void renderLevel(float param0, long param1) {
        this.lightTexture.updateLightTexture(param0);
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.pick(param0);
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(516, 0.5F);
        this.minecraft.getProfiler().push("center");
        this.render(param0, param1);
        this.minecraft.getProfiler().pop();
    }

    private void render(float param0, long param1) {
        LevelRenderer var0 = this.minecraft.levelRenderer;
        ParticleEngine var1 = this.minecraft.particleEngine;
        boolean var2 = this.shouldRenderBlockOutline();
        RenderSystem.enableCull();
        this.minecraft.getProfiler().popPush("camera");
        this.setupCamera(param0);
        Camera var3 = this.mainCamera;
        var3.setup(
            this.minecraft.level,
            (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()),
            this.minecraft.options.thirdPersonView > 0,
            this.minecraft.options.thirdPersonView == 2,
            param0
        );
        FrustumData var4 = Frustum.getFrustum();
        var0.prepare(var3);
        this.minecraft.getProfiler().popPush("clear");
        RenderSystem.viewport(0, 0, this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
        this.fog.setupClearColor(var3, param0);
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        this.minecraft.getProfiler().popPush("culling");
        Culler var5 = new FrustumCuller(var4);
        double var6 = var3.getPosition().x;
        double var7 = var3.getPosition().y;
        double var8 = var3.getPosition().z;
        var5.prepare(var6, var7, var8);
        if (this.minecraft.options.renderDistance >= 4) {
            this.fog.setupFog(var3, -1);
            this.minecraft.getProfiler().popPush("sky");
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(
                Matrix4f.perspective(
                    this.getFov(var3, param0, true),
                    (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
                    0.05F,
                    this.renderDistance * 2.0F
                )
            );
            RenderSystem.matrixMode(5888);
            var0.renderSky(param0);
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(
                Matrix4f.perspective(
                    this.getFov(var3, param0, true),
                    (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
                    0.05F,
                    this.renderDistance * Mth.SQRT_OF_TWO
                )
            );
            RenderSystem.matrixMode(5888);
        }

        this.fog.setupFog(var3, 0);
        RenderSystem.shadeModel(7425);
        if (var3.getPosition().y < 128.0) {
            this.prepareAndRenderClouds(var3, var0, param0, var6, var7, var8);
        }

        this.minecraft.getProfiler().popPush("prepareterrain");
        this.fog.setupFog(var3, 0);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        Lighting.turnOff();
        this.minecraft.getProfiler().popPush("terrain_setup");
        this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
        var0.setupRender(var3, var5, this.frameId++, this.minecraft.player.isSpectator());
        this.minecraft.getProfiler().popPush("updatechunks");
        this.minecraft.levelRenderer.compileChunksUntil(param1);
        this.minecraft.getProfiler().popPush("terrain");
        RenderSystem.matrixMode(5888);
        RenderSystem.pushMatrix();
        RenderSystem.disableAlphaTest();
        var0.render(BlockLayer.SOLID, var3);
        RenderSystem.enableAlphaTest();
        var0.render(BlockLayer.CUTOUT_MIPPED, var3);
        this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
        var0.render(BlockLayer.CUTOUT, var3);
        this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
        RenderSystem.shadeModel(7424);
        RenderSystem.alphaFunc(516, 0.1F);
        RenderSystem.matrixMode(5888);
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        Lighting.turnOn();
        this.minecraft.getProfiler().popPush("entities");
        var0.renderEntities(var3, var5, param0);
        Lighting.turnOff();
        this.turnOffLightLayer();
        RenderSystem.matrixMode(5888);
        RenderSystem.popMatrix();
        if (var2 && this.minecraft.hitResult != null) {
            RenderSystem.disableAlphaTest();
            this.minecraft.getProfiler().popPush("outline");
            var0.renderHitOutline(var3, this.minecraft.hitResult, 0);
            RenderSystem.enableAlphaTest();
        }

        this.minecraft.debugRenderer.render(param1);
        this.minecraft.getProfiler().popPush("destroyProgress");
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
        var0.renderDestroyAnimation(Tesselator.getInstance(), Tesselator.getInstance().getBuilder(), var3);
        this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
        RenderSystem.disableBlend();
        this.turnOnLightLayer();
        this.fog.setupFog(var3, 0);
        this.minecraft.getProfiler().popPush("particles");
        var1.render(var3, param0);
        this.turnOffLightLayer();
        RenderSystem.depthMask(false);
        RenderSystem.enableCull();
        this.minecraft.getProfiler().popPush("weather");
        this.renderSnowAndRain(param0);
        RenderSystem.depthMask(true);
        var0.renderWorldBounds(var3, param0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.alphaFunc(516, 0.1F);
        this.fog.setupFog(var3, 0);
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.shadeModel(7425);
        this.minecraft.getProfiler().popPush("translucent");
        var0.render(BlockLayer.TRANSLUCENT, var3);
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableFog();
        if (var3.getPosition().y >= 128.0) {
            this.minecraft.getProfiler().popPush("aboveClouds");
            this.prepareAndRenderClouds(var3, var0, param0, var6, var7, var8);
        }

        this.minecraft.getProfiler().popPush("hand");
        if (this.renderHand) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            this.renderItemInHand(var3, param0);
        }

    }

    private void prepareAndRenderClouds(Camera param0, LevelRenderer param1, float param2, double param3, double param4, double param5) {
        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
            this.minecraft.getProfiler().popPush("clouds");
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(
                Matrix4f.perspective(
                    this.getFov(param0, param2, true),
                    (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
                    0.05F,
                    this.renderDistance * 4.0F
                )
            );
            RenderSystem.matrixMode(5888);
            RenderSystem.pushMatrix();
            this.fog.setupFog(param0, 0);
            param1.renderClouds(param2, param3, param4, param5);
            RenderSystem.disableFog();
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(
                Matrix4f.perspective(
                    this.getFov(param0, param2, true),
                    (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
                    0.05F,
                    this.renderDistance * Mth.SQRT_OF_TWO
                )
            );
            RenderSystem.matrixMode(5888);
        }

    }

    private void tickRain() {
        float var0 = this.minecraft.level.getRainLevel(1.0F);
        if (!this.minecraft.options.fancyGraphics) {
            var0 /= 2.0F;
        }

        if (var0 != 0.0F) {
            this.random.setSeed((long)this.tick * 312987231L);
            LevelReader var1 = this.minecraft.level;
            BlockPos var2 = new BlockPos(this.mainCamera.getPosition());
            int var3 = 10;
            double var4 = 0.0;
            double var5 = 0.0;
            double var6 = 0.0;
            int var7 = 0;
            int var8 = (int)(100.0F * var0 * var0);
            if (this.minecraft.options.particles == ParticleStatus.DECREASED) {
                var8 >>= 1;
            } else if (this.minecraft.options.particles == ParticleStatus.MINIMAL) {
                var8 = 0;
            }

            for(int var9 = 0; var9 < var8; ++var9) {
                BlockPos var10 = var1.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING,
                    var2.offset(this.random.nextInt(10) - this.random.nextInt(10), 0, this.random.nextInt(10) - this.random.nextInt(10))
                );
                Biome var11 = var1.getBiome(var10);
                BlockPos var12 = var10.below();
                if (var10.getY() <= var2.getY() + 10
                    && var10.getY() >= var2.getY() - 10
                    && var11.getPrecipitation() == Biome.Precipitation.RAIN
                    && var11.getTemperature(var10) >= 0.15F) {
                    double var13 = this.random.nextDouble();
                    double var14 = this.random.nextDouble();
                    BlockState var15 = var1.getBlockState(var12);
                    FluidState var16 = var1.getFluidState(var10);
                    VoxelShape var17 = var15.getCollisionShape(var1, var12);
                    double var18 = var17.max(Direction.Axis.Y, var13, var14);
                    double var19 = (double)var16.getHeight(var1, var10);
                    double var20;
                    double var21;
                    if (var18 >= var19) {
                        var20 = var18;
                        var21 = var17.min(Direction.Axis.Y, var13, var14);
                    } else {
                        var20 = 0.0;
                        var21 = 0.0;
                    }

                    if (var20 > -Double.MAX_VALUE) {
                        if (!var16.is(FluidTags.LAVA)
                            && var15.getBlock() != Blocks.MAGMA_BLOCK
                            && (var15.getBlock() != Blocks.CAMPFIRE || !var15.getValue(CampfireBlock.LIT))) {
                            if (this.random.nextInt(++var7) == 0) {
                                var4 = (double)var12.getX() + var13;
                                var5 = (double)((float)var12.getY() + 0.1F) + var20 - 1.0;
                                var6 = (double)var12.getZ() + var14;
                            }

                            this.minecraft
                                .level
                                .addParticle(
                                    ParticleTypes.RAIN,
                                    (double)var12.getX() + var13,
                                    (double)((float)var12.getY() + 0.1F) + var20,
                                    (double)var12.getZ() + var14,
                                    0.0,
                                    0.0,
                                    0.0
                                );
                        } else {
                            this.minecraft
                                .level
                                .addParticle(
                                    ParticleTypes.SMOKE,
                                    (double)var10.getX() + var13,
                                    (double)((float)var10.getY() + 0.1F) - var21,
                                    (double)var10.getZ() + var14,
                                    0.0,
                                    0.0,
                                    0.0
                                );
                        }
                    }
                }
            }

            if (var7 > 0 && this.random.nextInt(3) < this.rainSoundTime++) {
                this.rainSoundTime = 0;
                if (var5 > (double)(var2.getY() + 1) && var1.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, var2).getY() > Mth.floor((float)var2.getY())) {
                    this.minecraft.level.playLocalSound(var4, var5, var6, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
                } else {
                    this.minecraft.level.playLocalSound(var4, var5, var6, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
                }
            }

        }
    }

    protected void renderSnowAndRain(float param0) {
        float var0 = this.minecraft.level.getRainLevel(param0);
        if (!(var0 <= 0.0F)) {
            this.turnOnLightLayer();
            Level var1 = this.minecraft.level;
            int var2 = Mth.floor(this.mainCamera.getPosition().x);
            int var3 = Mth.floor(this.mainCamera.getPosition().y);
            int var4 = Mth.floor(this.mainCamera.getPosition().z);
            Tesselator var5 = Tesselator.getInstance();
            BufferBuilder var6 = var5.getBuilder();
            RenderSystem.disableCull();
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
            RenderSystem.alphaFunc(516, 0.1F);
            double var7 = this.mainCamera.getPosition().x;
            double var8 = this.mainCamera.getPosition().y;
            double var9 = this.mainCamera.getPosition().z;
            int var10 = Mth.floor(var8);
            int var11 = 5;
            if (this.minecraft.options.fancyGraphics) {
                var11 = 10;
            }

            int var12 = -1;
            float var13 = (float)this.tick + param0;
            var6.offset(-var7, -var8, -var9);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos var14 = new BlockPos.MutableBlockPos();

            for(int var15 = var4 - var11; var15 <= var4 + var11; ++var15) {
                for(int var16 = var2 - var11; var16 <= var2 + var11; ++var16) {
                    int var17 = (var15 - var4 + 16) * 32 + var16 - var2 + 16;
                    double var18 = (double)this.rainSizeX[var17] * 0.5;
                    double var19 = (double)this.rainSizeZ[var17] * 0.5;
                    var14.set(var16, 0, var15);
                    Biome var20 = var1.getBiome(var14);
                    if (var20.getPrecipitation() != Biome.Precipitation.NONE) {
                        int var21 = var1.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, var14).getY();
                        int var22 = var3 - var11;
                        int var23 = var3 + var11;
                        if (var22 < var21) {
                            var22 = var21;
                        }

                        if (var23 < var21) {
                            var23 = var21;
                        }

                        int var24 = var21;
                        if (var21 < var10) {
                            var24 = var10;
                        }

                        if (var22 != var23) {
                            this.random.setSeed((long)(var16 * var16 * 3121 + var16 * 45238971 ^ var15 * var15 * 418711 + var15 * 13761));
                            var14.set(var16, var22, var15);
                            float var25 = var20.getTemperature(var14);
                            if (var25 >= 0.15F) {
                                if (var12 != 0) {
                                    if (var12 >= 0) {
                                        var5.end();
                                    }

                                    var12 = 0;
                                    this.minecraft.getTextureManager().bind(RAIN_LOCATION);
                                    var6.begin(7, DefaultVertexFormat.PARTICLE);
                                }

                                double var26 = -(
                                        (double)(this.tick + var16 * var16 * 3121 + var16 * 45238971 + var15 * var15 * 418711 + var15 * 13761 & 31)
                                            + (double)param0
                                    )
                                    / 32.0
                                    * (3.0 + this.random.nextDouble());
                                double var27 = (double)((float)var16 + 0.5F) - this.mainCamera.getPosition().x;
                                double var28 = (double)((float)var15 + 0.5F) - this.mainCamera.getPosition().z;
                                float var29 = Mth.sqrt(var27 * var27 + var28 * var28) / (float)var11;
                                float var30 = ((1.0F - var29 * var29) * 0.5F + 0.5F) * var0;
                                var14.set(var16, var24, var15);
                                int var31 = var1.getLightColor(var14);
                                int var32 = var31 >> 16 & 65535;
                                int var33 = var31 & 65535;
                                var6.vertex((double)var16 - var18 + 0.5, (double)var23, (double)var15 - var19 + 0.5)
                                    .uv(0.0, (double)var22 * 0.25 + var26)
                                    .color(1.0F, 1.0F, 1.0F, var30)
                                    .uv2(var32, var33)
                                    .endVertex();
                                var6.vertex((double)var16 + var18 + 0.5, (double)var23, (double)var15 + var19 + 0.5)
                                    .uv(1.0, (double)var22 * 0.25 + var26)
                                    .color(1.0F, 1.0F, 1.0F, var30)
                                    .uv2(var32, var33)
                                    .endVertex();
                                var6.vertex((double)var16 + var18 + 0.5, (double)var22, (double)var15 + var19 + 0.5)
                                    .uv(1.0, (double)var23 * 0.25 + var26)
                                    .color(1.0F, 1.0F, 1.0F, var30)
                                    .uv2(var32, var33)
                                    .endVertex();
                                var6.vertex((double)var16 - var18 + 0.5, (double)var22, (double)var15 - var19 + 0.5)
                                    .uv(0.0, (double)var23 * 0.25 + var26)
                                    .color(1.0F, 1.0F, 1.0F, var30)
                                    .uv2(var32, var33)
                                    .endVertex();
                            } else {
                                if (var12 != 1) {
                                    if (var12 >= 0) {
                                        var5.end();
                                    }

                                    var12 = 1;
                                    this.minecraft.getTextureManager().bind(SNOW_LOCATION);
                                    var6.begin(7, DefaultVertexFormat.PARTICLE);
                                }

                                double var34 = (double)(-((float)(this.tick & 511) + param0) / 512.0F);
                                double var35 = this.random.nextDouble() + (double)var13 * 0.01 * (double)((float)this.random.nextGaussian());
                                double var36 = this.random.nextDouble() + (double)(var13 * (float)this.random.nextGaussian()) * 0.001;
                                double var37 = (double)((float)var16 + 0.5F) - this.mainCamera.getPosition().x;
                                double var38 = (double)((float)var15 + 0.5F) - this.mainCamera.getPosition().z;
                                float var39 = Mth.sqrt(var37 * var37 + var38 * var38) / (float)var11;
                                float var40 = ((1.0F - var39 * var39) * 0.3F + 0.5F) * var0;
                                var14.set(var16, var24, var15);
                                int var41 = (var1.getLightColor(var14) * 3 + 15728880) / 4;
                                int var42 = var41 >> 16 & 65535;
                                int var43 = var41 & 65535;
                                var6.vertex((double)var16 - var18 + 0.5, (double)var23, (double)var15 - var19 + 0.5)
                                    .uv(0.0 + var35, (double)var22 * 0.25 + var34 + var36)
                                    .color(1.0F, 1.0F, 1.0F, var40)
                                    .uv2(var42, var43)
                                    .endVertex();
                                var6.vertex((double)var16 + var18 + 0.5, (double)var23, (double)var15 + var19 + 0.5)
                                    .uv(1.0 + var35, (double)var22 * 0.25 + var34 + var36)
                                    .color(1.0F, 1.0F, 1.0F, var40)
                                    .uv2(var42, var43)
                                    .endVertex();
                                var6.vertex((double)var16 + var18 + 0.5, (double)var22, (double)var15 + var19 + 0.5)
                                    .uv(1.0 + var35, (double)var23 * 0.25 + var34 + var36)
                                    .color(1.0F, 1.0F, 1.0F, var40)
                                    .uv2(var42, var43)
                                    .endVertex();
                                var6.vertex((double)var16 - var18 + 0.5, (double)var22, (double)var15 - var19 + 0.5)
                                    .uv(0.0 + var35, (double)var23 * 0.25 + var34 + var36)
                                    .color(1.0F, 1.0F, 1.0F, var40)
                                    .uv2(var42, var43)
                                    .endVertex();
                            }
                        }
                    }
                }
            }

            if (var12 >= 0) {
                var5.end();
            }

            var6.offset(0.0, 0.0, 0.0);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.alphaFunc(516, 0.1F);
            this.turnOffLightLayer();
        }
    }

    public void resetFogColor(boolean param0) {
        this.fog.resetFogColor(param0);
    }

    public void resetData() {
        this.itemActivationItem = null;
        this.mapRenderer.resetData();
        this.mainCamera.reset();
    }

    public MapRenderer getMapRenderer() {
        return this.mapRenderer;
    }

    public static void renderNameTagInWorld(
        Font param0, String param1, float param2, float param3, float param4, int param5, float param6, float param7, boolean param8
    ) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(param2, param3, param4);
        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(-param6, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(param7, 1.0F, 0.0F, 0.0F);
        RenderSystem.scalef(-0.025F, -0.025F, 0.025F);
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        if (!param8) {
            RenderSystem.disableDepthTest();
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        int var0 = param0.width(param1) / 2;
        RenderSystem.disableTexture();
        Tesselator var1 = Tesselator.getInstance();
        BufferBuilder var2 = var1.getBuilder();
        var2.begin(7, DefaultVertexFormat.POSITION_COLOR);
        float var3 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        var2.vertex((double)(-var0 - 1), (double)(-1 + param5), 0.0).color(0.0F, 0.0F, 0.0F, var3).endVertex();
        var2.vertex((double)(-var0 - 1), (double)(8 + param5), 0.0).color(0.0F, 0.0F, 0.0F, var3).endVertex();
        var2.vertex((double)(var0 + 1), (double)(8 + param5), 0.0).color(0.0F, 0.0F, 0.0F, var3).endVertex();
        var2.vertex((double)(var0 + 1), (double)(-1 + param5), 0.0).color(0.0F, 0.0F, 0.0F, var3).endVertex();
        var1.end();
        RenderSystem.enableTexture();
        if (!param8) {
            param0.draw(param1, (float)(-param0.width(param1) / 2), (float)param5, 553648127);
            RenderSystem.enableDepthTest();
        }

        RenderSystem.depthMask(true);
        param0.draw(param1, (float)(-param0.width(param1) / 2), (float)param5, param8 ? 553648127 : -1);
        RenderSystem.enableLighting();
        RenderSystem.disableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.popMatrix();
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
            RenderSystem.enableAlphaTest();
            RenderSystem.pushMatrix();
            RenderSystem.pushLightingAttributes();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            Lighting.turnOn();
            RenderSystem.translatef(
                (float)(param0 / 2) + var6 * Mth.abs(Mth.sin(var5 * 2.0F)), (float)(param1 / 2) + var7 * Mth.abs(Mth.sin(var5 * 2.0F)), -50.0F
            );
            float var8 = 50.0F + 175.0F * Mth.sin(var5);
            RenderSystem.scalef(var8, -var8, var8);
            RenderSystem.rotatef(900.0F * Mth.abs(Mth.sin(var5)), 0.0F, 1.0F, 0.0F);
            RenderSystem.rotatef(6.0F * Mth.cos(var1 * 8.0F), 1.0F, 0.0F, 0.0F);
            RenderSystem.rotatef(6.0F * Mth.cos(var1 * 8.0F), 0.0F, 0.0F, 1.0F);
            this.minecraft.getItemRenderer().renderStatic(this.itemActivationItem, ItemTransforms.TransformType.FIXED);
            RenderSystem.popAttributes();
            RenderSystem.popMatrix();
            Lighting.turnOff();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
        }
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
}
