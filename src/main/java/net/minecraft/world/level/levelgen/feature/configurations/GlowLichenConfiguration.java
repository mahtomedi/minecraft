package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GlowLichenConfiguration implements FeatureConfiguration {
    public static final Codec<GlowLichenConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter(param0x -> param0x.searchRange),
                    Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter(param0x -> param0x.canPlaceOnFloor),
                    Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter(param0x -> param0x.canPlaceOnCeiling),
                    Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter(param0x -> param0x.canPlaceOnWall),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spreading").orElse(0.5F).forGetter(param0x -> param0x.chanceOfSpreading),
                    BlockState.CODEC.listOf().fieldOf("can_be_placed_on").forGetter(param0x -> new ArrayList<>(param0x.canBePlacedOn))
                )
                .apply(param0, GlowLichenConfiguration::new)
    );
    public final int searchRange;
    public final boolean canPlaceOnFloor;
    public final boolean canPlaceOnCeiling;
    public final boolean canPlaceOnWall;
    public final float chanceOfSpreading;
    public final List<BlockState> canBePlacedOn;
    public final List<Direction> validDirections;

    public GlowLichenConfiguration(int param0, boolean param1, boolean param2, boolean param3, float param4, List<BlockState> param5) {
        this.searchRange = param0;
        this.canPlaceOnFloor = param1;
        this.canPlaceOnCeiling = param2;
        this.canPlaceOnWall = param3;
        this.chanceOfSpreading = param4;
        this.canBePlacedOn = param5;
        List<Direction> var0 = Lists.newArrayList();
        if (param2) {
            var0.add(Direction.UP);
        }

        if (param1) {
            var0.add(Direction.DOWN);
        }

        if (param3) {
            Direction.Plane.HORIZONTAL.forEach(var0::add);
        }

        this.validDirections = Collections.unmodifiableList(var0);
    }

    public boolean canBePlacedOn(Block param0) {
        return this.canBePlacedOn.stream().anyMatch(param1 -> param1.is(param0));
    }
}
