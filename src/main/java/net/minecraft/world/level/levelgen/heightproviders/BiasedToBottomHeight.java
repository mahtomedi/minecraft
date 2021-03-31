package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiasedToBottomHeight extends HeightProvider {
    public static final Codec<BiasedToBottomHeight> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                        VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(param0x -> param0x.maxInclusive),
                        Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("inner", 1).forGetter(param0x -> param0x.inner)
                    )
                    .apply(param0, BiasedToBottomHeight::new)
        )
        .comapFlatMap(DataResult::success, Function.identity());
    private static final Logger LOGGER = LogManager.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final int inner;

    private BiasedToBottomHeight(VerticalAnchor param0, VerticalAnchor param1, int param2) {
        this.minInclusive = param0;
        this.maxInclusive = param1;
        this.inner = param2;
    }

    public static BiasedToBottomHeight of(VerticalAnchor param0, VerticalAnchor param1, int param2) {
        return new BiasedToBottomHeight(param0, param1, param2);
    }

    @Override
    public int sample(Random param0, WorldGenerationContext param1) {
        int var0 = this.minInclusive.resolveY(param1);
        int var1 = this.maxInclusive.resolveY(param1);
        if (var1 - var0 - this.inner + 1 <= 0) {
            LOGGER.warn("Empty height range: {}", this);
            return var0;
        } else {
            int var2 = param0.nextInt(var1 - var0 - this.inner + 1);
            return param0.nextInt(var2 + this.inner) + var0;
        }
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.BIASED_TO_BOTTOM;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            BiasedToBottomHeight var0 = (BiasedToBottomHeight)param0;
            return this.minInclusive.equals(var0.minInclusive) && this.maxInclusive.equals(this.maxInclusive) && this.inner == var0.inner;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.minInclusive, this.maxInclusive);
    }

    @Override
    public String toString() {
        return "biased[" + this.minInclusive + '-' + this.maxInclusive + " inner: " + this.inner + "]";
    }
}
