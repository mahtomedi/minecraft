package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ColoredFallingBlock extends FallingBlock {
    public static final MapCodec<ColoredFallingBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(ColorRGBA.CODEC.fieldOf("falling_dust_color").forGetter(param0x -> param0x.dustColor), propertiesCodec())
                .apply(param0, ColoredFallingBlock::new)
    );
    private final ColorRGBA dustColor;

    @Override
    public MapCodec<ColoredFallingBlock> codec() {
        return CODEC;
    }

    public ColoredFallingBlock(ColorRGBA param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.dustColor = param0;
    }

    @Override
    public int getDustColor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.dustColor.rgba();
    }
}
