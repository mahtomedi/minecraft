package net.minecraft.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum InputType {
    NONE,
    MOUSE,
    KEYBOARD_OTHER,
    KEYBOARD_TAB;

    public boolean isMouse() {
        return this == MOUSE;
    }

    public boolean isKeyboard() {
        return this == KEYBOARD_OTHER || this == KEYBOARD_TAB;
    }
}
