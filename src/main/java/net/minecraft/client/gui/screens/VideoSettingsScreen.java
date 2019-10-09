package net.minecraft.client.gui.screens;

import net.minecraft.client.FullscreenResolutionProgressOption;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
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
        Option.BIOME_BLEND_RADIUS
    };
    private int oldMipmaps;

    public VideoSettingsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.videoTitle"));
    }

    @Override
    protected void init() {
        this.oldMipmaps = this.options.mipmapLevels;
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addBig(new FullscreenResolutionProgressOption(this.minecraft.getWindow()));
        this.list.addSmall(OPTIONS);
        this.children.add(this.list);
        this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, I18n.get("gui.done"), param0 -> {
            this.minecraft.options.save();
            this.minecraft.getWindow().changeFullscreenVideoMode();
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    @Override
    public void removed() {
        if (this.options.mipmapLevels != this.oldMipmaps) {
            this.minecraft.getTextureAtlas().setMaxMipLevel(this.options.mipmapLevels);
            this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
            this.minecraft.getTextureAtlas().setFilter(false, this.options.mipmapLevels > 0);
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
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.list.render(param0, param1, param2);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 5, 16777215);
        super.render(param0, param1, param2);
    }
}
