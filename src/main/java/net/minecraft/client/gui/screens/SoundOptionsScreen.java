package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.components.VolumeSlider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundOptionsScreen extends OptionsSubScreen {
    @Nullable
    private AbstractWidget directionalAudioButton;

    public SoundOptionsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.sounds.title"));
    }

    @Override
    protected void init() {
        int var0 = this.height / 6 - 12;
        int var1 = 22;
        int var2 = 0;
        this.addRenderableWidget(new VolumeSlider(this.minecraft, this.width / 2 - 155 + var2 % 2 * 160, var0 + 22 * (var2 >> 1), SoundSource.MASTER, 310));
        var2 += 2;

        for(SoundSource var3 : SoundSource.values()) {
            if (var3 != SoundSource.MASTER) {
                this.addRenderableWidget(new VolumeSlider(this.minecraft, this.width / 2 - 155 + var2 % 2 * 160, var0 + 22 * (var2 >> 1), var3, 150));
                ++var2;
            }
        }

        if (var2 % 2 == 1) {
            ++var2;
        }

        this.addRenderableWidget(this.options.soundDevice().createButton(this.options, this.width / 2 - 155, var0 + 22 * (var2 >> 1), 310));
        var2 += 2;
        this.addRenderableWidget(this.options.showSubtitles().createButton(this.options, this.width / 2 - 155, var0 + 22 * (var2 >> 1), 150));
        this.directionalAudioButton = this.options.directionalAudio().createButton(this.options, this.width / 2 + 5, var0 + 22 * (var2 >> 1), 150);
        this.addRenderableWidget(this.directionalAudioButton);
        var2 += 2;
        this.addRenderableWidget(
            new Button(this.width / 2 - 100, var0 + 22 * (var2 >> 1), 200, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(param0, param1, param2, param3);
        if (this.directionalAudioButton != null && this.directionalAudioButton.isMouseOver((double)param1, (double)param2)) {
            List<FormattedCharSequence> var0 = ((TooltipAccessor)this.directionalAudioButton).getTooltip();
            this.renderTooltip(param0, var0, param1, param2);
        }

    }
}
