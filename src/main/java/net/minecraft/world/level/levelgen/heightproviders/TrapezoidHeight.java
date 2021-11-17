package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrapezoidHeight extends HeightProvider {
    public static final Codec<TrapezoidHeight> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                    VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(param0x -> param0x.maxInclusive),
                    Codec.INT.optionalFieldOf("plateau", Integer.valueOf(0)).forGetter(param0x -> param0x.plateau)
                )
                .apply(param0, TrapezoidHeight::new)
    );
    private static final Logger LOGGER = LogManager.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final int plateau;

    private TrapezoidHeight(VerticalAnchor param0, VerticalAnchor param1, int param2) {
        this.minInclusive = param0;
        this.maxInclusive = param1;
        this.plateau = param2;
    }

    public static TrapezoidHeight of(VerticalAnchor param0, VerticalAnchor param1, int param2) {
        return new TrapezoidHeight(param0, param1, param2);
    }

    public static TrapezoidHeight of(VerticalAnchor param0, VerticalAnchor param1) {
        return of(param0, param1, 0);
    }

    @Override
    public int sample(Random param0, WorldGenerationContext param1) {
        int var0 = this.minInclusive.resolveY(param1);
        int var1 = this.maxInclusive.resolveY(param1);
        if (var0 > var1) {
            LOGGER.warn("Empty height range: {}", this);
            return var0;
        } else {
            int var2 = var1 - var0;
            if (this.plateau >= var2) {
                return Mth.randomBetweenInclusive(param0, var0, var1);
            } else {
                int var3 = (var2 - this.plateau) / 2;
                int var4 = var2 - var3;
                return var0 + Mth.randomBetweenInclusive(param0, 0, var4) + Mth.randomBetweenInclusive(param0, 0, var3);
            }
        }
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.TRAPEZOID;
    }

    @Override
    public String toString() {
        return this.plateau == 0
            ? "triangle (" + this.minInclusive + "-" + this.maxInclusive + ")"
            : "trapezoid(" + this.plateau + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}
