package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UniformHeight extends HeightProvider {
    public static final Codec<UniformHeight> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                        VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(param0x -> param0x.maxInclusive)
                    )
                    .apply(param0, UniformHeight::new)
        )
        .comapFlatMap(DataResult::success, Function.identity());
    private static final Logger LOGGER = LogManager.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;

    private UniformHeight(VerticalAnchor param0, VerticalAnchor param1) {
        this.minInclusive = param0;
        this.maxInclusive = param1;
    }

    public static UniformHeight of(VerticalAnchor param0, VerticalAnchor param1) {
        return new UniformHeight(param0, param1);
    }

    @Override
    public int sample(Random param0, WorldGenerationContext param1) {
        int var0 = this.minInclusive.resolveY(param1);
        int var1 = this.maxInclusive.resolveY(param1);
        if (var0 > var1) {
            LOGGER.warn("Empty height range: {}", this);
            return var0;
        } else {
            return Mth.randomBetweenInclusive(param0, var0, var1);
        }
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.UNIFORM;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            UniformHeight var0 = (UniformHeight)param0;
            return this.minInclusive.equals(var0.minInclusive) && this.maxInclusive.equals(var0.maxInclusive);
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
        return "[" + this.minInclusive + '-' + this.maxInclusive + ']';
    }
}
