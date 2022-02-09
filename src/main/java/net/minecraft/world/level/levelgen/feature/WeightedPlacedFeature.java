package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WeightedPlacedFeature {
    public static final Codec<WeightedPlacedFeature> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    PlacedFeature.CODEC.fieldOf("feature").forGetter(param0x -> param0x.feature),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(param0x -> param0x.chance)
                )
                .apply(param0, WeightedPlacedFeature::new)
    );
    public final Holder<PlacedFeature> feature;
    public final float chance;

    public WeightedPlacedFeature(Holder<PlacedFeature> param0, float param1) {
        this.feature = param0;
        this.chance = param1;
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3) {
        return this.feature.value().place(param0, param1, param2, param3);
    }
}
