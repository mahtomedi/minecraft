package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.FullscreenResolutionProgressOption;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
    private static final Component FABULOUS = new TranslatableComponent("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC);
    private static final Component WARNING_MESSAGE = new TranslatableComponent("options.graphics.warning.message", FABULOUS, FABULOUS);
    private static final Component WARNING_TITLE = new TranslatableComponent("options.graphics.warning.title").withStyle(ChatFormatting.RED);
    private static final Component BUTTON_ACCEPT = new TranslatableComponent("options.graphics.warning.accept");
    private static final Component BUTTON_CANCEL = new TranslatableComponent("options.graphics.warning.cancel");
    private static final Component NEW_LINE = new TextComponent("\n");
    private static final Option[] OPTIONS = new Option[]{
        Option.GRAPHICS,
        Option.RENDER_DISTANCE,
        Option.PRIORITIZE_CHUNK_UPDATES,
        Option.SIMULATION_DISTANCE,
        Option.AMBIENT_OCCLUSION,
        Option.FRAMERATE_LIMIT,
        Option.ENABLE_VSYNC,
        Option.VIEW_BOBBING,
        Option.GUI_SCALE,
        Option.ATTACK_INDICATOR,
        Option.GAMMA,
        Option.RENDER_CLOUDS,
        Option.USE_FULLSCREEN,
        Option.PARTICLES,
        Option.MIPMAP_LEVELS,
        Option.ENTITY_SHADOWS,
        Option.SCREEN_EFFECTS_SCALE,
        Option.ENTITY_DISTANCE_SCALING,
        Option.FOV_EFFECTS_SCALE
    };
    private OptionsList list;
    private final GpuWarnlistManager gpuWarnlistManager;
    private final int oldMipmaps;

    public VideoSettingsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.videoTitle"));
        this.gpuWarnlistManager = param0.minecraft.getGpuWarnlistManager();
        this.gpuWarnlistManager.resetWarnings();
        if (param1.graphicsMode == GraphicsStatus.FABULOUS) {
            this.gpuWarnlistManager.dismissWarning();
        }

        this.oldMipmaps = param1.mipmapLevels;
    }

    @Override
    protected void init() {
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addBig(new FullscreenResolutionProgressOption(this.minecraft.getWindow()));
        this.list.addBig(Option.BIOME_BLEND_RADIUS);
        this.list.addSmall(OPTIONS);
        this.addWidget(this.list);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, param0 -> {
            this.minecraft.options.save();
            this.minecraft.getWindow().changeFullscreenVideoMode();
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    @Override
    public void removed() {
        if (this.options.mipmapLevels != this.oldMipmaps) {
            this.minecraft.updateMaxMipLevel(this.options.mipmapLevels);
            this.minecraft.delayTextureReload();
        }

        super.removed();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        int var0 = this.options.guiScale;
        if (super.mouseClicked(param0, param1, param2)) {
            if (this.options.guiScale != var0) {
                this.minecraft.resizeDisplay();
            }

            if (this.gpuWarnlistManager.isShowingWarning()) {
                List<Component> var1 = Lists.newArrayList(WARNING_MESSAGE, NEW_LINE);
                String var2 = this.gpuWarnlistManager.getRendererWarnings();
                if (var2 != null) {
                    var1.add(NEW_LINE);
                    var1.add(new TranslatableComponent("options.graphics.warning.renderer", var2).withStyle(ChatFormatting.GRAY));
                }

                String var3 = this.gpuWarnlistManager.getVendorWarnings();
                if (var3 != null) {
                    var1.add(NEW_LINE);
                    var1.add(new TranslatableComponent("options.graphics.warning.vendor", var3).withStyle(ChatFormatting.GRAY));
                }

                String var4 = this.gpuWarnlistManager.getVersionWarnings();
                if (var4 != null) {
                    var1.add(NEW_LINE);
                    var1.add(new TranslatableComponent("options.graphics.warning.version", var4).withStyle(ChatFormatting.GRAY));
                }

                this.minecraft.setScreen(new PopupScreen(WARNING_TITLE, var1, ImmutableList.of(new PopupScreen.ButtonOption(BUTTON_ACCEPT, param0x -> {
                    this.options.graphicsMode = GraphicsStatus.FABULOUS;
                    Minecraft.getInstance().levelRenderer.allChanged();
                    this.gpuWarnlistManager.dismissWarning();
                    this.minecraft.setScreen(this);
                }), new PopupScreen.ButtonOption(BUTTON_CANCEL, param0x -> {
                    this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
                    this.minecraft.setScreen(this);
                }))));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        int var0 = this.options.guiScale;
        if (super.mouseReleased(param0, param1, param2)) {
            return true;
        } else if (this.list.mouseReleased(param0, param1, param2)) {
            if (this.options.guiScale != var0) {
                this.minecraft.resizeDisplay();
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.list.render(param0, param1, param2, param3);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 5, 16777215);
        super.render(param0, param1, param2, param3);
        List<FormattedCharSequence> var0 = tooltipAt(this.list, param1, param2);
        if (var0 != null) {
            this.renderTooltip(param0, var0, param1, param2);
        }

    }
}
