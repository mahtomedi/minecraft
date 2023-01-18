package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TitleScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEMO_LEVEL_ID = "Demo_World";
    public static final Component COPYRIGHT_TEXT = Component.literal("Copyright Mojang AB. Do not distribute!");
    public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    @Nullable
    private String splash;
    private Button resetDemoButton;
    @Nullable
    private RealmsNotificationsScreen realmsNotificationsScreen;
    private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
    private final boolean fading;
    private long fadeInStart;
    @Nullable
    private TitleScreen.WarningLabel warningLabel;
    private final LogoRenderer logoRenderer;

    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean param0) {
        this(param0, null);
    }

    public TitleScreen(boolean param0, @Nullable LogoRenderer param1) {
        super(Component.translatable("narrator.screen.title"));
        this.fading = param0;
        this.logoRenderer = (LogoRenderer)Objects.requireNonNullElseGet(param1, () -> new LogoRenderer(false));
    }

    private boolean realmsNotificationsEnabled() {
        return this.minecraft.options.realmsNotifications().get() && this.realmsNotificationsScreen != null;
    }

    @Override
    public void tick() {
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.tick();
        }

        this.minecraft.getRealms32BitWarningStatus().showRealms32BitWarningIfNeeded(this);
    }

    public static CompletableFuture<Void> preloadResources(TextureManager param0, Executor param1) {
        return CompletableFuture.allOf(
            param0.preload(LogoRenderer.MINECRAFT_LOGO, param1),
            param0.preload(LogoRenderer.MINECRAFT_EDITION, param1),
            param0.preload(PANORAMA_OVERLAY, param1),
            CUBE_MAP.preload(param0, param1)
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }

        int var0 = this.font.width(COPYRIGHT_TEXT);
        int var1 = this.width - var0 - 2;
        int var2 = 24;
        int var3 = this.height / 4 + 48;
        if (this.minecraft.isDemo()) {
            this.createDemoMenuOptions(var3, 24);
        } else {
            this.createNormalMenuOptions(var3, 24);
        }

        this.addRenderableWidget(
            new ImageButton(
                this.width / 2 - 124,
                var3 + 72 + 12,
                20,
                20,
                0,
                106,
                20,
                Button.WIDGETS_LOCATION,
                256,
                256,
                param0 -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())),
                Component.translatable("narrator.button.language")
            )
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("menu.options"), param0 -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)))
                .bounds(this.width / 2 - 100, var3 + 72 + 12, 98, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("menu.quit"), param0 -> this.minecraft.stop()).bounds(this.width / 2 + 2, var3 + 72 + 12, 98, 20).build()
        );
        this.addRenderableWidget(
            new ImageButton(
                this.width / 2 + 104,
                var3 + 72 + 12,
                20,
                20,
                0,
                0,
                20,
                ACCESSIBILITY_TEXTURE,
                32,
                64,
                param0 -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)),
                Component.translatable("narrator.button.accessibility")
            )
        );
        this.addRenderableWidget(
            new PlainTextButton(
                var1,
                this.height - 10,
                var0,
                10,
                COPYRIGHT_TEXT,
                param0 -> this.minecraft.setScreen(new WinScreen(false, this.logoRenderer, Runnables.doNothing())),
                this.font
            )
        );
        this.minecraft.setConnectedToRealms(false);
        if (this.minecraft.options.realmsNotifications().get() && this.realmsNotificationsScreen == null) {
            this.realmsNotificationsScreen = new RealmsNotificationsScreen();
        }

        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
        }

        if (!this.minecraft.is64Bit()) {
            this.warningLabel = new TitleScreen.WarningLabel(
                this.font, MultiLineLabel.create(this.font, Component.translatable("title.32bit.deprecation"), 350, 2), this.width / 2, var3 - 24
            );
        }

    }

    private void createNormalMenuOptions(int param0, int param1) {
        this.addRenderableWidget(
            Button.builder(Component.translatable("menu.singleplayer"), param0x -> this.minecraft.setScreen(new SelectWorldScreen(this)))
                .bounds(this.width / 2 - 100, param0, 200, 20)
                .build()
        );
        Component var0 = this.getMultiplayerDisabledReason();
        boolean var1 = var0 == null;
        Tooltip var2 = var0 != null ? Tooltip.create(var0) : null;
        this.addRenderableWidget(Button.builder(Component.translatable("menu.multiplayer"), param0x -> {
            Screen var0x = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
            this.minecraft.setScreen(var0x);
        }).bounds(this.width / 2 - 100, param0 + param1 * 1, 200, 20).tooltip(var2).build()).active = var1;
        this.addRenderableWidget(
                Button.builder(Component.translatable("menu.online"), param0x -> this.realmsButtonClicked())
                    .bounds(this.width / 2 - 100, param0 + param1 * 2, 200, 20)
                    .tooltip(var2)
                    .build()
            )
            .active = var1;
    }

    @Nullable
    private Component getMultiplayerDisabledReason() {
        if (this.minecraft.allowsMultiplayer()) {
            return null;
        } else {
            BanDetails var0 = this.minecraft.multiplayerBan();
            if (var0 != null) {
                return var0.expires() != null
                    ? Component.translatable("title.multiplayer.disabled.banned.temporary")
                    : Component.translatable("title.multiplayer.disabled.banned.permanent");
            } else {
                return Component.translatable("title.multiplayer.disabled");
            }
        }
    }

    private void createDemoMenuOptions(int param0, int param1) {
        boolean var0 = this.checkDemoWorldPresence();
        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.playdemo"),
                    param1x -> {
                        if (var0) {
                            this.minecraft.createWorldOpenFlows().loadLevel(this, "Demo_World");
                        } else {
                            this.minecraft
                                .createWorldOpenFlows()
                                .createFreshLevel(
                                    "Demo_World", MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions
                                );
                        }
            
                    }
                )
                .bounds(this.width / 2 - 100, param0, 200, 20)
                .build()
        );
        this.resetDemoButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.resetdemo"),
                    param0x -> {
                        LevelStorageSource var0x = this.minecraft.getLevelSource();
            
                        try (LevelStorageSource.LevelStorageAccess var3x = var0x.createAccess("Demo_World")) {
                            LevelSummary var2x = var3x.getSummary();
                            if (var2x != null) {
                                this.minecraft
                                    .setScreen(
                                        new ConfirmScreen(
                                            this::confirmDemo,
                                            Component.translatable("selectWorld.deleteQuestion"),
                                            Component.translatable("selectWorld.deleteWarning", var2x.getLevelName()),
                                            Component.translatable("selectWorld.deleteButton"),
                                            CommonComponents.GUI_CANCEL
                                        )
                                    );
                            }
                        } catch (IOException var8) {
                            SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
                            LOGGER.warn("Failed to access demo world", (Throwable)var8);
                        }
            
                    }
                )
                .bounds(this.width / 2 - 100, param0 + param1 * 1, 200, 20)
                .build()
        );
        this.resetDemoButton.active = var0;
    }

    private boolean checkDemoWorldPresence() {
        try {
            boolean var2;
            try (LevelStorageSource.LevelStorageAccess var0 = this.minecraft.getLevelSource().createAccess("Demo_World")) {
                var2 = var0.getSummary() != null;
            }

            return var2;
        } catch (IOException var6) {
            SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to read demo world data", (Throwable)var6);
            return false;
        }
    }

    private void realmsButtonClicked() {
        this.minecraft.setScreen(new RealmsMainScreen(this));
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float var0 = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
        this.panorama.render(param3, Mth.clamp(var0, 0.0F, 1.0F));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(var0, 0.0F, 1.0F)) : 1.0F);
        blit(param0, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = this.fading ? Mth.clamp(var0 - 1.0F, 0.0F, 1.0F) : 1.0F;
        this.logoRenderer.renderLogo(param0, this.width, var1);
        int var2 = Mth.ceil(var1 * 255.0F) << 24;
        if ((var2 & -67108864) != 0) {
            if (this.warningLabel != null) {
                this.warningLabel.render(param0, var2);
            }

            if (this.splash != null) {
                param0.pushPose();
                param0.translate((float)(this.width / 2 + 90), 70.0F, 0.0F);
                param0.mulPose(Axis.ZP.rotationDegrees(-20.0F));
                float var3 = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float) (Math.PI * 2)) * 0.1F);
                var3 = var3 * 100.0F / (float)(this.font.width(this.splash) + 32);
                param0.scale(var3, var3, var3);
                drawCenteredString(param0, this.font, this.splash, 0, -8, 16776960 | var2);
                param0.popPose();
            }

            String var4 = "Minecraft " + SharedConstants.getCurrentVersion().getName();
            if (this.minecraft.isDemo()) {
                var4 = var4 + " Demo";
            } else {
                var4 = var4 + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
            }

            if (Minecraft.checkModStatus().shouldReportAsModified()) {
                var4 = var4 + I18n.get("menu.modded");
            }

            drawString(param0, this.font, var4, 2, this.height - 10, 16777215 | var2);

            for(GuiEventListener var5 : this.children()) {
                if (var5 instanceof AbstractWidget) {
                    ((AbstractWidget)var5).setAlpha(var1);
                }
            }

            super.render(param0, param1, param2, param3);
            if (this.realmsNotificationsEnabled() && var1 >= 1.0F) {
                RenderSystem.enableDepthTest();
                this.realmsNotificationsScreen.render(param0, param1, param2, param3);
            }

        }
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (super.mouseClicked(param0, param1, param2)) {
            return true;
        } else {
            return this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public void removed() {
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.removed();
        }

    }

    private void confirmDemo(boolean param0) {
        if (param0) {
            try (LevelStorageSource.LevelStorageAccess var0 = this.minecraft.getLevelSource().createAccess("Demo_World")) {
                var0.deleteLevel();
            } catch (IOException var7) {
                SystemToast.onWorldDeleteFailure(this.minecraft, "Demo_World");
                LOGGER.warn("Failed to delete demo world", (Throwable)var7);
            }
        }

        this.minecraft.setScreen(this);
    }

    @OnlyIn(Dist.CLIENT)
    static record WarningLabel(Font font, MultiLineLabel label, int x, int y) {
        public void render(PoseStack param0, int param1) {
            this.label.renderBackgroundCentered(param0, this.x, this.y, 9, 2, 2097152 | Math.min(param1, 1426063360));
            this.label.renderCentered(param0, this.x, this.y, 9, 16777215 | param1);
        }
    }
}
