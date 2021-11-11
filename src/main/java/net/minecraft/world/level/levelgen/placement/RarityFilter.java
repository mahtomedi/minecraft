package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;

public class RarityFilter extends PlacementFilter {
    public static final Codec<RarityFilter> CODEC = ExtraCodecs.POSITIVE_INT.fieldOf("chance").xmap(RarityFilter::new, param0 -> param0.chance).codec();
    private final int chance;

    private RarityFilter(int param0) {
        this.chance = param0;
    }

    public static RarityFilter onAverageOnceEvery(int param0) {
        return new RarityFilter(param0);
    }

    @Override
    protected boolean shouldPlace(PlacementContext param0, Random param1, BlockPos param2) {
        return param1.nextFloat() < 1.0F / (float)this.chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.RARITY_FILTER;
    }
}
