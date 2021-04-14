package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class ConstantHeight extends HeightProvider {
    public static final ConstantHeight ZERO = new ConstantHeight(VerticalAnchor.absolute(0));
    public static final Codec<ConstantHeight> CODEC = Codec.either(
            VerticalAnchor.CODEC,
            RecordCodecBuilder.create(
                param0 -> param0.group(VerticalAnchor.CODEC.fieldOf("value").forGetter(param0x -> param0x.value)).apply(param0, ConstantHeight::new)
            )
        )
        .xmap(param0 -> param0.map(ConstantHeight::of, param0x -> param0x), param0 -> Either.left(param0.value));
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
    public int sample(Random param0, WorldGenerationContext param1) {
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
