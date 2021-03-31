package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;

public enum MobCategory implements StringRepresentable {
    MONSTER("monster", 70, false, false, 128),
    CREATURE("creature", 10, true, true, 128),
    AMBIENT("ambient", 15, true, false, 128),
    UNDERGROUND_WATER_CREATURE("underground_water_creature", 5, true, false, 128),
    WATER_CREATURE("water_creature", 5, true, false, 128),
    WATER_AMBIENT("water_ambient", 20, true, false, 64),
    MISC("misc", -1, true, true, 128);

    public static final Codec<MobCategory> CODEC = StringRepresentable.fromEnum(MobCategory::values, MobCategory::byName);
    private static final Map<String, MobCategory> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(MobCategory::getName, param0 -> param0));
    private final int max;
    private final boolean isFriendly;
    private final boolean isPersistent;
    private final String name;
    private final int noDespawnDistance = 32;
    private final int despawnDistance;

    private MobCategory(String param0, int param1, boolean param2, boolean param3, int param4) {
        this.name = param0;
        this.max = param1;
        this.isFriendly = param2;
        this.isPersistent = param3;
        this.despawnDistance = param4;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static MobCategory byName(String param0) {
        return BY_NAME.get(param0);
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

    public int getDespawnDistance() {
        return this.despawnDistance;
    }

    public int getNoDespawnDistance() {
        return 32;
    }
}
