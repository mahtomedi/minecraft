package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.UniformInt;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GrowingPlantConfiguration implements FeatureConfiguration {
    public static final Codec<GrowingPlantConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    WeightedList.codec(UniformInt.CODEC).fieldOf("height_distribution").forGetter(param0x -> param0x.heightDistribution),
                    Direction.CODEC.fieldOf("direction").forGetter(param0x -> param0x.direction),
                    BlockStateProvider.CODEC.fieldOf("body_provider").forGetter(param0x -> param0x.bodyProvider),
                    BlockStateProvider.CODEC.fieldOf("head_provider").forGetter(param0x -> param0x.headProvider),
                    Codec.BOOL.fieldOf("allow_water").forGetter(param0x -> param0x.allowWater)
                )
                .apply(param0, GrowingPlantConfiguration::new)
    );
    public final WeightedList<UniformInt> heightDistribution;
    public final Direction direction;
    public final BlockStateProvider bodyProvider;
    public final BlockStateProvider headProvider;
    public final boolean allowWater;

    public GrowingPlantConfiguration(WeightedList<UniformInt> param0, Direction param1, BlockStateProvider param2, BlockStateProvider param3, boolean param4) {
        this.heightDistribution = param0;
        this.direction = param1;
        this.bodyProvider = param2;
        this.headProvider = param3;
        this.allowWater = param4;
    }
}
