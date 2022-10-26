package net.minecraft.client.gui.components;

import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpacerWidget extends AbstractWidget {
    public SpacerWidget(int param0, int param1) {
        this(0, 0, param0, param1);
    }

    public SpacerWidget(int param0, int param1, int param2, int param3) {
        super(param0, param1, param2, param3, Component.empty());
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
    }

    public static AbstractWidget width(int param0) {
        return new SpacerWidget(param0, 0);
    }

    public static AbstractWidget height(int param0) {
        return new SpacerWidget(0, param0);
    }
}
