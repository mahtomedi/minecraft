package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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

    public static SurfaceBuilderBaseConfiguration deserialize(Dynamic<?> param0) {
        BlockState var0 = param0.get("top_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var1 = param0.get("under_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var2 = param0.get("underwater_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new SurfaceBuilderBaseConfiguration(var0, var1, var2);
    }
}
