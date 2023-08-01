package net.minecraft.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
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
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Gui {
    private static final ResourceLocation CROSSHAIR_SPRITE = new ResourceLocation("hud/crosshair");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE = new ResourceLocation("hud/crosshair_attack_indicator_full");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE = new ResourceLocation("hud/crosshair_attack_indicator_background");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE = new ResourceLocation("hud/crosshair_attack_indicator_progress");
    private static final ResourceLocation EFFECT_BACKGROUND_AMBIENT_SPRITE = new ResourceLocation("hud/effect_background_ambient");
    private static final ResourceLocation EFFECT_BACKGROUND_SPRITE = new ResourceLocation("hud/effect_background");
    private static final ResourceLocation HOTBAR_SPRITE = new ResourceLocation("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION_SPRITE = new ResourceLocation("hud/hotbar_selection");
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = new ResourceLocation("hud/hotbar_offhand_left");
    private static final ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE = new ResourceLocation("hud/hotbar_offhand_right");
    private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = new ResourceLocation("hud/hotbar_attack_indicator_background");
    private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = new ResourceLocation("hud/hotbar_attack_indicator_progress");
    private static final ResourceLocation JUMP_BAR_BACKGROUND_SPRITE = new ResourceLocation("hud/jump_bar_background");
    private static final ResourceLocation JUMP_BAR_COOLDOWN_SPRITE = new ResourceLocation("hud/jump_bar_cooldown");
    private static final ResourceLocation JUMP_BAR_PROGRESS_SPRITE = new ResourceLocation("hud/jump_bar_progress");
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = new ResourceLocation("hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = new ResourceLocation("hud/experience_bar_progress");
    private static final ResourceLocation ARMOR_EMPTY_SPRITE = new ResourceLocation("hud/armor_empty");
    private static final ResourceLocation ARMOR_HALF_SPRITE = new ResourceLocation("hud/armor_half");
    private static final ResourceLocation ARMOR_FULL_SPRITE = new ResourceLocation("hud/armor_full");
    private static final ResourceLocation FOOD_EMPTY_HUNGER_SPRITE = new ResourceLocation("hud/food_empty_hunger");
    private static final ResourceLocation FOOD_HALF_HUNGER_SPRITE = new ResourceLocation("hud/food_half_hunger");
    private static final ResourceLocation FOOD_FULL_HUNGER_SPRITE = new ResourceLocation("hud/food_full_hunger");
    private static final ResourceLocation FOOD_EMPTY_SPRITE = new ResourceLocation("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_SPRITE = new ResourceLocation("hud/food_half");
    private static final ResourceLocation FOOD_FULL_SPRITE = new ResourceLocation("hud/food_full");
    private static final ResourceLocation AIR_SPRITE = new ResourceLocation("hud/air");
    private static final ResourceLocation AIR_BURSTING_SPRITE = new ResourceLocation("hud/air_bursting");
    private static final ResourceLocation HEART_VEHICLE_CONTAINER_SPRITE = new ResourceLocation("hud/heart/vehicle_container");
    private static final ResourceLocation HEART_VEHICLE_FULL_SPRITE = new ResourceLocation("hud/heart/vehicle_full");
    private static final ResourceLocation HEART_VEHICLE_HALF_SPRITE = new ResourceLocation("hud/heart/vehicle_half");
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
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

    public void render(GuiGraphics param0, float param1) {
        Window var0 = this.minecraft.getWindow();
        this.screenWidth = param0.guiWidth();
        this.screenHeight = param0.guiHeight();
        Font var1 = this.getFont();
        RenderSystem.enableBlend();
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(param0, this.minecraft.getCameraEntity());
        } else {
            RenderSystem.enableDepthTest();
        }

        float var2 = this.minecraft.getDeltaFrameTime();
        this.scopeScale = Mth.lerp(0.5F * var2, this.scopeScale, 1.125F);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (this.minecraft.player.isScoping()) {
                this.renderSpyglassOverlay(param0, this.scopeScale);
            } else {
                this.scopeScale = 0.5F;
                ItemStack var3 = this.minecraft.player.getInventory().getArmor(3);
                if (var3.is(Blocks.CARVED_PUMPKIN.asItem())) {
                    this.renderTextureOverlay(param0, PUMPKIN_BLUR_LOCATION, 1.0F);
                }
            }
        }

        if (this.minecraft.player.getTicksFrozen() > 0) {
            this.renderTextureOverlay(param0, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
        }

        float var4 = Mth.lerp(param1, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
        if (var4 > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(param0, var4);
        }

        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(param0);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(param1, param0);
        }

        if (!this.minecraft.options.hideGui) {
            RenderSystem.enableBlend();
            this.renderCrosshair(param0);
            this.minecraft.getProfiler().push("bossHealth");
            this.bossOverlay.render(param0);
            this.minecraft.getProfiler().pop();
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

            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(param0);
            } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(param0);
            }
        }

        if (this.minecraft.player.getSleepTimer() > 0) {
            this.minecraft.getProfiler().push("sleep");
            float var7 = (float)this.minecraft.player.getSleepTimer();
            float var8 = var7 / 100.0F;
            if (var8 > 1.0F) {
                var8 = 1.0F - (var7 - 100.0F) / 10.0F;
            }

            int var9 = (int)(220.0F * var8) << 24 | 1052704;
            param0.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, this.screenHeight, var9);
            this.minecraft.getProfiler().pop();
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
                    param0.pose().pushPose();
                    param0.pose().translate((float)(this.screenWidth / 2), (float)(this.screenHeight - 68), 0.0F);
                    int var12 = 16777215;
                    if (this.animateOverlayMessageColor) {
                        var12 = Mth.hsvToRgb(var10 / 50.0F, 0.7F, 0.6F) & 16777215;
                    }

                    int var13 = var11 << 24 & 0xFF000000;
                    int var14 = var1.width(this.overlayMessageString);
                    this.drawBackdrop(param0, var1, -4, var14, 16777215 | var13);
                    param0.drawString(var1, this.overlayMessageString, -var14 / 2, -4, var12 | var13);
                    param0.pose().popPose();
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
                    param0.pose().pushPose();
                    param0.pose().translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
                    RenderSystem.enableBlend();
                    param0.pose().pushPose();
                    param0.pose().scale(4.0F, 4.0F, 4.0F);
                    int var18 = var16 << 24 & 0xFF000000;
                    int var19 = var1.width(this.title);
                    this.drawBackdrop(param0, var1, -10, var19, 16777215 | var18);
                    param0.drawString(var1, this.title, -var19 / 2, -10, 16777215 | var18);
                    param0.pose().popPose();
                    if (this.subtitle != null) {
                        param0.pose().pushPose();
                        param0.pose().scale(2.0F, 2.0F, 2.0F);
                        int var20 = var1.width(this.subtitle);
                        this.drawBackdrop(param0, var1, 5, var20, 16777215 | var18);
                        param0.drawString(var1, this.subtitle, -var20 / 2, 5, 16777215 | var18);
                        param0.pose().popPose();
                    }

                    RenderSystem.disableBlend();
                    param0.pose().popPose();
                }

                this.minecraft.getProfiler().pop();
            }

            this.subtitleOverlay.render(param0);
            Scoreboard var21 = this.minecraft.level.getScoreboard();
            Objective var22 = null;
            PlayerTeam var23 = var21.getPlayersTeam(this.minecraft.player.getScoreboardName());
            if (var23 != null) {
                DisplaySlot var24 = DisplaySlot.teamColorToSlot(var23.getColor());
                if (var24 != null) {
                    var22 = var21.getDisplayObjective(var24);
                }
            }

            Objective var25 = var22 != null ? var22 : var21.getDisplayObjective(DisplaySlot.SIDEBAR);
            if (var25 != null) {
                this.displayScoreboardSidebar(param0, var25);
            }

            RenderSystem.enableBlend();
            int var26 = Mth.floor(this.minecraft.mouseHandler.xpos() * (double)var0.getGuiScaledWidth() / (double)var0.getScreenWidth());
            int var27 = Mth.floor(this.minecraft.mouseHandler.ypos() * (double)var0.getGuiScaledHeight() / (double)var0.getScreenHeight());
            this.minecraft.getProfiler().push("chat");
            this.chat.render(param0, this.tickCount, var26, var27);
            this.minecraft.getProfiler().pop();
            var25 = var21.getDisplayObjective(DisplaySlot.LIST);
            if (!this.minecraft.options.keyPlayerList.isDown()
                || this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && var25 == null) {
                this.tabList.setVisible(false);
            } else {
                this.tabList.setVisible(true);
                this.tabList.render(param0, this.screenWidth, var21, var25);
            }

            this.renderSavingIndicator(param0);
        }

    }

    private void drawBackdrop(GuiGraphics param0, Font param1, int param2, int param3, int param4) {
        int var0 = this.minecraft.options.getBackgroundColor(0.0F);
        if (var0 != 0) {
            int var1 = -param3 / 2;
            param0.fill(var1 - 2, param2 - 2, var1 + param3 + 2, param2 + 9 + 2, FastColor.ARGB32.multiply(var0, param4));
        }

    }

    private void renderCrosshair(GuiGraphics param0) {
        Options var0 = this.minecraft.options;
        if (var0.getCameraType().isFirstPerson()) {
            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
                if (var0.renderDebug && !var0.hideGui && !this.minecraft.player.isReducedDebugInfo() && !var0.reducedDebugInfo().get()) {
                    Camera var1 = this.minecraft.gameRenderer.getMainCamera();
                    PoseStack var2 = RenderSystem.getModelViewStack();
                    var2.pushPose();
                    var2.mulPoseMatrix(param0.pose().last().pose());
                    var2.translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
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
                    param0.blitSprite(CROSSHAIR_SPRITE, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 15, 15);
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
                            param0.blitSprite(CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, var7, var6, 16, 16);
                        } else if (var4 < 1.0F) {
                            int var8 = (int)(var4 * 17.0F);
                            param0.blitSprite(CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, var7, var6, 16, 4);
                            param0.blitSprite(CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, var7, var6, var8, 4);
                        }
                    }

                    RenderSystem.defaultBlendFunc();
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

    protected void renderEffects(GuiGraphics param0) {
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

                    float var10 = 1.0F;
                    if (var6.isAmbient()) {
                        param0.blitSprite(EFFECT_BACKGROUND_AMBIENT_SPRITE, var8, var9, 24, 24);
                    } else {
                        param0.blitSprite(EFFECT_BACKGROUND_SPRITE, var8, var9, 24, 24);
                        if (var6.endsWithin(200)) {
                            int var11 = var6.getDuration();
                            int var12 = 10 - var11 / 20;
                            var10 = Mth.clamp((float)var11 / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                                + Mth.cos((float)var11 * (float) Math.PI / 5.0F) * Mth.clamp((float)var12 / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    TextureAtlasSprite var13 = var4.get(var7);
                    int var15 = var9;
                    float var16 = var10;
                    var5.add(() -> {
                        param0.setColor(1.0F, 1.0F, 1.0F, var16);
                        param0.blit(var8 + 3, var15 + 3, 0, 18, 18, var13);
                        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    });
                }
            }

            var5.forEach(Runnable::run);
        }
    }

    private void renderHotbar(float param0, GuiGraphics param1) {
        Player var0 = this.getCameraPlayer();
        if (var0 != null) {
            ItemStack var1 = var0.getOffhandItem();
            HumanoidArm var2 = var0.getMainArm().getOpposite();
            int var3 = this.screenWidth / 2;
            int var4 = 182;
            int var5 = 91;
            param1.pose().pushPose();
            param1.pose().translate(0.0F, 0.0F, -90.0F);
            param1.blitSprite(HOTBAR_SPRITE, var3 - 91, this.screenHeight - 22, 182, 22);
            param1.blitSprite(HOTBAR_SELECTION_SPRITE, var3 - 91 - 1 + var0.getInventory().selected * 20, this.screenHeight - 22 - 1, 24, 23);
            if (!var1.isEmpty()) {
                if (var2 == HumanoidArm.LEFT) {
                    param1.blitSprite(HOTBAR_OFFHAND_LEFT_SPRITE, var3 - 91 - 29, this.screenHeight - 23, 29, 24);
                } else {
                    param1.blitSprite(HOTBAR_OFFHAND_RIGHT_SPRITE, var3 + 91, this.screenHeight - 23, 29, 24);
                }
            }

            param1.pose().popPose();
            int var6 = 1;

            for(int var7 = 0; var7 < 9; ++var7) {
                int var8 = var3 - 90 + var7 * 20 + 2;
                int var9 = this.screenHeight - 16 - 3;
                this.renderSlot(param1, var8, var9, param0, var0, var0.getInventory().items.get(var7), var6++);
            }

            if (!var1.isEmpty()) {
                int var10 = this.screenHeight - 16 - 3;
                if (var2 == HumanoidArm.LEFT) {
                    this.renderSlot(param1, var3 - 91 - 26, var10, param0, var0, var1, var6++);
                } else {
                    this.renderSlot(param1, var3 + 91 + 10, var10, param0, var0, var1, var6++);
                }
            }

            RenderSystem.enableBlend();
            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
                float var11 = this.minecraft.player.getAttackStrengthScale(0.0F);
                if (var11 < 1.0F) {
                    int var12 = this.screenHeight - 20;
                    int var13 = var3 + 91 + 6;
                    if (var2 == HumanoidArm.RIGHT) {
                        var13 = var3 - 91 - 22;
                    }

                    int var14 = (int)(var11 * 19.0F);
                    param1.blitSprite(HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, var13, var12, 18, 18);
                    param1.blitSprite(HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - var14, var13, var12 + 18 - var14, 18, var14);
                }
            }

            RenderSystem.disableBlend();
        }
    }

    public void renderJumpMeter(PlayerRideableJumping param0, GuiGraphics param1, int param2) {
        this.minecraft.getProfiler().push("jumpBar");
        float var0 = this.minecraft.player.getJumpRidingScale();
        int var1 = 182;
        int var2 = (int)(var0 * 183.0F);
        int var3 = this.screenHeight - 32 + 3;
        param1.blitSprite(JUMP_BAR_BACKGROUND_SPRITE, param2, var3, 182, 5);
        if (param0.getJumpCooldown() > 0) {
            param1.blitSprite(JUMP_BAR_COOLDOWN_SPRITE, param2, var3, 182, 5);
        } else if (var2 > 0) {
            param1.blitSprite(JUMP_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, param2, var3, var2, 5);
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderExperienceBar(GuiGraphics param0, int param1) {
        this.minecraft.getProfiler().push("expBar");
        int var0 = this.minecraft.player.getXpNeededForNextLevel();
        if (var0 > 0) {
            int var1 = 182;
            int var2 = (int)(this.minecraft.player.experienceProgress * 183.0F);
            int var3 = this.screenHeight - 32 + 3;
            param0.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, param1, var3, 182, 5);
            if (var2 > 0) {
                param0.blitSprite(EXPERIENCE_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, param1, var3, var2, 5);
            }
        }

        this.minecraft.getProfiler().pop();
        if (this.minecraft.player.experienceLevel > 0) {
            this.minecraft.getProfiler().push("expLevel");
            String var4 = this.minecraft.player.experienceLevel + "";
            int var5 = (this.screenWidth - this.getFont().width(var4)) / 2;
            int var6 = this.screenHeight - 31 - 4;
            param0.drawString(this.getFont(), var4, var5 + 1, var6, 0, false);
            param0.drawString(this.getFont(), var4, var5 - 1, var6, 0, false);
            param0.drawString(this.getFont(), var4, var5, var6 + 1, 0, false);
            param0.drawString(this.getFont(), var4, var5, var6 - 1, 0, false);
            param0.drawString(this.getFont(), var4, var5, var6, 8453920, false);
            this.minecraft.getProfiler().pop();
        }

    }

    public void renderSelectedItemName(GuiGraphics param0) {
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
                param0.fill(var2 - 2, var3 - 2, var2 + var1 + 2, var3 + 9 + 2, this.minecraft.options.getBackgroundColor(0));
                param0.drawString(this.getFont(), var0, var2, var3, 16777215 + (var4 << 24));
            }
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderDemoOverlay(GuiGraphics param0) {
        this.minecraft.getProfiler().push("demo");
        Component var0;
        if (this.minecraft.level.getGameTime() >= 120500L) {
            var0 = DEMO_EXPIRED_TEXT;
        } else {
            var0 = Component.translatable("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
        }

        int var2 = this.getFont().width(var0);
        param0.drawString(this.getFont(), var0, this.screenWidth - var2 - 10, 5, 16777215);
        this.minecraft.getProfiler().pop();
    }

    private void displayScoreboardSidebar(GuiGraphics param0, Objective param1) {
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
            param0.fill(var14 - 2, var23, var24, var23 + 9, var16);
            param0.drawString(this.getFont(), var20, var14, var23, -1, false);
            param0.drawString(this.getFont(), var21, var24 - this.getFont().width(var21), var23, -1, false);
            if (var15 == var1.size()) {
                param0.fill(var14 - 2, var23 - 9 - 1, var24, var23 - 1, var17);
                param0.fill(var14 - 2, var23 - 1, var24, var23, var16);
                param0.drawString(this.getFont(), var4, var14 + var6 / 2 - var5 / 2, var23 - 9, -1, false);
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

    private void renderPlayerHealth(GuiGraphics param0) {
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
                        param0.blitSprite(ARMOR_FULL_SPRITE, var19, var14, 9, 9);
                    }

                    if (var18 * 2 + 1 == var16) {
                        param0.blitSprite(ARMOR_HALF_SPRITE, var19, var14, 9, 9);
                    }

                    if (var18 * 2 + 1 > var16) {
                        param0.blitSprite(ARMOR_EMPTY_SPRITE, var19, var14, 9, 9);
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
                    ResourceLocation var24;
                    ResourceLocation var25;
                    ResourceLocation var26;
                    if (var0.hasEffect(MobEffects.HUNGER)) {
                        var24 = FOOD_EMPTY_HUNGER_SPRITE;
                        var25 = FOOD_HALF_HUNGER_SPRITE;
                        var26 = FOOD_FULL_HUNGER_SPRITE;
                    } else {
                        var24 = FOOD_EMPTY_SPRITE;
                        var25 = FOOD_HALF_SPRITE;
                        var26 = FOOD_FULL_SPRITE;
                    }

                    if (var0.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (var6 * 3 + 1) == 0) {
                        var23 = var9 + (this.random.nextInt(3) - 1);
                    }

                    int var30 = var8 - var22 * 8 - 9;
                    param0.blitSprite(var24, var30, var23, 9, 9);
                    if (var22 * 2 + 1 < var6) {
                        param0.blitSprite(var25, var30, var23, 9, 9);
                    }

                    if (var22 * 2 + 1 == var6) {
                        param0.blitSprite(var26, var30, var23, 9, 9);
                    }
                }

                var15 -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            int var31 = var0.getMaxAirSupply();
            int var32 = Math.min(var0.getAirSupply(), var31);
            if (var0.isEyeInFluid(FluidTags.WATER) || var32 < var31) {
                int var33 = this.getVisibleVehicleHeartRows(var21) - 1;
                var15 -= var33 * 10;
                int var34 = Mth.ceil((double)(var32 - 2) * 10.0 / (double)var31);
                int var35 = Mth.ceil((double)var32 * 10.0 / (double)var31) - var34;

                for(int var36 = 0; var36 < var34 + var35; ++var36) {
                    if (var36 < var34) {
                        param0.blitSprite(AIR_SPRITE, var8 - var36 * 8 - 9, var15, 9, 9);
                    } else {
                        param0.blitSprite(AIR_BURSTING_SPRITE, var8 - var36 * 8 - 9, var15, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }
    }

    private void renderHearts(
        GuiGraphics param0, Player param1, int param2, int param3, int param4, int param5, float param6, int param7, int param8, int param9, boolean param10
    ) {
        Gui.HeartType var0 = Gui.HeartType.forPlayer(param1);
        boolean var1 = param1.level().getLevelData().isHardcore();
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

    private void renderHeart(GuiGraphics param0, Gui.HeartType param1, int param2, int param3, boolean param4, boolean param5, boolean param6) {
        param0.blitSprite(param1.getSprite(param4, param6, param5), param2, param3, 9, 9);
    }

    private void renderVehicleHealth(GuiGraphics param0) {
        LivingEntity var0 = this.getPlayerVehicleWithHealth();
        if (var0 != null) {
            int var1 = this.getVehicleMaxHearts(var0);
            if (var1 != 0) {
                int var2 = (int)Math.ceil((double)var0.getHealth());
                this.minecraft.getProfiler().popPush("mountHealth");
                int var3 = this.screenHeight - 39;
                int var4 = this.screenWidth / 2 + 91;
                int var5 = var3;

                for(int var6 = 0; var1 > 0; var6 += 20) {
                    int var7 = Math.min(var1, 10);
                    var1 -= var7;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        int var9 = var4 - var8 * 8 - 9;
                        param0.blitSprite(HEART_VEHICLE_CONTAINER_SPRITE, var9, var5, 9, 9);
                        if (var8 * 2 + 1 + var6 < var2) {
                            param0.blitSprite(HEART_VEHICLE_FULL_SPRITE, var9, var5, 9, 9);
                        }

                        if (var8 * 2 + 1 + var6 == var2) {
                            param0.blitSprite(HEART_VEHICLE_HALF_SPRITE, var9, var5, 9, 9);
                        }
                    }

                    var5 -= 10;
                }

            }
        }
    }

    private void renderTextureOverlay(GuiGraphics param0, ResourceLocation param1, float param2) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        param0.setColor(1.0F, 1.0F, 1.0F, param2);
        param0.blit(param1, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSpyglassOverlay(GuiGraphics param0, float param1) {
        float var0 = (float)Math.min(this.screenWidth, this.screenHeight);
        float var2 = Math.min((float)this.screenWidth / var0, (float)this.screenHeight / var0) * param1;
        int var3 = Mth.floor(var0 * var2);
        int var4 = Mth.floor(var0 * var2);
        int var5 = (this.screenWidth - var3) / 2;
        int var6 = (this.screenHeight - var4) / 2;
        int var7 = var5 + var3;
        int var8 = var6 + var4;
        param0.blit(SPYGLASS_SCOPE_LOCATION, var5, var6, -90, 0.0F, 0.0F, var3, var4, var3, var4);
        param0.fill(RenderType.guiOverlay(), 0, var8, this.screenWidth, this.screenHeight, -90, -16777216);
        param0.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, var6, -90, -16777216);
        param0.fill(RenderType.guiOverlay(), 0, var6, var5, var8, -90, -16777216);
        param0.fill(RenderType.guiOverlay(), var7, var6, this.screenWidth, var8, -90, -16777216);
    }

    private void updateVignetteBrightness(Entity param0) {
        if (param0 != null) {
            BlockPos var0 = BlockPos.containing(param0.getX(), param0.getEyeY(), param0.getZ());
            float var1 = LightTexture.getBrightness(param0.level().dimensionType(), param0.level().getMaxLocalRawBrightness(var0));
            float var2 = Mth.clamp(1.0F - var1, 0.0F, 1.0F);
            this.vignetteBrightness += (var2 - this.vignetteBrightness) * 0.01F;
        }
    }

    private void renderVignette(GuiGraphics param0, Entity param1) {
        WorldBorder var0 = this.minecraft.level.getWorldBorder();
        float var1 = (float)var0.getDistanceToBorder(param1);
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
            param0.setColor(0.0F, var1, var1, 1.0F);
        } else {
            float var4 = this.vignetteBrightness;
            var4 = Mth.clamp(var4, 0.0F, 1.0F);
            param0.setColor(var4, var4, var4, 1.0F);
        }

        param0.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

    private void renderPortalOverlay(GuiGraphics param0, float param1) {
        if (param1 < 1.0F) {
            param1 *= param1;
            param1 *= param1;
            param1 = param1 * 0.8F + 0.2F;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        param0.setColor(1.0F, 1.0F, 1.0F, param1);
        TextureAtlasSprite var0 = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        param0.blit(0, 0, -90, this.screenWidth, this.screenHeight, var0);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSlot(GuiGraphics param0, int param1, int param2, float param3, Player param4, ItemStack param5, int param6) {
        if (!param5.isEmpty()) {
            float var0 = (float)param5.getPopTime() - param3;
            if (var0 > 0.0F) {
                float var1 = 1.0F + var0 / 5.0F;
                param0.pose().pushPose();
                param0.pose().translate((float)(param1 + 8), (float)(param2 + 12), 0.0F);
                param0.pose().scale(1.0F / var1, (var1 + 1.0F) / 2.0F, 1.0F);
                param0.pose().translate((float)(-(param1 + 8)), (float)(-(param2 + 12)), 0.0F);
            }

            param0.renderItem(param4, param5, param1, param2, param6);
            if (var0 > 0.0F) {
                param0.pose().popPose();
            }

            param0.renderItemDecorations(this.minecraft.font, param5, param1, param2);
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
                this.toolHighlightTimer = (int)(40.0 * this.minecraft.options.notificationDisplayTime().get());
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

    private void renderSavingIndicator(GuiGraphics param0) {
        if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
            int var0 = Mth.floor(
                255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F)
            );
            if (var0 > 8) {
                Font var1 = this.getFont();
                int var2 = var1.width(SAVING_TEXT);
                int var3 = 16777215 | var0 << 24 & 0xFF000000;
                param0.drawString(var1, SAVING_TEXT, this.screenWidth - var2 - 10, this.screenHeight - 15, var3);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    static enum HeartType {
        CONTAINER(
            new ResourceLocation("hud/heart/container"),
            new ResourceLocation("hud/heart/container_blinking"),
            new ResourceLocation("hud/heart/container"),
            new ResourceLocation("hud/heart/container_blinking"),
            new ResourceLocation("hud/heart/container_hardcore"),
            new ResourceLocation("hud/heart/container_hardcore_blinking"),
            new ResourceLocation("hud/heart/container_hardcore"),
            new ResourceLocation("hud/heart/container_hardcore_blinking")
        ),
        NORMAL(
            new ResourceLocation("hud/heart/full"),
            new ResourceLocation("hud/heart/full_blinking"),
            new ResourceLocation("hud/heart/half"),
            new ResourceLocation("hud/heart/half_blinking"),
            new ResourceLocation("hud/heart/hardcore_full"),
            new ResourceLocation("hud/heart/hardcore_full_blinking"),
            new ResourceLocation("hud/heart/hardcore_half"),
            new ResourceLocation("hud/heart/hardcore_half_blinking")
        ),
        POISIONED(
            new ResourceLocation("hud/heart/poisoned_full"),
            new ResourceLocation("hud/heart/poisoned_full_blinking"),
            new ResourceLocation("hud/heart/poisoned_half"),
            new ResourceLocation("hud/heart/poisoned_half_blinking"),
            new ResourceLocation("hud/heart/poisoned_hardcore_full"),
            new ResourceLocation("hud/heart/poisoned_hardcore_full_blinking"),
            new ResourceLocation("hud/heart/poisoned_hardcore_half"),
            new ResourceLocation("hud/heart/poisoned_hardcore_half_blinking")
        ),
        WITHERED(
            new ResourceLocation("hud/heart/withered_full"),
            new ResourceLocation("hud/heart/withered_full_blinking"),
            new ResourceLocation("hud/heart/withered_half"),
            new ResourceLocation("hud/heart/withered_half_blinking"),
            new ResourceLocation("hud/heart/withered_hardcore_full"),
            new ResourceLocation("hud/heart/withered_hardcore_full_blinking"),
            new ResourceLocation("hud/heart/withered_hardcore_half"),
            new ResourceLocation("hud/heart/withered_hardcore_half_blinking")
        ),
        ABSORBING(
            new ResourceLocation("hud/heart/absorbing_full"),
            new ResourceLocation("hud/heart/absorbing_full_blinking"),
            new ResourceLocation("hud/heart/absorbing_half"),
            new ResourceLocation("hud/heart/absorbing_half_blinking"),
            new ResourceLocation("hud/heart/absorbing_hardcore_full"),
            new ResourceLocation("hud/heart/absorbing_hardcore_full_blinking"),
            new ResourceLocation("hud/heart/absorbing_hardcore_half"),
            new ResourceLocation("hud/heart/absorbing_hardcore_half_blinking")
        ),
        FROZEN(
            new ResourceLocation("hud/heart/frozen_full"),
            new ResourceLocation("hud/heart/frozen_full_blinking"),
            new ResourceLocation("hud/heart/frozen_half"),
            new ResourceLocation("hud/heart/frozen_half_blinking"),
            new ResourceLocation("hud/heart/frozen_hardcore_full"),
            new ResourceLocation("hud/heart/frozen_hardcore_full_blinking"),
            new ResourceLocation("hud/heart/frozen_hardcore_half"),
            new ResourceLocation("hud/heart/frozen_hardcore_half_blinking")
        );

        private final ResourceLocation full;
        private final ResourceLocation fullBlinking;
        private final ResourceLocation half;
        private final ResourceLocation halfBlinking;
        private final ResourceLocation hardcoreFull;
        private final ResourceLocation hardcoreFullBlinking;
        private final ResourceLocation hardcoreHalf;
        private final ResourceLocation hardcoreHalfBlinking;

        private HeartType(
            ResourceLocation param0,
            ResourceLocation param1,
            ResourceLocation param2,
            ResourceLocation param3,
            ResourceLocation param4,
            ResourceLocation param5,
            ResourceLocation param6,
            ResourceLocation param7
        ) {
            this.full = param0;
            this.fullBlinking = param1;
            this.half = param2;
            this.halfBlinking = param3;
            this.hardcoreFull = param4;
            this.hardcoreFullBlinking = param5;
            this.hardcoreHalf = param6;
            this.hardcoreHalfBlinking = param7;
        }

        public ResourceLocation getSprite(boolean param0, boolean param1, boolean param2) {
            if (!param0) {
                if (param1) {
                    return param2 ? this.halfBlinking : this.half;
                } else {
                    return param2 ? this.fullBlinking : this.full;
                }
            } else if (param1) {
                return param2 ? this.hardcoreHalfBlinking : this.hardcoreHalf;
            } else {
                return param2 ? this.hardcoreFullBlinking : this.hardcoreFull;
            }
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
