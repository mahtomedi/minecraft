package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum PrioritizeChunkUpdates {
    NONE(0, "options.prioritizeChunkUpdates.none"),
    PLAYER_AFFECTED(1, "options.prioritizeChunkUpdates.byPlayer"),
    NEARBY(2, "options.prioritizeChunkUpdates.nearby");

    private static final PrioritizeChunkUpdates[] BY_ID = Arrays.stream(values())
        .sorted(Comparator.comparingInt(PrioritizeChunkUpdates::getId))
        .toArray(param0 -> new PrioritizeChunkUpdates[param0]);
    private final int id;
    private final String key;

    private PrioritizeChunkUpdates(int param0, String param1) {
        this.id = param0;
        this.key = param1;
    }

    public int getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }

    public static PrioritizeChunkUpdates byId(int param0) {
        return BY_ID[Mth.positiveModulo(param0, BY_ID.length)];
    }
}
