package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class SurfaceRelativeThresholdConfiguration implements DecoratorConfiguration {
    public static final Codec<SurfaceRelativeThresholdConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(param0x -> param0x.heightmap),
                    Codec.INT.optionalFieldOf("min_inclusive", Integer.valueOf(Integer.MIN_VALUE)).forGetter(param0x -> param0x.minInclusive),
                    Codec.INT.optionalFieldOf("max_inclusive", Integer.valueOf(Integer.MAX_VALUE)).forGetter(param0x -> param0x.maxInclusive)
                )
                .apply(param0, SurfaceRelativeThresholdConfiguration::new)
    );
    public final Heightmap.Types heightmap;
    public final int minInclusive;
    public final int maxInclusive;

    public SurfaceRelativeThresholdConfiguration(Heightmap.Types param0, int param1, int param2) {
        this.heightmap = param0;
        this.minInclusive = param1;
        this.maxInclusive = param2;
    }
}
