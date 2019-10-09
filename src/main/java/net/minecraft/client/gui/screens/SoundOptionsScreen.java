package net.minecraft.client.gui.screens;

import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.components.VolumeSlider;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundOptionsScreen extends OptionsSubScreen {
    public SoundOptionsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.sounds.title"));
    }

    @Override
    protected void init() {
        int var0 = 0;
        this.addButton(
            new VolumeSlider(this.minecraft, this.width / 2 - 155 + var0 % 2 * 160, this.height / 6 - 12 + 24 * (var0 >> 1), SoundSource.MASTER, 310)
        );
        var0 += 2;

        for(SoundSource var1 : SoundSource.values()) {
            if (var1 != SoundSource.MASTER) {
                this.addButton(new VolumeSlider(this.minecraft, this.width / 2 - 155 + var0 % 2 * 160, this.height / 6 - 12 + 24 * (var0 >> 1), var1, 150));
                ++var0;
            }
        }

        this.addButton(
            new OptionButton(
                this.width / 2 - 75,
                this.height / 6 - 12 + 24 * (++var0 >> 1),
                150,
                20,
                Option.SHOW_SUBTITLES,
                Option.SHOW_SUBTITLES.getMessage(this.options),
                param0 -> {
                    Option.SHOW_SUBTITLES.toggle(this.minecraft.options);
                    param0.setMessage(Option.SHOW_SUBTITLES.getMessage(this.minecraft.options));
                    this.minecraft.options.save();
                }
            )
        );
        this.addButton(
            new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, I18n.get("gui.done"), param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 15, 16777215);
        super.render(param0, param1, param2);
    }
}
