package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public abstract class HeightProvider {
    private static final Codec<Either<VerticalAnchor, HeightProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
        VerticalAnchor.CODEC, Registry.HEIGHT_PROVIDER_TYPES.dispatch(HeightProvider::getType, HeightProviderType::codec)
    );
    public static final Codec<HeightProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
        param0 -> param0.map(ConstantHeight::of, param0x -> param0x),
        param0 -> param0.getType() == HeightProviderType.CONSTANT ? Either.left(((ConstantHeight)param0).getValue()) : Either.right(param0)
    );

    public abstract int sample(Random var1, WorldGenerationContext var2);

    public abstract HeightProviderType<?> getType();
}