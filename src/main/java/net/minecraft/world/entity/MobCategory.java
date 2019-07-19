package net.minecraft.world.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MobCategory {
    MONSTER("monster", 70, false, false),
    CREATURE("creature", 10, true, true),
    AMBIENT("ambient", 15, true, false),
    WATER_CREATURE("water_creature", 15, true, false),
    MISC("misc", 15, true, false);

    private static final Map<String, MobCategory> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(MobCategory::getName, param0 -> param0));
    private final int max;
    private final boolean isFriendly;
    private final boolean isPersistent;
    private final String name;

    private MobCategory(String param0, int param1, boolean param2, boolean param3) {
        this.name = param0;
        this.max = param1;
        this.isFriendly = param2;
        this.isPersistent = param3;
    }

    public String getName() {
        return this.name;
    }

    public int getMaxInstancesPerChunk() {
        return this.max;
    }

    public boolean isFriendly() {
        return this.isFriendly;
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }
}
