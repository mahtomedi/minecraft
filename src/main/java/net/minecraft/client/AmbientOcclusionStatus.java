package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum AmbientOcclusionStatus implements OptionEnum {
    OFF(0, "options.ao.off"),
    MIN(1, "options.ao.min"),
    MAX(2, "options.ao.max");

    private static final IntFunction<AmbientOcclusionStatus> BY_ID = ByIdMap.continuous(
        AmbientOcclusionStatus::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP
    );
    private final int id;
    private final String key;

    private AmbientOcclusionStatus(int param0, String param1) {
        this.id = param0;
        this.key = param1;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public static AmbientOcclusionStatus byId(int param0) {
        return BY_ID.apply(param0);
    }
}
