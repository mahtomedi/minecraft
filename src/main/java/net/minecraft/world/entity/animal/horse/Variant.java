package net.minecraft.world.entity.animal.horse;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.StringRepresentable;

public enum Variant implements StringRepresentable {
    WHITE(0, "white"),
    CREAMY(1, "creamy"),
    CHESTNUT(2, "chestnut"),
    BROWN(3, "brown"),
    BLACK(4, "black"),
    GRAY(5, "gray"),
    DARK_BROWN(6, "dark_brown");

    public static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);
    private static final Variant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Variant::getId)).toArray(param0 -> new Variant[param0]);
    private final int id;
    private final String name;

    private Variant(int param0, String param1) {
        this.id = param0;
        this.name = param1;
    }

    public int getId() {
        return this.id;
    }

    public static Variant byId(int param0) {
        return BY_ID[param0 % BY_ID.length];
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
