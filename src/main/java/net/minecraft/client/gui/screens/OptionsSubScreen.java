package net.minecraft.client.gui.screens;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsSubScreen extends Screen {
    protected final Screen lastScreen;
    protected final Options options;

    public OptionsSubScreen(Screen param0, Options param1, Component param2) {
        super(param2);
        this.lastScreen = param0;
        this.options = param1;
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Nullable
    public static List<FormattedCharSequence> tooltipAt(OptionsList param0, int param1, int param2) {
        Optional<AbstractWidget> var0 = param0.getMouseOver((double)param1, (double)param2);
        if (var0.isPresent() && var0.get() instanceof TooltipAccessor) {
            Optional<List<FormattedCharSequence>> var1 = ((TooltipAccessor)var0.get()).getTooltip();
            return var1.orElse(null);
        } else {
            return null;
        }
    }
}
