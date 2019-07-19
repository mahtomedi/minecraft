package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DiskConfiguration implements FeatureConfiguration {
    public final BlockState state;
    public final int radius;
    public final int ySize;
    public final List<BlockState> targets;

    public DiskConfiguration(BlockState param0, int param1, int param2, List<BlockState> param3) {
        this.state = param0;
        this.radius = param1;
        this.ySize = param2;
        this.targets = param3;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("state"),
                    BlockState.serialize(param0, this.state).getValue(),
                    param0.createString("radius"),
                    param0.createInt(this.radius),
                    param0.createString("y_size"),
                    param0.createInt(this.ySize),
                    param0.createString("targets"),
                    param0.createList(this.targets.stream().map(param1 -> BlockState.serialize(param0, param1).getValue()))
                )
            )
        );
    }

    public static <T> DiskConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        int var1 = param0.get("radius").asInt(0);
        int var2 = param0.get("y_size").asInt(0);
        List<BlockState> var3 = param0.get("targets").asList(BlockState::deserialize);
        return new DiskConfiguration(var0, var1, var2, var3);
    }
}
