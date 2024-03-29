package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class InfestedRotatedPillarBlock extends InfestedBlock {
    public static final MapCodec<InfestedRotatedPillarBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("host").forGetter(InfestedBlock::getHostBlock), propertiesCodec())
                .apply(param0, InfestedRotatedPillarBlock::new)
    );

    @Override
    public MapCodec<InfestedRotatedPillarBlock> codec() {
        return CODEC;
    }

    public InfestedRotatedPillarBlock(Block param0, BlockBehaviour.Properties param1) {
        super(param0, param1);
        this.registerDefaultState(this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return RotatedPillarBlock.rotatePillar(param0, param1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(RotatedPillarBlock.AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, param0.getClickedFace().getAxis());
    }
}
