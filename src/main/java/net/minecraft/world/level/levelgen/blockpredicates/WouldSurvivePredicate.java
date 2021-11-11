package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public class WouldSurvivePredicate implements BlockPredicate {
    public static final Codec<WouldSurvivePredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(param0x -> param0x.offset),
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state)
                )
                .apply(param0, WouldSurvivePredicate::new)
    );
    private final Vec3i offset;
    private final BlockState state;

    protected WouldSurvivePredicate(Vec3i param0, BlockState param1) {
        this.offset = param0;
        this.state = param1;
    }

    public boolean test(WorldGenLevel param0, BlockPos param1) {
        return this.state.canSurvive(param0, param1.offset(this.offset));
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.WOULD_SURVIVE;
    }
}
