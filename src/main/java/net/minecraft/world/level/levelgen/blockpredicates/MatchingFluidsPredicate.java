package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate extends StateTestingPredicate {
    private final HolderSet<Fluid> fluids;
    public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> stateTestingCodec(param0)
                .and(RegistryCodecs.homogeneousList(Registry.FLUID_REGISTRY).fieldOf("fluids").forGetter(param0x -> param0x.fluids))
                .apply(param0, MatchingFluidsPredicate::new)
    );

    public MatchingFluidsPredicate(Vec3i param0, HolderSet<Fluid> param1) {
        super(param0);
        this.fluids = param1;
    }

    @Override
    protected boolean test(BlockState param0) {
        return param0.getFluidState().is(this.fluids);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_FLUIDS;
    }
}
