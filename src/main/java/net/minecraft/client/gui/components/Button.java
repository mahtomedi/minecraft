package net.minecraft.client.gui.components;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Button extends AbstractButton {
    protected final Button.OnPress onPress;

    public Button(int param0, int param1, int param2, int param3, Component param4, Button.OnPress param5) {
        super(param0, param1, param2, param3, param4);
        this.onPress = param5;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(Button var1);
    }
}
