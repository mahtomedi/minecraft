package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class EnvironmentScanPlacement extends PlacementModifier {
    private final Direction directionOfSearch;
    private final BlockPredicate targetCondition;
    private final BlockPredicate allowedSearchCondition;
    private final int maxSteps;
    public static final Codec<EnvironmentScanPlacement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Direction.VERTICAL_CODEC.fieldOf("direction_of_search").forGetter(param0x -> param0x.directionOfSearch),
                    BlockPredicate.CODEC.fieldOf("target_condition").forGetter(param0x -> param0x.targetCondition),
                    BlockPredicate.CODEC
                        .optionalFieldOf("allowed_search_condition", BlockPredicate.alwaysTrue())
                        .forGetter(param0x -> param0x.allowedSearchCondition),
                    Codec.intRange(1, 32).fieldOf("max_steps").forGetter(param0x -> param0x.maxSteps)
                )
                .apply(param0, EnvironmentScanPlacement::new)
    );

    private EnvironmentScanPlacement(Direction param0, BlockPredicate param1, BlockPredicate param2, int param3) {
        this.directionOfSearch = param0;
        this.targetCondition = param1;
        this.allowedSearchCondition = param2;
        this.maxSteps = param3;
    }

    public static EnvironmentScanPlacement scanningFor(Direction param0, BlockPredicate param1, BlockPredicate param2, int param3) {
        return new EnvironmentScanPlacement(param0, param1, param2, param3);
    }

    public static EnvironmentScanPlacement scanningFor(Direction param0, BlockPredicate param1, int param2) {
        return scanningFor(param0, param1, BlockPredicate.alwaysTrue(), param2);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, RandomSource param1, BlockPos param2) {
        BlockPos.MutableBlockPos var0 = param2.mutable();
        WorldGenLevel var1 = param0.getLevel();
        if (!this.allowedSearchCondition.test(var1, var0)) {
            return Stream.of();
        } else {
            for(int var2 = 0; var2 < this.maxSteps; ++var2) {
                if (this.targetCondition.test(var1, var0)) {
                    return Stream.of(var0);
                }

                var0.move(this.directionOfSearch);
                if (var1.isOutsideBuildHeight(var0.getY())) {
                    return Stream.of();
                }

                if (!this.allowedSearchCondition.test(var1, var0)) {
                    break;
                }
            }

            return this.targetCondition.test(var1, var0) ? Stream.of(var0) : Stream.of();
        }
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.ENVIRONMENT_SCAN;
    }
}
