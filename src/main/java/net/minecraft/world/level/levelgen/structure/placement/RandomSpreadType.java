package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.RandomSource;

public enum RandomSpreadType implements StringRepresentable {
    LINEAR("linear"),
    TRIANGULAR("triangular");

    private static final RandomSpreadType[] VALUES = values();
    public static final Codec<RandomSpreadType> CODEC = StringRepresentable.fromEnum(() -> VALUES, RandomSpreadType::byName);
    private final String id;

    private RandomSpreadType(String param0) {
        this.id = param0;
    }

    public static RandomSpreadType byName(String param0) {
        for(RandomSpreadType var0 : VALUES) {
            if (var0.getSerializedName().equals(param0)) {
                return var0;
            }
        }

        throw new IllegalArgumentException("Unknown Random Spread type: " + param0);
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
