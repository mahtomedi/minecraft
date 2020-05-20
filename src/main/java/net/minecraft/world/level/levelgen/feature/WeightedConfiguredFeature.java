package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class WeightedConfiguredFeature<FC extends FeatureConfiguration> {
    public static final Codec<WeightedConfiguredFeature<?>> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ConfiguredFeature.CODEC.fieldOf("feature").forGetter(param0x -> param0x.feature),
                    Codec.FLOAT.fieldOf("chance").forGetter(param0x -> param0x.chance)
                )
                .apply(param0, WeightedConfiguredFeature::new)
    );
    public final ConfiguredFeature<FC, ?> feature;
    public final float chance;

    public WeightedConfiguredFeature(ConfiguredFeature<FC, ?> param0, float param1) {
        this.feature = param0;
        this.chance = param1;
    }

    public boolean place(WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4) {
        return this.feature.place(param0, param1, param2, param3, param4);
    }
}
