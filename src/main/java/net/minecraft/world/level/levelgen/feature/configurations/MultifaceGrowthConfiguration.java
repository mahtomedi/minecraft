package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;

public class MultifaceGrowthConfiguration implements FeatureConfiguration {
    public static final Codec<MultifaceGrowthConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Registry.BLOCK
                        .byNameCodec()
                        .fieldOf("block")
                        .flatXmap(MultifaceGrowthConfiguration::apply, DataResult::success)
                        .orElse((MultifaceBlock)Blocks.GLOW_LICHEN)
                        .forGetter(param0x -> param0x.placeBlock),
                    Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter(param0x -> param0x.searchRange),
                    Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter(param0x -> param0x.canPlaceOnFloor),
                    Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter(param0x -> param0x.canPlaceOnCeiling),
                    Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter(param0x -> param0x.canPlaceOnWall),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spreading").orElse(0.5F).forGetter(param0x -> param0x.chanceOfSpreading),
                    RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("can_be_placed_on").forGetter(param0x -> param0x.canBePlacedOn)
                )
                .apply(param0, MultifaceGrowthConfiguration::new)
    );
    public final MultifaceBlock placeBlock;
    public final int searchRange;
    public final boolean canPlaceOnFloor;
    public final boolean canPlaceOnCeiling;
    public final boolean canPlaceOnWall;
    public final float chanceOfSpreading;
    public final HolderSet<Block> canBePlacedOn;
    public final List<Direction> validDirections;

    private static DataResult<MultifaceBlock> apply(Block param0) {
        return param0 instanceof MultifaceBlock var0 ? DataResult.success(var0) : DataResult.error("Growth block should be a multiface block");
    }

    public MultifaceGrowthConfiguration(
        MultifaceBlock param0, int param1, boolean param2, boolean param3, boolean param4, float param5, HolderSet<Block> param6
    ) {
        this.placeBlock = param0;
        this.searchRange = param1;
        this.canPlaceOnFloor = param2;
        this.canPlaceOnCeiling = param3;
        this.canPlaceOnWall = param4;
        this.chanceOfSpreading = param5;
        this.canBePlacedOn = param6;
        List<Direction> var0 = Lists.newArrayList();
        if (param3) {
            var0.add(Direction.UP);
        }

        if (param2) {
            var0.add(Direction.DOWN);
        }

        if (param4) {
            Direction.Plane.HORIZONTAL.forEach(var0::add);
        }

        this.validDirections = Collections.unmodifiableList(var0);
    }
}
