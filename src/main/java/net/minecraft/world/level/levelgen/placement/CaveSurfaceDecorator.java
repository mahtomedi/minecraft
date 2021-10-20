package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;

public class CaveSurfaceDecorator extends FeatureDecorator<CaveDecoratorConfiguration> {
    public CaveSurfaceDecorator(Codec<CaveDecoratorConfiguration> param0) {
        super(param0);
    }

    public static boolean isAirOrWater(BlockState param0) {
        return param0.isAir() || param0.is(Blocks.WATER);
    }

    private static Predicate<BlockState> getInsideColumnPredicate(boolean param0) {
        return param0 ? CaveSurfaceDecorator::isAirOrWater : BlockBehaviour.BlockStateBase::isAir;
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, CaveDecoratorConfiguration param2, BlockPos param3) {
        Optional<Column> var0 = Column.scan(
            param0.getLevel(),
            param3,
            param2.floorToCeilingSearchRange,
            getInsideColumnPredicate(param2.allowWater),
            param0x -> param0x.getMaterial().isSolid()
        );
        if (var0.isEmpty()) {
            return Stream.of();
        } else {
            OptionalInt var1 = param2.surface == CaveSurface.CEILING ? var0.get().getCeiling() : var0.get().getFloor();
            return var1.isEmpty() ? Stream.of() : Stream.of(param3.atY(var1.getAsInt() - param2.surface.getY()));
        }
    }
}
