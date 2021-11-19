package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;

public class HasSturdyFacePredicate implements BlockPredicate {
    private final Vec3i offset;
    private final Direction direction;
    public static final Codec<HasSturdyFacePredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(param0x -> param0x.offset),
                    Direction.CODEC.fieldOf("direction").forGetter(param0x -> param0x.direction)
                )
                .apply(param0, HasSturdyFacePredicate::new)
    );

    public HasSturdyFacePredicate(Vec3i param0, Direction param1) {
        this.offset = param0;
        this.direction = param1;
    }

    public boolean test(WorldGenLevel param0, BlockPos param1) {
        BlockPos var0 = param1.offset(this.offset);
        return param0.getBlockState(var0).isFaceSturdy(param0, var0, this.direction);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.HAS_STURDY_FACE;
    }
}
