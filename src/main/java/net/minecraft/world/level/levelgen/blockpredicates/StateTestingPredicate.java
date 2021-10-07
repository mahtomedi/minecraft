package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StateTestingPredicate implements BlockPredicate {
    protected final BlockPos offset;

    protected static <P extends StateTestingPredicate> P1<Mu<P>, BlockPos> stateTestingCodec(Instance<P> param0) {
        return param0.group(BlockPos.CODEC.optionalFieldOf("offset", BlockPos.ZERO).forGetter(param0x -> param0x.offset));
    }

    protected StateTestingPredicate(BlockPos param0) {
        this.offset = param0;
    }

    public final boolean test(WorldGenLevel param0, BlockPos param1) {
        return this.test(param0.getBlockState(param1.offset(this.offset)));
    }

    protected abstract boolean test(BlockState var1);
}
