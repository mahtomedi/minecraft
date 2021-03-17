package net.minecraft.util.profiling.registry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum MeasurementCategory {
    EVENT_LOOP("eventLoops"),
    MAIL_BOX("mailBoxes");

    private final String name;

    private MeasurementCategory(String param0) {
        this.name = param0;
    }

    public String getName() {
        return this.name;
    }
}
