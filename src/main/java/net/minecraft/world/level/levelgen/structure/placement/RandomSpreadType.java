package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum RandomSpreadType implements StringRepresentable {
    LINEAR("linear"),
    TRIANGULAR("triangular");

    public static final Codec<RandomSpreadType> CODEC = StringRepresentable.fromEnum(RandomSpreadType::values);
    private final String id;

    private RandomSpreadType(String param0) {
        this.id = param0;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public int evaluate(RandomSource param0, int param1) {
        return switch(this) {
            case LINEAR -> param0.nextInt(param1);
            case TRIANGULAR -> (param0.nextInt(param1) + param0.nextInt(param1)) / 2;
        };
    }
}
