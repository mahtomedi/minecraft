package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
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

    protected void basicListRender(PoseStack param0, OptionsList param1, int param2, int param3, float param4) {
        this.renderBackground(param0);
        param1.render(param0, param2, param3, param4);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 20, 16777215);
        super.render(param0, param2, param3, param4);
        List<FormattedCharSequence> var0 = tooltipAt(param1, param2, param3);
        this.renderTooltip(param0, var0, param2, param3);
    }
}
