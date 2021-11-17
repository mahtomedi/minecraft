package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate extends StateTestingPredicate {
    private final List<Fluid> fluids;
    public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> stateTestingCodec(param0)
                .and(Registry.FLUID.byNameCodec().listOf().fieldOf("fluids").forGetter(param0x -> param0x.fluids))
                .apply(param0, MatchingFluidsPredicate::new)
    );

    public MatchingFluidsPredicate(Vec3i param0, List<Fluid> param1) {
        super(param0);
        this.fluids = param1;
    }

    @Override
    protected boolean test(BlockState param0) {
        return this.fluids.contains(param0.getFluidState().getType());
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_FLUIDS;
    }
}
