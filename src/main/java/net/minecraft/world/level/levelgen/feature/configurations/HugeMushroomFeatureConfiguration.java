package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class HugeMushroomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<HugeMushroomFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter(param0x -> param0x.capProvider),
                    BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter(param0x -> param0x.stemProvider),
                    Codec.INT.fieldOf("foliage_radius").withDefault(2).forGetter(param0x -> param0x.foliageRadius)
                )
                .apply(param0, HugeMushroomFeatureConfiguration::new)
    );
    public final BlockStateProvider capProvider;
    public final BlockStateProvider stemProvider;
    public final int foliageRadius;

    public HugeMushroomFeatureConfiguration(BlockStateProvider param0, BlockStateProvider param1, int param2) {
        this.capProvider = param0;
        this.stemProvider = param1;
        this.foliageRadius = param2;
    }
}
