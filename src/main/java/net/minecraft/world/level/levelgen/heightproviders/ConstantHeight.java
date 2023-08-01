package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class ConstantHeight extends HeightProvider {
    public static final ConstantHeight ZERO = new ConstantHeight(VerticalAnchor.absolute(0));
    public static final Codec<ConstantHeight> CODEC = ExtraCodecs.withAlternative(VerticalAnchor.CODEC, VerticalAnchor.CODEC.fieldOf("value").codec())
        .xmap(ConstantHeight::new, ConstantHeight::getValue);
    private final VerticalAnchor value;

    public static ConstantHeight of(VerticalAnchor param0) {
        return new ConstantHeight(param0);
    }

    private ConstantHeight(VerticalAnchor param0) {
        this.value = param0;
    }

    public VerticalAnchor getValue() {
        return this.value;
    }

    @Override
    public int sample(RandomSource param0, WorldGenerationContext param1) {
        return this.value.resolveY(param1);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.CONSTANT;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
