package net.minecraft.client.gui.screens;

import java.util.Arrays;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundOptionsScreen extends OptionsSubScreen {
    private OptionsList list;

    private static OptionInstance<?>[] buttonOptions(Options param0) {
        return new OptionInstance[]{param0.showSubtitles(), param0.directionalAudio()};
    }

    public SoundOptionsScreen(Screen param0, Options param1) {
        super(param0, param1, Component.translatable("options.sounds.title"));
    }

    @Override
    protected void init() {
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addBig(this.options.getSoundSourceOptionInstance(SoundSource.MASTER));
        this.list.addSmall(this.getAllSoundOptionsExceptMaster());
        this.list.addBig(this.options.soundDevice());
        this.list.addSmall(buttonOptions(this.options));
        this.addWidget(this.list);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> {
            this.minecraft.options.save();
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    private OptionInstance<?>[] getAllSoundOptionsExceptMaster() {
        return Arrays.stream(SoundSource.values())
            .filter(param0 -> param0 != SoundSource.MASTER)
            .map(param0 -> this.options.getSoundSourceOptionInstance(param0))
            .toArray(param0 -> new OptionInstance[param0]);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.basicListRender(param0, this.list, param1, param2, param3);
    }
}
