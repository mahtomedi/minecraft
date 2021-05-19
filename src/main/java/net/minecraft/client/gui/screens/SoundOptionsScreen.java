package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.VolumeSlider;
import net.minecraft.network.chat.CommonComponents;
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
        this.addRenderableWidget(
            new VolumeSlider(this.minecraft, this.width / 2 - 155 + var0 % 2 * 160, this.height / 6 - 12 + 24 * (var0 >> 1), SoundSource.MASTER, 310)
        );
        var0 += 2;

        for(SoundSource var1 : SoundSource.values()) {
            if (var1 != SoundSource.MASTER) {
                this.addRenderableWidget(
                    new VolumeSlider(this.minecraft, this.width / 2 - 155 + var0 % 2 * 160, this.height / 6 - 12 + 24 * (var0 >> 1), var1, 150)
                );
                ++var0;
            }
        }

        this.addRenderableWidget(Option.SHOW_SUBTITLES.createButton(this.options, this.width / 2 - 75, this.height / 6 - 12 + 24 * (++var0 >> 1), 150));
        this.addRenderableWidget(
            new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
