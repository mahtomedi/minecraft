package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
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

    public static List<FormattedCharSequence> tooltipAt(OptionsList param0, int param1, int param2) {
        Optional<AbstractWidget> var0 = param0.getMouseOver((double)param1, (double)param2);
        return (List<FormattedCharSequence>)(var0.isPresent() && var0.get() instanceof TooltipAccessor
            ? ((TooltipAccessor)var0.get()).getTooltip()
            : ImmutableList.of());
    }
}
