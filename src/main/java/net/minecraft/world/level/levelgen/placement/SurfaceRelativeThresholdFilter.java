package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceRelativeThresholdFilter extends PlacementFilter {
    public static final Codec<SurfaceRelativeThresholdFilter> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(param0x -> param0x.heightmap),
                    Codec.INT.optionalFieldOf("min_inclusive", Integer.valueOf(Integer.MIN_VALUE)).forGetter(param0x -> param0x.minInclusive),
                    Codec.INT.optionalFieldOf("max_inclusive", Integer.valueOf(Integer.MAX_VALUE)).forGetter(param0x -> param0x.maxInclusive)
                )
                .apply(param0, SurfaceRelativeThresholdFilter::new)
    );
    private final Heightmap.Types heightmap;
    private final int minInclusive;
    private final int maxInclusive;

    private SurfaceRelativeThresholdFilter(Heightmap.Types param0, int param1, int param2) {
        this.heightmap = param0;
        this.minInclusive = param1;
        this.maxInclusive = param2;
    }

    public static SurfaceRelativeThresholdFilter of(Heightmap.Types param0, int param1, int param2) {
        return new SurfaceRelativeThresholdFilter(param0, param1, param2);
    }

    @Override
    protected boolean shouldPlace(PlacementContext param0, RandomSource param1, BlockPos param2) {
        long var0 = (long)param0.getHeight(this.heightmap, param2.getX(), param2.getZ());
        long var1 = var0 + (long)this.minInclusive;
        long var2 = var0 + (long)this.maxInclusive;
        return var1 <= (long)param2.getY() && (long)param2.getY() <= var2;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.SURFACE_RELATIVE_THRESHOLD_FILTER;
    }
}
