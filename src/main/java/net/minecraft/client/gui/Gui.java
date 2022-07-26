package net.minecraft.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Gui extends GuiComponent {
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
    private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
    private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = new ResourceLocation("textures/misc/powder_snow_outline.png");
    private static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
    private static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
    private static final int COLOR_WHITE = 16777215;
    private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0F;
    private static final int NUM_HEARTS_PER_ROW = 10;
    private static final int LINE_HEIGHT = 10;
    private static final String SPACER = ": ";
    private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2F;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SEPARATION = 8;
    private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2F;
    private final RandomSource random = RandomSource.create();
    private final Minecraft minecraft;
    private final ItemRenderer itemRenderer;
    private final ChatComponent chat;
    private int tickCount;
    @Nullable
    private Component overlayMessageString;
    private int overlayMessageTime;
    private boolean animateOverlayMessageColor;
    private boolean chatDisabledByPlayerShown;
    public float vignetteBrightness = 1.0F;
    private int toolHighlightTimer;
    private ItemStack lastToolHighlight = ItemStack.EMPTY;
    private final DebugScreenOverlay debugScreen;
    private final SubtitleOverlay subtitleOverlay;
    private final SpectatorGui spectatorGui;
    private final PlayerTabOverlay tabList;
    private final BossHealthOverlay bossOverlay;
    private int titleTime;
    @Nullable
    private Component title;
    @Nullable
    private Component subtitle;
    private int titleFadeInTime;
    private int titleStayTime;
    private int titleFadeOutTime;
    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;
    private int screenWidth;
    private int screenHeight;
    private float autosaveIndicatorValue;
    private float lastAutosaveIndicatorValue;
    private float scopeScale;

    public Gui(Minecraft param0, ItemRenderer param1) {
        this.minecraft = param0;
        this.itemRenderer = param1;
        this.debugScreen = new DebugScreenOverlay(param0);
        this.spectatorGui = new SpectatorGui(param0);
        this.chat = new ChatComponent(param0);
        this.tabList = new PlayerTabOverlay(param0, this);
        this.bossOverlay = new BossHealthOverlay(param0);
        this.subtitleOverlay = new SubtitleOverlay(param0);
        this.resetTitleTimes();
    }

    public void resetTitleTimes() {
        this.titleFadeInTime = 10;
        this.titleStayTime = 70;
        this.titleFadeOutTime = 20;
    }

    public void render(PoseStack param0, float param1) {
        Window var0 = this.minecraft.getWindow();
        this.screenWidth = var0.getGuiScaledWidth();
        this.screenHeight = var0.getGuiScaledHeight();
        Font var1 = this.getFont();
        RenderSystem.enableBlend();
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(this.minecraft.getCameraEntity());
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
        }

        float var2 = this.minecraft.getDeltaFrameTime();
        this.scopeScale = Mth.lerp(0.5F * var2, this.scopeScale, 1.125F);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (this.minecraft.player.isScoping()) {
                this.renderSpyglassOverlay(this.scopeScale);
            } else {
                this.scopeScale = 0.5F;
                ItemStack var3 = this.minecraft.player.getInventory().getArmor(3);
                if (var3.is(Blocks.CARVED_PUMPKIN.asItem())) {
                    this.renderTextureOverlay(PUMPKIN_BLUR_LOCATION, 1.0F);
                }
            }
        }

        if (this.minecraft.player.getTicksFrozen() > 0) {
            this.renderTextureOverlay(POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
        }

        float var4 = Mth.lerp(param1, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
        if (var4 > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(var4);
        }

        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(param0);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(param1, param0);
        }

        if (!this.minecraft.options.hideGui) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
            RenderSystem.enableBlend();
            this.renderCrosshair(param0);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.defaultBlendFunc();
            this.minecraft.getProfiler().push("bossHealth");
            this.bossOverlay.render(param0);
            this.minecraft.getProfiler().pop();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
            if (this.minecraft.gameMode.canHurtPlayer()) {
                this.renderPlayerHealth(param0);
            }

            this.renderVehicleHealth(param0);
            RenderSystem.disableBlend();
            int var5 = this.screenWidth / 2 - 91;
            PlayerRideableJumping var6 = this.minecraft.player.jumpableVehicle();
            if (var6 != null) {
                this.renderJumpMeter(var6, param0, var5);
            } else if (this.minecraft.gameMode.hasExperience()) {
                this.renderExperienceBar(param0, var5);
            }

            if (this.minecraft.options.heldItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(param0);
            } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(param0);
            }
        }

        if (this.minecraft.player.getSleepTimer() > 0) {
            this.minecraft.getProfiler().push("sleep");
            RenderSystem.disableDepthTest();
            float var7 = (float)this.minecraft.player.getSleepTimer();
            float var8 = var7 / 100.0F;
            if (var8 > 1.0F) {
                var8 = 1.0F - (var7 - 100.0F) / 10.0F;
            }

            int var9 = (int)(220.0F * var8) << 24 | 1052704;
            fill(param0, 0, 0, this.screenWidth, this.screenHeight, var9);
            RenderSystem.enableDepthTest();
            this.minecraft.getProfiler().pop();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (this.minecraft.isDemo()) {
            this.renderDemoOverlay(param0);
        }

        this.renderEffects(param0);
        if (this.minecraft.options.renderDebug) {
            this.debugScreen.render(param0);
        }

        if (!this.minecraft.options.hideGui) {
            if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
                this.minecraft.getProfiler().push("overlayMessage");
                float var10 = (float)this.overlayMessageTime - param1;
                int var11 = (int)(var10 * 255.0F / 20.0F);
                if (var11 > 255) {
                    var11 = 255;
                }

                if (var11 > 8) {
                    param0.pushPose();
                    param0.translate((float)(this.screenWidth / 2), (float)(this.screenHeight - 68), 0.0F);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    int var12 = 16777215;
                    if (this.animateOverlayMessageColor) {
                        var12 = Mth.hsvToRgb(var10 / 50.0F, 0.7F, 0.6F) & 16777215;
                    }

                    int var13 = var11 << 24 & 0xFF000000;
                    int var14 = var1.width(this.overlayMessageString);
                    this.drawBackdrop(param0, var1, -4, var14, 16777215 | var13);
                    var1.drawShadow(param0, this.overlayMessageString, (float)(-var14 / 2), -4.0F, var12 | var13);
                    RenderSystem.disableBlend();
                    param0.popPose();
                }

                this.minecraft.getProfiler().pop();
            }

            if (this.title != null && this.titleTime > 0) {
                this.minecraft.getProfiler().push("titleAndSubtitle");
                float var15 = (float)this.titleTime - param1;
                int var16 = 255;
                if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
                    float var17 = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - var15;
                    var16 = (int)(var17 * 255.0F / (float)this.titleFadeInTime);
                }

                if (this.titleTime <= this.titleFadeOutTime) {
                    var16 = (int)(var15 * 255.0F / (float)this.titleFadeOutTime);
                }

                var16 = Mth.clamp(var16, 0, 255);
                if (var16 > 8) {
                    param0.pushPose();
                    param0.translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    param0.pushPose();
                    param0.scale(4.0F, 4.0F, 4.0F);
                    int var18 = var16 << 24 & 0xFF000000;
                    int var19 = var1.width(this.title);
                    this.drawBackdrop(param0, var1, -10, var19, 16777215 | var18);
                    var1.drawShadow(param0, this.title, (float)(-var19 / 2), -10.0F, 16777215 | var18);
                    param0.popPose();
                    if (this.subtitle != null) {
                        param0.pushPose();
                        param0.scale(2.0F, 2.0F, 2.0F);
                        int var20 = var1.width(this.subtitle);
                        this.drawBackdrop(param0, var1, 5, var20, 16777215 | var18);
                        var1.drawShadow(param0, this.subtitle, (float)(-var20 / 2), 5.0F, 16777215 | var18);
                        param0.popPose();
                    }

                    RenderSystem.disableBlend();
                    param0.popPose();
                }

                this.minecraft.getProfiler().pop();
            }

            this.subtitleOverlay.render(param0);
            Scoreboard var21 = this.minecraft.level.getScoreboard();
            Objective var22 = null;
            PlayerTeam var23 = var21.getPlayersTeam(this.minecraft.player.getScoreboardName());
            if (var23 != null) {
                int var24 = var23.getColor().getId();
                if (var24 >= 0) {
                    var22 = var21.getDisplayObjective(3 + var24);
                }
            }

            Objective var25 = var22 != null ? var22 : var21.getDisplayObjective(1);
            if (var25 != null) {
                this.displayScoreboardSidebar(param0, var25);
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int var26 = Mth.floor(this.minecraft.mouseHandler.xpos() * (double)var0.getGuiScaledWidth() / (double)var0.getScreenWidth());
            int var27 = Mth.floor(this.minecraft.mouseHandler.ypos() * (double)var0.getGuiScaledHeight() / (double)var0.getScreenHeight());
            this.minecraft.getProfiler().push("chat");
            this.chat.render(param0, this.tickCount, var26, var27);
            this.minecraft.getProfiler().pop();
            var25 = var21.getDisplayObjective(0);
            if (!this.minecraft.options.keyPlayerList.isDown()
                || this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && var25 == null) {
                this.tabList.setVisible(false);
            } else {
                this.tabList.setVisible(true);
                this.tabList.render(param0, this.screenWidth, var21, var25);
            }

            this.renderSavingIndicator(param0);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawBackdrop(PoseStack param0, Font param1, int param2, int param3, int param4) {
        int var0 = this.minecraft.options.getBackgroundColor(0.0F);
        if (var0 != 0) {
            int var1 = -param3 / 2;
            fill(param0, var1 - 2, param2 - 2, var1 + param3 + 2, param2 + 9 + 2, FastColor.ARGB32.multiply(var0, param4));
        }

    }

    private void renderCrosshair(PoseStack param0) {
        Options var0 = this.minecraft.options;
        if (var0.getCameraType().isFirstPerson()) {
            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
                if (var0.renderDebug && !var0.hideGui && !this.minecraft.player.isReducedDebugInfo() && !var0.reducedDebugInfo().get()) {
                    Camera var1 = this.minecraft.gameRenderer.getMainCamera();
                    PoseStack var2 = RenderSystem.getModelViewStack();
                    var2.pushPose();
                    var2.translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), (float)this.getBlitOffset());
                    var2.mulPose(Axis.XN.rotationDegrees(var1.getXRot()));
                    var2.mulPose(Axis.YP.rotationDegrees(var1.getYRot()));
                    var2.scale(-1.0F, -1.0F, -1.0F);
                    RenderSystem.applyModelViewMatrix();
                    RenderSystem.renderCrosshair(10);
                    var2.popPose();
                    RenderSystem.applyModelViewMatrix();
                } else {
                    RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO
                    );
                    int var3 = 15;
                    this.blit(param0, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
                    if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                        float var4 = this.minecraft.player.getAttackStrengthScale(0.0F);
                        boolean var5 = false;
                        if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && var4 >= 1.0F) {
                            var5 = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                            var5 &= this.minecraft.crosshairPickEntity.isAlive();
                        }

                        int var6 = this.screenHeight / 2 - 7 + 16;
                        int var7 = this.screenWidth / 2 - 8;
                        if (var5) {
                            this.blit(param0, var7, var6, 68, 94, 16, 16);
                        } else if (var4 < 1.0F) {
                            int var8 = (int)(var4 * 17.0F);
                            this.blit(param0, var7, var6, 36, 94, 16, 4);
                            this.blit(param0, var7, var6, 52, 94, var8, 4);
                        }
                    }
                }

            }
        }
    }

    private boolean canRenderCrosshairForSpectator(HitResult param0) {
        if (param0 == null) {
            return false;
        } else if (param0.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)param0).getEntity() instanceof MenuProvider;
        } else if (param0.getType() == HitResult.Type.BLOCK) {
            BlockPos var0 = ((BlockHitResult)param0).getBlockPos();
            Level var1 = this.minecraft.level;
            return var1.getBlockState(var0).getMenuProvider(var1, var0) != null;
        } else {
            return false;
        }
    }

    protected void renderEffects(PoseStack param0) {
        Collection<MobEffectInstance> var0 = this.minecraft.player.getActiveEffects();
        if (!var0.isEmpty()) {
            Screen var3 = this.minecraft.screen;
            if (var3 instanceof EffectRenderingInventoryScreen var1 && var1.canSeeEffects()) {
                return;
            }

            RenderSystem.enableBlend();
            int var2 = 0;
            int var3 = 0;
            MobEffectTextureManager var4 = this.minecraft.getMobEffectTextures();
            List<Runnable> var5 = Lists.newArrayListWithExpectedSize(var0.size());
            RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);

            for(MobEffectInstance var6 : Ordering.natural().reverse().sortedCopy(var0)) {
                MobEffect var7 = var6.getEffect();
                if (var6.showIcon()) {
                    int var8 = this.screenWidth;
                    int var9 = 1;
                    if (this.minecraft.isDemo()) {
                        var9 += 15;
                    }

                    if (var7.isBeneficial()) {
                        ++var2;
                        var8 -= 25 * var2;
                    } else {
                        ++var3;
                        var8 -= 25 * var3;
                        var9 += 26;
                    }

                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    float var10 = 1.0F;
                    if (var6.isAmbient()) {
                        this.blit(param0, var8, var9, 165, 166, 24, 24);
                    } else {
                        this.blit(param0, var8, var9, 141, 166, 24, 24);
                        if (var6.getDuration() <= 200) {
                            int var11 = 10 - var6.getDuration() / 20;
                            var10 = Mth.clamp((float)var6.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                                + Mth.cos((float)var6.getDuration() * (float) Math.PI / 5.0F) * Mth.clamp((float)var11 / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    TextureAtlasSprite var12 = var4.get(var7);
                    int var13 = var8;
                    int var14 = var9;
                    float var15 = var10;
                    var5.add(() -> {
                        RenderSystem.setShaderTexture(0, var12.atlasLocation());
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, var15);
                        blit(param0, var13 + 3, var14 + 3, this.getBlitOffset(), 18, 18, var12);
                    });
                }
            }

            var5.forEach(Runnable::run);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void renderHotbar(float param0, PoseStack param1) {
        Player var0 = this.getCameraPlayer();
        if (var0 != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            ItemStack var1 = var0.getOffhandItem();
            HumanoidArm var2 = var0.getMainArm().getOpposite();
            int var3 = this.screenWidth / 2;
            int var4 = this.getBlitOffset();
            int var5 = 182;
            int var6 = 91;
            this.setBlitOffset(-90);
            this.blit(param1, var3 - 91, this.screenHeight - 22, 0, 0, 182, 22);
            this.blit(param1, var3 - 91 - 1 + var0.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
            if (!var1.isEmpty()) {
                if (var2 == HumanoidArm.LEFT) {
                    this.blit(param1, var3 - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
                } else {
                    this.blit(param1, var3 + 91, this.screenHeight - 23, 53, 22, 29, 24);
                }
            }

            this.setBlitOffset(var4);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int var7 = 1;

            for(int var8 = 0; var8 < 9; ++var8) {
                int var9 = var3 - 90 + var8 * 20 + 2;
                int var10 = this.screenHeight - 16 - 3;
                this.renderSlot(var9, var10, param0, var0, var0.getInventory().items.get(var8), var7++);
            }

            if (!var1.isEmpty()) {
                int var11 = this.screenHeight - 16 - 3;
                if (var2 == HumanoidArm.LEFT) {
                    this.renderSlot(var3 - 91 - 26, var11, param0, var0, var1, var7++);
                } else {
                    this.renderSlot(var3 + 91 + 10, var11, param0, var0, var1, var7++);
                }
            }

            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
                float var12 = this.minecraft.player.getAttackStrengthScale(0.0F);
                if (var12 < 1.0F) {
                    int var13 = this.screenHeight - 20;
                    int var14 = var3 + 91 + 6;
                    if (var2 == HumanoidArm.RIGHT) {
                        var14 = var3 - 91 - 22;
                    }

                    RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                    int var15 = (int)(var12 * 19.0F);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    this.blit(param1, var14, var13, 0, 94, 18, 18);
                    this.blit(param1, var14, var13 + 18 - var15, 18, 112 - var15, 18, var15);
                }
            }

            RenderSystem.disableBlend();
        }
    }

    public void renderJumpMeter(PlayerRideableJumping param0, PoseStack param1, int param2) {
        this.minecraft.getProfiler().push("jumpBar");
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        float var0 = this.minecraft.player.getJumpRidingScale();
        int var1 = 182;
        int var2 = (int)(var0 * 183.0F);
        int var3 = this.screenHeight - 32 + 3;
        this.blit(param1, param2, var3, 0, 84, 182, 5);
        if (param0.getJumpCooldown() > 0) {
            this.blit(param1, param2, var3, 0, 74, 182, 5);
        } else if (var2 > 0) {
            this.blit(param1, param2, var3, 0, 89, var2, 5);
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderExperienceBar(PoseStack param0, int param1) {
        this.minecraft.getProfiler().push("expBar");
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        int var0 = this.minecraft.player.getXpNeededForNextLevel();
        if (var0 > 0) {
            int var1 = 182;
            int var2 = (int)(this.minecraft.player.experienceProgress * 183.0F);
            int var3 = this.screenHeight - 32 + 3;
            this.blit(param0, param1, var3, 0, 64, 182, 5);
            if (var2 > 0) {
                this.blit(param0, param1, var3, 0, 69, var2, 5);
            }
        }

        this.minecraft.getProfiler().pop();
        if (this.minecraft.player.experienceLevel > 0) {
            this.minecraft.getProfiler().push("expLevel");
            String var4 = this.minecraft.player.experienceLevel + "";
            int var5 = (this.screenWidth - this.getFont().width(var4)) / 2;
            int var6 = this.screenHeight - 31 - 4;
            this.getFont().draw(param0, var4, (float)(var5 + 1), (float)var6, 0);
            this.getFont().draw(param0, var4, (float)(var5 - 1), (float)var6, 0);
            this.getFont().draw(param0, var4, (float)var5, (float)(var6 + 1), 0);
            this.getFont().draw(param0, var4, (float)var5, (float)(var6 - 1), 0);
            this.getFont().draw(param0, var4, (float)var5, (float)var6, 8453920);
            this.minecraft.getProfiler().pop();
        }

    }

    public void renderSelectedItemName(PoseStack param0) {
        this.minecraft.getProfiler().push("selectedItemName");
        if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
            MutableComponent var0 = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
            if (this.lastToolHighlight.hasCustomHoverName()) {
                var0.withStyle(ChatFormatting.ITALIC);
            }

            int var1 = this.getFont().width(var0);
            int var2 = (this.screenWidth - var1) / 2;
            int var3 = this.screenHeight - 59;
            if (!this.minecraft.gameMode.canHurtPlayer()) {
                var3 += 14;
            }

            int var4 = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
            if (var4 > 255) {
                var4 = 255;
            }

            if (var4 > 0) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                fill(param0, var2 - 2, var3 - 2, var2 + var1 + 2, var3 + 9 + 2, this.minecraft.options.getBackgroundColor(0));
                this.getFont().drawShadow(param0, var0, (float)var2, (float)var3, 16777215 + (var4 << 24));
                RenderSystem.disableBlend();
            }
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderDemoOverlay(PoseStack param0) {
        this.minecraft.getProfiler().push("demo");
        Component var0;
        if (this.minecraft.level.getGameTime() >= 120500L) {
            var0 = DEMO_EXPIRED_TEXT;
        } else {
            var0 = Component.translatable("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
        }

        int var2 = this.getFont().width(var0);
        this.getFont().drawShadow(param0, var0, (float)(this.screenWidth - var2 - 10), 5.0F, 16777215);
        this.minecraft.getProfiler().pop();
    }

    private void displayScoreboardSidebar(PoseStack param0, Objective param1) {
        Scoreboard var0 = param1.getScoreboard();
        Collection<Score> var1 = var0.getPlayerScores(param1);
        List<Score> var2 = var1.stream().filter(param0x -> param0x.getOwner() != null && !param0x.getOwner().startsWith("#")).collect(Collectors.toList());
        if (var2.size() > 15) {
            var1 = Lists.newArrayList(Iterables.skip(var2, var1.size() - 15));
        } else {
            var1 = var2;
        }

        List<Pair<Score, Component>> var3 = Lists.newArrayListWithCapacity(var1.size());
        Component var4 = param1.getDisplayName();
        int var5 = this.getFont().width(var4);
        int var6 = var5;
        int var7 = this.getFont().width(": ");

        for(Score var8 : var1) {
            PlayerTeam var9 = var0.getPlayersTeam(var8.getOwner());
            Component var10 = PlayerTeam.formatNameForTeam(var9, Component.literal(var8.getOwner()));
            var3.add(Pair.of(var8, var10));
            var6 = Math.max(var6, this.getFont().width(var10) + var7 + this.getFont().width(Integer.toString(var8.getScore())));
        }

        int var11 = var1.size() * 9;
        int var12 = this.screenHeight / 2 + var11 / 3;
        int var13 = 3;
        int var14 = this.screenWidth - var6 - 3;
        int var15 = 0;
        int var16 = this.minecraft.options.getBackgroundColor(0.3F);
        int var17 = this.minecraft.options.getBackgroundColor(0.4F);

        for(Pair<Score, Component> var18 : var3) {
            ++var15;
            Score var19 = var18.getFirst();
            Component var20 = var18.getSecond();
            String var21 = "" + ChatFormatting.RED + var19.getScore();
            int var23 = var12 - var15 * 9;
            int var24 = this.screenWidth - 3 + 2;
            fill(param0, var14 - 2, var23, var24, var23 + 9, var16);
            this.getFont().draw(param0, var20, (float)var14, (float)var23, -1);
            this.getFont().draw(param0, var21, (float)(var24 - this.getFont().width(var21)), (float)var23, -1);
            if (var15 == var1.size()) {
                fill(param0, var14 - 2, var23 - 9 - 1, var24, var23 - 1, var17);
                fill(param0, var14 - 2, var23 - 1, var24, var23, var16);
                this.getFont().draw(param0, var4, (float)(var14 + var6 / 2 - var5 / 2), (float)(var23 - 9), -1);
            }
        }

    }

    private Player getCameraPlayer() {
        return !(this.minecraft.getCameraEntity() instanceof Player) ? null : (Player)this.minecraft.getCameraEntity();
    }

    private LivingEntity getPlayerVehicleWithHealth() {
        Player var0 = this.getCameraPlayer();
        if (var0 != null) {
            Entity var1 = var0.getVehicle();
            if (var1 == null) {
                return null;
            }

            if (var1 instanceof LivingEntity) {
                return (LivingEntity)var1;
            }
        }

        return null;
    }

    private int getVehicleMaxHearts(LivingEntity param0) {
        if (param0 != null && param0.showVehicleHealth()) {
            float var0 = param0.getMaxHealth();
            int var1 = (int)(var0 + 0.5F) / 2;
            if (var1 > 30) {
                var1 = 30;
            }

            return var1;
        } else {
            return 0;
        }
    }

    private int getVisibleVehicleHeartRows(int param0) {
        return (int)Math.ceil((double)param0 / 10.0);
    }

    private void renderPlayerHealth(PoseStack param0) {
        Player var0 = this.getCameraPlayer();
        if (var0 != null) {
            int var1 = Mth.ceil(var0.getHealth());
            boolean var2 = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
            long var3 = Util.getMillis();
            if (var1 < this.lastHealth && var0.invulnerableTime > 0) {
                this.lastHealthTime = var3;
                this.healthBlinkTime = (long)(this.tickCount + 20);
            } else if (var1 > this.lastHealth && var0.invulnerableTime > 0) {
                this.lastHealthTime = var3;
                this.healthBlinkTime = (long)(this.tickCount + 10);
            }

            if (var3 - this.lastHealthTime > 1000L) {
                this.lastHealth = var1;
                this.displayHealth = var1;
                this.lastHealthTime = var3;
            }

            this.lastHealth = var1;
            int var4 = this.displayHealth;
            this.random.setSeed((long)(this.tickCount * 312871));
            FoodData var5 = var0.getFoodData();
            int var6 = var5.getFoodLevel();
            int var7 = this.screenWidth / 2 - 91;
            int var8 = this.screenWidth / 2 + 91;
            int var9 = this.screenHeight - 39;
            float var10 = Math.max((float)var0.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(var4, var1));
            int var11 = Mth.ceil(var0.getAbsorptionAmount());
            int var12 = Mth.ceil((var10 + (float)var11) / 2.0F / 10.0F);
            int var13 = Math.max(10 - (var12 - 2), 3);
            int var14 = var9 - (var12 - 1) * var13 - 10;
            int var15 = var9 - 10;
            int var16 = var0.getArmorValue();
            int var17 = -1;
            if (var0.hasEffect(MobEffects.REGENERATION)) {
                var17 = this.tickCount % Mth.ceil(var10 + 5.0F);
            }

            this.minecraft.getProfiler().push("armor");

            for(int var18 = 0; var18 < 10; ++var18) {
                if (var16 > 0) {
                    int var19 = var7 + var18 * 8;
                    if (var18 * 2 + 1 < var16) {
                        this.blit(param0, var19, var14, 34, 9, 9, 9);
                    }

                    if (var18 * 2 + 1 == var16) {
                        this.blit(param0, var19, var14, 25, 9, 9, 9);
                    }

                    if (var18 * 2 + 1 > var16) {
                        this.blit(param0, var19, var14, 16, 9, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().popPush("health");
            this.renderHearts(param0, var0, var7, var9, var13, var17, var10, var1, var4, var11, var2);
            LivingEntity var20 = this.getPlayerVehicleWithHealth();
            int var21 = this.getVehicleMaxHearts(var20);
            if (var21 == 0) {
                this.minecraft.getProfiler().popPush("food");

                for(int var22 = 0; var22 < 10; ++var22) {
                    int var23 = var9;
                    int var24 = 16;
                    int var25 = 0;
                    if (var0.hasEffect(MobEffects.HUNGER)) {
                        var24 += 36;
                        var25 = 13;
                    }

                    if (var0.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (var6 * 3 + 1) == 0) {
                        var23 = var9 + (this.random.nextInt(3) - 1);
                    }

                    int var26 = var8 - var22 * 8 - 9;
                    this.blit(param0, var26, var23, 16 + var25 * 9, 27, 9, 9);
                    if (var22 * 2 + 1 < var6) {
                        this.blit(param0, var26, var23, var24 + 36, 27, 9, 9);
                    }

                    if (var22 * 2 + 1 == var6) {
                        this.blit(param0, var26, var23, var24 + 45, 27, 9, 9);
                    }
                }

                var15 -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            int var27 = var0.getMaxAirSupply();
            int var28 = Math.min(var0.getAirSupply(), var27);
            if (var0.isEyeInFluid(FluidTags.WATER) || var28 < var27) {
                int var29 = this.getVisibleVehicleHeartRows(var21) - 1;
                var15 -= var29 * 10;
                int var30 = Mth.ceil((double)(var28 - 2) * 10.0 / (double)var27);
                int var31 = Mth.ceil((double)var28 * 10.0 / (double)var27) - var30;

                for(int var32 = 0; var32 < var30 + var31; ++var32) {
                    if (var32 < var30) {
                        this.blit(param0, var8 - var32 * 8 - 9, var15, 16, 18, 9, 9);
                    } else {
                        this.blit(param0, var8 - var32 * 8 - 9, var15, 25, 18, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }
    }

    private void renderHearts(
        PoseStack param0, Player param1, int param2, int param3, int param4, int param5, float param6, int param7, int param8, int param9, boolean param10
    ) {
        Gui.HeartType var0 = Gui.HeartType.forPlayer(param1);
        int var1 = 9 * (param1.level.getLevelData().isHardcore() ? 5 : 0);
        int var2 = Mth.ceil((double)param6 / 2.0);
        int var3 = Mth.ceil((double)param9 / 2.0);
        int var4 = var2 * 2;

        for(int var5 = var2 + var3 - 1; var5 >= 0; --var5) {
            int var6 = var5 / 10;
            int var7 = var5 % 10;
            int var8 = param2 + var7 * 8;
            int var9 = param3 - var6 * param4;
            if (param7 + param9 <= 4) {
                var9 += this.random.nextInt(2);
            }

            if (var5 < var2 && var5 == param5) {
                var9 -= 2;
            }

            this.renderHeart(param0, Gui.HeartType.CONTAINER, var8, var9, var1, param10, false);
            int var10 = var5 * 2;
            boolean var11 = var5 >= var2;
            if (var11) {
                int var12 = var10 - var4;
                if (var12 < param9) {
                    boolean var13 = var12 + 1 == param9;
                    this.renderHeart(param0, var0 == Gui.HeartType.WITHERED ? var0 : Gui.HeartType.ABSORBING, var8, var9, var1, false, var13);
                }
            }

            if (param10 && var10 < param8) {
                boolean var14 = var10 + 1 == param8;
                this.renderHeart(param0, var0, var8, var9, var1, true, var14);
            }

            if (var10 < param7) {
                boolean var15 = var10 + 1 == param7;
                this.renderHeart(param0, var0, var8, var9, var1, false, var15);
            }
        }

    }

    private void renderHeart(PoseStack param0, Gui.HeartType param1, int param2, int param3, int param4, boolean param5, boolean param6) {
        this.blit(param0, param2, param3, param1.getX(param6, param5), param4, 9, 9);
    }

    private void renderVehicleHealth(PoseStack param0) {
        LivingEntity var0 = this.getPlayerVehicleWithHealth();
        if (var0 != null) {
            int var1 = this.getVehicleMaxHearts(var0);
            if (var1 != 0) {
                int var2 = (int)Math.ceil((double)var0.getHealth());
                this.minecraft.getProfiler().popPush("mountHealth");
                int var3 = this.screenHeight - 39;
                int var4 = this.screenWidth / 2 + 91;
                int var5 = var3;
                int var6 = 0;

                for(boolean var7 = false; var1 > 0; var6 += 20) {
                    int var8 = Math.min(var1, 10);
                    var1 -= var8;

                    for(int var9 = 0; var9 < var8; ++var9) {
                        int var10 = 52;
                        int var11 = 0;
                        int var12 = var4 - var9 * 8 - 9;
                        this.blit(param0, var12, var5, 52 + var11 * 9, 9, 9, 9);
                        if (var9 * 2 + 1 + var6 < var2) {
                            this.blit(param0, var12, var5, 88, 9, 9, 9);
                        }

                        if (var9 * 2 + 1 + var6 == var2) {
                            this.blit(param0, var12, var5, 97, 9, 9, 9);
                        }
                    }

                    var5 -= 10;
                }

            }
        }
    }

    private void renderTextureOverlay(ResourceLocation param0, float param1) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, param1);
        RenderSystem.setShaderTexture(0, param0);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var1.vertex(0.0, (double)this.screenHeight, -90.0).uv(0.0F, 1.0F).endVertex();
        var1.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(1.0F, 1.0F).endVertex();
        var1.vertex((double)this.screenWidth, 0.0, -90.0).uv(1.0F, 0.0F).endVertex();
        var1.vertex(0.0, 0.0, -90.0).uv(0.0F, 0.0F).endVertex();
        var0.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSpyglassOverlay(float param0) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SPYGLASS_SCOPE_LOCATION);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        float var2 = (float)Math.min(this.screenWidth, this.screenHeight);
        float var4 = Math.min((float)this.screenWidth / var2, (float)this.screenHeight / var2) * param0;
        float var5 = var2 * var4;
        float var6 = var2 * var4;
        float var7 = ((float)this.screenWidth - var5) / 2.0F;
        float var8 = ((float)this.screenHeight - var6) / 2.0F;
        float var9 = var7 + var5;
        float var10 = var8 + var6;
        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var1.vertex((double)var7, (double)var10, -90.0).uv(0.0F, 1.0F).endVertex();
        var1.vertex((double)var9, (double)var10, -90.0).uv(1.0F, 1.0F).endVertex();
        var1.vertex((double)var9, (double)var8, -90.0).uv(1.0F, 0.0F).endVertex();
        var1.vertex((double)var7, (double)var8, -90.0).uv(0.0F, 0.0F).endVertex();
        var0.end();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        var1.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var1.vertex(0.0, (double)this.screenHeight, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)this.screenWidth, (double)var10, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex(0.0, (double)var10, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex(0.0, (double)var8, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)this.screenWidth, (double)var8, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)this.screenWidth, 0.0, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex(0.0, 0.0, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex(0.0, (double)var10, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)var7, (double)var10, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)var7, (double)var8, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex(0.0, (double)var8, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)var9, (double)var10, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)this.screenWidth, (double)var10, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)this.screenWidth, (double)var8, -90.0).color(0, 0, 0, 255).endVertex();
        var1.vertex((double)var9, (double)var8, -90.0).color(0, 0, 0, 255).endVertex();
        var0.end();
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void updateVignetteBrightness(Entity param0) {
        if (param0 != null) {
            BlockPos var0 = new BlockPos(param0.getX(), param0.getEyeY(), param0.getZ());
            float var1 = LightTexture.getBrightness(param0.level.dimensionType(), param0.level.getMaxLocalRawBrightness(var0));
            float var2 = Mth.clamp(1.0F - var1, 0.0F, 1.0F);
            this.vignetteBrightness += (var2 - this.vignetteBrightness) * 0.01F;
        }
    }

    private void renderVignette(Entity param0) {
        WorldBorder var0 = this.minecraft.level.getWorldBorder();
        float var1 = (float)var0.getDistanceToBorder(param0);
        double var2 = Math.min(var0.getLerpSpeed() * (double)var0.getWarningTime() * 1000.0, Math.abs(var0.getLerpTarget() - var0.getSize()));
        double var3 = Math.max((double)var0.getWarningBlocks(), var2);
        if ((double)var1 < var3) {
            var1 = 1.0F - (float)((double)var1 / var3);
        } else {
            var1 = 0.0F;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        if (var1 > 0.0F) {
            var1 = Mth.clamp(var1, 0.0F, 1.0F);
            RenderSystem.setShaderColor(0.0F, var1, var1, 1.0F);
        } else {
            float var4 = this.vignetteBrightness;
            var4 = Mth.clamp(var4, 0.0F, 1.0F);
            RenderSystem.setShaderColor(var4, var4, var4, 1.0F);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
        Tesselator var5 = Tesselator.getInstance();
        BufferBuilder var6 = var5.getBuilder();
        var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var6.vertex(0.0, (double)this.screenHeight, -90.0).uv(0.0F, 1.0F).endVertex();
        var6.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(1.0F, 1.0F).endVertex();
        var6.vertex((double)this.screenWidth, 0.0, -90.0).uv(1.0F, 0.0F).endVertex();
        var6.vertex(0.0, 0.0, -90.0).uv(0.0F, 0.0F).endVertex();
        var5.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

    private void renderPortalOverlay(float param0) {
        if (param0 < 1.0F) {
            param0 *= param0;
            param0 *= param0;
            param0 = param0 * 0.8F + 0.2F;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, param0);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        TextureAtlasSprite var0 = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        float var1 = var0.getU0();
        float var2 = var0.getV0();
        float var3 = var0.getU1();
        float var4 = var0.getV1();
        Tesselator var5 = Tesselator.getInstance();
        BufferBuilder var6 = var5.getBuilder();
        var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var6.vertex(0.0, (double)this.screenHeight, -90.0).uv(var1, var4).endVertex();
        var6.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(var3, var4).endVertex();
        var6.vertex((double)this.screenWidth, 0.0, -90.0).uv(var3, var2).endVertex();
        var6.vertex(0.0, 0.0, -90.0).uv(var1, var2).endVertex();
        var5.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSlot(int param0, int param1, float param2, Player param3, ItemStack param4, int param5) {
        if (!param4.isEmpty()) {
            PoseStack var0 = RenderSystem.getModelViewStack();
            float var1 = (float)param4.getPopTime() - param2;
            if (var1 > 0.0F) {
                float var2 = 1.0F + var1 / 5.0F;
                var0.pushPose();
                var0.translate((float)(param0 + 8), (float)(param1 + 12), 0.0F);
                var0.scale(1.0F / var2, (var2 + 1.0F) / 2.0F, 1.0F);
                var0.translate((float)(-(param0 + 8)), (float)(-(param1 + 12)), 0.0F);
                RenderSystem.applyModelViewMatrix();
            }

            this.itemRenderer.renderAndDecorateItem(param3, param4, param0, param1, param5);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            if (var1 > 0.0F) {
                var0.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, param4, param0, param1);
        }
    }

    public void tick(boolean param0) {
        this.tickAutosaveIndicator();
        if (!param0) {
            this.tick();
        }

    }

    private void tick() {
        if (this.overlayMessageTime > 0) {
            --this.overlayMessageTime;
        }

        if (this.titleTime > 0) {
            --this.titleTime;
            if (this.titleTime <= 0) {
                this.title = null;
                this.subtitle = null;
            }
        }

        ++this.tickCount;
        Entity var0 = this.minecraft.getCameraEntity();
        if (var0 != null) {
            this.updateVignetteBrightness(var0);
        }

        if (this.minecraft.player != null) {
            ItemStack var1 = this.minecraft.player.getInventory().getSelected();
            if (var1.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (this.lastToolHighlight.isEmpty()
                || !var1.is(this.lastToolHighlight.getItem())
                || !var1.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
                this.toolHighlightTimer = 40;
            } else if (this.toolHighlightTimer > 0) {
                --this.toolHighlightTimer;
            }

            this.lastToolHighlight = var1;
        }

        this.chat.tick();
    }

    private void tickAutosaveIndicator() {
        MinecraftServer var0 = this.minecraft.getSingleplayerServer();
        boolean var1 = var0 != null && var0.isCurrentlySaving();
        this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
        this.autosaveIndicatorValue = Mth.lerp(0.2F, this.autosaveIndicatorValue, var1 ? 1.0F : 0.0F);
    }

    public void setNowPlaying(Component param0) {
        Component var0 = Component.translatable("record.nowPlaying", param0);
        this.setOverlayMessage(var0, true);
        this.minecraft.getNarrator().sayNow(var0);
    }

    public void setOverlayMessage(Component param0, boolean param1) {
        this.setChatDisabledByPlayerShown(false);
        this.overlayMessageString = param0;
        this.overlayMessageTime = 60;
        this.animateOverlayMessageColor = param1;
    }

    public void setChatDisabledByPlayerShown(boolean param0) {
        this.chatDisabledByPlayerShown = param0;
    }

    public boolean isShowingChatDisabledByPlayer() {
        return this.chatDisabledByPlayerShown && this.overlayMessageTime > 0;
    }

    public void setTimes(int param0, int param1, int param2) {
        if (param0 >= 0) {
            this.titleFadeInTime = param0;
        }

        if (param1 >= 0) {
            this.titleStayTime = param1;
        }

        if (param2 >= 0) {
            this.titleFadeOutTime = param2;
        }

        if (this.titleTime > 0) {
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
        }

    }

    public void setSubtitle(Component param0) {
        this.subtitle = param0;
    }

    public void setTitle(Component param0) {
        this.title = param0;
        this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
    }

    public void clear() {
        this.title = null;
        this.subtitle = null;
        this.titleTime = 0;
    }

    public ChatComponent getChat() {
        return this.chat;
    }

    public int getGuiTicks() {
        return this.tickCount;
    }

    public Font getFont() {
        return this.minecraft.font;
    }

    public SpectatorGui getSpectatorGui() {
        return this.spectatorGui;
    }

    public PlayerTabOverlay getTabList() {
        return this.tabList;
    }

    public void onDisconnected() {
        this.tabList.reset();
        this.bossOverlay.reset();
        this.minecraft.getToasts().clear();
        this.minecraft.options.renderDebug = false;
        this.chat.clearMessages(true);
    }

    public BossHealthOverlay getBossOverlay() {
        return this.bossOverlay;
    }

    public void clearCache() {
        this.debugScreen.clearChunkCache();
    }

    private void renderSavingIndicator(PoseStack param0) {
        if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
            int var0 = Mth.floor(
                255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F)
            );
            if (var0 > 8) {
                Font var1 = this.getFont();
                int var2 = var1.width(SAVING_TEXT);
                int var3 = 16777215 | var0 << 24 & 0xFF000000;
                var1.drawShadow(param0, SAVING_TEXT, (float)(this.screenWidth - var2 - 10), (float)(this.screenHeight - 15), var3);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    static enum HeartType {
        CONTAINER(0, false),
        NORMAL(2, true),
        POISIONED(4, true),
        WITHERED(6, true),
        ABSORBING(8, false),
        FROZEN(9, false);

        private final int index;
        private final boolean canBlink;

        private HeartType(int param0, boolean param1) {
            this.index = param0;
            this.canBlink = param1;
        }

        public int getX(boolean param0, boolean param1) {
            int var0;
            if (this == CONTAINER) {
                var0 = param1 ? 1 : 0;
            } else {
                int var1 = param0 ? 1 : 0;
                int var2 = this.canBlink && param1 ? 2 : 0;
                var0 = var1 + var2;
            }

            return 16 + (this.index * 2 + var0) * 9;
        }

        static Gui.HeartType forPlayer(Player param0) {
            Gui.HeartType var0;
            if (param0.hasEffect(MobEffects.POISON)) {
                var0 = POISIONED;
            } else if (param0.hasEffect(MobEffects.WITHER)) {
                var0 = WITHERED;
            } else if (param0.isFullyFrozen()) {
                var0 = FROZEN;
            } else {
                var0 = NORMAL;
            }

            return var0;
        }
    }
}
