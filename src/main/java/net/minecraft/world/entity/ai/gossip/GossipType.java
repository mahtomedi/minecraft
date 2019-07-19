package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public enum GossipType {
    MAJOR_NEGATIVE("major_negative", -5, 100, 10, 10),
    MINOR_NEGATIVE("minor_negative", -1, 200, 20, 20),
    MINOR_POSITIVE("minor_positive", 1, 200, 1, 5),
    MAJOR_POSITIVE("major_positive", 5, 100, 0, 100),
    TRADING("trading", 1, 25, 2, 20);

    public final String id;
    public final int weight;
    public final int max;
    public final int decayPerDay;
    public final int decayPerTransfer;
    private static final Map<String, GossipType> BY_ID = Stream.of(values()).collect(ImmutableMap.toImmutableMap(param0 -> param0.id, Function.identity()));

    private GossipType(String param0, int param1, int param2, int param3, int param4) {
        this.id = param0;
        this.weight = param1;
        this.max = param2;
        this.decayPerDay = param3;
        this.decayPerTransfer = param4;
    }

    @Nullable
    public static GossipType byId(String param0) {
        return BY_ID.get(param0);
    }
}
