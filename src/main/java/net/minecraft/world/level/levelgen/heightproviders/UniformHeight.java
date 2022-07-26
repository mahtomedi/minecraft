package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.slf4j.Logger;

public class UniformHeight extends HeightProvider {
    public static final Codec<UniformHeight> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                    VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(param0x -> param0x.maxInclusive)
                )
                .apply(param0, UniformHeight::new)
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final LongSet warnedFor = new LongOpenHashSet();

    private UniformHeight(VerticalAnchor param0, VerticalAnchor param1) {
        this.minInclusive = param0;
        this.maxInclusive = param1;
    }

    public static UniformHeight of(VerticalAnchor param0, VerticalAnchor param1) {
        return new UniformHeight(param0, param1);
    }

    @Override
    public int sample(RandomSource param0, WorldGenerationContext param1) {
        int var0 = this.minInclusive.resolveY(param1);
        int var1 = this.maxInclusive.resolveY(param1);
        if (var0 > var1) {
            if (this.warnedFor.add((long)var0 << 32 | (long)var1)) {
                LOGGER.warn("Empty height range: {}", this);
            }

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
    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}
