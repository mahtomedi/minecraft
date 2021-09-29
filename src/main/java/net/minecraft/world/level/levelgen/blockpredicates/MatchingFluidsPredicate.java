package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate implements BlockPredicate {
    private final List<Fluid> fluids;
    private final BlockPos offset;
    public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Registry.FLUID.listOf().fieldOf("fluids").forGetter(param0x -> param0x.fluids),
                    BlockPos.CODEC.fieldOf("offset").forGetter(param0x -> param0x.offset)
                )
                .apply(param0, MatchingFluidsPredicate::new)
    );

    public MatchingFluidsPredicate(List<Fluid> param0, BlockPos param1) {
        this.fluids = param0;
        this.offset = param1;
    }

    public boolean test(WorldGenLevel param0, BlockPos param1) {
        return this.fluids.contains(param0.getFluidState(param1.offset(this.offset)).getType());
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_FLUIDS;
    }
}
