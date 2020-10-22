package net.minecraft.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.ChatListener;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.chat.OverlayChatListener;
import net.minecraft.client.gui.chat.StandardChatListener;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
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
    private static final Component DEMO_EXPIRED_TEXT = new TranslatableComponent("demo.demoExpired");
    private final Random random = new Random();
    private final Minecraft minecraft;
    private final ItemRenderer itemRenderer;
    private final ChatComponent chat;
    private int tickCount;
    @Nullable
    private Component overlayMessageString;
    private int overlayMessageTime;
    private boolean animateOverlayMessageColor;
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
    private final Map<ChatType, List<ChatListener>> chatListeners = Maps.newHashMap();

    public Gui(Minecraft param0) {
        this.minecraft = param0;
        this.itemRenderer = param0.getItemRenderer();
        this.debugScreen = new DebugScreenOverlay(param0);
        this.spectatorGui = new SpectatorGui(param0);
        this.chat = new ChatComponent(param0);
        this.tabList = new PlayerTabOverlay(param0, this);
        this.bossOverlay = new BossHealthOverlay(param0);
        this.subtitleOverlay = new SubtitleOverlay(param0);

        for(ChatType var0 : ChatType.values()) {
            this.chatListeners.put(var0, Lists.newArrayList());
        }

        ChatListener var1 = NarratorChatListener.INSTANCE;
        this.chatListeners.get(ChatType.CHAT).add(new StandardChatListener(param0));
        this.chatListeners.get(ChatType.CHAT).add(var1);
        this.chatListeners.get(ChatType.SYSTEM).add(new StandardChatListener(param0));
        this.chatListeners.get(ChatType.SYSTEM).add(var1);
        this.chatListeners.get(ChatType.GAME_INFO).add(new OverlayChatListener(param0));
        this.resetTitleTimes();
    }

    public void resetTitleTimes() {
        this.titleFadeInTime = 10;
        this.titleStayTime = 70;
        this.titleFadeOutTime = 20;
    }

    public void render(PoseStack param0, float param1) {
        this.screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        this.screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        Font var0 = this.getFont();
        RenderSystem.enableBlend();
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(this.minecraft.getCameraEntity());
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
        }

        ItemStack var1 = this.minecraft.player.inventory.getArmor(3);
        if (this.minecraft.options.getCameraType().isFirstPerson() && var1.getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
            this.renderPumpkin();
        }

        float var2 = Mth.lerp(param1, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
        if (var2 > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(var2);
        }

        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(param0, param1);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(param1, param0);
        }

        if (!this.minecraft.options.hideGui) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            this.renderCrosshair(param0);
            RenderSystem.defaultBlendFunc();
            this.minecraft.getProfiler().push("bossHealth");
            this.bossOverlay.render(param0);
            this.minecraft.getProfiler().pop();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
            if (this.minecraft.gameMode.canHurtPlayer()) {
                this.renderPlayerHealth(param0);
            }

            this.renderVehicleHealth(param0);
            RenderSystem.disableBlend();
            int var3 = this.screenWidth / 2 - 91;
            if (this.minecraft.player.isRidingJumpable()) {
                this.renderJumpMeter(param0, var3);
            } else if (this.minecraft.gameMode.hasExperience()) {
                this.renderExperienceBar(param0, var3);
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
            RenderSystem.disableAlphaTest();
            float var4 = (float)this.minecraft.player.getSleepTimer();
            float var5 = var4 / 100.0F;
            if (var5 > 1.0F) {
                var5 = 1.0F - (var4 - 100.0F) / 10.0F;
            }

            int var6 = (int)(220.0F * var5) << 24 | 1052704;
            fill(param0, 0, 0, this.screenWidth, this.screenHeight, var6);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            this.minecraft.getProfiler().pop();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
                float var7 = (float)this.overlayMessageTime - param1;
                int var8 = (int)(var7 * 255.0F / 20.0F);
                if (var8 > 255) {
                    var8 = 255;
                }

                if (var8 > 8) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef((float)(this.screenWidth / 2), (float)(this.screenHeight - 68), 0.0F);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    int var9 = 16777215;
                    if (this.animateOverlayMessageColor) {
                        var9 = Mth.hsvToRgb(var7 / 50.0F, 0.7F, 0.6F) & 16777215;
                    }

                    int var10 = var8 << 24 & 0xFF000000;
                    int var11 = var0.width(this.overlayMessageString);
                    this.drawBackdrop(param0, var0, -4, var11, 16777215 | var10);
                    var0.draw(param0, this.overlayMessageString, (float)(-var11 / 2), -4.0F, var9 | var10);
                    RenderSystem.disableBlend();
                    RenderSystem.popMatrix();
                }

                this.minecraft.getProfiler().pop();
            }

            if (this.title != null && this.titleTime > 0) {
                this.minecraft.getProfiler().push("titleAndSubtitle");
                float var12 = (float)this.titleTime - param1;
                int var13 = 255;
                if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
                    float var14 = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - var12;
                    var13 = (int)(var14 * 255.0F / (float)this.titleFadeInTime);
                }

                if (this.titleTime <= this.titleFadeOutTime) {
                    var13 = (int)(var12 * 255.0F / (float)this.titleFadeOutTime);
                }

                var13 = Mth.clamp(var13, 0, 255);
                if (var13 > 8) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.pushMatrix();
                    RenderSystem.scalef(4.0F, 4.0F, 4.0F);
                    int var15 = var13 << 24 & 0xFF000000;
                    int var16 = var0.width(this.title);
                    this.drawBackdrop(param0, var0, -10, var16, 16777215 | var15);
                    var0.drawShadow(param0, this.title, (float)(-var16 / 2), -10.0F, 16777215 | var15);
                    RenderSystem.popMatrix();
                    if (this.subtitle != null) {
                        RenderSystem.pushMatrix();
                        RenderSystem.scalef(2.0F, 2.0F, 2.0F);
                        int var17 = var0.width(this.subtitle);
                        this.drawBackdrop(param0, var0, 5, var17, 16777215 | var15);
                        var0.drawShadow(param0, this.subtitle, (float)(-var17 / 2), 5.0F, 16777215 | var15);
                        RenderSystem.popMatrix();
                    }

                    RenderSystem.disableBlend();
                    RenderSystem.popMatrix();
                }

                this.minecraft.getProfiler().pop();
            }

            this.subtitleOverlay.render(param0);
            Scoreboard var18 = this.minecraft.level.getScoreboard();
            Objective var19 = null;
            PlayerTeam var20 = var18.getPlayersTeam(this.minecraft.player.getScoreboardName());
            if (var20 != null) {
                int var21 = var20.getColor().getId();
                if (var21 >= 0) {
                    var19 = var18.getDisplayObjective(3 + var21);
                }
            }

            Objective var22 = var19 != null ? var19 : var18.getDisplayObjective(1);
            if (var22 != null) {
                this.displayScoreboardSidebar(param0, var22);
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableAlphaTest();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, (float)(this.screenHeight - 48), 0.0F);
            this.minecraft.getProfiler().push("chat");
            this.chat.render(param0, this.tickCount);
            this.minecraft.getProfiler().pop();
            RenderSystem.popMatrix();
            var22 = var18.getDisplayObjective(0);
            if (!this.minecraft.options.keyPlayerList.isDown()
                || this.minecraft.isLocalServer() && this.minecraft.player.connection.getOnlinePlayers().size() <= 1 && var22 == null) {
                this.tabList.setVisible(false);
            } else {
                this.tabList.setVisible(true);
                this.tabList.render(param0, this.screenWidth, var18, var22);
            }
        }

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
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
                if (var0.renderDebug && !var0.hideGui && !this.minecraft.player.isReducedDebugInfo() && !var0.reducedDebugInfo) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), (float)this.getBlitOffset());
                    Camera var1 = this.minecraft.gameRenderer.getMainCamera();
                    RenderSystem.rotatef(var1.getXRot(), -1.0F, 0.0F, 0.0F);
                    RenderSystem.rotatef(var1.getYRot(), 0.0F, 1.0F, 0.0F);
                    RenderSystem.scalef(-1.0F, -1.0F, -1.0F);
                    RenderSystem.renderCrosshair(10);
                    RenderSystem.popMatrix();
                } else {
                    RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO
                    );
                    int var2 = 15;
                    this.blit(param0, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
                    if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float var3 = this.minecraft.player.getAttackStrengthScale(0.0F);
                        boolean var4 = false;
                        if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && var3 >= 1.0F) {
                            var4 = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                            var4 &= this.minecraft.crosshairPickEntity.isAlive();
                        }

                        int var5 = this.screenHeight / 2 - 7 + 16;
                        int var6 = this.screenWidth / 2 - 8;
                        if (var4) {
                            this.blit(param0, var6, var5, 68, 94, 16, 16);
                        } else if (var3 < 1.0F) {
                            int var7 = (int)(var3 * 17.0F);
                            this.blit(param0, var6, var5, 36, 94, 16, 4);
                            this.blit(param0, var6, var5, 52, 94, var7, 4);
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
            RenderSystem.enableBlend();
            int var1 = 0;
            int var2 = 0;
            MobEffectTextureManager var3 = this.minecraft.getMobEffectTextures();
            List<Runnable> var4 = Lists.newArrayListWithExpectedSize(var0.size());
            this.minecraft.getTextureManager().bind(AbstractContainerScreen.INVENTORY_LOCATION);

            for(MobEffectInstance var5 : Ordering.natural().reverse().sortedCopy(var0)) {
                MobEffect var6 = var5.getEffect();
                if (var5.showIcon()) {
                    int var7 = this.screenWidth;
                    int var8 = 1;
                    if (this.minecraft.isDemo()) {
                        var8 += 15;
                    }

                    if (var6.isBeneficial()) {
                        ++var1;
                        var7 -= 25 * var1;
                    } else {
                        ++var2;
                        var7 -= 25 * var2;
                        var8 += 26;
                    }

                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    float var9 = 1.0F;
                    if (var5.isAmbient()) {
                        this.blit(param0, var7, var8, 165, 166, 24, 24);
                    } else {
                        this.blit(param0, var7, var8, 141, 166, 24, 24);
                        if (var5.getDuration() <= 200) {
                            int var10 = 10 - var5.getDuration() / 20;
                            var9 = Mth.clamp((float)var5.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                                + Mth.cos((float)var5.getDuration() * (float) Math.PI / 5.0F) * Mth.clamp((float)var10 / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    TextureAtlasSprite var11 = var3.get(var6);
                    int var12 = var7;
                    int var13 = var8;
                    float var14 = var9;
                    var4.add(() -> {
                        this.minecraft.getTextureManager().bind(var11.atlas().location());
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, var14);
                        blit(param0, var12 + 3, var13 + 3, this.getBlitOffset(), 18, 18, var11);
                    });
                }
            }

            var4.forEach(Runnable::run);
        }
    }

    protected void renderHotbar(float param0, PoseStack param1) {
        Player var0 = this.getCameraPlayer();
        if (var0 != null) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
            ItemStack var1 = var0.getOffhandItem();
            HumanoidArm var2 = var0.getMainArm().getOpposite();
            int var3 = this.screenWidth / 2;
            int var4 = this.getBlitOffset();
            int var5 = 182;
            int var6 = 91;
            this.setBlitOffset(-90);
            this.blit(param1, var3 - 91, this.screenHeight - 22, 0, 0, 182, 22);
            this.blit(param1, var3 - 91 - 1 + var0.inventory.selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
            if (!var1.isEmpty()) {
                if (var2 == HumanoidArm.LEFT) {
                    this.blit(param1, var3 - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
                } else {
                    this.blit(param1, var3 + 91, this.screenHeight - 23, 53, 22, 29, 24);
                }
            }

            this.setBlitOffset(var4);
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            for(int var7 = 0; var7 < 9; ++var7) {
                int var8 = var3 - 90 + var7 * 20 + 2;
                int var9 = this.screenHeight - 16 - 3;
                this.renderSlot(var8, var9, param0, var0, var0.inventory.items.get(var7));
            }

            if (!var1.isEmpty()) {
                int var10 = this.screenHeight - 16 - 3;
                if (var2 == HumanoidArm.LEFT) {
                    this.renderSlot(var3 - 91 - 26, var10, param0, var0, var1);
                } else {
                    this.renderSlot(var3 + 91 + 10, var10, param0, var0, var1);
                }
            }

            if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
                float var11 = this.minecraft.player.getAttackStrengthScale(0.0F);
                if (var11 < 1.0F) {
                    int var12 = this.screenHeight - 20;
                    int var13 = var3 + 91 + 6;
                    if (var2 == HumanoidArm.RIGHT) {
                        var13 = var3 - 91 - 22;
                    }

                    this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
                    int var14 = (int)(var11 * 19.0F);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.blit(param1, var13, var12, 0, 94, 18, 18);
                    this.blit(param1, var13, var12 + 18 - var14, 18, 112 - var14, 18, var14);
                }
            }

            RenderSystem.disableRescaleNormal();
            RenderSystem.disableBlend();
        }
    }

    public void renderJumpMeter(PoseStack param0, int param1) {
        this.minecraft.getProfiler().push("jumpBar");
        this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
        float var0 = this.minecraft.player.getJumpRidingScale();
        int var1 = 182;
        int var2 = (int)(var0 * 183.0F);
        int var3 = this.screenHeight - 32 + 3;
        this.blit(param0, param1, var3, 0, 84, 182, 5);
        if (var2 > 0) {
            this.blit(param0, param1, var3, 0, 89, var2, 5);
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderExperienceBar(PoseStack param0, int param1) {
        this.minecraft.getProfiler().push("expBar");
        this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
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
            String var4 = "" + this.minecraft.player.experienceLevel;
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
            MutableComponent var0 = new TextComponent("").append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
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
                RenderSystem.pushMatrix();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                fill(param0, var2 - 2, var3 - 2, var2 + var1 + 2, var3 + 9 + 2, this.minecraft.options.getBackgroundColor(0));
                this.getFont().drawShadow(param0, var0, (float)var2, (float)var3, 16777215 + (var4 << 24));
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
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
            var0 = new TranslatableComponent("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
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
            Component var10 = PlayerTeam.formatNameForTeam(var9, new TextComponent(var8.getOwner()));
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
            String var21 = ChatFormatting.RED + "" + var19.getScore();
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
            float var10 = (float)var0.getAttributeValue(Attributes.MAX_HEALTH);
            int var11 = Mth.ceil(var0.getAbsorptionAmount());
            int var12 = Mth.ceil((var10 + (float)var11) / 2.0F / 10.0F);
            int var13 = Math.max(10 - (var12 - 2), 3);
            int var14 = var9 - (var12 - 1) * var13 - 10;
            int var15 = var9 - 10;
            int var16 = var11;
            int var17 = var0.getArmorValue();
            int var18 = -1;
            if (var0.hasEffect(MobEffects.REGENERATION)) {
                var18 = this.tickCount % Mth.ceil(var10 + 5.0F);
            }

            this.minecraft.getProfiler().push("armor");

            for(int var19 = 0; var19 < 10; ++var19) {
                if (var17 > 0) {
                    int var20 = var7 + var19 * 8;
                    if (var19 * 2 + 1 < var17) {
                        this.blit(param0, var20, var14, 34, 9, 9, 9);
                    }

                    if (var19 * 2 + 1 == var17) {
                        this.blit(param0, var20, var14, 25, 9, 9, 9);
                    }

                    if (var19 * 2 + 1 > var17) {
                        this.blit(param0, var20, var14, 16, 9, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().popPush("health");

            for(int var21 = Mth.ceil((var10 + (float)var11) / 2.0F) - 1; var21 >= 0; --var21) {
                int var22 = 16;
                if (var0.hasEffect(MobEffects.POISON)) {
                    var22 += 36;
                } else if (var0.hasEffect(MobEffects.WITHER)) {
                    var22 += 72;
                }

                int var23 = 0;
                if (var2) {
                    var23 = 1;
                }

                int var24 = Mth.ceil((float)(var21 + 1) / 10.0F) - 1;
                int var25 = var7 + var21 % 10 * 8;
                int var26 = var9 - var24 * var13;
                if (var1 <= 4) {
                    var26 += this.random.nextInt(2);
                }

                if (var16 <= 0 && var21 == var18) {
                    var26 -= 2;
                }

                int var27 = 0;
                if (var0.level.getLevelData().isHardcore()) {
                    var27 = 5;
                }

                this.blit(param0, var25, var26, 16 + var23 * 9, 9 * var27, 9, 9);
                if (var2) {
                    if (var21 * 2 + 1 < var4) {
                        this.blit(param0, var25, var26, var22 + 54, 9 * var27, 9, 9);
                    }

                    if (var21 * 2 + 1 == var4) {
                        this.blit(param0, var25, var26, var22 + 63, 9 * var27, 9, 9);
                    }
                }

                if (var16 > 0) {
                    if (var16 == var11 && var11 % 2 == 1) {
                        this.blit(param0, var25, var26, var22 + 153, 9 * var27, 9, 9);
                        --var16;
                    } else {
                        this.blit(param0, var25, var26, var22 + 144, 9 * var27, 9, 9);
                        var16 -= 2;
                    }
                } else {
                    if (var21 * 2 + 1 < var1) {
                        this.blit(param0, var25, var26, var22 + 36, 9 * var27, 9, 9);
                    }

                    if (var21 * 2 + 1 == var1) {
                        this.blit(param0, var25, var26, var22 + 45, 9 * var27, 9, 9);
                    }
                }
            }

            LivingEntity var28 = this.getPlayerVehicleWithHealth();
            int var29 = this.getVehicleMaxHearts(var28);
            if (var29 == 0) {
                this.minecraft.getProfiler().popPush("food");

                for(int var30 = 0; var30 < 10; ++var30) {
                    int var31 = var9;
                    int var32 = 16;
                    int var33 = 0;
                    if (var0.hasEffect(MobEffects.HUNGER)) {
                        var32 += 36;
                        var33 = 13;
                    }

                    if (var0.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (var6 * 3 + 1) == 0) {
                        var31 = var9 + (this.random.nextInt(3) - 1);
                    }

                    int var34 = var8 - var30 * 8 - 9;
                    this.blit(param0, var34, var31, 16 + var33 * 9, 27, 9, 9);
                    if (var30 * 2 + 1 < var6) {
                        this.blit(param0, var34, var31, var32 + 36, 27, 9, 9);
                    }

                    if (var30 * 2 + 1 == var6) {
                        this.blit(param0, var34, var31, var32 + 45, 27, 9, 9);
                    }
                }

                var15 -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            int var35 = var0.getMaxAirSupply();
            int var36 = Math.min(var0.getAirSupply(), var35);
            if (var0.isEyeInFluid(FluidTags.WATER) || var36 < var35) {
                int var37 = this.getVisibleVehicleHeartRows(var29) - 1;
                var15 -= var37 * 10;
                int var38 = Mth.ceil((double)(var36 - 2) * 10.0 / (double)var35);
                int var39 = Mth.ceil((double)var36 * 10.0 / (double)var35) - var38;

                for(int var40 = 0; var40 < var38 + var39; ++var40) {
                    if (var40 < var38) {
                        this.blit(param0, var8 - var40 * 8 - 9, var15, 16, 18, 9, 9);
                    } else {
                        this.blit(param0, var8 - var40 * 8 - 9, var15, 25, 18, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }
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

    private void renderPumpkin() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableAlphaTest();
        this.minecraft.getTextureManager().bind(PUMPKIN_BLUR_LOCATION);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(7, DefaultVertexFormat.POSITION_TEX);
        var1.vertex(0.0, (double)this.screenHeight, -90.0).uv(0.0F, 1.0F).endVertex();
        var1.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(1.0F, 1.0F).endVertex();
        var1.vertex((double)this.screenWidth, 0.0, -90.0).uv(1.0F, 0.0F).endVertex();
        var1.vertex(0.0, 0.0, -90.0).uv(0.0F, 0.0F).endVertex();
        var0.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void updateVignetteBrightness(Entity param0) {
        if (param0 != null) {
            float var0 = Mth.clamp(1.0F - param0.getBrightness(), 0.0F, 1.0F);
            this.vignetteBrightness = (float)((double)this.vignetteBrightness + (double)(var0 - this.vignetteBrightness) * 0.01);
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
            RenderSystem.color4f(0.0F, var1, var1, 1.0F);
        } else {
            RenderSystem.color4f(this.vignetteBrightness, this.vignetteBrightness, this.vignetteBrightness, 1.0F);
        }

        this.minecraft.getTextureManager().bind(VIGNETTE_LOCATION);
        Tesselator var4 = Tesselator.getInstance();
        BufferBuilder var5 = var4.getBuilder();
        var5.begin(7, DefaultVertexFormat.POSITION_TEX);
        var5.vertex(0.0, (double)this.screenHeight, -90.0).uv(0.0F, 1.0F).endVertex();
        var5.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(1.0F, 1.0F).endVertex();
        var5.vertex((double)this.screenWidth, 0.0, -90.0).uv(1.0F, 0.0F).endVertex();
        var5.vertex(0.0, 0.0, -90.0).uv(0.0F, 0.0F).endVertex();
        var4.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

    private void renderPortalOverlay(float param0) {
        if (param0 < 1.0F) {
            param0 *= param0;
            param0 *= param0;
            param0 = param0 * 0.8F + 0.2F;
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, param0);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite var0 = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        float var1 = var0.getU0();
        float var2 = var0.getV0();
        float var3 = var0.getU1();
        float var4 = var0.getV1();
        Tesselator var5 = Tesselator.getInstance();
        BufferBuilder var6 = var5.getBuilder();
        var6.begin(7, DefaultVertexFormat.POSITION_TEX);
        var6.vertex(0.0, (double)this.screenHeight, -90.0).uv(var1, var4).endVertex();
        var6.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(var3, var4).endVertex();
        var6.vertex((double)this.screenWidth, 0.0, -90.0).uv(var3, var2).endVertex();
        var6.vertex(0.0, 0.0, -90.0).uv(var1, var2).endVertex();
        var5.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSlot(int param0, int param1, float param2, Player param3, ItemStack param4) {
        if (!param4.isEmpty()) {
            float var0 = (float)param4.getPopTime() - param2;
            if (var0 > 0.0F) {
                RenderSystem.pushMatrix();
                float var1 = 1.0F + var0 / 5.0F;
                RenderSystem.translatef((float)(param0 + 8), (float)(param1 + 12), 0.0F);
                RenderSystem.scalef(1.0F / var1, (var1 + 1.0F) / 2.0F, 1.0F);
                RenderSystem.translatef((float)(-(param0 + 8)), (float)(-(param1 + 12)), 0.0F);
            }

            this.itemRenderer.renderAndDecorateItem(param3, param4, param0, param1);
            if (var0 > 0.0F) {
                RenderSystem.popMatrix();
            }

            this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, param4, param0, param1);
        }
    }

    public void tick() {
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
            ItemStack var1 = this.minecraft.player.inventory.getSelected();
            if (var1.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (this.lastToolHighlight.isEmpty()
                || var1.getItem() != this.lastToolHighlight.getItem()
                || !var1.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
                this.toolHighlightTimer = 40;
            } else if (this.toolHighlightTimer > 0) {
                --this.toolHighlightTimer;
            }

            this.lastToolHighlight = var1;
        }

    }

    public void setNowPlaying(Component param0) {
        this.setOverlayMessage(new TranslatableComponent("record.nowPlaying", param0), true);
    }

    public void setOverlayMessage(Component param0, boolean param1) {
        this.overlayMessageString = param0;
        this.overlayMessageTime = 60;
        this.animateOverlayMessageColor = param1;
    }

    public void setTitles(@Nullable Component param0, @Nullable Component param1, int param2, int param3, int param4) {
        if (param0 == null && param1 == null && param2 < 0 && param3 < 0 && param4 < 0) {
            this.title = null;
            this.subtitle = null;
            this.titleTime = 0;
        } else if (param0 != null) {
            this.title = param0;
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
        } else if (param1 != null) {
            this.subtitle = param1;
        } else {
            if (param2 >= 0) {
                this.titleFadeInTime = param2;
            }

            if (param3 >= 0) {
                this.titleStayTime = param3;
            }

            if (param4 >= 0) {
                this.titleFadeOutTime = param4;
            }

            if (this.titleTime > 0) {
                this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
            }

        }
    }

    public void handleChat(ChatType param0, Component param1, UUID param2) {
        for(ChatListener var0 : this.chatListeners.get(param0)) {
            var0.handle(param0, param1, param2);
        }

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
    }

    public BossHealthOverlay getBossOverlay() {
        return this.bossOverlay;
    }

    public void clearCache() {
        this.debugScreen.clearChunkCache();
    }
}
