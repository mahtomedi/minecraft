package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceWaterDepthFilter extends PlacementFilter {
    public static final Codec<SurfaceWaterDepthFilter> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.INT.fieldOf("max_water_depth").forGetter(param0x -> param0x.maxWaterDepth)).apply(param0, SurfaceWaterDepthFilter::new)
    );
    private final int maxWaterDepth;

    private SurfaceWaterDepthFilter(int param0) {
        this.maxWaterDepth = param0;
    }

    public static SurfaceWaterDepthFilter forMaxDepth(int param0) {
        return new SurfaceWaterDepthFilter(param0);
    }

    @Override
    protected boolean shouldPlace(PlacementContext param0, RandomSource param1, BlockPos param2) {
        int var0 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR, param2.getX(), param2.getZ());
        int var1 = param0.getHeight(Heightmap.Types.WORLD_SURFACE, param2.getX(), param2.getZ());
        return var1 - var0 <= this.maxWaterDepth;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.SURFACE_WATER_DEPTH_FILTER;
    }
}
