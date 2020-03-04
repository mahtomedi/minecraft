package net.minecraft.realms;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsScreen extends Screen {
    public RealmsScreen() {
        super(NarratorChatListener.NO_TITLE);
    }

    protected static int row(int param0) {
        return 40 + param0 * 13;
    }

    @Override
    public void tick() {
        for(AbstractWidget var0 : this.buttons) {
            if (var0 instanceof TickableWidget) {
                ((TickableWidget)var0).tick();
            }
        }

    }

    public void narrateLabels() {
        List<String> var0 = this.children
            .stream()
            .filter(RealmsLabel.class::isInstance)
            .map(RealmsLabel.class::cast)
            .map(RealmsLabel::getText)
            .collect(Collectors.toList());
        NarrationHelper.now(var0);
    }
}
