package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class SimpleBlockConfiguration implements FeatureConfiguration {
    public static final Codec<SimpleBlockConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("to_place").forGetter(param0x -> param0x.toPlace),
                    BlockState.CODEC.listOf().fieldOf("place_on").orElse(ImmutableList.of()).forGetter(param0x -> param0x.placeOn),
                    BlockState.CODEC.listOf().fieldOf("place_in").orElse(ImmutableList.of()).forGetter(param0x -> param0x.placeIn),
                    BlockState.CODEC.listOf().fieldOf("place_under").orElse(ImmutableList.of()).forGetter(param0x -> param0x.placeUnder)
                )
                .apply(param0, SimpleBlockConfiguration::new)
    );
    public final BlockStateProvider toPlace;
    public final List<BlockState> placeOn;
    public final List<BlockState> placeIn;
    public final List<BlockState> placeUnder;

    public SimpleBlockConfiguration(BlockStateProvider param0, List<BlockState> param1, List<BlockState> param2, List<BlockState> param3) {
        this.toPlace = param0;
        this.placeOn = param1;
        this.placeIn = param2;
        this.placeUnder = param3;
    }

    public SimpleBlockConfiguration(BlockStateProvider param0) {
        this(param0, ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
    }
}
