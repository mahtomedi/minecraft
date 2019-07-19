package net.minecraft.client.gui.screens.advancements;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum AdvancementWidgetType {
    OBTAINED(0),
    UNOBTAINED(1);

    private final int y;

    private AdvancementWidgetType(int param0) {
        this.y = param0;
    }

    public int getIndex() {
        return this.y;
    }
}
