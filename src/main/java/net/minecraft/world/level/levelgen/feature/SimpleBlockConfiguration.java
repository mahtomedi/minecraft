package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockConfiguration implements FeatureConfiguration {
    protected final BlockState toPlace;
    protected final List<BlockState> placeOn;
    protected final List<BlockState> placeIn;
    protected final List<BlockState> placeUnder;

    public SimpleBlockConfiguration(BlockState param0, List<BlockState> param1, List<BlockState> param2, List<BlockState> param3) {
        this.toPlace = param0;
        this.placeOn = param1;
        this.placeIn = param2;
        this.placeUnder = param3;
    }

    public SimpleBlockConfiguration(BlockState param0, BlockState[] param1, BlockState[] param2, BlockState[] param3) {
        this(param0, Lists.newArrayList(param1), Lists.newArrayList(param2), Lists.newArrayList(param3));
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        T var0 = BlockState.serialize(param0, this.toPlace).getValue();
        T var1 = param0.createList(this.placeOn.stream().map(param1 -> BlockState.serialize(param0, param1).getValue()));
        T var2 = param0.createList(this.placeIn.stream().map(param1 -> BlockState.serialize(param0, param1).getValue()));
        T var3 = param0.createList(this.placeUnder.stream().map(param1 -> BlockState.serialize(param0, param1).getValue()));
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("to_place"),
                    var0,
                    param0.createString("place_on"),
                    var1,
                    param0.createString("place_in"),
                    var2,
                    param0.createString("place_under"),
                    var3
                )
            )
        );
    }

    public static <T> SimpleBlockConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("to_place").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        List<BlockState> var1 = param0.get("place_on").asList(BlockState::deserialize);
        List<BlockState> var2 = param0.get("place_in").asList(BlockState::deserialize);
        List<BlockState> var3 = param0.get("place_under").asList(BlockState::deserialize);
        return new SimpleBlockConfiguration(var0, var1, var2, var3);
    }
}
