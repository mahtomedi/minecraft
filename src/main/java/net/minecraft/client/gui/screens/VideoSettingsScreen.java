package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
    private static final Component FABULOUS = Component.translatable("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC);
    private static final Component WARNING_MESSAGE = Component.translatable("options.graphics.warning.message", FABULOUS, FABULOUS);
    private static final Component WARNING_TITLE = Component.translatable("options.graphics.warning.title").withStyle(ChatFormatting.RED);
    private static final Component BUTTON_ACCEPT = Component.translatable("options.graphics.warning.accept");
    private static final Component BUTTON_CANCEL = Component.translatable("options.graphics.warning.cancel");
    private OptionsList list;
    private final GpuWarnlistManager gpuWarnlistManager;
    private final int oldMipmaps;

    private static OptionInstance<?>[] options(Options param0) {
        return new OptionInstance[]{
            param0.graphicsMode(),
            param0.renderDistance(),
            param0.prioritizeChunkUpdates(),
            param0.simulationDistance(),
            param0.ambientOcclusion(),
            param0.framerateLimit(),
            param0.enableVsync(),
            param0.bobView(),
            param0.guiScale(),
            param0.attackIndicator(),
            param0.gamma(),
            param0.cloudStatus(),
            param0.fullscreen(),
            param0.particles(),
            param0.mipmapLevels(),
            param0.entityShadows(),
            param0.screenEffectScale(),
            param0.entityDistanceScaling(),
            param0.fovEffectScale(),
            param0.showAutosaveIndicator(),
            param0.glintSpeed(),
            param0.glintStrength()
        };
    }

    public VideoSettingsScreen(Screen param0, Options param1) {
        super(param0, param1, Component.translatable("options.videoTitle"));
        this.gpuWarnlistManager = param0.minecraft.getGpuWarnlistManager();
        this.gpuWarnlistManager.resetWarnings();
        if (param1.graphicsMode().get() == GraphicsStatus.FABULOUS) {
            this.gpuWarnlistManager.dismissWarning();
        }

        this.oldMipmaps = param1.mipmapLevels().get();
    }

    @Override
    protected void init() {
        this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height - 64, 32, 25));
        int var0 = -1;
        Window var1 = this.minecraft.getWindow();
        Monitor var2 = var1.findBestMonitor();
        int var3;
        if (var2 == null) {
            var3 = -1;
        } else {
            Optional<VideoMode> var4 = var1.getPreferredFullscreenVideoMode();
            var3 = var4.map(var2::getVideoModeIndex).orElse(-1);
        }

        OptionInstance<Integer> var6 = new OptionInstance<>(
            "options.fullscreen.resolution",
            OptionInstance.noTooltip(),
            (param1, param2) -> {
                if (var2 == null) {
                    return Component.translatable("options.fullscreen.unavailable");
                } else if (param2 == -1) {
                    return Options.genericValueLabel(param1, Component.translatable("options.fullscreen.current"));
                } else {
                    VideoMode var0x = var2.getMode(param2);
                    return Options.genericValueLabel(
                        param1,
                        Component.translatable(
                            "options.fullscreen.entry",
                            var0x.getWidth(),
                            var0x.getHeight(),
                            var0x.getRefreshRate(),
                            var0x.getRedBits() + var0x.getGreenBits() + var0x.getBlueBits()
                        )
                    );
                }
            },
            new OptionInstance.IntRange(-1, var2 != null ? var2.getModeCount() - 1 : -1),
            var3,
            param2 -> {
                if (var2 != null) {
                    var1.setPreferredFullscreenVideoMode(param2 == -1 ? Optional.empty() : Optional.of(var2.getMode(param2)));
                }
            }
        );
        this.list.addBig(var6);
        this.list.addBig(this.options.biomeBlendRadius());
        this.list.addSmall(options(this.options));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param1 -> {
            this.minecraft.options.save();
            var1.changeFullscreenVideoMode();
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void removed() {
        if (this.options.mipmapLevels().get() != this.oldMipmaps) {
            this.minecraft.updateMaxMipLevel(this.options.mipmapLevels().get());
            this.minecraft.delayTextureReload();
        }

        super.removed();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        int var0 = this.options.guiScale().get();
        if (super.mouseClicked(param0, param1, param2)) {
            if (this.options.guiScale().get() != var0) {
                this.minecraft.resizeDisplay();
            }

            if (this.gpuWarnlistManager.isShowingWarning()) {
                List<Component> var1 = Lists.newArrayList(WARNING_MESSAGE, CommonComponents.NEW_LINE);
                String var2 = this.gpuWarnlistManager.getRendererWarnings();
                if (var2 != null) {
                    var1.add(CommonComponents.NEW_LINE);
                    var1.add(Component.translatable("options.graphics.warning.renderer", var2).withStyle(ChatFormatting.GRAY));
                }

                String var3 = this.gpuWarnlistManager.getVendorWarnings();
                if (var3 != null) {
                    var1.add(CommonComponents.NEW_LINE);
                    var1.add(Component.translatable("options.graphics.warning.vendor", var3).withStyle(ChatFormatting.GRAY));
                }

                String var4 = this.gpuWarnlistManager.getVersionWarnings();
                if (var4 != null) {
                    var1.add(CommonComponents.NEW_LINE);
                    var1.add(Component.translatable("options.graphics.warning.version", var4).withStyle(ChatFormatting.GRAY));
                }

                this.minecraft
                    .setScreen(
                        new UnsupportedGraphicsWarningScreen(
                            WARNING_TITLE, var1, ImmutableList.of(new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_ACCEPT, param0x -> {
                                this.options.graphicsMode().set(GraphicsStatus.FABULOUS);
                                Minecraft.getInstance().levelRenderer.allChanged();
                                this.gpuWarnlistManager.dismissWarning();
                                this.minecraft.setScreen(this);
                            }), new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_CANCEL, param0x -> {
                                this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
                                this.minecraft.setScreen(this);
                            }))
                        )
                    );
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2, double param3) {
        if (Screen.hasControlDown()) {
            OptionInstance<Integer> var0 = this.options.guiScale();
            int var1 = var0.get() + (int)Math.signum(param3);
            if (var1 != 0) {
                var0.set(var1);
                if (var0.get() == var1) {
                    this.minecraft.resizeDisplay();
                    return true;
                }
            }

            return false;
        } else {
            return super.mouseScrolled(param0, param1, param2, param3);
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
    }
}
