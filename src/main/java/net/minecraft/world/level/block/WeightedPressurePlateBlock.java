package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WeightedPressurePlateBlock extends BasePressurePlateBlock {
    public static final MapCodec<WeightedPressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.intRange(1, 1024).fieldOf("max_weight").forGetter(param0x -> param0x.maxWeight),
                    BlockSetType.CODEC.fieldOf("block_set_type").forGetter(param0x -> param0x.type),
                    propertiesCodec()
                )
                .apply(param0, WeightedPressurePlateBlock::new)
    );
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private final int maxWeight;

    @Override
    public MapCodec<WeightedPressurePlateBlock> codec() {
        return CODEC;
    }

    protected WeightedPressurePlateBlock(int param0, BlockSetType param1, BlockBehaviour.Properties param2) {
        super(param2, param1);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
        this.maxWeight = param0;
    }

    @Override
    protected int getSignalStrength(Level param0, BlockPos param1) {
        int var0 = Math.min(getEntityCount(param0, TOUCH_AABB.move(param1), Entity.class), this.maxWeight);
        if (var0 > 0) {
            float var1 = (float)Math.min(this.maxWeight, var0) / (float)this.maxWeight;
            return Mth.ceil(var1 * 15.0F);
        } else {
            return 0;
        }
    }

    @Override
    protected int getSignalForState(BlockState param0) {
        return param0.getValue(POWER);
    }

    @Override
    protected BlockState setSignalForState(BlockState param0, int param1) {
        return param0.setValue(POWER, Integer.valueOf(param1));
    }

    @Override
    protected int getPressedTime() {
        return 10;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(POWER);
    }
}
