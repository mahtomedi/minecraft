package net.minecraft.world.level.levelgen.flat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public class FlatLayerInfo {
    public static final Codec<FlatLayerInfo> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(FlatLayerInfo::getHeight),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").orElse(Blocks.AIR).forGetter(param0x -> param0x.getBlockState().getBlock())
                )
                .apply(param0, FlatLayerInfo::new)
    );
    private final Block block;
    private final int height;

    public FlatLayerInfo(int param0, Block param1) {
        this.height = param0;
        this.block = param1;
    }

    public int getHeight() {
        return this.height;
    }

    public BlockState getBlockState() {
        return this.block.defaultBlockState();
    }

    @Override
    public String toString() {
        return (this.height != 1 ? this.height + "*" : "") + BuiltInRegistries.BLOCK.getKey(this.block);
    }
}
