package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum AmbientOcclusionStatus {
    OFF(0, "options.ao.off"),
    MIN(1, "options.ao.min"),
    MAX(2, "options.ao.max");

    private static final AmbientOcclusionStatus[] BY_ID = Arrays.stream(values())
        .sorted(Comparator.comparingInt(AmbientOcclusionStatus::getId))
        .toArray(param0 -> new AmbientOcclusionStatus[param0]);
    private final int id;
    private final String key;

    private AmbientOcclusionStatus(int param0, String param1) {
        this.id = param0;
        this.key = param1;
    }

    public int getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }

    public static AmbientOcclusionStatus byId(int param0) {
        return BY_ID[Mth.positiveModulo(param0, BY_ID.length)];
    }
}
