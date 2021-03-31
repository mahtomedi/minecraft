package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class SurfaceBuilderBaseConfiguration implements SurfaceBuilderConfiguration {
    public static final Codec<SurfaceBuilderBaseConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("top_material").forGetter(param0x -> param0x.topMaterial),
                    BlockState.CODEC.fieldOf("under_material").forGetter(param0x -> param0x.underMaterial),
                    BlockState.CODEC.fieldOf("underwater_material").forGetter(param0x -> param0x.underwaterMaterial)
                )
                .apply(param0, SurfaceBuilderBaseConfiguration::new)
    );
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

    @Override
    public BlockState getUnderwaterMaterial() {
        return this.underwaterMaterial;
    }
}
