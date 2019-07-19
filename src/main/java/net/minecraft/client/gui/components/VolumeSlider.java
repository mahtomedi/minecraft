package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VolumeSlider extends AbstractSliderButton {
    private final SoundSource source;

    public VolumeSlider(Minecraft param0, int param1, int param2, SoundSource param3, int param4) {
        super(param0.options, param1, param2, param4, 20, (double)param0.options.getSoundSourceVolume(param3));
        this.source = param3;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        String var0 = (float)this.value == (float)this.getYImage(false) ? I18n.get("options.off") : (int)((float)this.value * 100.0F) + "%";
        this.setMessage(I18n.get("soundCategory." + this.source.getName()) + ": " + var0);
    }

    @Override
    protected void applyValue() {
        this.options.setSoundCategoryVolume(this.source, (float)this.value);
        this.options.save();
    }
}
