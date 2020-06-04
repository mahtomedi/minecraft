package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.FullscreenResolutionProgressOption;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
    @Nullable
    private List<FormattedText> tooltip;
    private OptionsList list;
    private static final Option[] OPTIONS = new Option[]{
        Option.GRAPHICS,
        Option.RENDER_DISTANCE,
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
        Option.ENTITY_DISTANCE_SCALING
    };
    private int oldMipmaps;

    public VideoSettingsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.videoTitle"));
        this.oldMipmaps = param1.mipmapLevels;
    }

    @Override
    protected void init() {
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addBig(new FullscreenResolutionProgressOption(this.minecraft.getWindow()));
        this.list.addBig(Option.BIOME_BLEND_RADIUS);
        this.list.addSmall(OPTIONS);
        this.children.add(this.list);
        this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, param0 -> {
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
        this.tooltip = null;
        Optional<AbstractWidget> var0 = this.list.getMouseOver((double)param1, (double)param2);
        if (var0.isPresent() && var0.get() instanceof OptionButton) {
            Optional<TranslatableComponent> var1 = ((OptionButton)var0.get()).getOption().getTooltip();
            if (var1.isPresent()) {
                Builder<FormattedText> var2 = ImmutableList.builder();
                this.font.split(var1.get(), 200).forEach(var2::add);
                this.tooltip = var2.build();
            }
        }

        this.renderBackground(param0);
        this.list.render(param0, param1, param2, param3);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 5, 16777215);
        super.render(param0, param1, param2, param3);
        if (this.tooltip != null) {
            this.renderTooltip(param0, this.tooltip, param1, param2);
        }

    }
}
