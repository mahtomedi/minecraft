package net.minecraft.client.renderer;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
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
import net.minecraft.server.packs.resources.SimpleResource;
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
import net.minecraft.world.level.material.FluidState;
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
    private static final Logger LOGGER = LogManager.getLogger();
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
            this.postEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
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

            if (param0.getEntity() instanceof LivingEntity && ((LivingEntity)param0.getEntity()).isDeadOrDying()) {
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

    private void bobHurt(PoseStack param0, float param1) {
        if (this.minecraft.getCameraEntity() instanceof LivingEntity) {
            LivingEntity var0 = (LivingEntity)this.minecraft.getCameraEntity();
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

    private void renderItemInHand(PoseStack param0, Camera param1, float param2) {
        if (!this.panoramicMode) {
            this.resetProjectionMatrix(this.getProjectionMatrix(param1, param2, false));
            PoseStack.Pose var0 = param0.last();
            var0.pose().setIdentity();
            var0.normal().setIdentity();
            param0.pushPose();
            this.bobHurt(param0, param2);
            if (this.minecraft.options.bobView) {
                this.bobView(param0, param2);
            }

            boolean var1 = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
            if (this.minecraft.options.thirdPersonView == 0
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
            if (this.minecraft.options.thirdPersonView == 0 && !var1) {
                ScreenEffectRenderer.renderScreenEffect(this.minecraft, param0);
                this.bobHurt(param0, param2);
            }

            if (this.minecraft.options.bobView) {
                this.bobView(param0, param2);
            }

        }
    }

    public void resetProjectionMatrix(Matrix4f param0) {
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(param0);
        RenderSystem.matrixMode(5888);
    }

    public Matrix4f getProjectionMatrix(Camera param0, float param1, boolean param2) {
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
                    this.getFov(param0, param1, param2),
                    (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(),
                    0.05F,
                    this.renderDistance * 4.0F
                )
            );
        return var0.last().pose();
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
                    RenderSystem.disableAlphaTest();
                    RenderSystem.enableTexture();
                    RenderSystem.matrixMode(5890);
                    RenderSystem.pushMatrix();
                    RenderSystem.loadIdentity();
                    this.postEffect.process(param0);
                    RenderSystem.popMatrix();
                }

                this.minecraft.getMainRenderTarget().bindWrite(true);
            }

            Window var2 = this.minecraft.getWindow();
            RenderSystem.clear(256, Minecraft.ON_OSX);
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.ortho(0.0, (double)var2.getWidth() / var2.getGuiScale(), (double)var2.getHeight() / var2.getGuiScale(), 0.0, 1000.0, 3000.0);
            RenderSystem.matrixMode(5888);
            RenderSystem.loadIdentity();
            RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
            Lighting.setupFor3DItems();
            PoseStack var3 = new PoseStack();
            if (param2 && this.minecraft.level != null) {
                this.minecraft.getProfiler().popPush("gui");
                if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
                    RenderSystem.defaultAlphaFunc();
                    this.renderItemActivationAnimation(this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), param0);
                    this.minecraft.gui.render(var3, param0);
                    RenderSystem.clear(256, Minecraft.ON_OSX);
                }

                this.minecraft.getProfiler().pop();
            }

            if (this.minecraft.overlay != null) {
                try {
                    this.minecraft.overlay.render(var3, var0, var1, this.minecraft.getDeltaFrameTime());
                } catch (Throwable var13) {
                    CrashReport var5 = CrashReport.forThrowable(var13, "Rendering overlay");
                    CrashReportCategory var6 = var5.addCategory("Overlay render details");
                    var6.setDetail("Overlay name", () -> this.minecraft.overlay.getClass().getCanonicalName());
                    throw new ReportedException(var5);
                }
            } else if (this.minecraft.screen != null) {
                try {
                    this.minecraft.screen.render(var3, var0, var1, this.minecraft.getDeltaFrameTime());
                } catch (Throwable var12) {
                    CrashReport var8 = CrashReport.forThrowable(var12, "Rendering screen");
                    CrashReportCategory var9 = var8.addCategory("Screen render details");
                    var9.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                    var9.setDetail(
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
                    var9.setDetail(
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
                    throw new ReportedException(var8);
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
        var2.last().pose().multiply(this.getProjectionMatrix(var1, param0, true));
        this.bobHurt(var2, param0);
        if (this.minecraft.options.bobView) {
            this.bobView(var2, param0);
        }

        float var3 = Mth.lerp(param0, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
        if (var3 > 0.0F) {
            int var4 = 20;
            if (this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
                var4 = 7;
            }

            float var5 = 5.0F / (var3 * var3 + 5.0F) - var3 * 0.04F;
            var5 *= var5;
            Vector3f var6 = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
            var2.mulPose(var6.rotationDegrees(((float)this.tick + param0) * (float)var4));
            var2.scale(1.0F / var5, 1.0F, 1.0F);
            float var7 = -((float)this.tick + param0) * (float)var4;
            var2.mulPose(var6.rotationDegrees(var7));
        }

        Matrix4f var8 = var2.last().pose();
        this.resetProjectionMatrix(var8);
        var1.setup(
            this.minecraft.level,
            (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()),
            this.minecraft.options.thirdPersonView > 0,
            this.minecraft.options.thirdPersonView == 2,
            param0
        );
        param2.mulPose(Vector3f.XP.rotationDegrees(var1.getXRot()));
        param2.mulPose(Vector3f.YP.rotationDegrees(var1.getYRot() + 180.0F));
        this.minecraft.levelRenderer.renderLevel(param2, param0, param1, var0, var1, this, this.lightTexture, var8);
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
            RenderSystem.enableAlphaTest();
            RenderSystem.pushMatrix();
            RenderSystem.pushLightingAttributes();
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
                .renderStatic(this.itemActivationItem, ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, var8, var10);
            var8.popPose();
            var10.endBatch();
            RenderSystem.popAttributes();
            RenderSystem.popMatrix();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
        }
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
}
