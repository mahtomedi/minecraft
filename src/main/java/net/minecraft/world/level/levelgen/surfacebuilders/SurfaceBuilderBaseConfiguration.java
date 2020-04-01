package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;

public class SurfaceBuilderBaseConfiguration implements SurfaceBuilderConfiguration {
    private final BlockState topMaterial;
    private final BlockState underMaterial;
    private final BlockState underwaterMaterial;

    public SurfaceBuilderBaseConfiguration(BlockState param0, BlockState param1, BlockState param2) {
        this.topMaterial = param0;
        this.underMaterial = param1;
        this.underwaterMaterial = param2;
    }

    @Override
    public BlockState getTopMaterial() {
        return this.topMaterial;
    }

    @Override
    public BlockState getUnderMaterial() {
        return this.underMaterial;
    }

    public BlockState getUnderwaterMaterial() {
        return this.underwaterMaterial;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("top_material"),
                    BlockState.serialize(param0, this.topMaterial).getValue(),
                    param0.createString("under_material"),
                    BlockState.serialize(param0, this.underMaterial).getValue(),
                    param0.createString("underwater_material"),
                    BlockState.serialize(param0, this.underwaterMaterial).getValue()
                )
            )
        );
    }

    public static SurfaceBuilderBaseConfiguration deserialize(Dynamic<?> param0) {
        BlockState var0 = param0.get("top_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var1 = param0.get("under_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var2 = param0.get("underwater_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new SurfaceBuilderBaseConfiguration(var0, var1, var2);
    }

    public static SurfaceBuilderBaseConfiguration random(Random param0) {
        BlockState var0 = Util.randomObject(param0, OverworldGeneratorSettings.SAFE_BLOCKS);
        BlockState var1 = Util.randomObject(param0, OverworldGeneratorSettings.SAFE_BLOCKS);
        BlockState var2 = Util.randomObject(param0, OverworldGeneratorSettings.SAFE_BLOCKS);
        return new SurfaceBuilderBaseConfiguration(var0, var1, var2);
    }
}
