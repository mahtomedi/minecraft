package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VeryBiasedToBottomHeight extends HeightProvider {
    public static final Codec<VeryBiasedToBottomHeight> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                    VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(param0x -> param0x.maxInclusive),
                    Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("inner", 1).forGetter(param0x -> param0x.inner)
                )
                .apply(param0, VeryBiasedToBottomHeight::new)
    );
    private static final Logger LOGGER = LogManager.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final int inner;

    private VeryBiasedToBottomHeight(VerticalAnchor param0, VerticalAnchor param1, int param2) {
        this.minInclusive = param0;
        this.maxInclusive = param1;
        this.inner = param2;
    }

    public static VeryBiasedToBottomHeight of(VerticalAnchor param0, VerticalAnchor param1, int param2) {
        return new VeryBiasedToBottomHeight(param0, param1, param2);
    }

    @Override
    public int sample(Random param0, WorldGenerationContext param1) {
        int var0 = this.minInclusive.resolveY(param1);
        int var1 = this.maxInclusive.resolveY(param1);
        if (var1 - var0 - this.inner + 1 <= 0) {
            LOGGER.warn("Empty height range: {}", this);
            return var0;
        } else {
            int var2 = Mth.nextInt(param0, var0 + this.inner, var1);
            int var3 = Mth.nextInt(param0, var0, var2 - 1);
            return Mth.nextInt(param0, var0, var3 - 1 + this.inner);
        }
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.VERY_BIASED_TO_BOTTOM;
    }

    @Override
    public String toString() {
        return "biased[" + this.minInclusive + "-" + this.maxInclusive + " inner: " + this.inner + "]";
    }
}
