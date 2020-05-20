package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockConfiguration implements FeatureConfiguration {
    public static final Codec<SimpleBlockConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("to_place").forGetter(param0x -> param0x.toPlace),
                    BlockState.CODEC.listOf().fieldOf("place_on").forGetter(param0x -> param0x.placeOn),
                    BlockState.CODEC.listOf().fieldOf("place_in").forGetter(param0x -> param0x.placeIn),
                    BlockState.CODEC.listOf().fieldOf("place_under").forGetter(param0x -> param0x.placeUnder)
                )
                .apply(param0, SimpleBlockConfiguration::new)
    );
    public final BlockState toPlace;
    public final List<BlockState> placeOn;
    public final List<BlockState> placeIn;
    public final List<BlockState> placeUnder;

    public SimpleBlockConfiguration(BlockState param0, List<BlockState> param1, List<BlockState> param2, List<BlockState> param3) {
        this.toPlace = param0;
        this.placeOn = param1;
        this.placeIn = param2;
        this.placeUnder = param3;
    }
}
