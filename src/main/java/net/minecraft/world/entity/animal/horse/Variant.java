package net.minecraft.world.entity.animal.horse;

import java.util.Arrays;
import java.util.Comparator;

public enum Variant {
    WHITE(0),
    CREAMY(1),
    CHESTNUT(2),
    BROWN(3),
    BLACK(4),
    GRAY(5),
    DARKBROWN(6);

    private static final Variant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Variant::getId)).toArray(param0 -> new Variant[param0]);
    private final int id;

    private Variant(int param0) {
        this.id = param0;
    }

    public int getId() {
        return this.id;
    }

    public static Variant byId(int param0) {
        return BY_ID[param0 % BY_ID.length];
    }
}
