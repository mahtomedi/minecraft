package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;

public class InsideWorldBoundsPredicate implements BlockPredicate {
    public static final Codec<InsideWorldBoundsPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", BlockPos.ZERO).forGetter(param0x -> param0x.offset))
                .apply(param0, InsideWorldBoundsPredicate::new)
    );
    private final Vec3i offset;

    public InsideWorldBoundsPredicate(Vec3i param0) {
        this.offset = param0;
    }

    public boolean test(WorldGenLevel param0, BlockPos param1) {
        return !param0.isOutsideBuildHeight(param1.offset(this.offset));
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.INSIDE_WORLD_BOUNDS;
    }
}
